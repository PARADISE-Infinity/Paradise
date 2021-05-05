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
import static org.junit.Assert.assertNull;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.functions.RangeConstraint;
import de.dlr.premise.functions.UseCaseFactory;
import de.dlr.premise.functions.provider.my.RangeConstraintItemProviderMy;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;

public class RangeConstraintItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static RangeConstraintItemProviderMy itemAdapter;
    
    private static RangeConstraint rangeConstraint;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // initialize adapter factory
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }

	@Test
	public void testRangeConstraintItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new RangeConstraintItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {
        itemAdapter = new RangeConstraintItemProviderMy(adapterFactory);
        assertNull(itemAdapter.getText(rangeConstraint));
        
        rangeConstraint = UseCaseFactory.eINSTANCE.createRangeConstraint();
        assertEquals("RangeConstraint [-Inf, Inf]", itemAdapter.getText(rangeConstraint));
        
        rangeConstraint.setLower("-10");
        assertEquals("RangeConstraint [-10, Inf]", itemAdapter.getText(rangeConstraint));
        
        rangeConstraint.setUpper("20");
        assertEquals("RangeConstraint [-10, 20]", itemAdapter.getText(rangeConstraint));
	}
}
