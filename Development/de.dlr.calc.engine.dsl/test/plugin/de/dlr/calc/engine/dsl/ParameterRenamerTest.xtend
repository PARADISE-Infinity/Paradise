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

import de.dlr.calc.engine.dsl.xtext.ui.internal.CalcDslActivator
import de.dlr.premise.util.TestHelper
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import org.junit.Before
import org.junit.Test
import de.dlr.premise.system.ProjectRepository

import static org.junit.Assert.*

import static extension de.dlr.calc.engine.dsl.XbaseEngineTestHelper.*

class ParameterRenamerTest {
	val static TEST_FOLDER = "test/data/ParameterRenamerTest/"
	val static PATH_INPUT_FILE = TestHelper.locateFile("de.dlr.calc.engine.dsl", TEST_FOLDER + "System.premise").getPath().substring(1);
	
	var ProjectRepository rep
	
	var ParameterRenamer parameterRenamer
	
	@Before
    def void setUp() throws Exception {
        val resource = TestHelper.loadResource(PATH_INPUT_FILE)
        rep = resource.getContents().get(0) as ProjectRepository
        
        val activator = CalcDslActivator::getInstance()
		val injector = activator.getInjector(CalcDslActivator::DE_DLR_CALC_ENGINE_DSL_XTEXT_CALCDSL)
        parameterRenamer = injector.getInstance(ParameterRenamer)
    }
    
    @Test
    def simpleRename() {
    	val bal = rep.getBalancingByName("Balancing")
    	val param2 = rep.getAParameterDefByName("Param2")
    	    	
    	param2.name = "NewName"
    	parameterRenamer.doRename(bal)
    	
    	assertEquals("Param1 = NewName + Param3", bal.function)
    }
    
    @Test
    def complexNameRename() {
    	val bal = rep.getBalancingByName("Balancing")
    	val param5 = rep.getAParameterDefByName("Param5")
    	
    	param5.name = "Param2"
    	parameterRenamer.doRename(bal)
    	
    	assertEquals("Param1 = Root1.Param2 + Param3", bal.function)
    }   
    
    @Test
    def targetRename() {
    	val bal = rep.getBalancingByName("Balancing")
    	val param1 = rep.getAParameterDefByName("Param1")
    	
    	param1.name = "Target"
    	parameterRenamer.doRename(bal)
    	
    	assertEquals("Target = Param2 + Param3", bal.function)
    }
    
    @Test
    def simpleRenameFunctionOnly() {
    	val bal = rep.getBalancingByName("Balancing")
    	val param2 = rep.getAParameterDefByName("Param2")
    	    	
    	param2.name = "MyLittleParameter"
    	
    	assertEquals("Param1 = MyLittleParameter + Param3", parameterRenamer.createRenamedFunction(bal))
    }
    
    @Test
    def failedRename() {
    	val oldStderr = System.err
    	try {
			val errContent =  new ByteArrayOutputStream()
	        System.setErr(new PrintStream(errContent))
	    	
	    	val bal = rep.getBalancingByName("InvalidBalancing")
	    	val param2 = rep.getAParameterDefByName("Param1")
	    	    	
	    	param2.name = "MyLittleParameter"
	    	parameterRenamer.doRename(bal)
	    	
	    	assertEquals("Param1 = <invalid>", bal.function)
   			assertEquals('''
				Function change ommited: Function 'Param1 = <invalid>' for Balancing 'InvalidBalancing' is not valid!
				ERROR: no viable alternative at input '<'
   			'''.toString(), errContent.toString());
    	} finally {
			System.setErr(oldStderr)
    	}
    }
}