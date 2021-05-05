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

import java.util.ArrayList;
import java.util.List;

import de.dlr.calc.engine.dsl.ParameterRenamer;
import de.dlr.premise.system.Balancing;


public class ParameterRenamerMock extends ParameterRenamer {
    private List<Balancing> doRenameCalls = new ArrayList<>();
    
    @Override
    public void doRename(Balancing balancing) {
        doRenameCalls.add(balancing);
    }
    
    @Override
    public String createRenamedFunction(Balancing balancing) {
        throw new UnsupportedOperationException("Client is not expected to call this method!");
    }
    
    public List<Balancing> getDoRenameCalls() {
        return doRenameCalls;
    }
    
    public void reset() {
        doRenameCalls.clear();
    }
}
