/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.safetyview;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class ShowSafetyViewHandler extends AbstractHandler {

    private static final String DE_DLR_PREMISE_VIEW_SAFETY_VIEW_SAFETY_VIEW_SHEET = "de.dlr.premise.view.SafetyView.SafetyViewSheet";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // refresh or open SafetyView
        
        try {
            IViewPart safetyView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DE_DLR_PREMISE_VIEW_SAFETY_VIEW_SAFETY_VIEW_SHEET);
            if (safetyView != null) {
                
                // dispose open view
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(safetyView);
                
            }
            
            ISelection selection = HandlerUtil.getCurrentSelection(event);
            if (selection instanceof IStructuredSelection){
                EObject selectedObject = (EObject) ((IStructuredSelection) selection).getFirstElement();
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DE_DLR_PREMISE_VIEW_SAFETY_VIEW_SAFETY_VIEW_SHEET);
                IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
                ((SafetyViewSheet)part).setSelectedElement(selectedObject);
                
            }
            
            // create a new one
            
            
        } catch (PartInitException e) {
            System.err.println("Fehler beim Laden der View");
        }
        
        return null;
    }

}
