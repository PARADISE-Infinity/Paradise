/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.migration;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ModelFileTypeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


    @Test
    public void testGetType() {
    	assertEquals(ModelFileType.FUNCTION, ModelFileType.getModelFileType("Test.usecase"));
        assertEquals(ModelFileType.FUNCTION, ModelFileType.getModelFileType("Test.function"));
    	assertEquals(ModelFileType.SYSTEM, ModelFileType.getModelFileType("Test.premise"));
        assertEquals(ModelFileType.SYSTEM, ModelFileType.getModelFileType("Test.system"));
    	assertEquals(ModelFileType.FUNCTIONPOOL, ModelFileType.getModelFileType("Test.functionpool"));
    	assertEquals(ModelFileType.REGISTRY, ModelFileType.getModelFileType("Test.registry"));
    	assertEquals(ModelFileType.REPRESENTATION, ModelFileType.getModelFileType("Test.representation"));
        assertEquals(ModelFileType.COMPONENT, ModelFileType.getModelFileType("Test.component"));
    	assertEquals(ModelFileType.UNKNOWN, ModelFileType.getModelFileType("n"));
    }    
}
