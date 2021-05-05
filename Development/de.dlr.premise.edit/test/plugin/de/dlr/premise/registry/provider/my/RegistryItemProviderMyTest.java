/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.registry.provider.my;

import static org.junit.Assert.*;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.registry.Registry;
import de.dlr.premise.registry.provider.my.ConstantItemProviderMy;
import de.dlr.premise.registry.provider.my.MetaTypeDefItemProviderMy;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;
import de.dlr.premise.registry.provider.my.UnitItemProviderMy;
import de.dlr.premise.provider.util.ItemProviderAdapterMy;
import de.dlr.premise.util.TestHelper;


public class RegistryItemProviderMyTest {

    private static String PATH_INPUT_FILE = 
            de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_EDIT, "test/data/LabelTestCases.registry").getPath();
    private static AdapterFactory registryAdapterFactory;
    private static ItemProviderAdapterMy itemAdapter;

    private static Registry reg;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // load model from .registry file
        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);

        // initialize top components
        reg = (Registry) resource.getContents().get(0);

        // initialize adapter factory
        registryAdapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }

    @Test
    public void testUnit() {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new UnitItemProviderMy(registryAdapterFactory);

        // check labels
        assertEquals("Unit",           itemAdapter.getText(reg.getUnits().get(0)));
        assertEquals("Without Symbol", itemAdapter.getText(reg.getUnits().get(1)));
        assertEquals("Unit",           itemAdapter.getText(reg.getUnits().get(2)));
        assertEquals("Per Cent [%]",   itemAdapter.getText(reg.getUnits().get(3)));
    }

    @Test
    public void testConstant() {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new ConstantItemProviderMy(registryAdapterFactory);

        // check labels
        assertEquals("Constant ",   itemAdapter.getText(reg.getConstants().get(0)));
        assertEquals("Pi",          itemAdapter.getText(reg.getConstants().get(1)));
        assertEquals("Constant ",   itemAdapter.getText(reg.getConstants().get(2)));
        assertEquals("Pi [1]",      itemAdapter.getText(reg.getConstants().get(3)));
        assertEquals("Pi",          itemAdapter.getText(reg.getConstants().get(4)));
        assertEquals("Pi = 3.14",     itemAdapter.getText(reg.getConstants().get(5)));
        assertEquals("Pi = 3.14 [1]", itemAdapter.getText(reg.getConstants().get(6)));
    }

    @Test
    public void testMetaTypeDef() {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new MetaTypeDefItemProviderMy(registryAdapterFactory);

        // check labels
        assertEquals("MetaTypeDef ",  itemAdapter.getText(reg.getMetaTypes().get(0)));
        assertEquals("Without Icon", itemAdapter.getText(reg.getMetaTypes().get(1)));
        assertEquals("Container",    itemAdapter.getText(reg.getMetaTypes().get(2)));
    }
}
