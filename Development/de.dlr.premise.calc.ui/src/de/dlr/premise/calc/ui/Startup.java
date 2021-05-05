/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.calc.ui;

import org.eclipse.ui.IStartup;

import de.dlr.premise.system.presentation.my.EditorHelper;


public class Startup implements IStartup {

    @Override
    public void earlyStartup() {

        // create adapter
        RegisterCalculationAdapterPartListener listener = new RegisterCalculationAdapterPartListener(); 
        EditorHelper.getPage().addPartListener(listener);
        
        // we need to manually add adapters to editors that are already open because they 
        // were restored from the previous workbench state
        RegisterCalculationAdapterPartListener.addCalculationAdapters();
    }
}
