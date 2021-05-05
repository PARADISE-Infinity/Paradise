/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.states.test.reachability.mode

import de.dlr.premise.element.StateMachine
import de.dlr.premise.states.reachability.mode.BaseModeReachabilityCheckerDelegate
import de.dlr.premise.states.reachability.mode.ModeReachabilityChecker
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.util.TestHelper
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.*

import static extension de.dlr.premise.states.test.StateTestHelper.*

class ModeReachabilityCheckerTest {
	static public val TEST_FILE = TestHelper.locateFileForJUnit("de.dlr.premise.states", "test/data/ModeReachabilityCheckerTest/ModeReachabilityCheckerTest.system");
		
	var static SystemComponent root 
	
	var static StateMachine sm1
	var static StateMachine sm2
		
	@BeforeClass
	def static void beforeClass() {
		val resSet = new ResourceSetImpl()
		val premiseRes = resSet.getResource(URI.createFileURI(TEST_FILE), true)
		val repo = premiseRes.contents.head as ProjectRepository
		root = repo.projects.named("Root")
		sm1 = root.statemachines.named("SM1")
		sm2 = root.statemachines.named("SM2")
	}
	
	var ModeReachabilityChecker modeReachabilityChecker
	
	@Before
	def void before() {
		modeReachabilityChecker = new ModeReachabilityChecker(new BaseModeReachabilityCheckerDelegate)
	}
	
	@Test
	def testAllModesReachable() {
		assertTrue(modeReachabilityChecker.allModesReachable(createState(sm1->"B", sm2->"1")))
	}
	
	@Test
	def testIsReachableInitial() {
		assertTrue(modeReachabilityChecker.isReachable(sm1.getMode("A")))
	}
	
	@Test
	def testIsReachable() {
		// A->B->C (note that c is actually globally unreachable, since B->C needs Mode 2, but it's reachable locally)
		assertTrue(modeReachabilityChecker.isReachable(sm1.getMode("C")))
	}
	
	
	@Test
	def testIsReachableValidLocalCondition() {
		assertTrue(modeReachabilityChecker.isReachable(sm1.getMode("D")))
	}
	
	@Test
	def testIsReachableNoTransition() {
		assertFalse(modeReachabilityChecker.isReachable(sm1.getMode("U")))
	}
	
	@Test
	def testIsReachableInvalidLocalCondition() {
		assertFalse(modeReachabilityChecker.isReachable(sm1.getMode("V")))
	}
}
