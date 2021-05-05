/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.calculation;

import static org.junit.Assert.assertEquals;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Before;
import org.junit.Test;

import de.dlr.premise.calculation.transition.ITransitionBalancingCalculator;
import de.dlr.premise.calculation.transition.TransitionBalancingCalculator;
import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.util.TestHelper;

public class TransitionBalancingTest {

    private static String PATH_TESTDATA = "test/data/TransitionBalTest/";
    private static String PATH_INPUT_FILE = de.dlr.premise.util.TestHelper.locateFile("de.dlr.premise.calc",  PATH_TESTDATA + "TransitionBalancingCalc.system").getPath();
    private static ProjectRepository rep = null;
    
    @Before
    public void setUp() throws Exception {
        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);
        rep = (ProjectRepository) resource.getContents().get(0);
    }
    
    @Test
    public void testGateCalculation() {
        SystemComponent sc1 = rep.getProjects().get(0);
        ITransitionBalancingCalculator bcalc = new TransitionBalancingCalculator();
        
      // Test OR
        // get systemcomponent
        SystemComponent scGATE = (SystemComponent) sc1.getChildren().get(0);
        // get a source and the target parameter
        AParameterDef targetParam = scGATE.getStatemachines().get(0).getTransitions().get(0).getParameters().get(0);
        AParameterDef oneSource = scGATE.getStatemachines().get(1).getTransitions().get(0).getParameters().get(0);
        // change the source parameter to "0.2"
        oneSource.getValue().setValue("0.2");
        // start calculating
        bcalc.calculate(scGATE.getStatemachines().get(0).getTransitions().get(0).getBalancings().get(0));
        // check that the calculation was fulfilled correctly
        assertEquals("0.6639999999999999", targetParam.getValue().getValue());
        
     // Test AND
        // get systemcomponent
        scGATE = (SystemComponent) sc1.getChildren().get(1);
        // get a source and the target parameter
        targetParam = scGATE.getStatemachines().get(0).getTransitions().get(0).getParameters().get(0);
        oneSource = scGATE.getStatemachines().get(1).getTransitions().get(0).getParameters().get(0);
        // change the source parameter to "0.2"
        oneSource.getValue().setValue("0.2");
        // start calculating
        bcalc.calculate(scGATE.getStatemachines().get(0).getTransitions().get(0).getBalancings().get(0));
        // check that the calculation was fulfilled correctly
        assertEquals("0.024", targetParam.getValue().getValue());
        
     // Test XOR
        // get systemcomponent
        scGATE = (SystemComponent) sc1.getChildren().get(2);
        // get a source and the target parameter
        targetParam = scGATE.getStatemachines().get(0).getTransitions().get(0).getParameters().get(0);
        oneSource = scGATE.getStatemachines().get(1).getTransitions().get(0).getParameters().get(0);
        // change the source parameter to "0.2"
        oneSource.getValue().setValue("0.2");
        // start calculating
        bcalc.calculate(scGATE.getStatemachines().get(0).getTransitions().get(0).getBalancings().get(0));
        // check that the calculation was fulfilled correctly
        assertEquals("0.391541504", targetParam.getValue().getValue());
        
     // Test VOTE
        // get systemcomponent
        scGATE = (SystemComponent) sc1.getChildren().get(3);
        // get a source and the target parameter
        targetParam = scGATE.getStatemachines().get(0).getTransitions().get(0).getParameters().get(0);
        oneSource = scGATE.getStatemachines().get(1).getTransitions().get(0).getParameters().get(0);
        // change the source parameter to "0.2"
        oneSource.getValue().setValue("0.2");
        // start calculating
        bcalc.calculate(scGATE.getStatemachines().get(0).getTransitions().get(0).getBalancings().get(0));
        // check that the calculation was fulfilled correctly
        assertEquals("0.19708924313600007", targetParam.getValue().getValue());
     
    }
    
}
