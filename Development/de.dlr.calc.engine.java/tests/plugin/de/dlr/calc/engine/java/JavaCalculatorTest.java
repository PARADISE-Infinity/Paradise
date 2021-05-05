/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.java;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.junit.Test;

import de.dlr.calc.my.CalculatorParameter;
import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.element.Mode;
import de.dlr.premise.element.StateMachine;
import de.dlr.premise.functionpool.CalcEngineJava;
import de.dlr.premise.functionpool.FnDefJava;
import de.dlr.premise.functionpool.FnInput;
import de.dlr.premise.functionpool.FnOutput;
import de.dlr.premise.functionpool.FunctionpoolFactory;
import de.dlr.premise.graph.DirectPointer;
import de.dlr.premise.graph.GraphFactory;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.registry.Value;
import de.dlr.premise.system.ModeValueRef;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.SystemFactory;

public class JavaCalculatorTest {

    @Test
    public void testCalculation() {        

        FnDefJava funDef = FunctionpoolFactory.eINSTANCE.createFnDefJava();
        funDef.setMethodName("calculate");
        funDef.setLastInputMultiple(true);

        FnOutput output = FunctionpoolFactory.eINSTANCE.createFnOutput();
        funDef.setOutput(output);
        
        FnInput input = FunctionpoolFactory.eINSTANCE.createFnInput();
        funDef.getInputs().add(input);

        CalcEngineJava calcEngine = FunctionpoolFactory.eINSTANCE.createCalcEngineJava();
        calcEngine.setClassName("de.dlr.calc.engine.java.implementations.SumOverAllModes");
        funDef.setCalcEngine(calcEngine);
        
        SystemComponent root = SystemFactory.eINSTANCE.createSystemComponent();
        StateMachine sm = ElementFactory.eINSTANCE.createStateMachine();
        root.getStatemachines().add(sm);
        
        Mode mode1 = ElementFactory.eINSTANCE.createMode();
        sm.getModes().add(mode1);       
        
        Mode mode2 = ElementFactory.eINSTANCE.createMode();
        sm.getModes().add(mode2);

        Parameter input1 = SystemFactory.eINSTANCE.createParameter();
        input1.setValue(createValue("5"));
        input1.getModeValues().add(createModeValueRef("2", mode1));
        root.getParameters().add(input1);
        
        Parameter input2 = SystemFactory.eINSTANCE.createParameter();
        input2.setValue(createValue("7"));  
        input2.getModeValues().add(createModeValueRef("5", mode2));
        root.getParameters().add(input2);        

        // create paramters
        EList<CalculatorParameter> parameters = new BasicEList<>();
        parameters.add(createCalculatorParameter(input1));
        parameters.add(createCalculatorParameter(6));
        parameters.add(createCalculatorParameter(input2));

        JavaCalculator calculator = new JavaCalculator();
        double result = calculator.calculate(funDef, parameters, null);

        assertEquals(25d, result, 10e-5);
    }
    
    private CalculatorParameter createCalculatorParameter(Parameter parameter) {
        return new CalculatorParameter(parameter);
    }
    
    private CalculatorParameter createCalculatorParameter(double number) {
        return new CalculatorParameter(number);
    }
    
    private Value createValue(String strValue) {
        Value value = RegistryFactory.eINSTANCE.createValue();
        value.setValue(strValue);
        return value;
    }
    
    private ModeValueRef createModeValueRef(String strValue, Mode... modes) {
        ModeValueRef mvr = SystemFactory.eINSTANCE.createModeValueRef();
        mvr.setValue(strValue);
        mvr.getModePointers().addAll(createDirectPointers(modes));
        return mvr;
    }
    

    public static <T> List<DirectPointer<T>> createDirectPointers(T[] targets) {
        return Arrays.stream(targets).map(JavaCalculatorTest::createDirectPointer).collect(Collectors.toList());
    }
    
    public static <T> DirectPointer<T> createDirectPointer(T target) {
        DirectPointer<T> pointer = GraphFactory.eINSTANCE.createDirectPointer();
        pointer.setTarget(target);
        return pointer;
    }
}
