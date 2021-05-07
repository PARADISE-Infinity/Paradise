/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.validation.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.core.internal.resources.MarkerSet;
import org.eclipse.core.internal.resources.ResourceInfo;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.ui.action.ValidateAction.EclipseResourcesUtil;

@SuppressWarnings("restriction")
public class MarkerHelperMy extends EclipseResourcesUtil {

    /**
     * This is a workaround to resolve https://trello.com/c/JAdZuz8s The code is taken from the superclass, with additional 
     * checks (as marked below) to prevent adding URIs of EObjects not contained in a resource.
     */
    @Override
    protected void adjustMarker(IMarker marker, Diagnostic diagnostic, Diagnostic parentDiagnostic) throws CoreException {
        List<?> data = diagnostic.getData();
        StringBuilder relatedURIs = new StringBuilder();
        boolean first = true;
        for (Object object : data) {
            if (object instanceof EObject) {
                EObject eObject = (EObject) object;
                if (first) {
                    first = false;
                    // ADDED CHECK
                    if (eObject.eResource() != null) {
                        marker.setAttribute(EValidator.URI_ATTRIBUTE, EcoreUtil.getURI(eObject).toString());
                    }
                } else {
                    // ADDED CHECK
                    if (eObject.eResource() != null) {
                        if (relatedURIs.length() != 0) {
                            relatedURIs.append(' ');
                        }
                        relatedURIs.append(URI.encodeFragment(EcoreUtil.getURI(eObject).toString(), false));
                    }
                }
            }
        }

        if (relatedURIs.length() > 0) {
            marker.setAttribute(EValidator.RELATED_URIS_ATTRIBUTE, relatedURIs.toString());
        }

        if (!adjustMarker(marker, diagnostic) && parentDiagnostic != null) {
          adjustMarker(marker, parentDiagnostic);
        }
    }
    
    public void deleteMarkers(Resource resource, Predicate<IMarker> predicate) {
        try {
            IFile file = getFile(resource.getURI());
            IMarker[] markers = file.findMarkers(getMarkerID(), true, IResource.DEPTH_INFINITE);
            Arrays.stream(markers).filter(predicate).forEach(this::deleteMarker);
        } catch (CoreException e) {
            // ignore
        }
    }
    
    private void deleteMarker(IMarker m) {
        try {
            m.delete();

            // Workaround for empty sets of markers not being persisted
            IResource resource = m.getResource();
            Workspace workspace = (Workspace) resource.getWorkspace();
            ResourceInfo info = workspace.getResourceInfo(resource.getFullPath(), false, true);
            MarkerSet markers = info.getMarkers(true);
            if (markers == null) {
                info.setMarkers(new MarkerSet());
            }
        } catch (CoreException e) {
            // ignore
        }
    }
}
