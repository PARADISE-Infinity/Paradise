/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.dsl

import de.dlr.premise.element.Mode
import de.dlr.premise.states.data.State
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.util.TestHelper
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import org.eclipse.emf.ecore.util.EcoreUtil
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.*

import static extension de.dlr.calc.engine.dsl.XbaseEngineTestHelper.*

class BalancingInterpreterTest {
	
	val static TEST_FOLDER = "test/data/BalancingInterpreterTest/"
	val static PATH_INPUT_FILE = TestHelper.locateFile("de.dlr.calc.engine.dsl", TEST_FOLDER + "System.premise").getPath().substring(1);
	
	var static ProjectRepository rep
	var static BalancingScopeFactory balancingScopeFactory
	var static BalancingInterpreter balancingInterpreter
	
	@BeforeClass
    def static void setUp() throws Exception {   	
        val resource = TestHelper.loadResource(PATH_INPUT_FILE)
        EcoreUtil.resolveAll(resource)
        rep = resource.getContents().get(0) as ProjectRepository
        
        balancingScopeFactory = new BalancingScopeFactory
        balancingInterpreter = BalancingInterpreter.newInstance
    }
	
	@Test
	def simple() {
		val bal = rep.getBalancingByName("SimpleCalc")
		assertEquals("-15.0", balancingInterpreter.calculate(bal, balancingScopeFactory.createUsedScope(bal), null))
	}
	
	@Test
	def invalidFunction() {
		val bal = rep.getBalancingByName("InvalidFunctionCalc")
		assertNull(balancingInterpreter.calculate(bal, balancingScopeFactory.createUsedScope(bal), null))
	}
	
	@Test
	def invalidInput() {
		val bal = rep.getBalancingByName("InvalidInputCalc")
		assertEquals("NaN", balancingInterpreter.calculate(bal, balancingScopeFactory.createUsedScope(bal), null))
	}	
	
	@Test
	def invalidFnDef() {
		val bal = rep.getBalancingByName("InvalidFnDefCalc")
		
		val oldStderr = System.err
    	try {
			val errContent =  new ByteArrayOutputStream()
	        System.setErr(new PrintStream(errContent))
	        
			assertEquals(null, balancingInterpreter.calculate(bal, balancingScopeFactory.createUsedScope(bal), null))
			assertTrue(errContent.toString.startsWith('''
				Calculation failed: Function 'InvalidFnDefCalcContainer.Target = InvalidFnDef(InvalidFnDefCalcContainer.S1, 42)' for Balancing 'InvalidFnDefCalc' returned invalid result!
				java.lang.IllegalArgumentException: A spreadsheet function must be backed by a spreadsheet calc engine
			'''))
		} finally {
			System.setErr(oldStderr)
    	}
	}	
	
	@Test
	def invalidFnArity() {
		val bal = rep.getBalancingByName("InvalidFnArityCalc")
		
		val oldStderr = System.err
    	try {
			val errContent =  new ByteArrayOutputStream()
	        System.setErr(new PrintStream(errContent))
	        
			assertEquals(null, balancingInterpreter.calculate(bal, balancingScopeFactory.createUsedScope(bal), null))
			assertEquals('''
				Calculation omitted: Function 'InvalidFnArityCalcContainer.Target = Sin(InvalidFnArityCalcContainer.S1, 10) + InvalidFnDef(5)' for Balancing 'InvalidFnArityCalc' is not valid!
				ERROR: Function Sin must have exactly 1 argument
				ERROR: Function InvalidFnDef must have 2 or more arguments
			'''.toString(), errContent.toString())
		} finally {
			System.setErr(oldStderr)
    	}
	}
	
	@Test
	def mode() {
		val bal = rep.getBalancingByName("ModeCalc")
		val modeA = rep.getModeByName("A")
		val modeB = rep.getModeByName("B")
		val scope = balancingScopeFactory.createUsedScope(bal)
		assertEquals("15.0", balancingInterpreter.calculate(bal, scope, null).toString)
		assertEquals("-3.0", balancingInterpreter.calculate(bal, scope, createState(modeA)).toString)
		assertEquals("75.0", balancingInterpreter.calculate(bal, scope, createState(modeB)).toString)
	}
	
		
	@Test
	def excel() {
		val bal = rep.getBalancingByName("ExcelCalc")
		assertEquals("900.0", balancingInterpreter.calculate(bal, balancingScopeFactory.createUsedScope(bal), null).toString)
	}
	
	@Test
	def java() {
		val bal = rep.getBalancingByName("JavaCalc")
		// Values for each system mode: A: 3, B: 3, C: 100, D: 50
		// SumOverAllModes(JavaCalcContainer.S1, 10)
		assertEquals("166.0", balancingInterpreter.calculate(bal, balancingScopeFactory.createUsedScope(bal), null).toString)
	}

	
	def createState(Mode... stateModes) {
		new State(stateModes)
	}
}