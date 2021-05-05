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

import com.google.inject.Injector
import de.dlr.calc.engine.dsl.scope.impl.UsedBalancingScope
import de.dlr.calc.engine.dsl.xtext.calcDsl.Model
import de.dlr.calc.engine.dsl.xtext.ui.internal.CalcDslActivator
import de.dlr.premise.graph.APointer
import de.dlr.premise.util.TestHelper
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.validation.CheckMode
import org.eclipse.xtext.validation.IResourceValidator
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.registry.AParameterDef

import static org.junit.Assert.*

import static extension de.dlr.calc.engine.dsl.XbaseEngineTestHelper.*

class BalancingScopeTest {
	
	val static TEST_FOLDER = "test/data/BalancingScopeTest/"
	val static PATH_INPUT_FILE = TestHelper.locateFile("de.dlr.calc.engine.dsl", TEST_FOLDER + "My.premise").getPath().substring(1);
		
	var static Injector injector
	
	var ResourceSet resSet
	var ProjectRepository rep
	
	var BalancingScopeFactory balancingScopeMapCreator

	
	@BeforeClass
    def static void setUpBeforeClass() throws Exception {   	
        val activator = CalcDslActivator::getInstance()
		injector = activator.getInjector(CalcDslActivator::DE_DLR_CALC_ENGINE_DSL_XTEXT_CALCDSL)
    }
	
	@Before
    def void setUp() throws Exception {
		val resource = TestHelper.loadResource(PATH_INPUT_FILE)
		EcoreUtil.resolveAll(resource)
		resSet = resource.resourceSet
		rep =  resSet.resources.get(0).contents.head as ProjectRepository
        
        balancingScopeMapCreator = new BalancingScopeFactory
 
    }
    
    @Test
    def testBalancingScope() {
    	val bal = resSet.getBalancingByName("Balancing")
    	val scope = balancingScopeMapCreator.createScope(bal)
    	
    	// test parameter names
    	assertEquals(newHashSet("T1", "T2", "asf", "asd", "C3.DuplicateName", "const", "siebzehn", "ref1.P1", "ref1.p3", "ref1.ppp", "ref2.P1", "ref2.p3", "ref2.ppp", "ref4.P1", "ref4.p3", "ref4.ppp"), scope.sources.keySet)
    	assertEquals(newHashSet("T1", "T2", "ref1.P1",  "ref1.p3", "ref1.ppp", "ref2.P1", "ref2.p3", "ref2.ppp"), scope.targets.keySet)
    	assertEquals(newHashSet("fn2"), scope.functions.keySet)
    	
    	// test name to value mapping for a few examples
    	assertEquals(resSet.getAParameterDefByName("T1"), scope.getSourceParameter("T1"))
    	assertEquals(resSet.getAParameterDefByName("T2"), scope.getSourceParameter("T2"))
    	assertEquals(resSet.getAParameterDefByName("T1"), scope.getTargetParameter("T1"))
    	assertEquals(resSet.getAParameterDefByName("T2"), scope.getTargetParameter("T2"))
    	
    	// test that parameter not in scope resolves to null
    	assertNull(scope.getSourceParameter("UR1"))
    	assertNull(scope.getTargetParameter("UR1"))
    }
    
        
    @Test
    def testUsedBalancingScope() {
    	val bal = resSet.getBalancingByName("Balancing")
    	val scope = balancingScopeMapCreator.createUsedScope(bal)
    	
    	// test parameter names
    	assertEquals(newHashSet("C3.DuplicateName", "const", "ref1.P1"), scope.sources.keySet)
    	assertEquals(newHashSet("T1"), scope.targets.keySet)
    	assertEquals(newHashSet("fn2"), scope.functions.keySet)
    	assertEquals("T1", scope.targetName)
    	
    	// test name to value mapping for a few examples
    	assertEquals(resSet.getAParameterDefByName("p"), scope.getSourceParameter("ref1.P1"))
    	assertEquals(resSet.getAParameterDefByName("const"), scope.getSourceParameter("const"))
    	assertEquals(resSet.getAParameterDefByName("T1"), scope.targetParameter)
    	
    	// test that parameter not in scope resolves to null
    	assertNull(scope.getSourceParameter("T1"))
    }
    
    @Test(expected=IllegalArgumentException)
    def void testInvalidUsedBalancingScope() {
    	val targets = newHashMap(
    		"T1" -> resSet.getAParameterDefByName("T1").createDirectPointer as APointer<? extends AParameterDef>,
    		"T2" -> resSet.getAParameterDefByName("T2").createDirectPointer
    	)
    	
    	new UsedBalancingScope(newHashMap, targets, newHashMap)
    }
    
    @Test
    def testFilteredBalancingScope() {
    	val resourceProvider = injector.getInstance(ResourceProviderMy)
    	var validator = injector.getInstance(IResourceValidator)
    	
    	val bal = resSet.getBalancingByName("Balancing")
    	val scope = balancingScopeMapCreator.createScope(bal)
    	
    	val resource = resourceProvider.createResource(DocumentCreator.createDocument(scope, "T2 = siebzehn^ref2.p3"))
    	assertEquals(0, validator.validate(resource, CheckMode.ALL, null).size)
    	
    	val model = resource.contents.get(0) as Model
    	
    	val filteredScope = balancingScopeMapCreator.createFilteredScope(scope, model)
    	
    	assertEquals(newHashSet("siebzehn", "ref2.p3"), filteredScope.sources.keySet)
    	assertEquals(newHashSet("T2"), filteredScope.targets.keySet)
    	assertEquals(newHashSet("fn2"), filteredScope.functions.keySet)
    }
}