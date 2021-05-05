/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.view

import de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry
import java.awt.Color
import java.util.Observable
import y.base.Edge
import y.base.GraphEvent
import y.base.GraphListener
import y.base.Node
import y.view.Arrow

import static de.dlr.premise.view.graphicaleditorview.view.GraphRegistry.getGraph

/**
 * 
 */
class YEdge extends Observable implements GraphListener {
	Edge edge
	boolean childEdge
	boolean inverted = true
	boolean bidirectional
	Color color
	String label

	YNode source = YNode.NULL_NODE
	YNode target = YNode.NULL_NODE

	new(String label, YNode source, YNode target, Color color, boolean childEdge, boolean bidirectional,
		boolean inverted, Edge edge) {
		this.label = label
		this.inverted = inverted
		this.source = source ?: YNode.NULL_NODE
		this.target = target ?: YNode.NULL_NODE
		this.color = color
		this.childEdge = childEdge
		this.bidirectional = bidirectional
		this.edge = edge

		var realizer = graph.getRealizer(edge)
		realizer.labelText = label

		this.source.addEdges(this)
		this.target.addEdges(this)
		graph.addGraphListener(this)
		addObserver(MapperRegistry.modelToGraphMapper)
	}

	new(String label, YNode source, YNode target, Color color, boolean childEdge, boolean bidirectional) {
		this(label, source, target, color, childEdge, bidirectional, false,
			graph.createEdge(source.node, target.node, GraphItemProvider.getRelationRealizer(color)))
	}

	new(String label, YNode source, YNode target, Color color, boolean childEdge, boolean bidirectional,
		boolean inverted) {
		this(label, source, target, color, childEdge, bidirectional, inverted,
			graph.createEdge(getEdgeSource(source.node, target.node, inverted),
				getEdgeTarget(source.node, target.node, inverted), GraphItemProvider.getRelationRealizer(color, inverted)))
	}

	new(Edge edge, boolean childEdge) {
		this(graph.getRealizer(edge).labelText, MapperRegistry.modelToGraphMapper.getYNode(edge.source),
			MapperRegistry.modelToGraphMapper.getYNode(edge.target), graph.getRealizer(edge).lineColor, childEdge,
			false, false, edge)
	}

	/**
	 * @return the edge
	 */
	def Edge getEdge() {
		return edge
	}

	/** 
	 * @return the node
	 */
	def YNode getSource() {
		return source
	}

	/** 
	 * @param source the source to set
	 */
	def void setSource(YNode source) {
		if (this.source != (source ?: YNode.NULL_NODE)) {
			this.source.removeEdgesToChangeEdges(this)
			this.source = source ?: YNode.NULL_NODE
			this.source.addEdges(this) // add to both in case source == target
			this.target.addEdges(this)
			graph.graphListeners.toList.removeAll(#[this])
			graph.changeEdge(edge, getEdgeSource(this.source.node, target.node, inverted),
				getEdgeTarget(this.source.node, target.node, inverted))
			if(!GraphRegistry.graph.graphListeners.toList.contains(this)) graph.addGraphListener(this)
		}
	}

	/** 
	 * @return the target
	 */
	def YNode getTarget() {
		return target
	}

	/** 
	 * @param target the target to set
	 */
	def void setTarget(YNode target) {
		if (this.target != (target ?: YNode.NULL_NODE)) {
			this.target.removeEdgesToChangeEdges(this)
			this.target = target ?: YNode.NULL_NODE
			this.source.addEdges(this) // add to both in case source == target
			this.target.addEdges(this)
			graph.graphListeners.toList.removeAll(#[this])
			graph.changeEdge(edge, getEdgeSource(source.node, this.target.node, inverted),
				getEdgeTarget(source.node, this.target.node, inverted))
			if(!GraphRegistry.graph.graphListeners.toList.contains(this)) graph.addGraphListener(this)
		}
	}

	def getLabel() {
		return label
	}

	def void setLabel(String label) {
		if (this.label != label) {
			this.label = label
			graph.getRealizer(edge).labelText = label
		}
	}

	def isChildEdge() {
		return childEdge
	}

	def isBidirectional() {
		return bidirectional
	}

	def setBidirectional(boolean bidirectional) {
		this.bidirectional = bidirectional
		val realizer = graph.getRealizer(edge)
		if (bidirectional) {
			realizer.sourceArrow = Arrow.STANDARD
		} else {
			realizer.sourceArrow = Arrow.NONE
		}
	}

	/**
	 * Remove this edge from the Graph.
	 */
	def void delete() {
		graph.graphListeners.toList.removeAll(#[this])
		if(edge != null && graph.contains(edge)) graph.removeEdge(edge)
		if (childEdge) {
			source.removeChildren(target)
		} else {
			source.removeEdges(this)
			target.removeEdges(this)
		}
	}

	def void setColor(Color color) {
		this.color = color
		graph.getRealizer(edge).lineColor = color
	}

	override onGraphEvent(GraphEvent e) {
		if (e.data instanceof Edge && e.data == edge) {
			var edge = e.data as Edge
			val YNode source = MapperRegistry.modelToGraphMapper.getYNode(edge.source)
			val YNode target = MapperRegistry.modelToGraphMapper.getYNode(edge.target)
			switch (e.type) {
				case e.type == GraphEvent.POST_EDGE_CHANGE && !childEdge: {
					setSource(source)
					setTarget(target)
					setChanged()
				}
				case e.type == GraphEvent.POST_EDGE_REMOVAL && !childEdge: {
					source.removeEdges(this)
					target.removeEdges(this)
					setChanged()
				}
				case e.type == GraphEvent.POST_EDGE_CHANGE && childEdge: {
					this.source.removeChildren(this.target)
					source.addChildren(target)
					delete()
				}
				case e.type == GraphEvent.POST_EDGE_REMOVAL && childEdge: {
					this.source.removeChildren(this.target)
					delete()
				}
			}
			source.notifyObservers(source)
			target.notifyObservers(target)
			notifyObservers(this)
		}
	}

	override def String toString() {
		'''YEdge@«Integer.toHexString(hashCode)» [«IF label != ""»Label=«label», «ENDIF»source=«source.label», target=«target.label»]'''
	}

	def isPresent() {
		return graph.contains(edge)
	}

	private static def getEdgeSource(Node source, Node target, boolean inverted) {
		if(inverted) return target else return source
	}

	private static def getEdgeTarget(Node source, Node target, boolean inverted) {
		if(inverted) return source else return target
	}
}
