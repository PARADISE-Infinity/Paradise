/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.matlab;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MatlabHelperTest {

    @Test
    public void testGetTmpVarName() {
        
        int length = 35; 
        String name = MatlabHelper.getTmpVarName(length);
        assertEquals(32, name.length());

        length = 32;
        name = MatlabHelper.getTmpVarName(length);
        assertEquals(32, name.length());

        length = 6;
        name = MatlabHelper.getTmpVarName(length);
        assertEquals(6, name.length());

        length = 5;
        name = MatlabHelper.getTmpVarName(length);
        assertEquals(6, name.length());
        
        length = -7;
        name = MatlabHelper.getTmpVarName(length);
        assertEquals(6, name.length());
    }
}
