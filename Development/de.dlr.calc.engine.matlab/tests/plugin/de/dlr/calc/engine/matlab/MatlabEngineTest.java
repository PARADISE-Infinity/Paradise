/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.calc.engine.matlab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.calc.my.CalculatorParameter;
import de.dlr.premise.functionpool.CalcEngineScript;
import de.dlr.premise.functionpool.FnDefScript;
import de.dlr.premise.functionpool.FnInput;
import de.dlr.premise.functionpool.FnOutput;
import de.dlr.premise.functionpool.FunctionpoolFactory;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.registry.Value;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.SystemFactory;

public class MatlabEngineTest extends MatlabTestHelper {

    private static boolean shallBeTested = false;

    // substring(1) strips leading slash
    static String LIBPATH = de.dlr.premise.util.TestHelper.locateFile("de.dlr.calc.engine.matlab", "tests/data/matlab").getPath().substring(1);

    @BeforeClass
    public static void checkMatlab() {
        MatlabEngineJMBridge engine = new MatlabEngineJMBridge(new MatlabConfig());
        if (engine != null && engine.isAvailable()) {
            shallBeTested = true;
            engine.close();
        }
    }

    @Test
    public void testCalculation() {

        assumeTrue(shallBeTested);

        // create test data
        Parameter input1 = SystemFactory.eINSTANCE.createParameter();
        Parameter input2 = SystemFactory.eINSTANCE.createParameter();

        FnDefScript funDef = FunctionpoolFactory.eINSTANCE.createFnDefScript();

        FnOutput outputP = FunctionpoolFactory.eINSTANCE.createFnOutput();
        FnInput inputP = FunctionpoolFactory.eINSTANCE.createFnInput();
        FnInput inputP2 = FunctionpoolFactory.eINSTANCE.createFnInput();

        funDef.getInputs().add(inputP);
        funDef.getInputs().add(inputP2);
        funDef.setOutput(outputP);
        funDef.setScriptName("force");

        Value val1 = RegistryFactory.eINSTANCE.createValue();
        Value val2 = RegistryFactory.eINSTANCE.createValue();

        val1.setValue("5");
        val2.setValue("7");

        input1.setValue(val1);
        input2.setValue(val2);

        // create paramters
        EList<CalculatorParameter> parameters = new BasicEList<>();
        parameters.add(createCalculatorParameter(input1));
        parameters.add(createCalculatorParameter(input2));

        delay(1000);

        // create MATLAB engine with m scripts
        CalcEngineScript engine = FunctionpoolFactory.eINSTANCE.createCalcEngineScript();
        engine.getLibURIs().add(LIBPATH);

        // don't remove the delay, startup MATLAB needs time
        funDef.setCalcEngine(engine);
        delay(400);

        ScriptCalculator calculator = new ScriptCalculator();
        double result = calculator.calculate(funDef, parameters, null);

        assertEquals(35d, result, 10e-5d);
    }

    private CalculatorParameter createCalculatorParameter(Parameter parameter) {
        return new CalculatorParameter(parameter);
    }
}
