/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.validation;

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IStartup;

import de.dlr.premise.system.presentation.my.EditorHelper;

public class Startup implements IStartup {

    @Override
    public void earlyStartup() {
        // register OCL if it isn't
        OCLRegistration.register();
        
        // Attach the part listener
        RegisterValidationAdapterPartListener listener = new RegisterValidationAdapterPartListener();
        EditorHelper.getPage().addPartListener(listener);
        
        // we need to manually call partOpened for editors that are already open because they 
        // were restored from the previous workbench state
        for(IEditorReference partRef : EditorHelper.getPage().getEditorReferences()) {
            listener.partOpened(partRef);
        }
    }
}
