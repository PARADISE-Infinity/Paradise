/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.calculation;

import static org.junit.Assert.assertEquals;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.TransitionBalancing;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.scope.ScopedEObjectFactory;
import de.dlr.premise.util.TestHelper;
import de.dlr.premise.util.cyclecheck.BalancingCycleChecker;
import de.dlr.premise.util.cyclecheck.IBalancingCycleChecker;



/**
 * @author hschum
 *
 */
public class ABalancingTest {

    private static String PATH_TESTDATA = "test/data/ABalancingTest/";
    private static String PATH_INPUT_FILE = de.dlr.premise.util.TestHelper.locateFile("de.dlr.premise.calc",  PATH_TESTDATA + "ABalancingCyclesTest.premise").getPath();
    private static String PATH_FUNCPOOL_FILE = de.dlr.premise.util.TestHelper.locateFile("de.dlr.premise.calc", PATH_TESTDATA + "_GLOBAL/GlobalFuncDefs.premise.functionpool").getPath();
    private static String PATH_EXCEL_FILE = de.dlr.premise.util.TestHelper.locateFile("de.dlr.premise.calc", PATH_TESTDATA + "_GLOBAL/GlobalCalcFunctions.xls").getPath();
  
    private static ProjectRepository rep = null;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestHelper.replaceFileString(PATH_FUNCPOOL_FILE, PATH_FUNCPOOL_FILE, "fileURI=\"test/data/_GLOBAL/GlobalCalcFunctions.xls\"", "fileURI=\"" + PATH_EXCEL_FILE.substring(1) + "\"");
    }
    
    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestHelper.replaceFileString(PATH_FUNCPOOL_FILE, PATH_FUNCPOOL_FILE, "fileURI=\"" + PATH_EXCEL_FILE.substring(1)  + "\"", "fileURI=\"test/data/_GLOBAL/GlobalCalcFunctions.xls\"");
    }


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);
        rep = (ProjectRepository) resource.getContents().get(0);
    }

    // TODO: remove deprecated code?
    /*@Test
    public void testNestedBalancingCycles() {
        SystemComponent sc = rep.getProjects().get(0);
        rep.getProjects().remove(1);
        BalancingCycleCheckAdapter balList;

        balList = new BalancingCycleCheckAdapter(sc, sc.getBalancings().get(0));
        assertEquals(false, balList.hasCycle());

        NestedBalancing nbal2 = (NestedBalancing) sc.getBalancings().get(1);
        balList = new BalancingCycleCheckAdapter(sc, nbal2);
        assertEquals(true, balList.hasCycle());

        // make P2 input of NestedBal2.cyclic instead of P3, so other NestedBal1 becomes cyclic
        ((ParamRef) nbal2.getExpressions().get(0)).setParameter(sc.getParameters().get(1));
        balList = new BalancingCycleCheckAdapter(sc, sc.getBalancings().get(0));
        assertEquals(true, balList.hasCycle());
    }*/

    @Test
    public void testTransitionBalancingCycles() {
        
        // get "SC2" 
        SystemComponent sc = rep.getProjects().get(1);
        
        IBalancingCycleChecker balList;
        balList = new BalancingCycleChecker(sc, ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(PremiseHelper.getTransitions(sc).get(0).getBalancings().get(0)));
        assertEquals(true, balList.hasCycle());

        //TODO: Why is this not working?
        TransitionBalancing trans = PremiseHelper.getTransitions(sc.getChildren().get(0).getComponent()).get(2).getBalancings().get(0);
        balList =
                new BalancingCycleChecker(sc, ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(trans));
        
        assertEquals(true, balList.hasCycle());
    }
}
