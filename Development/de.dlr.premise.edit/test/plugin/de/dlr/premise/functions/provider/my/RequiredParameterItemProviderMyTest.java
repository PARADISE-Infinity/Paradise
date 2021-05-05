/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.functions.provider.my;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.functions.RequiredParameter;
import de.dlr.premise.functions.UseCaseFactory;
import de.dlr.premise.functions.provider.my.RequiredParameterItemProviderMy;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.registry.Unit;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;

public class RequiredParameterItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static RequiredParameterItemProviderMy itemAdapter;
    
    private static RequiredParameter reqParam;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }

	@Test
	public void testParameterConstraintItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new RequiredParameterItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new RequiredParameterItemProviderMy(adapterFactory);
		// hschum: to call/test adapter with null makes no sense to me
        //assertNull(itemAdapter.getText(reqParam));

        reqParam = UseCaseFactory.eINSTANCE.createRequiredParameter();
        assertEquals("RequiredParameter ", itemAdapter.getText(reqParam));
        
        reqParam.setName("Const");
        assertEquals("RequiredParameter Const", itemAdapter.getText(reqParam));        

        // set empty unit only
        Unit unit = RegistryFactory.eINSTANCE.createUnit();
        reqParam.setUnit(unit);
        assertEquals("RequiredParameter Const", itemAdapter.getText(reqParam));

        // set unit
        unit.setSymbol("kg");
        assertEquals("RequiredParameter Const [kg]", itemAdapter.getText(reqParam));
	}
}
