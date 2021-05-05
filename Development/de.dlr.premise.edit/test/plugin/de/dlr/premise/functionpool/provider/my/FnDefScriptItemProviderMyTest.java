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
import de.dlr.premise.functionpool.FnDefScript;
import de.dlr.premise.functionpool.FunctionpoolFactory;
import de.dlr.premise.functionpool.provider.my.FnDefScriptItemProviderMy;

public class FnDefScriptItemProviderMyTest {
	
    private static AdapterFactory adapterFactory;
    private static FnDefScriptItemProviderMy itemAdapter;
    
    private static FnDefScript fnDefScript;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }

	@Test
	public void testFnDefScriptItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new FnDefScriptItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {
        itemAdapter = new FnDefScriptItemProviderMy(adapterFactory);
        assertNull(itemAdapter.getText(fnDefScript));
        
        fnDefScript = FunctionpoolFactory.eINSTANCE.createFnDefScript();
        assertEquals("Fn Def Script", itemAdapter.getText(fnDefScript));
        
        fnDefScript.setName("Matlab Fun");
        assertEquals("Matlab Fun", itemAdapter.getText(fnDefScript));
	}
}
