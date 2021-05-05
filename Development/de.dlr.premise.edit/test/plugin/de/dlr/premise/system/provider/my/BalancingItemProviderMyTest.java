/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.system.provider.my;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.system.provider.my.BalancingItemProviderMy;
import de.dlr.premise.system.Balancing;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;

public class BalancingItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static BalancingItemProviderMy itemAdapter;
    
    private static Balancing bal;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }

	@Test
	public void testBalancingItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new BalancingItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {
        itemAdapter = new BalancingItemProviderMy(adapterFactory);
        assertNull(itemAdapter.getText(bal));

        bal = SystemFactory.eINSTANCE.createBalancing();
        assertEquals("Balancing ", itemAdapter.getText(bal));
        
        bal.setName("Multiply");
        assertEquals("Balancing Multiply", itemAdapter.getText(bal));
	}
}
