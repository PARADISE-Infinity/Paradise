/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.calc.ui;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.EditorPart;

import de.dlr.premise.system.presentation.my.EditorHelper;
import system.util.my.ValueChangedContentAdapter;

public class RegisterCalculationAdapterPartListener implements IPartListener2 {

    public static void addCalculationAdapters() {
        for(IEditorReference partRef : EditorHelper.getPage().getEditorReferences()) {
            addCalculationAdapter(partRef);
        }
    }
    
    public static void removeCalculationAdapters() {
        for(IEditorReference partRef : EditorHelper.getPage().getEditorReferences()) {
            removeCalculationAdapter(partRef);
        }
    }
    
    public static void addCalculationAdapter(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof EditorPart) {
            Resource resource = EditorHelper.getEMFResource((EditorPart) part);

            if (resource != null) {
                ResourceSet resSet = resource.getResourceSet();
                
                if (resSet != null) {
                    boolean hasAdapter = false;
                    for (Adapter adapter : resSet.eAdapters()) {
                        if (adapter instanceof ValueChangedContentAdapter) {
                            hasAdapter = true;
                            break;
                        }
                    }
                    if (!hasAdapter) {
                        ValueChangedContentAdapter adapter = new ValueChangedContentAdapter();
                        resSet.eAdapters().add(adapter);
                    }
                }
            }
        }
    }
    
    public static void removeCalculationAdapter(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof EditorPart) {
            Resource resource = EditorHelper.getEMFResource((EditorPart) part);

            if (resource != null) {
                ResourceSet resSet = resource.getResourceSet();
                if (resSet != null) {
                    resSet.eAdapters().removeIf(ValueChangedContentAdapter.class::isInstance);
                }
            }
        }
    }
    
    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
        if (Activator.getDefault().getCalculationPreference()) {
            addCalculationAdapter(partRef);
        }
    }

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        if (Activator.getDefault().getCalculationPreference()) {
            addCalculationAdapter(partRef);
        }
    }
    

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {        
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
