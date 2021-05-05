/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.states.test.stability

import de.dlr.premise.element.StateMachine
import de.dlr.premise.states.stability.StableSubstatesGenerator
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.util.TestHelper
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.*

import static extension de.dlr.premise.states.test.StateTestHelper.*

class StableSubstatesGeneratorTest {
	static public val TEST_FILE = TestHelper.locateFileForJUnit("de.dlr.premise.states", "test/data/StableSubstatesGeneratorTest/StableSubstatesGeneratorTest.system");
		
	var static SystemComponent root
	
	var static StateMachine sm1
	var static StateMachine sm2
	
	var static StableSubstatesGenerator stableSubstatesGenerator
		
	@BeforeClass
	def static void beforeClass() {
		val resSet = new ResourceSetImpl()
		val premiseRes = resSet.getResource(URI.createFileURI(TEST_FILE), true)
		val repo = premiseRes.contents.head as ProjectRepository
		root = repo.projects.named("Root")
		sm1 = root.statemachines.named("SM1")
		sm2 = root.statemachines.named("SM2")
		
		stableSubstatesGenerator = new StableSubstatesGenerator()
	}
	
	@Test
	def void testGetStableSubstatesNoConditionBehaviorDefault() {	
		// A->B has behavior=DEFAULT and no condition, so A is stable
		assertEquals(
			createStates(#{sm1->"A"}),
			stableSubstatesGenerator.getStableSubstates(createState(sm1->"A"))
		)
	}
	
	@Test
	def void testGetStableSubstatesNoConditionBehaviorExternal() {	
		// B->C has behavior=DEFAULT, so B is stable
		assertEquals(
			createStates(#{sm1->"B"}),
			stableSubstatesGenerator.getStableSubstates(createState(sm1->"B"))
		)
	}
	
	@Test
	def void testGetStableSubstatesNoConditionBehaviorImmediate() {	
		// C->D has behavior=IMMEDIATE, so C is NOT stable
		assertEquals(
			#{},
			stableSubstatesGenerator.getStableSubstates(createState(sm1->"C"))
		)
	}
	
	
	@Test
	def void testGetStableSubstatesNoConditionBehaviorImmediateLoop() {	
		// D->D is a loop, so D is stable
		assertEquals(
			createStates(#{sm1->"D"}),
			stableSubstatesGenerator.getStableSubstates(createState(sm1->"D"))
		)
	}
	
	
	@Test
	def void testGetStableSubstatesCondition() {	
		assertEquals(
			createStates(
				#{sm1->"A", sm2->"1"},
				#{sm1->"B", sm2->"1"}
			),
			stableSubstatesGenerator.getStableSubstates(createState(sm2->"1"))
		)
	}
	
	
	@Test
	def void testGetStableSubstatesConditionUnstable() {	
		assertEquals(
			#{},
			stableSubstatesGenerator.getStableSubstates(createState(sm2->"2"))
		)
	}
}