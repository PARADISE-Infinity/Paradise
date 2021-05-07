/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.calc.engine.excel;

import static org.junit.Assert.assertEquals;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.calc.my.CalculatorParameter;
import de.dlr.premise.functionpool.CalcEngineSpreadSheet;
import de.dlr.premise.functionpool.FnDefSpreadSheet;
import de.dlr.premise.functionpool.FnInputSpreadSheet;
import de.dlr.premise.functionpool.FnOutputSpreadSheet;
import de.dlr.premise.functionpool.FunctionpoolFactory;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.registry.Value;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.SystemFactory;

public class SpreadsheetCalculatorTest {

    private static String EXCEL_FILE = de.dlr.premise.util.TestHelper.locateFile("de.dlr.calc.engine.excel", "tests/data/worksheet.xls").getPath().substring(1);

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void startUp() throws Exception {
	}

	@Test
	public void testCalculation(){
		Parameter input1 = SystemFactory.eINSTANCE.createParameter();
		Parameter input2 = SystemFactory.eINSTANCE.createParameter();
		Parameter input3 = SystemFactory.eINSTANCE.createParameter();
		Parameter input4 = SystemFactory.eINSTANCE.createParameter();
		Parameter input5 = SystemFactory.eINSTANCE.createParameter();

		FnDefSpreadSheet funDef = FunctionpoolFactory.eINSTANCE.createFnDefSpreadSheet();

		FnOutputSpreadSheet outputCell = FunctionpoolFactory.eINSTANCE.createFnOutputSpreadSheet();
		FnInputSpreadSheet inputCell = FunctionpoolFactory.eINSTANCE.createFnInputSpreadSheet();
		FnInputSpreadSheet inputCell2 = FunctionpoolFactory.eINSTANCE.createFnInputSpreadSheet();

		outputCell.setCell("A2");
		inputCell.setCell("A1");
		inputCell2.setCell("B1");

		funDef.getInputs().add(inputCell);
		funDef.getInputs().add(inputCell2);
		funDef.setOutput(outputCell);

		funDef.setLastInputMultiple(true);

		Value val1 = RegistryFactory.eINSTANCE.createValue();
		Value val2 = RegistryFactory.eINSTANCE.createValue();
		Value val3 = RegistryFactory.eINSTANCE.createValue();
		Value val4 = RegistryFactory.eINSTANCE.createValue();
		Value val5 = RegistryFactory.eINSTANCE.createValue();

		val1.setValue("5");
		val2.setValue("7");
		val3.setValue("6");
		val4.setValue("2");
		val5.setValue("4");

		input1.setValue(val1);
		input2.setValue(val2);
		input3.setValue(val3);
		input4.setValue(val4);
		input5.setValue(val5);
		
		// create paramters
		EList<CalculatorParameter> parameters = new BasicEList<>();
        parameters.add(createCalculatorParameter(input1));
		parameters.add(createCalculatorParameter(input2));
        parameters.add(createCalculatorParameter(input3));
        parameters.add(createCalculatorParameter(input4));
        parameters.add(createCalculatorParameter(input5));

		// create ExcelEngine with Excel file
		CalcEngineSpreadSheet engine = FunctionpoolFactory.eINSTANCE.createCalcEngineSpreadSheet();
		engine.setFileURI(EXCEL_FILE);
		funDef.setCalcEngine(engine);

		SpreadsheetCalculator calculator = new SpreadsheetCalculator();
		double result = calculator.calculate(funDef, parameters, null);

		assertEquals(24d, result, 10e-5);
	}

	@Test
	public void testColRowOrder() {
		// set parameters
		Parameter param1 = SystemFactory.eINSTANCE.createParameter();
		Parameter param2 = SystemFactory.eINSTANCE.createParameter();

		// set values
        Value val;
        val = RegistryFactory.eINSTANCE.createValue();
        val.setValue("1111");
        param1.setValue(val);
        val = RegistryFactory.eINSTANCE.createValue();
        val.setValue("2222");
        param2.setValue(val);

		// set fnDef
		FnDefSpreadSheet fnDef = FunctionpoolFactory.eINSTANCE.createFnDefSpreadSheet();
		fnDef.setLastInputMultiple(true);

		// set fnDef cells
		FnOutputSpreadSheet out = FunctionpoolFactory.eINSTANCE.createFnOutputSpreadSheet();
		out.setCell("AB12"); // to use columns with two letters
		fnDef.setOutput(out);
		FnInputSpreadSheet in = FunctionpoolFactory.eINSTANCE.createFnInputSpreadSheet();
		in.setCell("E1"); // row: 0, col 4
		fnDef.getInputs().add(in);
		
	      // create paramters
        EList<CalculatorParameter> parameters = new BasicEList<>();
        parameters.add(createCalculatorParameter(param1));
        parameters.add(createCalculatorParameter(param2));


		// set calc engine
		CalcEngineSpreadSheet engine = FunctionpoolFactory.eINSTANCE.createCalcEngineSpreadSheet();
		engine.setFileURI(EXCEL_FILE);
		fnDef.setCalcEngine(engine);
		
        SpreadsheetCalculator calculator = new SpreadsheetCalculator();
        double result = calculator.calculate(fnDef, parameters, null);

		assertEquals(3333d, result, 10e-5d);
	}

    @Test(expected=IllegalArgumentException.class)
	public void testNotExistingSpreadsheet() {
        CalcEngineSpreadSheet engine = FunctionpoolFactory.eINSTANCE.createCalcEngineSpreadSheet();
        engine.setFileURI("idontexist.xls");
        
        FnDefSpreadSheet fnDef = FunctionpoolFactory.eINSTANCE.createFnDefSpreadSheet();
        fnDef.setCalcEngine(engine);
        
        
        SpreadsheetCalculator calculator = new SpreadsheetCalculator();
        calculator.calculate(fnDef, new BasicEList<CalculatorParameter>(), null);
	}
	
	private CalculatorParameter createCalculatorParameter(Parameter parameter) {
	    return new CalculatorParameter(parameter);
	}
}
