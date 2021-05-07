/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.constraints;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.edit.ui.util.EditUIUtil;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import de.dlr.premise.constraints.handlers.MarkerCreatingConstraintViolationHandler;
import de.dlr.premise.system.presentation.SystemEditor;
import de.dlr.premise.util.urihandlers.URIHandlerHelper;

public class RegisterConstraintCheckAdapterPartListener implements IPartListener2 {
	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false) instanceof SystemEditor) {
		    SystemEditor editor = (SystemEditor) partRef.getPart(false);
			Resource resource = getResource(editor);
			
			IResource eclipseResource = getEclipseResource(editor);
			
            if (resource != null && resource.getResourceSet() != null  && eclipseResource != null) {
                ResourceSet resSet = resource.getResourceSet();
                
                MarkerCreatingConstraintViolationHandler violationHandler = new MarkerCreatingConstraintViolationHandler(eclipseResource, resSet);
                violationHandler.removeViolations();
                
                ConstraintChecker adapter = new ConstraintChecker(violationHandler);

                resSet.eAdapters().add(adapter);
                adapter.recheck(resSet);
            }
		}
	}

    private IResource getEclipseResource(SystemEditor editor) {
        IResource eclipseResource = null;
        if (editor.getEditorInput() instanceof FileEditorInput) {
            eclipseResource = ((FileEditorInput) editor.getEditorInput()).getFile();
        }
        return eclipseResource;
    }
	
    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        // recheck on closing editor, in case file was not saved, which will change validity
        try {
            if (partRef.getPart(false) instanceof SystemEditor) {
                SystemEditor editor = (SystemEditor) partRef.getPart(false);
                // if nothing was changed, no re-checking is needed
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
                
                Resource resource = getResource(resSet, editor);               
                resource.load(null);
                EcoreUtil.resolveAll(resSet);
                
                // we also need the eclipse resource for creating/removing markers
                IResource eclipseResource = getEclipseResource(editor);
                
                // set up a new consraint checker
                MarkerCreatingConstraintViolationHandler violationHandler = new MarkerCreatingConstraintViolationHandler(eclipseResource, resSet);
                violationHandler.removeViolations();
                
                ConstraintChecker adapter = new ConstraintChecker(violationHandler);
                
                resSet.eAdapters().add(adapter);
                adapter.recheck(resSet);
            }
        } catch (Exception e) {
            // do nothing, if file is no longer accessible errors don't matter
        }
    }

	private Resource getResource(SystemEditor editor) {
		return getResource(editor.getEditingDomain().getResourceSet(), editor);
	}
	
    private Resource getResource(ResourceSet resSet, EditorPart editor) {
        return resSet.getResource(EditUIUtil.getURI(editor.getEditorInput()), true);
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
