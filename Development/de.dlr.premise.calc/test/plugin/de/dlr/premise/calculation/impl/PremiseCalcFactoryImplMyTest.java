/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.calculation.impl;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.dlr.premise.system.SystemPackage;
import de.dlr.premise.system.TransitionParameter;
import de.dlr.premise.util.RegistryHelper;


public class PremiseCalcFactoryImplMyTest {

    @Before
    public void setUp() throws Exception {
        RegistryHelper.registerFactory(SystemPackage.eNS_URI, new PremiseCalcFactoryImplMy());
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testPremiseCalcFactory() {        
        // check correct implementation of TransitionParameter
        TransitionParameter tParam= PremiseCalcFactoryImplMy.eINSTANCE.createTransitionParameter();
        assertTrue(tParam instanceof TransitionParameterImplMy);
        assertNotNull(tParam.getId());
    }
}
