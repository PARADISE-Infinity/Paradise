/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.calculation.valuechangedcontentadapter;

import de.dlr.premise.util.cyclecheck.IBalancingCycleChecker;


public class BalancingCycleCheckerMock implements IBalancingCycleChecker {
    private boolean cycle = false;
    
    @Override
    public boolean hasCycle(){
        return cycle;
    }

    public void setCycle(boolean cycle) {
        this.cycle = cycle;
    }
    
    public void reset() {
        cycle = false;
    }
}
