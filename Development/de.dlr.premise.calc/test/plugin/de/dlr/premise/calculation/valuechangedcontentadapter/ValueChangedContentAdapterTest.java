/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.calculation.valuechangedcontentadapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.Before;
import org.junit.Test;

import de.dlr.premise.calculation.valuechangedcontentadapter.BalancingCalculatorMock.CalculateArguments;
import de.dlr.premise.element.Mode;
import de.dlr.premise.graph.DirectPointer;
import de.dlr.premise.graph.GraphFactory;
import de.dlr.premise.states.data.State;
import de.dlr.premise.system.Balancing;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.TestHelper;

public class ValueChangedContentAdapterTest {
    private static String PATH_TESTDATA = "test/data/ValueChangedContentAdapterTest/";
    private static String PATH_INPUT_FILE = de.dlr.premise.util.TestHelper.locateFile("de.dlr.premise.calc",  PATH_TESTDATA + "System.premise").getPath();

    private ResourceSet resSet;
    private ProjectRepository projectRepository;
    
    private SpiedValueChangedContentAdapter contentAdapter;
    
    @Before
    public void before() throws Exception {

        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);
        resSet = resource.getResourceSet();
        EcoreUtil.resolveAll(resSet);
        
        projectRepository = (ProjectRepository) resource.getContents().get(0);
        
        contentAdapter = new SpiedValueChangedContentAdapter();
        resSet.eAdapters().add(contentAdapter);
    }
    
    /**
     * Change value of value of a parameter triggers recalculation of balancing which have it as source.
     */
    @Test
    public void testValueValue() {
        Balancing bal = projectRepository.getProjects().get(0).getBalancings().get(0); // Target = Source
        Parameter param = projectRepository.getProjects().get(0).getParameters().get(0); // Source
        
        param.getValue().setValue("2");
        
        List<CalculateArguments> calculateCalls = getCalculateCalls();
        assertEquals(1, calculateCalls.size());
        assertEquals(bal ,calculateCalls.get(0).getBalancing());
        
        assertTrue(calculateCalls.get(0).getTargetStatePredicate().test(State.EMPTY_STATE));
        assertFalse(calculateCalls.get(0).getTargetStatePredicate().test(new State(projectRepository.getProjects().get(0).getStatemachines().get(0).getModes().get(0))));
    }
    
    /**
     * Change parameter name leads to balancing functions being changed
     */
    @Test
    public void testAParameterDefNameSource() {
        Balancing bal = projectRepository.getProjects().get(0).getBalancings().get(0); // Target = Source
        Parameter param = projectRepository.getProjects().get(0).getParameters().get(0); // Source
        
        param.setName("NewName");
        
        List<Balancing> renameCalls = contentAdapter.getParameterRenamer().getDoRenameCalls();
        assertEquals(1, renameCalls.size());
        assertEquals(bal, renameCalls.get(0));
        
        List<CalculateArguments> calculateCalls = getCalculateCalls();
        assertEquals(0, calculateCalls.size());
    }
    
    /**
     * Change parameter name leads to balancing functions being changed
     */
    @Test
    public void testAParameterDefNameTarget() {
        Parameter param = projectRepository.getProjects().get(0).getParameters().get(1); // Target
        
        param.setName("NewName");
        
        List<Balancing> renameCalls = contentAdapter.getParameterRenamer().getDoRenameCalls();
        assertEquals(0, renameCalls.size());
        
        List<CalculateArguments> calculateCalls = getCalculateCalls();
        assertEquals(0, calculateCalls.size());
    }
    
    /**
     * Change of function of a balancing triggers recalculation.
     */
    @Test
    public void testBalancingFunction() {
        Balancing bal = projectRepository.getProjects().get(0).getBalancings().get(0); // Target = Source
        bal.setFunction("Target = Source + 1");
        
        List<CalculateArguments> calculateCalls = getCalculateCalls();
        assertEquals(1, calculateCalls.size());
        assertEquals(bal ,calculateCalls.get(0).getBalancing());
        assertNull(calculateCalls.get(0).getTargetStatePredicate());
    }
    
    /**
     * Change of function of a balancing doesn't trigger recalculation, if there is a cycle in the file.
     */
    @Test
    public void testBalancingFunctionWithCycle() {
        contentAdapter.getCycleCheckAdapter().setCycle(true);
        
        Balancing bal = projectRepository.getProjects().get(0).getBalancings().get(0); // Target = Source
        bal.setFunction("Target = Source + 1");
        
        List<CalculateArguments> calculateCalls = getCalculateCalls();
        assertEquals(0, calculateCalls.size());
    }
    
    /**
     * Change of mode of a mode value ref triggers recalculation.
     */
    @Test
    public void testModeValueRefMode() {
        Balancing bal = projectRepository.getProjects().get(0).getBalancings().get(0); // Target = Source
        Parameter param = projectRepository.getProjects().get(0).getParameters().get(0); // Source
        Mode mode =  PremiseHelper.getModes(projectRepository.getProjects().get(0)).get(1); // Mode B
        
        DirectPointer<Mode> modePointer = GraphFactory.eINSTANCE.createDirectPointer();
        modePointer.setTarget(mode);
        
        param.getModeValues().get(0).getModePointers().clear();
        param.getModeValues().get(0).getModePointers().add(modePointer);
        
        List<CalculateArguments> calculateCalls = getCalculateCalls();
        assertEquals(2, calculateCalls.size());
        assertEquals(bal, calculateCalls.get(0).getBalancing());
        assertNull(calculateCalls.get(0).getTargetStatePredicate());
        assertEquals(bal ,calculateCalls.get(1).getBalancing());
        assertNull(calculateCalls.get(1).getTargetStatePredicate());
    }
    
/*
 *        TODO: Rewrite this test to use Balancings
 *        currently balancings do not save resources
 * 
    @Test
    public void testFnDefScript() {
        TransitionBalancing transBal =  PremiseHelper.getTransitions(projectRepository.getProjects().get(1)).get(0).getBalancings().get(0);
        

       // ((FnDefScript) transBal.getFunctionDefAnd()).setScriptName("dummy.m");
        
        
        List<CalculateArguments> calculateCalls = getCalculateCalls();
        assertEquals(1, calculateCalls.size());
        assertEquals(transBal ,calculateCalls.get(0).getBalancing());
        assertNull(calculateCalls.get(0).getTargetStatePredicate());
    }    
    
    @Test
    public void testFnDefSpreadsheet() {
        TransitionBalancing transBal =  PremiseHelper.getTransitions(projectRepository.getProjects().get(1)).get(0).getBalancings().get(0);
        
        // TODO: Rerun this Test
        //((FnDefSpreadSheet) transBal.getFunctionDefOr()).setSheetName("Dummy");
        
        List<CalculateArguments> calculateCalls = getCalculateCalls();
        assertEquals(1, calculateCalls.size());
        assertEquals(transBal ,calculateCalls.get(0).getBalancing());
        assertNull(calculateCalls.get(0).getTargetStatePredicate());
    }
    
    @Test
    public void testCalcEngineScript() {
        TransitionBalancing transBal =  PremiseHelper.getTransitions(projectRepository.getProjects().get(1)).get(0).getBalancings().get(0);
        
        //((CalcEngineScript) transBal.getFunctionDefAnd().getCalcEngine()).setWorkDirURI("dummy");
        
        List<CalculateArguments> calculateCalls = getCalculateCalls();
        assertEquals(1, calculateCalls.size());
        assertEquals(transBal, calculateCalls.get(0).getBalancing());
        assertNull(calculateCalls.get(0).getTargetStatePredicate());
    }    
    
    @Test
    public void testCalcEngineSpreadsheet() {
        TransitionBalancing transBal =  PremiseHelper.getTransitions(projectRepository.getProjects().get(1)).get(0).getBalancings().get(0);

        //((FnInputSpreadSheet) transBal.getFunctionDefOr().getInputs().get(0)).setCell("A1");
        //((FnOutputSpreadSheet) transBal.getFunctionDefOr().getOutput()).setCell("X42");
        
        List<CalculateArguments> calculateCalls = getCalculateCalls();
        assertEquals(2, calculateCalls.size());
        assertEquals(transBal ,calculateCalls.get(0).getBalancing());
        assertNull(calculateCalls.get(0).getTargetStatePredicate());
        assertEquals(transBal, calculateCalls.get(1).getBalancing());
        assertNullOrEmpty(calculateCalls.get(1).getFilterStatesBy());
    }
    
    @Test
    public void testFnInputFnOutput() {
        TransitionBalancing transBal =  PremiseHelper.getTransitions(projectRepository.getProjects().get(1)).get(0).getBalancings().get(0);
//        ((CalcEngineSpreadSheet) transBal.getFunctionDefOr().getCalcEngine()).setFileURI("dummy.xls");
        
        List<CalculateArguments> calculateCalls = getCalculateCalls();
        assertEquals(1, calculateCalls.size());
        assertEquals(transBal ,calculateCalls.get(0).getBalancing());
        assertNull(calculateCalls.get(0).getTargetStatePredicate());
    }
    */

    private List<CalculateArguments> getCalculateCalls() {
        return contentAdapter.getBalancingCalculator().getCalculateCalls();
    }   
}
