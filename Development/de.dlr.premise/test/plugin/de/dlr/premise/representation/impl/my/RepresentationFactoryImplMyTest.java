/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.representation.impl.my;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.dlr.premise.representation.Representation;
import de.dlr.premise.representation.RepresentationFactory;

public class RepresentationFactoryImplMyTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testCreateRepresentation() throws Exception {

        // test the default values of the representation file generation        
        final String defaultName = "set a name";
        final String metaModelRegex = "\\d.\\d\\d";
        
        // test creation with default values
        Representation rep = RepresentationFactory.eINSTANCE.createRepresentation();
        assertEquals(defaultName, rep.getName());
        assertTrue(rep.getMetaModel().matches(metaModelRegex));        

        // each representation file needs a UUID set
        assertNotNull(rep.getId());
    }
}
