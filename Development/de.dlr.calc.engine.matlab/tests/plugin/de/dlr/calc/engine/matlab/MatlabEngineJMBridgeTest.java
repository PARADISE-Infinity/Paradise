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
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;


public class MatlabEngineJMBridgeTest extends MatlabTestHelper {
    
    static MatlabEngineJMBridge engine;
    
    static String LIBPATH = de.dlr.premise.util.TestHelper.locateFile("de.dlr.calc.engine.matlab", "tests/data/matlab").getPath();
    
    @BeforeClass
    public static void setUp() {
        engine = new MatlabEngineJMBridge(new MatlabConfig());
    }

    @AfterClass
    public static void tearDown() {
        engine.close();
    }

    @Test
	public void testSetWorkingDir() {
        // test only if matlab is installed
        Assume.assumeTrue(engine.isAvailable());
        
		// set up MATLAB
		assertTrue("missing matlab engine",engine.isAvailable());
		assertTrue(engine.setWorkingDir(LIBPATH));		
	}

	@Test
	public void testAddLibrary() {
	    
        // test only if matlab is installed
        Assume.assumeTrue(engine.isAvailable());
	    
		// add a library to MATLAB path
		assertTrue(engine.addLibrary(LIBPATH));
	}
	
	@Test
	public void testCallScript() {
	    
        // test only if matlab is installed
        Assume.assumeTrue(engine.isAvailable());
		
		// set up MATLAB
		assertTrue("missing matlab engine",engine.isAvailable());

		// add script folder
		engine.addLibrary(LIBPATH);
		
		// create parameter list
		EList<Double> params = new BasicEList<Double>();
		params.add(new Double(5));
		params.add(new Double(8));
		
		// check calculation
		assertEquals(40, engine.callScript("force", params), 0.001);
		assertEquals(390625, engine.callScript("power", params),0.001);
	}	
}
