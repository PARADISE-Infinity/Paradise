/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.util;

import org.junit.Test
import static org.junit.Assert.assertEquals

class LabelHelperTest {

    @Test
	def	public cleanSpacesTest() {
		var label = " a s d fgg 2"
		assertEquals("asdfgg2", LabelHelper.cleanSpaces(label))
		
		label = "a    s s  s  ... . "
		assertEquals("asss....", LabelHelper.cleanSpaces(label))
    }

    @Test
	def	public singleSpacesTest() {
		var label = " The  use of single   spaces is     useful "
		assertEquals("The use of single spaces is useful", LabelHelper.singleSpaces(label))
    }
    
    @Test
    def public cleanNameTest() {
    	var label = "\tLast \nEntry"	
		assertEquals("LastEntry", LabelHelper.cleanSpaces(label))		
    }    
}