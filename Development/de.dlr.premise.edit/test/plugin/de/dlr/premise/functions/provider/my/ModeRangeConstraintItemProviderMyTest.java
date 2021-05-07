/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.functions.provider.my;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.functions.ModeRangeConstraint;
import de.dlr.premise.functions.UseCaseFactory;
import de.dlr.premise.functions.provider.my.ModeRangeConstraintItemProviderMy;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;

public class ModeRangeConstraintItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static ModeRangeConstraintItemProviderMy itemAdapter;

    private static ModeRangeConstraint modeRangeConstraint;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
    }
    
	@Test
	public void testModeRangeConstraintItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new ModeRangeConstraintItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {
		
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new ModeRangeConstraintItemProviderMy(adapterFactory);
        
        assertNull(itemAdapter.getText(modeRangeConstraint));

        modeRangeConstraint = UseCaseFactory.eINSTANCE.createModeRangeConstraint();
        assertEquals("ModeRangeConstraint [-Inf, Inf]", itemAdapter.getText(modeRangeConstraint));

        modeRangeConstraint.setLower("-20");
        assertEquals("ModeRangeConstraint [-20, Inf]", itemAdapter.getText(modeRangeConstraint));

        modeRangeConstraint.setUpper("22");
        assertEquals("ModeRangeConstraint [-20, 22]", itemAdapter.getText(modeRangeConstraint));
	}
}
