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

import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;
import de.dlr.premise.element.Connection;
import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.element.provider.my.ConnectionItemProviderMy;

public class ConnectionItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static ConnectionItemProviderMy itemAdapter;
    
    private static Connection<SystemComponent> connection;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }

	@Test
	public void testConnectionItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new ConnectionItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {
        itemAdapter = new ConnectionItemProviderMy(adapterFactory);
        assertNull(itemAdapter.getText(connection));
        
        connection = ElementFactory.eINSTANCE.createConnection();
        assertEquals("Connection ", itemAdapter.getText(connection));
        
        SystemComponent src = SystemFactory.eINSTANCE.createSystemComponent();
        src.setName("1");
        SystemComponent tar = SystemFactory.eINSTANCE.createSystemComponent();
        tar.setName("1");

        connection.setSource(src);
        connection.setTarget(tar);
        assertEquals("Connection Output {1} > Input {1}", itemAdapter.getText(connection));
	}
}
