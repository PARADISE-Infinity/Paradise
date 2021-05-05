/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.element.provider.my;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.registry.Junction;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;
import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.element.GuardCombination;

public class GuardCombinationItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static GuardCombinationItemProviderMy itemAdapter;

    private static GuardCombination guardCombination;
	
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }

	@Test
	public void testGuardCombinationItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new GuardCombinationItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}
    
	@Test
	public void testGetText() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new GuardCombinationItemProviderMy(adapterFactory);
        
        String label = itemAdapter.getText(guardCombination);
        assertEquals("", label);
        
        guardCombination = ElementFactory.eINSTANCE.createGuardCombination();
        
        guardCombination.setJunction(Junction.AND);
        label = itemAdapter.getText(guardCombination);
        assertEquals("GuardCombination AND ", label);
                
        guardCombination.setJunction(Junction.OR);
        label = itemAdapter.getText(guardCombination);
        assertEquals("GuardCombination OR ", label);
	}
}
