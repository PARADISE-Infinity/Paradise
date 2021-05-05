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

import de.dlr.premise.system.provider.my.ModeValueRefItemProviderMy;
import de.dlr.premise.system.ModeValueRef;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;

public class ModeValueRefItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static ModeValueRefItemProviderMy itemAdapter;
    
    private static ModeValueRef modeValueRef;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }
	
	@Test
	public void testModeValueRefItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new ModeValueRefItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {
        itemAdapter = new ModeValueRefItemProviderMy(adapterFactory);
        assertNull(itemAdapter.getText(modeValueRef));
        
        modeValueRef = SystemFactory.eINSTANCE.createModeValueRef();
        assertEquals("Value ", itemAdapter.getText(modeValueRef));
        
        modeValueRef.setValue("27.0");
        assertEquals("Value 27", itemAdapter.getText(modeValueRef));
        
        modeValueRef.setValue("0.000005");
        assertEquals("Value 5.0E-6", itemAdapter.getText(modeValueRef));
	}
}
