/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.safetyview.generic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class DefaultHazardsTest {

    
    private DefaultHazards defHazards;
    
    @Before
    public void init() {
        defHazards = new DefaultHazards();
    }
    
    @Test
    public void testDefaultHazards() {
        assertNotNull(defHazards);
        assertEquals(6, defHazards.length);
    }

    @Test
    public void testGetHazard() {
        
        assertNotNull(defHazards);
        String ref = defHazards.getHazardName(0);
        assertEquals("Fails to operate State", ref); 

        ref = defHazards.getFailureModeName(0);
        assertEquals("Fails to operate", ref);
        
        ref = defHazards.getEventName(0);
        assertEquals("F: Fails to operate", ref);
    }
}
