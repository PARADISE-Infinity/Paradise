/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.states.test.reachability

import de.dlr.premise.element.StateMachine
import de.dlr.premise.states.reachability.BaseStateReachabilityCheckerDelegate
import de.dlr.premise.states.reachability.StateReachabilityChecker
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

class StateReachabilityCheckerTest {
	static public val TEST_FILE = TestHelper.locateFileForJUnit("de.dlr.premise.states", "test/data/StateReachabilityCheckerTest/StateReachabilityCheckerTest.system");
		
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
	
	var StateReachabilityChecker reachabilityChecker
	
	@Before
	def void before() {
		reachabilityChecker = new StateReachabilityChecker(new BaseStateReachabilityCheckerDelegate)
	}
	
	@Test
	def testIsReachableSimple() {
		assertTrue(reachabilityChecker.isReachable(createState(sm1->"C")))
	}
	
	@Test
	def testIsReachableSimpleInitial() {
		assertTrue(reachabilityChecker.isReachable(createState(sm1->"A")))
	}
	
	@Test
	def testIsReachableSimpleUnreachable() {
		assertFalse(reachabilityChecker.isReachable(createState(sm1->"-")))
	}
	
	@Test
	def testIsReachableCondition() {
		assertTrue(reachabilityChecker.isReachable(createState(sm2->"2", sm3->"Y")))
	}
}
