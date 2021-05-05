/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.states.test

import de.dlr.premise.element.ElementFactory
import de.dlr.premise.element.StateMachine
import de.dlr.premise.element.TransitionActivationBehavior
import de.dlr.premise.states.StateCheckerAdapter
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.util.TestHelper
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

import static extension de.dlr.premise.states.test.StateTestHelper.*

class StateCheckerAdapterTest {
	static public val TEST_FILE = TestHelper.locateFileForJUnit("de.dlr.premise.states", "test/data/StateCheckerAdapterTest/StateCheckerAdapterTest.system");
		
	var SystemComponent root 
	
	var StateMachine sm1
	var StateMachine sm2
	
	var StateCheckerAdapter adapter
	
	@Before
	def void before() {
		val resSet = new ResourceSetImpl()
		val premiseRes = resSet.getResource(URI.createFileURI(TEST_FILE), true)
		val repo = premiseRes.contents.head as ProjectRepository
		root = repo.projects.named("Root")
		sm1 = root.statemachines.named("SM1")
		sm2 = root.statemachines.named("SM2")
		
		adapter = new StateCheckerAdapter(resSet)
	}
	
	@Test
	def void testGetAllValidStates() {	
		val expectedValidStates = createStates(
			#{sm1->"A", sm2->"1"},
			#{sm1->"C", sm2->"1"},
			#{sm1->"C", sm2->"2"}
		)
		
		assertEquals(
			expectedValidStates,
			adapter.getAllValidStates(new NullProgressMonitor)
		)
		
		// valid states must be marked reachable in graph
		for (expectedValidState : expectedValidStates) {
			assertTrue(adapter.graph.isMarkedReachable(expectedValidState))
		}
	}
	
	@Test
	def void testIsValid() {
		val state = createState(sm1->"C", sm2->"1")
		assertTrue(adapter.isValid(state))
		
		// state is marked reachable
		assertTrue(adapter.graph.isMarkedReachable(state))
		// substates are marked reachable
		assertTrue(adapter.graph.isMarkedReachable(createState(sm1->"C")))
		// pre-states are marked reachable
		assertTrue(adapter.graph.isMarkedReachable(createState(sm1->"A", sm2->"1")))
	}
	
	@Test
	def void testIsValidNotStable() {
		val state = createState(sm1->"B")
		assertFalse(adapter.isValid(state))
	}
	
	@Test
	def void testIsValidLocallyUnreachableNoTransition() {
		val state = createState(sm2->"-2")
		assertFalse(adapter.isValid(state))
	}
	
	
	@Test
	def void testIsValidGloballyUnreachable() {
		val state = createState(sm2->"-5")
		assertFalse(adapter.isValid(state))
		
		// state is marked unreachable
		assertTrue(adapter.graph.isMarkedUnreachable(state))
		// pre-states are marked unreachable
		assertTrue(adapter.graph.isMarkedUnreachable(createState(sm1->"U", sm2->"-5")))
		
		// other states are not marked unreachable
		assertFalse(adapter.graph.isMarkedUnreachable(createState(sm1->"U")))
	}
		
	@Test
	def void testNotifyChangedAddMode() {
		testNotifyChangedWithSM1Change(true)[
			sm1.modes += ElementFactory.eINSTANCE.createMode
		]
	}
	
	@Test
	def void testNotifyChangedRemoveStateMachine() {
		testNotifyChangedWithSM1Change(true)[
			root.statemachines.remove(sm1)
		]
	}
	
	@Test
	def void testNotifyChangedChangeTransiton() {
		testNotifyChangedWithSM1Change(true)[
			sm1.transitions.named("ALL > C").behavior = TransitionActivationBehavior.IMMEDIATE
		]
	}

	@Test
	def void testNotifyChangedAddModeGuard() {
		// we want a guard combination to add our mode guard to and the guard combination must be valid
		val gc = ElementFactory.eINSTANCE.createGuardCombination => [
			children += ElementFactory.eINSTANCE.createModeGuard
		]
		sm1.transitions.named("B > C").condition = gc
		
		testNotifyChangedWithSM1Change(true)[
			gc.children += ElementFactory.eINSTANCE.createModeGuard
		]
	}
	
	
	@Test
	def void testNotifyChangedRenameMode() {		
		testNotifyChangedWithSM1Change(false)[
			sm1.modes.named("A").name = "WHAAAAAT"
		]
	}
	
	
	def void testNotifyChangedWithSM1Change(boolean shouldChange, Procedure0 modelChange) {
		val state1 = createState(sm1->"C", sm2->"1")
		
		// check that state is valid
		assertTrue(adapter.isValid(state1))
		// now state should be marked reachable
		assertTrue(adapter.graph.isMarkedReachable(state1))
		
		// edit the model in a way that affects sm1
		modelChange.apply()
		
		// wait for a little bit so job can update the graph
		Thread.sleep(500)
		
		// now state should no longer be marked as reachable
		assertEquals(!shouldChange, adapter.graph.isMarkedReachable(state1))
	}
}