/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.util;

import de.dlr.premise.element.Mode;

/**This class exists to differentiate drags out of the PremiseEditor from drags out of other sources  
 * */
public class SafetyModeContainer {
    
    private Mode containedMode;
    
    
    public Mode getContainedMode() {
        return containedMode;
    }

    
    public void setContainedMode(Mode containedMode) {
        this.containedMode = containedMode;
    }

    public SafetyModeContainer(Mode containingMode) {
        containedMode = containingMode;
    }
    
    
    
}
