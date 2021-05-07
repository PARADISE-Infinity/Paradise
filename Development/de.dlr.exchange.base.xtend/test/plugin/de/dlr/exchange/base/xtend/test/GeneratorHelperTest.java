/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.base.xtend.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.dlr.exchange.base.xtend.GeneratorHelper;

public class GeneratorHelperTest {

	@Test
	public void testEncodeFileName() throws Exception {
		
		String name;
		
		name = "Help\\nLine";
		assertEquals("HelpLine", GeneratorHelper.encodeFileName(name));
		
		name = "Test/Line/Base";
		assertEquals("Test_Line_Base", GeneratorHelper.encodeFileName(name));
		
		name = "Test\\Line\\Base";
		assertEquals("Test_Line_Base", GeneratorHelper.encodeFileName(name));

		name = "Test:Line:Base";
		assertEquals("Test_Line_Base", GeneratorHelper.encodeFileName(name));		

		name = "Test Line Base";
		assertEquals("Test_Line_Base", GeneratorHelper.encodeFileName(name));		

	}
}
