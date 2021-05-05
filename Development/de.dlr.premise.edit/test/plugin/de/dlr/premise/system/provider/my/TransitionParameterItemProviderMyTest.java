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

import de.dlr.premise.system.provider.my.TransitionParameterItemProviderMy;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.system.TransitionParameter;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;

public class TransitionParameterItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static TransitionParameterItemProviderMy itemAdapter;
    
    private static TransitionParameter tParameter;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }

	@Test
	public void testTransitionParameterItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new TransitionParameterItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {
		
        itemAdapter = new TransitionParameterItemProviderMy(adapterFactory);
        assertNull(itemAdapter.getText(tParameter));
        
        tParameter = SystemFactory.eINSTANCE.createTransitionParameter();
        assertEquals("TransitionParameter ", itemAdapter.getText(tParameter));
        
        tParameter.setName("Test");
        assertEquals("TransitionParameter Test", itemAdapter.getText(tParameter));
	}
}
