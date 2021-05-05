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
import de.dlr.premise.functionpool.FnOutput;
import de.dlr.premise.functionpool.FunctionpoolFactory;
import de.dlr.premise.functionpool.provider.my.FnOutputItemProviderMy;

public class FnOutputItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static FnOutputItemProviderMy itemAdapter;
    
    private static FnOutput fnOutput;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }

	@Test
	public void testFnOutputItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new FnOutputItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {
        itemAdapter = new FnOutputItemProviderMy(adapterFactory);
        assertNull(itemAdapter.getText(fnOutput));
        
        fnOutput = FunctionpoolFactory.eINSTANCE.createFnOutput();
        assertEquals("Fn Output", itemAdapter.getText(fnOutput));
        
        fnOutput.setName("Output");
        assertEquals("Output", itemAdapter.getText(fnOutput));
	}
}
