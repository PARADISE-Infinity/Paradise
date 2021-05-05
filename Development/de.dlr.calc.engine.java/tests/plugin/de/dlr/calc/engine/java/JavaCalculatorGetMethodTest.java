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

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class JavaCalculatorGetMethodTest {

    private JavaCalculator calculator;

    @Before
    public void before() {
        calculator = (JavaCalculator) new JavaCalculator();
    }

    @Test
    public void testValidMethod() {
        Method method = calculator.getMethod("de.dlr.calc.engine.java.implementations.SumOverAllModes", "calculate");
        assertEquals("calculate", method.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidClass() {
        calculator.getMethod("de.dlr.calc.engine.java.notimplementations.JavaCalculationEngineInvalidFunctionsMock", "calculate");
    }

    @Test(expected = ClassNotFoundException.class)
    public void testNotExistingClass() {
        calculator.getMethod("de.dlr.calc.engine.java.notexisting.JavaCalculationEngineNotExisting", "calculate");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidReturn() {
        calculator.getMethod("de.dlr.calc.engine.java.implementations.JavaCalculationEngineFunctionsMock", "calculateNoDoubleReturn");
    }
}
