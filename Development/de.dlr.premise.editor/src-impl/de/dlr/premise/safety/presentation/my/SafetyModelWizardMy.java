/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.safety.presentation.my;

import java.util.ArrayList;
import java.util.Collection;

import de.dlr.premise.safety.presentation.SafetyModelWizard;


public class SafetyModelWizardMy extends SafetyModelWizard {
    
    @Override
    protected Collection<String> getInitialObjectNames() {
        if (initialObjectNames == null) {
            initialObjectNames = new ArrayList<String>();
            initialObjectNames.add(safetyPackage.getSafetyAnalyses().getName());
        }
        return initialObjectNames;
    }
}
