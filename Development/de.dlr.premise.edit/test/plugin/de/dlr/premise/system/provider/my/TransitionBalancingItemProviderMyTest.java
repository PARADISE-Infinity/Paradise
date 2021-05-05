/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.system.provider.my;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.system.provider.my.TransitionBalancingItemProviderMy;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.system.TransitionBalancing;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;

public class TransitionBalancingItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static TransitionBalancingItemProviderMy itemAdapter;
    
    private static TransitionBalancing transitionBalancing;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }

	@Test
	public void testTransitionBalancingItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new TransitionBalancingItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {
        itemAdapter = new TransitionBalancingItemProviderMy(adapterFactory);
		assertNull(itemAdapter.getText(transitionBalancing));
		
		transitionBalancing = SystemFactory.eINSTANCE.createTransitionBalancing();
		assertEquals("TransitionBalancing ", itemAdapter.getText(transitionBalancing));
		
		transitionBalancing.setName("Defect");
		assertEquals("TransitionBalancing Defect", itemAdapter.getText(transitionBalancing));
	}
}
