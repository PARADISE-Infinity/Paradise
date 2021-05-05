/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.graphml.block

import de.dlr.exchange.graphml.block.settings.GraphMLBlockSettings
import de.dlr.premise.component.ComponentPackage
import de.dlr.premise.element.ElementPackage
import de.dlr.premise.util.TestHelper
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.EcoreUtil
import org.junit.Before
import org.junit.Test
import de.dlr.premise.system.SystemPackage
import de.dlr.premise.system.ProjectRepository

import static org.junit.Assert.*

class GraphMLBlockSettingsTest {
	
	val TEST_PREMISE = TestHelper.locateFileForJUnit("de.dlr.exchange.graphml.block", "test/data/GraphMLBlockSettingsTest/input.premise");
	
	var ResourceSet resSet
	var Resource premiseRes
	var ProjectRepository repo
	
	var GraphMLBlockSettings settings
	
	var boolean wasNotified
	
	@Before
	def void before() {
		resSet = new ResourceSetImpl()
		premiseRes = resSet.getResource(URI.createFileURI(TEST_PREMISE), true)
		repo = premiseRes.contents.head as ProjectRepository
		
		EcoreUtil.resolveAll(resSet)
		
		settings = new GraphMLBlockSettings
	}
	
	@Test
	def void testObserver() {
		wasNotified = false
		
		settings.addObserver[
			wasNotified = true
		]
		
		settings.initialRoots = newHashSet(repo.projects.head)
		
		assertTrue(wasNotified)
	}
	
	@Test
	def void testNodeSelectionDepthMetaTypes() {
		settings.initialRoots = newHashSet(repo.projects.head)	
		
		assertEquals(448, settings.allPossibleRoots.length)
		assertEquals(448, settings.selectedElements.length)
		assertEquals(456, settings.selectedNodes.length)
		assertEquals(456, settings.allNodes.length)
		
		settings.setRestictedDepth(true, 2)	
		assertEquals(448, settings.allPossibleRoots.length)
		assertEquals(15, settings.selectedElements.length)
		assertEquals(20, settings.selectedNodes.length)
		assertEquals(456, settings.allNodes.length)
		
		settings.setRestictedDepth(false, 0)
		assertEquals(448, settings.allPossibleRoots.length)
		assertEquals(448, settings.selectedElements.length)
		assertEquals(456, settings.selectedNodes.length)
		assertEquals(456, settings.allNodes.length)
		
		settings.removeSelectedAdditionalNodeType(ElementPackage.Literals.MODE)
		assertEquals(448, settings.allPossibleRoots.length)
		assertEquals(448, settings.selectedElements.length)
		assertEquals(452, settings.selectedNodes.length)
		assertEquals(456, settings.allNodes.length)		
		
		settings.addSelectedAdditionalNodeType(ElementPackage.Literals.MODE)
		assertEquals(448, settings.allPossibleRoots.length)
		assertEquals(448, settings.selectedElements.length)
		assertEquals(456, settings.selectedNodes.length)
		assertEquals(456, settings.allNodes.length)
		
		settings.selectedRootMetaTypes = settings.allRootMetaTypes.filter[id == "8a8f8a37-7fa8-487b-b18c-fdf292247269"].toSet
		assertEquals(448, settings.allPossibleRoots.length)
		assertEquals(95, settings.selectedElements.length)
		assertEquals(95, settings.allNodes.length)
		
		settings.setRestictedDepth(true, 2)		
		assertEquals(448, settings.allPossibleRoots.length)
		assertEquals(62, settings.selectedElements.length)
		assertEquals(62, settings.selectedNodes.length)
		assertEquals(95, settings.allNodes.length)
	}
	
	@Test
	def void testEdgeSelection() {
		settings.initialRoots = newHashSet(repo.projects.head.referencedChildren.findFirst[id == "cdc5ef64-ce08-410c-8b72-49ccc04f1ae4"])
		
		assertEquals(92, settings.allPossibleEdges.length)
		assertEquals(92, settings.selectedEdges.length)
	}
	
	@Test
	def void testEdgeSelectionDepth() {
		settings.initialRoots = newHashSet(repo.projects.head.referencedChildren.findFirst[id == "cdc5ef64-ce08-410c-8b72-49ccc04f1ae4"])
		
		settings.setRestictedDepth(true, 3)
		assertEquals(90, settings.allPossibleEdges.length)
		assertEquals(90, settings.selectedEdges.length)
		
		settings.moveEdgesToParents = false
		assertEquals(29, settings.allPossibleEdges.length)
		assertEquals(29, settings.selectedEdges.length)
	}
	
	@Test
	def void testEdgeSelectionExternals() {
		settings.initialRoots = newHashSet(repo.projects.head.referencedChildren.findFirst[id == "cdc5ef64-ce08-410c-8b72-49ccc04f1ae4"])
		
		settings.drawExternalDependencies = false
		assertEquals(70, settings.allPossibleEdges.length)
		assertEquals(70, settings.selectedEdges.length)
	}
	
	@Test
	def void testEdgeSelectionTypes() {
		settings.initialRoots = newHashSet(repo.projects.head.referencedChildren.findFirst[id == "cdc5ef64-ce08-410c-8b72-49ccc04f1ae4"])
		
		settings.removeSelectedEdgeType(ElementPackage.Literals.RELATION)
		assertEquals(50, settings.allPossibleEdges.length)
		assertEquals(50, settings.selectedEdges.length)
		
		settings.removeSelectedEdgeType(ElementPackage.Literals.CONNECTION)
		assertEquals(8, settings.allPossibleEdges.length)
		assertEquals(8, settings.selectedEdges.length)
		
		settings.removeSelectedEdgeType(ComponentPackage.Literals.SATISFIES)
		assertEquals(6, settings.allPossibleEdges.length)
		assertEquals(6, settings.selectedEdges.length)
		
		settings.removeSelectedEdgeType(ElementPackage.Literals.TRANSITION)
		assertEquals(1, settings.allPossibleEdges.length)
		assertEquals(1, settings.selectedEdges.length)
		
		settings.removeSelectedEdgeType(SystemPackage.Literals.ABALANCING)
		assertEquals(0, settings.allPossibleEdges.length)
		assertEquals(0, settings.selectedEdges.length)
		
		settings.addSelectedEdgeType(ElementPackage.Literals.CONNECTION)
		assertEquals(42, settings.allPossibleEdges.length)
		assertEquals(42, settings.selectedEdges.length)
	}
	
	@Test
	def void testEdgeSelectionMetaTypes() {
		settings.initialRoots = newHashSet(repo.projects.head.referencedChildren.findFirst[id == "cdc5ef64-ce08-410c-8b72-49ccc04f1ae4"])
		
		settings.selectedEdgeMetaTypes = settings.allEdgeMetaTypes.filter[id == "8a8f8a37-7fa8-487b-b18c-fdf292247269"].toSet
		assertEquals(92, settings.allPossibleEdges.length)
		assertEquals(5, settings.selectedEdges.length)
	}	
	
	@Test
	def void testEdgeSelectionSelfReferences() {
		settings.initialRoots = newHashSet(repo.projects.head.referencedChildren.findFirst[id == "cdc5ef64-ce08-410c-8b72-49ccc04f1ae4"])
		
		settings.drawSelfReferences = true
		assertEquals(94, settings.allPossibleEdges.length)
		assertEquals(94, settings.selectedEdges.length)
	}
    
}