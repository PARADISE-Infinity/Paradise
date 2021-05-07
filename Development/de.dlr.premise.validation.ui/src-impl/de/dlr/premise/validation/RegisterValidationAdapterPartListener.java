/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.validation;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.EditorPart;

import de.dlr.premise.system.presentation.my.EditorHelper;
import de.dlr.premise.util.urihandlers.URIHandlerHelper;
import de.dlr.premise.validation.adapter.PremiseResourceValidationAdapter;

public class RegisterValidationAdapterPartListener implements IPartListener2 {

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);

        if (part instanceof EditorPart) {
            Resource resource = EditorHelper.getEMFResource((EditorPart) part);

            if (resource != null) {
                resource.getResourceSet().eAdapters().add(new PremiseResourceValidationAdapter(resource));
            }
        }
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        // Revalidates on closing editor, in case file was not saved, which will change validity
        try {
            if (partRef.getPart(false) instanceof EditorPart) {
                EditorPart editor = (EditorPart) partRef.getPart(false);
                // if nothing was changed, no re-validation is needed
                if (!editor.isDirty()) {
                    return;
                }

                // we load the file from scratch, to get the version that is on disk
                // use a new ResourceSet, not the one of the editor, otherwise we will get the file in the dirty state
                ResourceSet resSet = new ResourceSetImpl();

                // disable notifications during load to speed it up (will otherwise hang on large files)
                resSet.getLoadOptions().put(XMIResource.OPTION_DISABLE_NOTIFY, Boolean.TRUE);
                // register URI handlers
                URIHandlerHelper.registerInto(resSet.getURIConverter());

                Resource resource = EditorHelper.getEMFResource(resSet, editor);

                if (resource != null) {
                    resource.load(null);
                    new PremiseResourceValidationAdapter(resource).doValidate();
                }
            }
        } catch (Exception e) {
            // do nothing, if file is no longer accessible errors don't matter
        }
    }

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        // Needed to satisfy interface
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        // Needed to satisfy interface
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
        // Needed to satisfy interface
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
        // Needed to satisfy interface
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
        // Needed to satisfy interface
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
        // Needed to satisfy interface
    }
}
