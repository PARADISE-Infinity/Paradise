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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;
import de.dlr.premise.functionpool.FnInputSpreadSheet;
import de.dlr.premise.functionpool.FunctionpoolFactory;
import de.dlr.premise.functionpool.provider.my.FnInputSpreadSheetItemProviderMy;

public class FnInputSpreadSheetItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static FnInputSpreadSheetItemProviderMy itemAdapter;
    
    private static FnInputSpreadSheet fnInputSpreadSheet;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }
    
	@Test
	public void testFnInputSpreadSheetItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new FnInputSpreadSheetItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {
        itemAdapter = new FnInputSpreadSheetItemProviderMy(adapterFactory);
        assertNull(itemAdapter.getText(fnInputSpreadSheet));
        
        fnInputSpreadSheet = FunctionpoolFactory.eINSTANCE.createFnInputSpreadSheet();
        assertEquals("Fn Input Spread Sheet", itemAdapter.getText(fnInputSpreadSheet));
        
        fnInputSpreadSheet.setName("Input");
        assertEquals("Input", itemAdapter.getText(fnInputSpreadSheet));
	}
}
