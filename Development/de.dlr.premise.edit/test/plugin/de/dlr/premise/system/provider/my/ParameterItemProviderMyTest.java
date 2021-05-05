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
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.system.provider.my.ParameterItemProviderMy;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.registry.Unit;
import de.dlr.premise.registry.Value;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;

public class ParameterItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static ParameterItemProviderMy itemAdapter;
    
    private static Parameter parameter;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
        itemAdapter = new ParameterItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
    }

	@Test
	public void testGetText() throws Exception {
        // initialize Parameter
        parameter = SystemFactory.eINSTANCE.createParameter();
        assertEquals("Parameter ", itemAdapter.getText(parameter));

        parameter.setName("Mass");
        assertEquals("Parameter Mass", itemAdapter.getText(parameter));

        // set empty value only
        Value value = RegistryFactory.eINSTANCE.createValue();
        parameter.setValue(value);
        assertEquals("Parameter Mass", itemAdapter.getText(parameter));

        // set empty unit only
        parameter.setValue(null);
        Unit unit = RegistryFactory.eINSTANCE.createUnit();
        parameter.setUnit(unit);
        assertEquals("Parameter Mass", itemAdapter.getText(parameter));

        // set empty value & unit
        parameter.setValue(value);
        assertEquals("Parameter Mass", itemAdapter.getText(parameter));

        // set unit
        unit.setSymbol("kg");
        assertEquals("Parameter Mass [kg]", itemAdapter.getText(parameter));

        // set values
        value.setValue("bla");
        assertEquals("Parameter Mass = bla [kg]", itemAdapter.getText(parameter));
        value.setValue("666.666");
        assertEquals("Parameter Mass = 666.666 [kg]", itemAdapter.getText(parameter));
        unit.setSymbol(null);
        assertEquals("Parameter Mass = 666.666", itemAdapter.getText(parameter));
        
        // large numbers
        value.setValue("123456789");
        assertEquals("Parameter Mass = 123.4568E6", itemAdapter.getText(parameter));
        
        // small numbers
        value.setValue("0.123456789");
        assertEquals("Parameter Mass = 0.123", itemAdapter.getText(parameter));
        
        value.setValue("0.000123456789");
        assertEquals("Parameter Mass = 123.4568E-6", itemAdapter.getText(parameter));
        
	}

	@Test
	public void testGetTextChangeAdapters() throws Exception {
        // initialize Parameter
        parameter = SystemFactory.eINSTANCE.createParameter();

        // add value and unit
        Value value = RegistryFactory.eINSTANCE.createValue();
        Unit unit = RegistryFactory.eINSTANCE.createUnit();
        parameter.setValue(value);
        parameter.setUnit(unit);

        assertEquals(0, value.eAdapters().size());
        // call of getText() sets adapter
        itemAdapter.getText(parameter);
        // check if parameter got the change adapter
        // TODO LabelChangeAdapter is a private inner class of ParameterItemProviderMy yet
        assertEquals(1, value.eAdapters().size());
        assertTrue(value.eAdapters().get(0).getClass().isMemberClass());
        assertEquals(ParameterItemProviderMy.class, value.eAdapters().get(0).getClass().getEnclosingClass());
        assertEquals(1, parameter.eAdapters().size()); // unit is attribute of parameter

        // repeated call of getText() sets no additional adapter
        itemAdapter.getText(parameter);
        assertEquals(1, value.eAdapters().size());
        assertEquals(1, parameter.eAdapters().size());

        // check getText() content after changing value and unit
        assertEquals("Parameter ", itemAdapter.getText(parameter));
        value.setValue("33");
        unit.setSymbol("kg");
        assertEquals("Parameter  = 33 [kg]", itemAdapter.getText(parameter));
	}
}
