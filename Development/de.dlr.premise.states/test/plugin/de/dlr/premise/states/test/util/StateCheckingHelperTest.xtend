/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.states.test.util

import de.dlr.premise.element.StateMachine
import de.dlr.premise.states.data.State
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.util.TestHelper
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.*

import static extension de.dlr.premise.states.test.StateTestHelper.*
import de.dlr.premise.states.util.StateCheckingHelper

class StateCheckingHelperTest {
	static public val TEST_FILE = TestHelper.locateFileForJUnit("de.dlr.premise.states", "test/data/StateCheckingHelperTest/StateCheckingHelperTest.system");
		
	var static SystemComponent root 
	
	var static StateMachine sm1
	var static StateMachine sm2
	var static StateMachine sm3
	
	
	@BeforeClass
	def static void beforeClass() {
		val resSet = new ResourceSetImpl()
		val premiseRes = resSet.getResource(URI.createFileURI(TEST_FILE), true)
		val repo = premiseRes.contents.head as ProjectRepository
		root = repo.projects.named("Root")
		sm1 = root.statemachines.named("SM1")
		sm2 = root.statemachines.named("SM2")
		sm3 = root.statemachines.named("SM3")
	}
	
	@Test
	def void testGetSimplifiedStateSet() {	
		val inputStateSet = createStates(
				#{sm1->"A", sm2->"1"},
				#{sm1->"A", sm2->"2"},
				#{sm1->"B", sm2->"1"},
				#{sm1->"C", sm2->"2"}
			)
			
		assertEquals(
			createStates(
				#{sm1->"A"          },
				#{sm1->"B", sm2->"1"},
				#{sm1->"C", sm2->"2"}
			),
			StateCheckingHelper.getSimplifiedStateSet(inputStateSet)
		)
	}	
	
	@Test
	def void testGetSimplifiedStateSetEmptyResult() {	
		val inputStateSet = createStates(
				#{sm1->"A", sm2->"1"},
				#{sm1->"A", sm2->"2"},
				#{sm1->"B", sm2->"1"},
				#{sm1->"B", sm2->"2"},
				#{sm1->"C", sm2->"1"},
				#{sm1->"C", sm2->"2"}
			)
			
		assertEquals(
			#{State.EMPTY_STATE},
			StateCheckingHelper.getSimplifiedStateSet(inputStateSet)
		)
	}
	
	@Test
	def void testGetRedundanciesFilteredStateSet() {	
		val inputStateSet = createStates(
				#{sm1->"A", sm2->"1"},
				#{sm1->"A"          },
				#{          sm2->"1"},
				#{sm1->"C", sm2->"2"}
			)
			
		assertEquals(
			createStates(
				#{sm1->"A"          },
				#{          sm2->"1"},
				#{sm1->"C", sm2->"2"}
			),
			StateCheckingHelper.getRedundanciesFilteredStateSet(inputStateSet)
		)
	}
	
	@Test
	def void testGetPossibleStatesAndOr() {
		val gc = root.statemachines.named("SM_GetPossibleStates").transitions.named("AND_OR").condition
		
		assertEquals(
			createStates(
				#{sm1->"A", sm2->"1"},
				#{sm1->"A", sm2->"2"},
				#{sm1->"B", sm2->"1"},
				#{sm1->"B", sm2->"2"}
			), 
			StateCheckingHelper.getPossibleStates(gc)
		)
	}
		
	@Test
	def void testGetPossibleStatesNot() {
		val gc = root.statemachines.named("SM_GetPossibleStates").transitions.named("NOT").condition
		
		assertEquals(
			createStates(
				#{sm1->"B"},
				#{sm1->"C"}
			), 
			StateCheckingHelper.getPossibleStates(gc)
		)
	}
	
	@Test
	def void testGetPossibleStatesEmptyNot() {
		val gc = root.statemachines.named("SM_GetPossibleStates").transitions.named("EMPTY_NOT").condition
		
		assertEquals(
			#{State.EMPTY_STATE},
			StateCheckingHelper.getPossibleStates(gc)
		)
	}
	
	@Test
	def void testGetPossibleStatesInvalidNot() {
		val gc = root.statemachines.named("SM_GetPossibleStates").transitions.named("INVALID_NOT").condition
		
		assertEquals(
			#{},
			StateCheckingHelper.getPossibleStates(gc)
		)
	}
		
	@Test
	def void testGetPossibleStatesXor() {
		val gc = root.statemachines.named("SM_GetPossibleStates").transitions.named("XOR").condition
		
		assertEquals(
			createStates(
				#{sm1->"A", sm2->"2"},
				#{sm1->"B", sm2->"2"},
				#{sm1->"C", sm2->"1"}
			), 
			StateCheckingHelper.getPossibleStates(gc)
		)
	}
	
		
	@Test
	def void testGetPossibleStatesVote() {
		val gc = root.statemachines.named("SM_GetPossibleStates").transitions.named("VOTE").condition
		
		assertEquals(
			createStates(
				#{sm1->"A", sm2->"1"          },
				#{sm1->"A",           sm3->"X"},
				#{sm1->"B", sm2->"1"          },
				#{sm1->"B",           sm3->"X"},
				#{          sm2->"1", sm3->"X"}
			), 
			StateCheckingHelper.getPossibleStates(gc)
		)
	}
	
	@Test
	def void testGetPossibleStatesComplex() {
		val gc = root.statemachines.named("SM_GetPossibleStates").transitions.named("COMPLEX").condition
		
		assertEquals(
			createStates(
				#{sm1->"A"          },
				#{          sm2->"2"},
				#{sm1->"C"          }
			), 
			StateCheckingHelper.getPossibleStates(gc)
		)
	}
	
	@Test
	def void testGetPossibleStatesNotImplemented() {
		val gc = root.statemachines.named("SM_GetPossibleStates").transitions.named("NOT_IMPLEMENTED").condition
		
		assertEquals(
			#{},
			StateCheckingHelper.getPossibleStates(gc)
		)
	}
	
	@Test
	def void testGetPossibleStatesEmptyModeGuard() {
		val gc = root.statemachines.named("SM_GetPossibleStates").transitions.named("EMPTY_MODE_GUARD").condition
		
		assertEquals(
			#{State.EMPTY_STATE},
			StateCheckingHelper.getPossibleStates(gc)
		)
	}
}
