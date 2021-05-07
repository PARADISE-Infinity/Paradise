/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.functionpool.provider.my;

import static org.junit.Assert.*;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;
import de.dlr.premise.provider.util.ItemProviderAdapterMy;
import de.dlr.premise.util.TestHelper;
import de.dlr.premise.functionpool.FunctionPool;
import de.dlr.premise.functionpool.provider.my.FnDefSpreadSheetItemProviderMy;


public class FunctionpoolItemProviderMyTest {

    private static String PATH_INPUT_FILE = 
            de.dlr.premise.util.TestHelper.locateFile("de.dlr.premise.edit", "test/data/LabelTestCases.functionpool").getPath();
    private static AdapterFactory funcpoolAdapterFactory;
    private static ItemProviderAdapterMy itemAdapter;

    private static FunctionPool pool;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // load model from .registry file
        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);

        // initialize top components
        pool = (FunctionPool) resource.getContents().get(0);

        // initialize adapter factory
        funcpoolAdapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }

    @Test
    public void testUnit() {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new FnDefSpreadSheetItemProviderMy(funcpoolAdapterFactory);

        // check labels
        assertEquals("Fn1", itemAdapter.getText(pool.getFunctions().get(0)));
    }
}
