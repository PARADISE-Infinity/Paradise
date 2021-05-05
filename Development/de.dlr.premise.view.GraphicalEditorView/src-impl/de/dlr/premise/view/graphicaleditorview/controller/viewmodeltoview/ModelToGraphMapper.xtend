/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.controller.viewmodeltoview

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import de.dlr.premise.view.graphicaleditorview.view.GraphRegistry
import de.dlr.premise.view.graphicaleditorview.view.YEdge
import de.dlr.premise.view.graphicaleditorview.view.YNode
import de.dlr.premise.view.graphicaleditorview.viewmodel.GraphableEdge
import de.dlr.premise.view.graphicaleditorview.viewmodel.GraphableModel
import de.dlr.premise.view.graphicaleditorview.viewmodel.GraphableNode
import de.dlr.premise.view.graphicaleditorview.viewmodel.GraphableObject
import java.util.Observable
import java.util.Observer
import y.base.Edge
import y.base.GraphEvent
import y.base.Node
import org.eclipse.swt.widgets.Display

/**
 * Maps the GraphableModel to the YFiles Graph Elements (and back)
 */
class ModelToGraphMapper implements Observer {

	val GraphableModel model

	var BiMap<GraphableNode, YNode> nodeMap
	var BiMap<GraphableEdge, YEdge> edgeMap

	new(GraphableModel model) {
		this.model = model
		this.model.addObserver(this)
		nodeMap = HashBiMap.create()
		edgeMap = HashBiMap.create()
	}

	override update(Observable o, Object object) {
		var keepZoomFactor = true
		switch (object) {
			GraphableObject: {
				synchronized (GraphRegistry.graph) {
					if (!model.contains(object) || !object.isPresent) {
						remove(object)
					} else {
						updateElement(object)
					}
				}
				keepZoomFactor = false
			}
			YNode: {
				updateElement(object)
			}
			YEdge: {
				updateElement(object)
			}
			GraphEvent case object.type == GraphEvent.EDGE_CREATION: {
				createEdge(object.data as Edge)
			}
			GraphEvent case object.type == GraphEvent.NODE_CREATION: {
				createNode(object.data as Node)
			}
			default:
				return
		}
		GraphRegistry.layoutExecutor.scheduleLayout(keepZoomFactor)
	}

	def dispatch void updateElement(GraphableNode gNode) {
		GraphRegistry.graphListener.suspendListening
		var yNode = nodeMap.get(gNode) ?: new YNode(gNode.name)
		if(!nodeMap.containsKey(gNode)) nodeMap.put(gNode, yNode)

		yNode.startTransaction()
		yNode.label = gNode.name
		yNode.color = gNode.color
		yNode.icon = gNode.icon
		yNode.secondary = gNode.secondary
		// update children
		gNode.children.forEach[updateElement]
		yNode.addChildren(gNode.children.map[nodeMap.get(it)].filterNull)
		yNode.removeChildren(yNode.children.filter[!gNode.children.contains(nodeMap.inverse.get(it))])
		yNode.commitTransaction()

		GraphRegistry.graphListener.resumeListening
	}

	def dispatch void updateElement(GraphableEdge gEdge) {
		if(gEdge.source == GraphableNode.NULL_NODE || gEdge.target == GraphableNode.NULL_NODE) return;
		if (!edgeMap.containsKey(gEdge)) {
			GraphRegistry.graphListener.suspendListening
			edgeMap.put(gEdge,
				new YEdge(gEdge.name, nodeMap.get(gEdge.source), nodeMap.get(gEdge.target), gEdge.color, false,
					gEdge.bidirectional))
			GraphRegistry.graphListener.resumeListening
			return
		}
		var yEdge = edgeMap.get(gEdge)
		yEdge.source = nodeMap.get(gEdge.source)
		yEdge.target = nodeMap.get(gEdge.target)
		yEdge.color = gEdge.color
		yEdge.label = gEdge.name
		yEdge.bidirectional = gEdge.bidirectional
	}

	def dispatch void updateElement(YNode yNode) {
		var gNode = nodeMap.inverse.get(yNode)
		if (!yNode.isPresent) {
			model.deleteObserver(this)
			gNode?.delete()
			model.addObserver(this)
			nodeMap.remove(gNode)
		}
	// TODO update existing GraphableNodes
	}

	def dispatch void updateElement(YEdge yEdge) {
		var gEdge = edgeMap.inverse.get(yEdge)
		if (!yEdge.isPresent) {
			model.deleteObserver(this)
			gEdge?.delete()
			model.addObserver(this)
			edgeMap.remove(gEdge)
		}
	// TODO update existing GraphableEdges
	}

	def dispatch remove(GraphableNode node) {
		nodeMap.get(node)?.delete()
		nodeMap.remove(node)
	}

	def dispatch remove(GraphableEdge edge) {
		edgeMap.get(edge)?.delete()
		edgeMap.remove(edge)
	}

	def getYNode(Node node) {
		nodeMap.values.findFirst[getNode() == node] ?: YNode.NULL_NODE
	}

	def getYEdge(Edge edge) {
		edgeMap.values.findFirst[getEdge() == edge] ?: new YEdge(edge, false)
	}

	def createEdge(Edge edge) {
		var yEdge = new YEdge(edge, false)
		model.deleteObserver(this)
		var gEdge = new GraphableEdge(yEdge.label, nodeMap.inverse.get(yEdge.source), nodeMap.inverse.get(yEdge.target))
		model.addObserver(this)
		if (gEdge.isPresent) {
			edgeMap.put(gEdge, yEdge)
		} else {
			yEdge.delete
		}
	}

	def createNode(Node node) {
		var yNode = new YNode(node)
		model.deleteObserver(this)
		var gNode = new GraphableNode(yNode.label)
		gNode.parent = GraphableNode.NULL_NODE
		gNode.notifyObservers
		yNode.icon = model.defaultNodeIcon
		Display.^default.asyncExec[GraphRegistry.graph.updateViews]
		model.addObserver(this)
		if (gNode.isPresent) {
			nodeMap.put(gNode, yNode)
		} else {
			yNode.delete
		}
	}
}
