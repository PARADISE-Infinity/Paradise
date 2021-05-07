/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.constraints.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.properties.PropertySheet;

import de.dlr.premise.constraints.ConstraintViolationKind;
import de.dlr.premise.constraints.handlers.MarkerCreatingConstraintViolationHandler;

public class ConstraintsStatusTracker implements IResourceChangeListener, ISelectionListener {

    private class CheckForUpdateJob extends Job {

        public CheckForUpdateJob() {
            super("Checking for changes in constraint violations");
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            ConstraintsStatus newStatus;
            IResource frontFile = getFrontFile();
            try {
                if (frontFile == null) {
                    newStatus = ConstraintsStatus.NOFILE;
                } else {
                    IMarker[] markers =
                            frontFile.findMarkers(MarkerCreatingConstraintViolationHandler.MARKER, true, IResource.DEPTH_INFINITE);
                    if (markers.length == 0) {
                        newStatus = ConstraintsStatus.OK;
                    } else {
                        boolean hasViolation = false;

                        for (IMarker marker : markers) {
                            ConstraintViolationKind kind = getViolationKind(marker);

                            if (kind == ConstraintViolationKind.VIOLATED || kind == ConstraintViolationKind.MULTIPLY_SATISFIED) {
                                hasViolation = true;
                                break;
                            }
                        }

                        if (hasViolation) {
                            newStatus = ConstraintsStatus.HAS_VIOLATIONS;
                        } else {
                            newStatus = ConstraintsStatus.HAS_NOT_SATISFIED;
                        }
                    }

                }
            } catch (CoreException e) {
                newStatus = ConstraintsStatus.INTERNAL_ERROR;
            }

            if (newStatus != currentStatus) {
                currentStatus = newStatus;
                view.updateView();
            }
            return Status.OK_STATUS;
        }

        private ConstraintViolationKind getViolationKind(IMarker marker) throws CoreException {
            String stringKind = (String) marker.getAttribute(MarkerCreatingConstraintViolationHandler.MARKER_ATTRIBUTE_KIND);

            try {
                return ConstraintViolationKind.valueOf(stringKind);
            } catch (NullPointerException e) {
                return null;
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

    }

    private final ConstraintsView view;

    private IResource frontFile = null;

    private ConstraintsStatus currentStatus = ConstraintsStatus.NOFILE;
    private CheckForUpdateJob updateJob = null;

    public ConstraintsStatusTracker(ConstraintsView view) {
        this.view = view;
    }

    private void updateStatus() {
        if (updateJob != null) {
            updateJob.cancel();
        }
        updateJob = new CheckForUpdateJob();
        updateJob.schedule();
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        IMarkerDelta[] markerDeltas = event.findMarkerDeltas(MarkerCreatingConstraintViolationHandler.MARKER, true);
        if (markerDeltas.length != 0) {
            for (IMarkerDelta markerDelta : markerDeltas) {
                if (markerDelta.getMarker().getResource().equals(getFrontFile())) {
                    updateStatus();
                    break;
                }
            }
        }
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        // These views can't contain models, but merely support an opened model, therefore we don't respond to them being selected
        if (part instanceof ConstraintsView || part instanceof MarkerSupportView || part instanceof PropertySheet) {
            return;
        }

        IResource selectedFile = null;

        if (part instanceof IEditorPart) {
            selectedFile = getEditorPartResource((IEditorPart) part);
        } else if (selection instanceof IStructuredSelection) {
            for (Object selectedObject : ((IStructuredSelection) selection).toList()) {
                if (selectedObject instanceof IResource) {
                    selectedFile = (IResource) selectedObject;
                    break;
                }
            }
        }

        if (frontFile != selectedFile) {
            frontFile = selectedFile;
            updateStatus();
        }
    }

    public ConstraintsStatus getStatus() {
        return currentStatus;
    }

    private IResource getFrontFile() {
        return frontFile;
    }

    private IResource getEditorPartResource(IEditorPart part) {
        IResource file = null;

        if (part != null && part.getEditorInput() instanceof IFileEditorInput) {
            IFileEditorInput inp = (IFileEditorInput) part.getEditorInput();
            file = inp.getFile();
        }

        return file;
    }
}
