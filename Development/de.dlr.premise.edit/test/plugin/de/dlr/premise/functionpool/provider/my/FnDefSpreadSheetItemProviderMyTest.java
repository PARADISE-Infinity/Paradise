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

import de.dlr.premise.functionpool.FnDefSpreadSheet;
import de.dlr.premise.functionpool.FunctionpoolFactory;
import de.dlr.premise.functionpool.provider.my.FnDefSpreadSheetItemProviderMy;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;

public class FnDefSpreadSheetItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static FnDefSpreadSheetItemProviderMy itemAdapter;
    
    private static FnDefSpreadSheet fnDefSpreadSheet;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }
    
	@Test
	public void testFnDefSpreadSheetItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new FnDefSpreadSheetItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	
	@Test
	public void testGetText() throws Exception {

        itemAdapter = new FnDefSpreadSheetItemProviderMy(adapterFactory);
        assertNull(itemAdapter.getText(fnDefSpreadSheet));
        
        fnDefSpreadSheet = FunctionpoolFactory.eINSTANCE.createFnDefSpreadSheet();
        assertEquals("Fn Def Spread Sheet", itemAdapter.getText(fnDefSpreadSheet));
        
        fnDefSpreadSheet.setName("Excel");
        assertEquals("Excel", itemAdapter.getText(fnDefSpreadSheet));
	}
}
