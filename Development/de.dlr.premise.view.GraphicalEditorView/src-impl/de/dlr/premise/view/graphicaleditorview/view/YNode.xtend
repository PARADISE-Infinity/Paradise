/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.view

import de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry
import de.dlr.premise.view.graphicaleditorview.viewmodel.Icon
import java.awt.Color
import java.util.Arrays
import java.util.Collection
import java.util.Observable
import java.util.Set
import y.base.GraphEvent
import y.base.GraphListener
import y.base.Node
import y.view.NodeRealizer

import static de.dlr.premise.view.graphicaleditorview.view.GraphRegistry.getGraph
import static de.dlr.premise.view.graphicaleditorview.view.GraphRegistry.isGroupsEnabled

/** 
 * Wrapper for y.base.Node to make it less painful to use.
 */
class YNode extends Observable implements GraphListener {
	public static val YNode NULL_NODE = new YNode(true) {
		override setNode(Node node) {}

		override addChildren(YNode... yNodes) {}

		override removeChildren(YNode... yNodes) {}

		override update() {}

		override setLabel(String label) {}

		override addEdges(YEdge... yEdges) {}

		override removeEdges(YEdge... yEdges) {}

		override delete() {}

		override setColor(Color color) {}

		override toString() ''''''

		override onGraphEvent(GraphEvent e) {}
	}
	Node node
	boolean wereGroupsEnabled
	volatile boolean updating = false
	boolean transactionOngoing = false
	String label
	Color color
	Icon icon = Icon.ROUND_RECT

	Set<YNode> children = newHashSet()
	Set<YEdge> incomingEdges = newHashSet()
	Set<YEdge> outgoingEdges = newHashSet()

	boolean secondary = false

	/** 
	 */
	new() {
		this("")
	}

	/** 
	 */
	new(String label) {
		this(label, null)
	}

	new(Node node) {
		this(graph.getRealizer(node).labelText, node)
	}

	new(String label, Node node) {
		this.label = label
		var NodeRealizer realizer
		wereGroupsEnabled = isGroupsEnabled
		realizer = GraphItemProvider.nodeRealizer
		realizer.setLabelText(label)
		this.node = node ?: graph.createNode(realizer)
		graph.addGraphListener(this)
		addObserver(MapperRegistry.modelToGraphMapper)
	}

	protected new(boolean doNothing) {
	}

	/** 
	 * @return the node
	 */
	def Node getNode() {
		return node
	}

	/** 
	 * @param node the node to set
	 */
	def void setNode(Node node) {
		if (node != this.node) {
			graph.graphListeners.toList.removeAll(#[this])
			graph.removeNode(this.node)
			if(!graph.graphListeners.toList.contains(this)) graph.addGraphListener(this)
			this.node = node
			this.label = graph.getRealizer(node).labelText
			setChanged()
		}
	}

	/** 
	 * @return an unmodifiable view on the children
	 */
	def Collection<YNode> getChildren() {
		return children.immutableCopy
	}

	def YNode addChildren(YNode... yNodes) {
		children.addAll(Arrays.asList(yNodes))
		if (isGroupsEnabled && !children.nullOrEmpty) {
			if (graph.contains(node) && !graph.hierarchyManager.isGroupNode(node)) {
				graph.hierarchyManager.convertToGroupNode(node)
			}
			yNodes.filter[graph.contains(it.getNode)].forEach [
				graph.hierarchyManager.setParentNode(it.getNode, this.getNode)
			]
		} else {
			if (graph.contains(node) && graph.hierarchyManager.isGroupNode(node)) {
				graph.hierarchyManager.convertToNormalNode(node)
			}
		}
		update()
		setChanged()
		return this
	}

	def void removeChildren(YNode... yNodes) {
		graph.graphListeners.toList.removeAll(#[this])
		children.removeAll(yNodes)
		yNodes.filter[isPresent].forEach[graph.hierarchyManager.setParentNode(it.getNode, null)]
		update()
		if(!graph.graphListeners.toList.contains(this)) graph.addGraphListener(this)
		setChanged()
	}

	def void update() {
		if(updating || transactionOngoing) return;
		updating = true
		GraphRegistry.graphListener.suspendListening
		if (isGroupsEnabled != wereGroupsEnabled) {
			children.immutableCopy.forEach[update()]
			if (wereGroupsEnabled) {
				if (graph.contains(node) && graph.hierarchyManager.isGroupNode(node)) {
					graph.hierarchyManager.convertToNormalNode(node)
				}
				graph.hierarchyManager.setParentNode(node, null)
			}
			wereGroupsEnabled = !wereGroupsEnabled
		}
		var NodeRealizer realizer
		if (isGroupsEnabled && !children.nullOrEmpty) {
			realizer = GraphItemProvider.groupNodeRealizer
			// delete old child edges
			outgoingEdges.immutableCopy.filter[isChildEdge].filter[isPresent].forEach[delete]
		} else {
			realizer = GraphItemProvider.getNodeRealizer(icon)
			// create new child edges
			val out = outgoingEdges
			children.filter[!out.filter[isChildEdge].map[target].toList.contains(it)].forEach [
				new YEdge("", this, it, Color.black, true, false, this.secondary)
			]
			// delete unused child edges
			val c = children.immutableCopy
			outgoingEdges.immutableCopy.filter[isChildEdge].filter[!c.contains(target)].forEach [
				delete
				outgoingEdges.remove(it)
			]
		}
		realizer.labelText = label
		realizer.fillColor2 = color
		graph.setRealizer(node, realizer)
		GraphRegistry.graphListener.resumeListening
		updating = false
	}

	def String getLabel() {
		return label
	}

	def void setLabel(String label) {
		this.label = label
		var realizer = graph.getRealizer(node)
		realizer.labelText = label
	}

	def void addEdges(YEdge... yEdges) {
		yEdges.forEach [ edge |
			if (edge.source == this) {
				outgoingEdges.add(edge)
			}
			if (edge.target == this) {
				incomingEdges.add(edge)
			}
			setChanged()
		]
	}

	def void removeEdges(YEdge... yEdges) {
		yEdges.forEach [ edge |
			if(outgoingEdges.remove(edge) || incomingEdges.remove(edge)) edge.delete()
			setChanged()
		]
	}
	
	def void removeEdgesToChangeEdges(YEdge... yEdges) {
		yEdges.forEach [ edge |
			outgoingEdges.remove(edge)
			incomingEdges.remove(edge)
			setChanged()
		]
	}

	/**
	 * Removes this YNode, it's children and outgoing Edges from the Graph.
	 */
	def void delete() {
		graph.graphListeners.toList.removeAll(#[this])
		removeChildren(children.toArray(#[]))
		removeEdges(incomingEdges + outgoingEdges)
		if (graph.contains(node)) {
			if (graph.hierarchyManager.isGroupNode(node))
				graph.hierarchyManager.removeGroupNode(node)
			else
				graph.removeNode(node)
		}
	}

	def boolean isPresent() {
		return graph.contains(node)
	}

	def void setColor(Color color) {
		this.color = color
		graph.getRealizer(node).fillColor2 = color
	}

	def void setIcon(Icon icon) {
		this.icon = icon
		var realizer = GraphItemProvider.getNodeRealizer(icon)
		realizer.labelText = label
		realizer.setLocation(graph.getRealizer(node).x, graph.getRealizer(node).y)
		graph.setRealizer(node, realizer)
	}

	/**
	 * Set whether this node is part of the secondary tree (i.e. functions) and should be drawn bottom-to-top
	 */
	def void setSecondary(boolean secondary) {
		if (this.secondary != secondary) {
			this.secondary = secondary
			if (!wereGroupsEnabled) {
				outgoingEdges.immutableCopy.filter[childEdge].filter[isPresent].forEach[delete]
				update()
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Object#toString()
	 */
	override String toString() {
		return '''YNode@«Integer.toHexString(this.hashCode)» [label=«label», present=«present»]'''
	}

	override void onGraphEvent(GraphEvent e) {
		if (e.data instanceof Node && e.data == node && e.type == GraphEvent.POST_NODE_REMOVAL) {
			delete()
			setChanged()
			notifyObservers(this)
		}
	}

	def startTransaction() {
		transactionOngoing = true
	}

	def commitTransaction() {
		transactionOngoing = false
		update()
	}

}
