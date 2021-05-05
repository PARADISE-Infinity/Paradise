/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.excel;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.dlr.calc.engine.excel.util.ExcelUtil;


public class ExcelUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public void testConvert() {
		
		// test invalid cells names
		assertNull(ExcelUtil.convert("a"));
		
		// test valid names
		assertEquals(0,ExcelUtil.convert("A1")[0]);
		assertEquals(0,ExcelUtil.convert("A1")[1]);
		
		assertEquals(26,ExcelUtil.convert("AA1")[0]);
		assertEquals(0,ExcelUtil.convert("AA1")[1]);
		
		assertEquals(52,ExcelUtil.convert("BA1")[0]);
		assertEquals(701,ExcelUtil.convert("ZZ1")[0]);
		
		assertEquals(701,ExcelUtil.convert("ZZ65536")[0]);
		assertEquals(65535,ExcelUtil.convert("ZZ65536")[1]);
	}
}
