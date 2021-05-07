/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.functionpool.provider.my;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import de.dlr.premise.functionpool.provider.my.LabelHelper;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.registry.Unit;

public class LabelHelperTest {

	@Test
	public void testGetUnitSymbol() throws Exception {
		
		Unit unit = RegistryFactory.eINSTANCE.createUnit();
		assertNull("", LabelHelper.getUnitSymbol(unit));

		String symbol = "m³"; 
		unit.setSymbol(symbol);
		
		assertEquals("m³", LabelHelper.getUnitSymbol(unit));
	}
}
