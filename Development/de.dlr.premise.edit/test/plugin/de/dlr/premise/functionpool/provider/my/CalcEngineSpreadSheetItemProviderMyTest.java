/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.functionpool.provider.my;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;
import de.dlr.premise.functionpool.CalcEngineSpreadSheet;
import de.dlr.premise.functionpool.FunctionpoolFactory;
import de.dlr.premise.functionpool.provider.my.CalcEngineSpreadSheetItemProviderMy;

public class CalcEngineSpreadSheetItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static CalcEngineSpreadSheetItemProviderMy itemAdapter;
    
    private static CalcEngineSpreadSheet calcEngineSpreadSheet;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }

	@Test
	public void testCalcEngineSpreadSheetItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new CalcEngineSpreadSheetItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}
	
	@Test
	public void testGetText() throws Exception {
        itemAdapter = new CalcEngineSpreadSheetItemProviderMy(adapterFactory);
		assertNull(itemAdapter.getText(calcEngineSpreadSheet));
		
		calcEngineSpreadSheet = FunctionpoolFactory.eINSTANCE.createCalcEngineSpreadSheet();
		assertEquals("Calc Engine Spread Sheet", itemAdapter.getText(calcEngineSpreadSheet));
		
		calcEngineSpreadSheet.setName("Excel");
		assertEquals("Excel", itemAdapter.getText(calcEngineSpreadSheet));
	}
}
