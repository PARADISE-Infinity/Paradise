/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.constraints.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.xtext.util.Strings;

import com.google.common.collect.ImmutableList;

import de.dlr.premise.constraints.ConstraintViolationKind;
import de.dlr.premise.functions.AConstraint;
import de.dlr.premise.functions.RequiredParameter;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.provider.util.PremiseAdapterFactoryHelper;

/**
 * Handles constraint violations by creating an {@link IMarker} for the violation.
 * 
 * Violations are added to a queue and markers are created in batch. This is necessary to get acceptable ui performance. Otherwise, a
 * {@link IResourceChangeEvent} would be created for every marker change.
 * 
 * @author steh_ti
 */
public class MarkerCreatingConstraintViolationHandler implements IConstraintViolationHandler {

    /**
     * Attributes of validation markers
     */
    public static final String MARKER = "de.dlr.premise.constraints.violation";
    public static final String MARKER_ATTRIBUTE_KIND = "de.dlr.premise.constraints.violation.kind";
    public static final String MARKER_ATTRIBUTE_URI = EValidator.URI_ATTRIBUTE;
    public static final String MARKER_ATTRIBUTE_MESSAGE = IMarker.MESSAGE;

    /**
     * Time to wait before updating the markers on the resource after the last violation has been received (ms).
     */
    private static final int CONSTRAINT_VIOLATION_MARKERS_UPDATE_DELAY = 10;

    /**
     * Resource and ResourceSet are needed to create the markers
     */
    private final IResource eclipseResource;
    private final ResourceSet resourceSet;

    /**
     * All operations are enqueued in here. When run, the job takes a copy of the current queue to process it, and empties this one. We
     * synchronize on the queue itself whenever we operate on it.
     */
    private final List<ViolationOperation> operationQueue;
    
    /**
     * This lock is used by scheduleValidations, to make sure only one marker update is being scheduled at a time
     */
    private final Object scheduleLock = new Object();

    /**
     * This lock is used by the marker updater, so that only one actually runs
     */
    private final Object performOperationsLock = new Object();

    /**
     * Map of an eObject to a list of markers associated with the eObject
     */
    private final Map<EObject, List<IMarker>> objToMarkerMap = new WeakHashMap<EObject, List<IMarker>>() {

        @Override
        public List<IMarker> get(Object k) {
            if (!containsKey(k)) {
                // Create an empty list, if none exists.
                put((EObject) k, new ArrayList<IMarker>());
            }
            return super.get(k);
        }
    };
    
    private AdapterFactory adapterFactory = PremiseAdapterFactoryHelper.createComposedAdapterFactory();

    protected Job markersJob;

    public MarkerCreatingConstraintViolationHandler(IResource file, ResourceSet resourceSet) {
        this.eclipseResource = file;
        this.resourceSet = resourceSet;
        this.operationQueue = new ArrayList<>();

        enqueueOperation(ViolationOperation.InitializeObjToMarkerMap.INSTANCE);
    }

    @Override
    public void removeViolations() {
        enqueueOperation(ViolationOperation.RemoveAll.INSTANCE);
    }

    @Override
    public void removeViolations(EObject obj) {
        enqueueOperation(new ViolationOperation.Remove(obj));
    }

    @Override
    public void addViolation(EObject violator, ConstraintViolationKind kind, Object violatedConstraint) {
        enqueueOperation(new ViolationOperation.Add(violator, kind, violatedConstraint));
    }
    
    private void enqueueOperation(ViolationOperation operation) {
        synchronized (operationQueue) {
            operationQueue.add(operation);
        }
        this.scheduleMarkerUpdate();
    }

    private void scheduleMarkerUpdate() {
        synchronized (scheduleLock) {
            if (markersJob != null) {
                markersJob.cancel();
            }
            markersJob = new ConstraintViolationMarkersUpdateJob();
            markersJob.schedule(CONSTRAINT_VIOLATION_MARKERS_UPDATE_DELAY);
        }
    }


    /**
     * The job that updates the constraint violation markers.
     * 
     * We use a {@link WorkspaceJob} here to batch up all marker changes into one change event.
     */
    private final class ConstraintViolationMarkersUpdateJob extends WorkspaceJob {
        
        public ConstraintViolationMarkersUpdateJob() {
            super("Displaying constraint check result");
        }

        @Override
        public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
            // only start running operations if nobody else is
            synchronized (performOperationsLock) {
                // get the currently enqueued operations
                List<ViolationOperation> operations;
                // the lock makes sure nobody adds in between copying and clearing, so we can't loose any ops
                synchronized (operationQueue) {
                    operations = ImmutableList.copyOf(operationQueue);
                    operationQueue.clear();
                }

                // perform all pending operations
                try {
                    monitor.beginTask("Updating constraint violation markers", operations.size());

                    for (ViolationOperation operation : operations) {
                        performOperation(operation);
                        monitor.worked(1);
                    }
                } finally {
                    monitor.done();
                }
            }

            return Status.OK_STATUS;
        }

        private void performOperation(ViolationOperation operation) {
            if (operation instanceof ViolationOperation.InitializeObjToMarkerMap) {
                performInitializeObjToMarkerMap((ViolationOperation.InitializeObjToMarkerMap) operation);
            } else if (operation instanceof ViolationOperation.Remove) {
                performRemoveViolations((ViolationOperation.Remove) operation);
            } else if (operation instanceof ViolationOperation.RemoveAll) {
                performRemoveAllViolations((ViolationOperation.RemoveAll) operation);
            } else if (operation instanceof ViolationOperation.Add) {
                performAddViolation((ViolationOperation.Add) operation);
            }
        }

        private void performInitializeObjToMarkerMap(ViolationOperation.InitializeObjToMarkerMap operation) {
            try {
                IMarker[] markers = eclipseResource.findMarkers(MARKER, false, IResource.DEPTH_INFINITE);

                for (IMarker marker : markers) {
                    EObject violator = null;

                    try {
                        URI uri = URI.createURI((String) marker.getAttribute(MARKER_ATTRIBUTE_URI));
                        violator = resourceSet.getEObject(uri, false);
                    } catch (RuntimeException e) {
                        // assume a null violator
                    }

                    // save markers with a null violator too, because we need them when we remove all violations
                    objToMarkerMap.get(violator).add(marker);
                }
            } catch (CoreException e) {
                throw new WrappedException(e);
            }
        }

        private void performRemoveViolations(ViolationOperation.Remove operation) {
            removeViolations(operation.getObj());
        }

        private void performRemoveAllViolations(ViolationOperation.RemoveAll operation) {
            for (EObject obj : objToMarkerMap.keySet()) {
                removeViolations(obj);
            }
        }

        private void performAddViolation(ViolationOperation.Add operation) {
            EObject violator = operation.getViolator();
            ConstraintViolationKind kind = operation.getKind();
            Object violatedConstraint = operation.getViolatedConstraint();
            
            // We won't add markers for null resources, because then the uri attribute would point to a non existing resource, which confuses EMF
            if (violator.eResource() == null) {
                return;
            }

            String uriString = EcoreUtil.getURI(EcoreUtil.resolve(violator, violator.eResource())).toString();

            try {
                IMarker marker = eclipseResource.createMarker(MARKER);

                switch (kind) {
                case NOT_SATISFIED:
                    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
                    break;
                case MULTIPLY_SATISFIED:
                case VIOLATED:
                    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                    break;
                }
                marker.setAttribute(MARKER_ATTRIBUTE_MESSAGE, produceErrorMessage(violator, kind, violatedConstraint));

                marker.setAttribute(MARKER_ATTRIBUTE_URI, uriString);
                marker.setAttribute(MARKER_ATTRIBUTE_KIND, kind.toString());

                objToMarkerMap.get(violator).add(marker);
            } catch (CoreException e) {
                throw new WrappedException(e);
            }
        }

        private void removeViolations(EObject obj) {
            try {
                for (IMarker marker : objToMarkerMap.get(obj)) {
                    marker.delete();
                }
            } catch (CoreException e) {
                throw new WrappedException(e);
            }
        }

        private String produceErrorMessage(EObject violator, ConstraintViolationKind kind, Object violatedConstraint) {
            switch (kind) {
            case MULTIPLY_SATISFIED:
                return getLabel(violator) + " is satisfied by multiple parameters (" + getLabel(violatedConstraint) + ")";
            case NOT_SATISFIED:
                return getLabel(violator) + " is not satisfied by any parameter";
            case VIOLATED:
                return getLabel(violator) + " violates " + getLabel(violatedConstraint);
            default:
                throw new IllegalArgumentException("Unknown violation kind!");
            }
        }

        private String getLabel(Object obj) {
            if (obj instanceof EObject) {
                return getLabel((EObject) obj);
            }
            if (obj instanceof Collection) {
                List<String> labels = new ArrayList<String>();
                for (Object elem : (Collection<?>) obj) {
                    labels.add(getLabel(elem));
                }
                return Strings.concat(", ", labels);
            }
            return obj == null ? "" : obj.toString();
        }

        private String getLabel(EObject obj) {
            String label = getText(obj);
            if (label == null) {
                label = "";
            }
            if (obj instanceof AConstraint) {
                if (obj.eContainer() instanceof RequiredParameter) {
                    label += " of \"" + getText(obj.eContainer()) + "\"";
                }
                if (obj.eContainer().eContainer() instanceof UseCase) {
                    label += " of \"" + getText(obj.eContainer().eContainer()) + "\"";
                }
            }
            return label;
        }

        private String getText(EObject obj) {
            if (adapterFactory.isFactoryForType(IItemLabelProvider.class)) {
                IItemLabelProvider labelProvider = (IItemLabelProvider) adapterFactory.adapt(obj, IItemLabelProvider.class);
                if (labelProvider != null) {
                    return labelProvider.getText(obj);
                }
            }
            return null;
        }
    }

    /**
     * Represents all the operations that can be performed on violations
     */
    private static interface ViolationOperation {

        public static class InitializeObjToMarkerMap implements ViolationOperation {

            public static final InitializeObjToMarkerMap INSTANCE = new InitializeObjToMarkerMap();

            private InitializeObjToMarkerMap() {
            }

            @Override
            public String toString() {
                return "InitializeObjToMarkerMap []";
            }
        }

        public static class Remove implements ViolationOperation {

            private final EObject obj;

            public Remove(EObject obj) {
                this.obj = obj;
            }

            public EObject getObj() {
                return obj;
            }

            @Override
            public String toString() {
                return "Remove [obj=" + obj + "]";
            }
        }

        public static class RemoveAll implements ViolationOperation {

            public static final RemoveAll INSTANCE = new RemoveAll();

            private RemoveAll() {
            }

            @Override
            public String toString() {
                return "RemoveAll []";
            }
        }

        public static class Add implements ViolationOperation {

            private final EObject violator;
            private final ConstraintViolationKind kind;
            private final Object violatedConstraint;

            public Add(EObject violator, ConstraintViolationKind kind, Object violatedConstraint) {
                super();
                this.violator = violator;
                this.kind = kind;
                this.violatedConstraint = violatedConstraint;
            }

            public EObject getViolator() {
                return violator;
            }

            public ConstraintViolationKind getKind() {
                return kind;
            }

            public Object getViolatedConstraint() {
                return violatedConstraint;
            }

            @Override
            public String toString() {
                return "Add [violator=" + violator + ", kind=" + kind + ", violatedConstraint=" + violatedConstraint + "]";
            }
        }
    }
}
