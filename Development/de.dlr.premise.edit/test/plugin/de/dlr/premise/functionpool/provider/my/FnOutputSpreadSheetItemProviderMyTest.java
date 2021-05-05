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
import de.dlr.premise.functionpool.FnOutputSpreadSheet;
import de.dlr.premise.functionpool.FunctionpoolFactory;
import de.dlr.premise.functionpool.provider.my.FnOutputSpreadSheetItemProviderMy;

public class FnOutputSpreadSheetItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static FnOutputSpreadSheetItemProviderMy itemAdapter;
    
    private static FnOutputSpreadSheet fnOutputSpreadSheet;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }

	@Test
	public void testFnOutputSpreadSheetItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new FnOutputSpreadSheetItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {

        itemAdapter = new FnOutputSpreadSheetItemProviderMy(adapterFactory);
        assertNull(itemAdapter.getText(fnOutputSpreadSheet));
        
        fnOutputSpreadSheet = FunctionpoolFactory.eINSTANCE.createFnOutputSpreadSheet();
        assertEquals("Fn Output Spread Sheet", itemAdapter.getText(fnOutputSpreadSheet));
        
        fnOutputSpreadSheet.setName("Out");
        assertEquals("Out", itemAdapter.getText(fnOutputSpreadSheet));
	}
}
