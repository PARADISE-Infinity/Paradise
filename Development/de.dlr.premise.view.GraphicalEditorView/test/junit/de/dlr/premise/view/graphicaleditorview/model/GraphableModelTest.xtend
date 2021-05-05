/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.model

import de.dlr.premise.view.graphicaleditorview.viewmodel.GraphableEdge
import de.dlr.premise.view.graphicaleditorview.viewmodel.GraphableModel
import de.dlr.premise.view.graphicaleditorview.viewmodel.GraphableNode
import java.util.ArrayList
import java.util.Observable
import java.util.Observer
import org.junit.Test

import static org.hamcrest.core.Is.*
import static org.hamcrest.core.IsSame.*
import static org.junit.Assert.*

/**
 * 
 */
class GraphableModelTest implements Observer {

	var Object notifiedBy = null;

	@Test
	def void testContains() {
		var model = new GraphableModel()
		var rootNodes = new ArrayList()
		var toFind1 = new GraphableNode("tofind1")
		var toFind2 = new GraphableEdge("tofind2")
		var toFind3 = new GraphableNode("tofind3")
		rootNodes.add(
			new GraphableNode("root").addChildren(new GraphableNode("child1"), new GraphableNode("child2"), toFind1).
				addEdges(new GraphableEdge("edge1"), toFind2))
		rootNodes.add(toFind3)
		model.rootNodes = rootNodes

		assertTrue(model.contains(toFind1))
		assertTrue(model.contains(toFind2))
		assertTrue(model.contains(toFind3))

		assertFalse(model.contains(new GraphableNode("not found :(")))
	}

	@Test
	def void testAddRootNodes() {
		var model = new GraphableModel()
		model.addObserver(this)
		model.addRootNodes(new GraphableNode("root1"), new GraphableNode("root2"))
		var source = new GraphableNode("source")
		var edge = new GraphableEdge("edge1", source, GraphableNode.NULL_NODE)
		var child1 = new GraphableNode("child1")
		model.rootNodes.get(0).addChildren(
			child1.addChildren(new GraphableNode("subchild1").addChildren(source)))
			
		child1.removeChildren(child1.children)

		edge.name = "changed name"
		edge.notifyObservers
		assertThat(notifiedBy, sameInstance(edge))

		assertThat(model.rootNodes.length, is(2))
	}

	override update(Observable o, Object arg) {
		notifiedBy = arg
	}

}
