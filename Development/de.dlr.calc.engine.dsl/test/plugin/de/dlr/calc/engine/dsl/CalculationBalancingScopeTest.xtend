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

import de.dlr.premise.component.ComponentRepository
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.Parameter
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.util.TestHelper
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreUtil
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

import static extension de.dlr.calc.engine.dsl.XbaseEngineTestHelper.*
import static extension de.dlr.premise.util.SubstitutionMappingHelper.*

class CalculationBalancingScopeTest {

	val static TEST_FOLDER = "test/data/CalculationBalancingScopeTest/"
	val static PATH_INPUT_FILE = TestHelper.locateFile("de.dlr.calc.engine.dsl", TEST_FOLDER + "My.premise").getPath().
		substring(1);
	
	var ResourceSet resSet
	var ProjectRepository rep
	var ComponentRepository compRep
	var BalancingScopeFactory balancingScopeFactory


	@Before
	def void setUp() throws Exception {
		val resource = TestHelper.loadResource(PATH_INPUT_FILE)
		EcoreUtil.resolveAll(resource)
		resSet = resource.resourceSet
		rep =  resSet.resources.get(0).contents.head as ProjectRepository
		compRep = resSet.resources.get(1).contents.head as ComponentRepository

		balancingScopeFactory = new BalancingScopeFactory
	}

	@Test
	def testCalculationBalancingScopeComponentReferencePointerTargetExisitingSubsitutionMap() {
		val bal = resSet.getScopedWrapper(Balancing, #[], "SetAny1A")
		val compRefAny1 = resSet.getComponentReferenceByName("Any1")
		val scope = balancingScopeFactory.createCalculationScope(bal)

		// since substitution map is already there, no prepare is needed
		assertFalse(scope.needsPrepare())

		// check scope parameters
		assertEquals(newHashSet(), scope.sourceNames)
		assertEquals("Any1.AnyComponent.A", scope.targetName)
		assertEquals(compRefAny1.substitutionMap.get(0).substitution, scope.targetParameter)
		
		// check functions
		assertEquals(newHashSet(), scope.functionNames)
		assertNull(scope.getFunction("ThereIsNoFunction"))
		
		// sanity check some none existing parameters
		assertNull(scope.getSourceParameter("Blub"))
		assertNull(scope.getSourceParameter("Blubb"))
	}

	@Test
	def testCalculationBalancingScopeComponentReferencePointerTargetNoSubstitutionMap() {
		val bal = resSet.getScopedWrapper(Balancing, #[], "SetAny1A")
		val compRefAny1 = resSet.getComponentReferenceByName("Any1")
		val scope = balancingScopeFactory.createCalculationScope(bal)
		
		// delete existing substitution mapping
		compRefAny1.substitutionMap.clear()

		// no substituiton map, so we need prepare
		assertTrue(scope.needsPrepare())
		
		// do prepare and check that substitution mapping is generated
		scope.prepareModel()
		assertEquals(1, compRefAny1.substitutionMap.size)
		assertEquals(0, compRefAny1.substitutionMap.get(0).relativeScope.size)
		assertEquals(resSet.getAParameterDefByName("A"), compRefAny1.substitutionMap.get(0).original)
	}
	
	@Test
	def testCalculationBalancingScopeDirectPointerTargetExisitingSubsitutionMap() {
		val bal = resSet.getScopedWrapper(Balancing, #[], "ReadAny1C")
		val compRefAny1 = resSet.getComponentReferenceByName("Any1")
		val scope = balancingScopeFactory.createCalculationScope(bal)

		// target is direct pointer, no prepare is needed
		assertFalse(scope.needsPrepare())

		// check scope parameters
		assertEquals(newHashSet("Any1.AnyComponent.C"), scope.sourceNames)
		assertEquals(compRefAny1.substitutionMap.get(1).substitution, scope.getSourceParameter("Any1.AnyComponent.C"))
		assertEquals("C1", scope.targetName)
		assertEquals(resSet.getAParameterDefByName("C1"), scope.targetParameter)
	}
	
	@Test
	def testCalculationBalancingScopeDirectPointerTargetNoSubsitutionMap() {
		val bal = resSet.getScopedWrapper(Balancing, #[], "ReadAny1C")
		val compRefAny1 = resSet.getComponentReferenceByName("Any1")
		val scope = balancingScopeFactory.createCalculationScope(bal)
		
		// delete substitution mapping, to treat input parameter as not being calculated
		compRefAny1.substitutionMap.clear()

		// target is direct pointer, no prepare is needed
		assertFalse(scope.needsPrepare())

		// check scope parameters
		assertEquals(newHashSet("Any1.AnyComponent.C"), scope.sourceNames)
		assertEquals(resSet.getAParameterDefByName("C"), scope.getSourceParameter("Any1.AnyComponent.C"))
		assertEquals("C1", scope.targetName)
		assertEquals(resSet.getAParameterDefByName("C1"), scope.targetParameter)
	}
	
	@Test
	def testCalculationComponentReferenceBalancingScope() {
		val bal = resSet.getScopedWrapper(Balancing, #["Any2", "Trans1"], "AddToAdd")
		val compRefAny2 = resSet.getComponentReferenceByName("Any2")
		val scope = balancingScopeFactory.createCalculationScope(bal)
		
		// since substitution map is already there, no prepare is needed
		assertFalse(scope.needsPrepare())
		
		// check scope parameters
		assertEquals(newHashSet("AddToAddTransformation.in", "ToAdd"), scope.sourceNames)
		assertEquals(resSet.getAParameterDefByName("ToAdd"), scope.getSourceParameter("ToAdd"))
		assertEquals(compRefAny2.substitutionMap.get(1).substitution, scope.getSourceParameter("AddToAddTransformation.in"))
		assertEquals("AddToAddTransformation.out", scope.targetName)
		assertEquals(compRefAny2.substitutionMap.get(2).substitution, scope.targetParameter)
		
		// check functions
		assertEquals(newHashSet(), scope.functionNames)
		assertNull(scope.getFunction("ThereIsNoFunction"))
		
		// sanity check some none existing parameters
		assertNull(scope.getSourceParameter("Blub"))
		assertNull(scope.getSourceParameter("Blubb"))
	}
	
	@Test
	def testCalculationComponentReferenceBalancingScopeNeedsPrepare() {
		val bal = resSet.getScopedWrapper(Balancing, #["Any2", "Trans1"], "AddToAdd")
		val compRefAny2 = resSet.getComponentReferenceByName("Any2")
		val compRefTrans1 = resSet.getComponentReferenceByName("Trans1")
		val compRefTrans2 = resSet.getComponentReferenceByName("Trans2")
		val scope = balancingScopeFactory.createCalculationScope(bal)
		
		// update active implementation for a nested component reference, this leads new substitution map being needed
		compRefTrans2.activeImplementation = compRefTrans1.activeImplementation
		
		// prepare is needed now
		assertTrue(scope.needsPrepare())
		
		// do prepare
		scope.prepareModel()
		
		// check resulting substitution map
		assertEquals(7, compRefAny2.substitutionMap.size)
		assertTrue(compRefAny2.substitutionMap.containsKey(resSet.getScopedWrapper(Parameter, "Any2", "A")))
		assertTrue(compRefAny2.substitutionMap.containsKey(resSet.getScopedWrapper(Parameter, "Any2", "B")))
		assertTrue(compRefAny2.substitutionMap.containsKey(resSet.getScopedWrapper(Parameter, "Any2", "C")))
		assertTrue(compRefAny2.substitutionMap.containsKey(resSet.getScopedWrapper(Parameter, "Any2", "Trans1", "in")))
		assertTrue(compRefAny2.substitutionMap.containsKey(resSet.getScopedWrapper(Parameter, "Any2", "Trans1", "out")))
		assertTrue(compRefAny2.substitutionMap.containsKey(resSet.getScopedWrapper(Parameter, "Any2", "Trans2", "in")))
		assertTrue(compRefAny2.substitutionMap.containsKey(resSet.getScopedWrapper(Parameter, "Any2", "Trans2", "out")))
	}
	
	@Test
	def testCalculationComponentReferenceBalancingScopeComponetReferencePointerTarget() {
		val bal = resSet.getScopedWrapper(Balancing, #["Any2"], "SetTrans1in")
		val compRefAny2 = resSet.getComponentReferenceByName("Any2")
		val scope = balancingScopeFactory.createCalculationScope(bal)
		
		// since substitution map is already there, no prepare is needed
		
		// check scope parameters
		assertEquals(newHashSet("A"), scope.sourceNames)
		assertEquals(compRefAny2.substitutionMap.get(0).substitution, scope.getSourceParameter("A"))
		assertEquals("Trans1.Transformation.in", scope.targetName)
		assertEquals(compRefAny2.substitutionMap.get(1).substitution, scope.targetParameter)
	}
}
