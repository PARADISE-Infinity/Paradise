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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MatlabConfigTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void MatlabConfig() {
		MatlabConfig config = new MatlabConfig();
		assertNotNull(config);
		
		assertEquals("C:/Program Files/MATLAB/R2007b/bin/MATLAB.exe", config.getMatlabPath());
		assertEquals("C:/Program Files/MATLAB/Tools/JMBridge", config.getJMBridgePath());
		assertEquals(5564, config.getJMBridgePort());
		assertEquals(60000, config.getJMBridgeTimeout());
		assertFalse(config.getMultiCalc());
	}

	@Test
	public void testGetAndSet() {
		
		MatlabConfig config = new MatlabConfig();
		assertNotNull(config);

		String matlabPath = "C:/Program Files (x86)/MATLAB/R2015b/bin/MATLAB.exe";
		config.setMatlabPath(matlabPath);
		assertEquals(matlabPath, config.getMatlabPath());
		assertFalse(config.MatlabExists());
		
		String jmPath = "C:/Program Files (x86)/MATLAB/Tools/JMBridge/1.9.3";
		config.setJMBridgePath(jmPath);
		assertEquals(jmPath, config.getJMBridgePath());
		assertFalse(config.JMBridgeExists());
		
		int port = 7234;
		config.setJMBridgePort(port);
		assertEquals(port, config.getJMBridgePort());

		int timeOut = 60000;
		config.setJMBridgeTimeOut(timeOut);
		assertEquals(timeOut, config.getJMBridgeTimeout());
		
		config.setMultiCalc("on");
		assertTrue(config.getMultiCalc());
		config.setMultiCalc("off");
		assertFalse(config.getMultiCalc());
	}	
}
