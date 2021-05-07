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


import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.element.Transition;
import de.dlr.premise.element.provider.my.TransitionItemProviderMy;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;

public class TransitionItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static TransitionItemProviderMy itemAdapter;
    
    private static Transition transition;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }
    
	@Test
	public void testTransitionItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new TransitionItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {
	    transition = null;
	    
        itemAdapter = new TransitionItemProviderMy(adapterFactory);
        assertNull(itemAdapter.getText(transition));
        
        transition = ElementFactory.eINSTANCE.createTransition();
        assertEquals("Transition ", itemAdapter.getText(transition));
        
        transition.setName("Defect");
        assertEquals("Transition Defect", itemAdapter.getText(transition));
	}
}
