/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.shared

import com.google.common.collect.BiMap
import com.google.common.collect.HashBasedTable
import com.google.common.collect.HashBiMap
import com.google.common.collect.Table
import de.dlr.exchange.base.xtend.RepresentationHelper
import de.dlr.premise.component.ComponentDefinition
import de.dlr.premise.component.ComponentReferencePointer
import de.dlr.premise.component.ParameterDefinition
import de.dlr.premise.component.Satisfies
import de.dlr.premise.element.AElement
import de.dlr.premise.element.Connection
import de.dlr.premise.element.Relation
import de.dlr.premise.element.Transition
import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.DirectPointer
import de.dlr.premise.graph.IEdge
import de.dlr.premise.graph.INode
import de.dlr.premise.query.QueryMode
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.representation.LabelStyle
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.functions.RequiredParameter
import de.dlr.premise.util.PremiseHelper
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.IAspectToModelMapper
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.SelectionHolder
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.SelectionListener
import de.dlr.premise.view.graphicaleditorview.view.GraphRegistry
import de.dlr.premise.view.graphicaleditorview.viewmodel.GraphableEdge
import de.dlr.premise.view.graphicaleditorview.viewmodel.GraphableNode
import de.dlr.premise.view.graphicaleditorview.viewmodel.IGraphableModel
import de.dlr.premise.view.graphicaleditorview.viewmodel.Icon
import java.awt.Color
import java.util.Collection
import java.util.Map
import java.util.Observable
import java.util.Optional
import org.eclipse.emf.common.command.Command
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.edit.command.DeleteCommand

import static de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry.getEditingDomain
import static de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry.getGraphableModel
import com.google.common.collect.HashMultimap

/** 
 */
abstract class AFilteredRepositoryToModelMapper implements IAspectToModelMapper {

	// model-element -> (source-element, target-element, representing edge)
	var Map<IEdge<? extends INode, ? extends INode>, Table<EObject, EObject, GraphableEdge>> edgeTable = newHashMap()
	// Can't use INode here since ComponentReference appears as a node in this view
	var BiMap<EObject, GraphableNode> nodeMap = HashBiMap::create()

	extension RepresentationHelper representationHelper

	new() {
		graphableModel.addObserver(this)
	}

	override update(Observable o, Object arg) {
		SelectionListener.instance.enableSchedule = false
		try {

			switch arg {
				GraphableNode case !arg.isPresent: {
					// delete node
					if (nodeMap.containsValue(arg)) {
						var command = new DeleteCommand(editingDomain, #[nodeMap.inverse.get(arg)])
						command.executeCommand
						nodeMap.inverse.remove(arg)
					}
				}
				GraphableNode case arg.isPresent && !nodeMap.containsValue(arg): {
					// create missing node
					var root = SelectionHolder.instance.selection.filter[node].head
					var name = arg.name
					if(root == null || root instanceof AElement) return;
					var node = createNode(root, name)
					if (node != null) {
						nodeMap.put(node, arg)
					} else {
						arg.delete
					}
				}
				GraphableEdge case !arg.isPresent: {
					var entry = edgeTable.filter[k, v|v.values.contains(arg)].entrySet.head
					var cell = entry?.value?.cellSet?.findFirst[value == arg]
					if(entry == null || cell == null) return;
					edgeTable.values.filter[values.contains(arg)].forEach[values.remove(arg)]

					graphableModel.deleteObserver(this)
					removeEdge(entry.key, cell.rowKey, cell.columnKey)
					graphableModel.addObserver(this)
				}
				GraphableEdge case !edgeTable.values.exists[containsValue(arg)]: {
					val source = nodeMap.inverse.get(arg.source)
					val target = nodeMap.inverse.get(arg.target)
					if (source == null || target == null) {
						return
					}
					var root = PremiseHelper.getLowestCommonAncestor(source, target)
					if(!root.isNode) root = source
					var relation = createEdge(source, target, root, arg.name)
					if (relation != null) {
						val table = HashBasedTable::create()
						table.put(source, target, arg)
						edgeTable.put(relation, table)
					} else {
						arg.delete
					}
				}
			}
		} finally {
			SelectionListener.instance.enableSchedule = true
		}

	}

	override selectionChanged(Collection<EObject> selection) {
		if (selection.nullOrEmpty || selection.head?.eResource?.resourceSet == null) {
			return
		}
		// update the GraphableModel to the model changes
		graphableModel.deleteObserver(this)
		representationHelper = RepresentationHelper.getInstance(selection.head.eResource.resourceSet)
		representationHelper.addObserver[representationChanged]
		// remove elements from selection that are children of other selected elements
		val s = newLinkedList()
		selection.forEach[if(!s.containsNode(it)) s.add(it)]
		s.nodes.forEach[update(graphableModel)]
		// remove deleted nodes from rootElements
		nodeMap.filter[key, value|!value.isPresent || !selection.containsNode(value) || key.affectedByFilterQuery].
			immutableCopy.forEach [ key, node |
				nodeMap.remove(key)
				node.delete()
			]
		val selectedRelations = selection.relations
		selectedRelations.forEach[update]
		// remove deleted edges
		edgeTable.filter[k, v|!selectedRelations.contains(k)].immutableCopy.forEach [ k, v |
			edgeTable.remove(k)
			v.values.forEach[delete]
		]
		graphableModel.addObserver(this)

	}

	private def void representationChanged(EObject element) {
		if (nodeMap.containsKey(element)) {
			val gNode = nodeMap.get(element)
			val color = element.color?.value?.toColor
			if (color != null) {
				gNode.color = color
			} else {
				gNode.resetColor
			}
			gNode.name = element.representedName
			gNode.notifyObservers
		}
		if (element instanceof IEdge<?, ?>) {
			update(element as IEdge<? extends INode, ? extends INode>)
		}
	}

	private def void update(IEdge<? extends INode, ? extends INode> edge) {
		var Collection<? extends EObject> sources = edge.sourcesForEdge
		var Collection<? extends EObject> targets = edge.targetsForEdge
		var bidirectional = edge.bidirectionalForEdge
		val concreteEdgeTable = edgeTable.computeIfAbsent(edge, [HashBasedTable.create])
		
		// all concrete edges that were touched, source -> targets
		val createdOrUpdatedConcreteEdges = HashMultimap.create
		
		// update or create edges
		for (source : sources) {
			var Color color = edge.getColor(source)?.value?.toColor ?: edge.colorForEdge
			val LabelStyle style = edge.getLabelStyle(source)
			var label = if(style == null || style.visible) edge.labelForEdge else ""
			for (target : targets) {
				val sourceNode = source.graphableNodeOrParent
				val targetNode = target.graphableNodeOrParent
				if (sourceNode != null && targetNode != null && (sourceNode != targetNode)) {
					var gEdge = concreteEdgeTable.get(source, target)
					if (gEdge == null || !gEdge.present) {
						gEdge = new GraphableEdge(label, sourceNode, targetNode)
					} else {
						gEdge.name = label
						gEdge.source = sourceNode
						gEdge.target = targetNode
					}
					gEdge.color = color
					gEdge.bidirectional = bidirectional
					gEdge.notifyObservers
					concreteEdgeTable.put(source, target, gEdge)
					createdOrUpdatedConcreteEdges.put(source, target)
				} else {
					concreteEdgeTable.remove(source, target)?.delete()
				}
			}
		}
		
		// delete edges
		for (val iter = concreteEdgeTable.cellSet.iterator; iter.hasNext; ) {
			val cell = iter.next
			if (!createdOrUpdatedConcreteEdges.containsEntry(cell.rowKey, cell.columnKey)) {
				println(cell)
				cell.value.delete()
				iter.remove()
			}
		}
	}
	
	private def getGraphableNodeOrParent(EObject eObj) {
		if (GraphRegistry.pullUpEdges) {
			var current = eObj
			while (current != null) {
				if (nodeMap.containsKey(current)) {
					return nodeMap.get(current)
				}
				current = current.eContainer
			} 
			return null
		} else {
			return nodeMap.get(eObj)
		}
	}

	private def Color toColor(String color) {
		Color.decode(color)
	}

	protected def Collection<? extends EObject> getSourcesForEdge(IEdge<? extends INode, ? extends INode> edge) {
		edge.sourcePointers.map[fromPointer]
	}

	protected def Collection<? extends EObject> getTargetsForEdge(IEdge<? extends INode, ? extends INode> edge) {
		edge.targetPointers.map[fromPointer]
	}

	private def void update(EObject node, Object parent) {
		update(node, parent, GraphRegistry.depth)
	}

	private def void update(EObject node, Object parent, int depth) {
		if(depth == 0) return;
		var gNode = nodeMap.get(node) ?: new GraphableNode(node.representedName)
		if(!nodeMap.containsKey(node)) nodeMap.put(node, gNode)
		switch (parent) {
			IGraphableModel: {
				gNode.parent = GraphableNode.NULL_NODE
			}
			GraphableNode:
				gNode.parent = parent
		}
		gNode.name = node.representedName
		gNode.icon = node.icon
		gNode.secondary = node.secondary

		val color = node.color?.value?.toColor
		if (color != null) {
			gNode.color = color
		} else {
			gNode.resetColor
		}
		gNode.notifyObservers
		for (children : node.children) {
			children.update(gNode, depth - 1)
		}
	}

	private def String getRepresentedName(EObject element) {
		if (Optional.ofNullable(element.labelStyle).map[visible].orElse(true)) {
			element.name
		} else {
			""
		}
	}

	def protected Icon getIcon(EObject object) {
		switch object {
			AParameterDef,
			RequiredParameter,
			ParameterDefinition:
				return Icon.PAPER
			ComponentDefinition,
			ComponentReference:
				return Icon.ROUND_RECT_DASH
		}
		return Icon.ROUND_RECT
	}

	override dispose() {
		graphableModel.deleteObserver(this)
		edgeTable.values.forEach[values.forEach[delete]]
		edgeTable.clear
		nodeMap.values.forEach[delete]
		nodeMap.clear
	}

	protected def boolean containsNode(Collection<? extends EObject> selection, EObject node) {
		containsNode(selection, node, GraphRegistry.depth)
	}

	protected def boolean containsNode(Collection<? extends EObject> selection, EObject node, int depth) {
		if(depth == 0) return false
		if(!node.isNode) return false
		if(selection.contains(node)) return true
		for (it : selection) {
			if(containsNode(it.children, node, depth - 1)) return true
		}
		return false
	}

	protected def boolean containsNode(Collection<EObject> selection, GraphableNode node) {
		if(nodeMap.containsValue(node)) return selection.containsNode(nodeMap.inverse.get(node))
		return false
	}

	protected def IEdge<? extends INode, ? extends INode> createEdge(EObject source, EObject target, EObject root,
		String name)

	/**
	 * If subclasses override this method to return anything but false, they must also override createNode
	 */
	override isNodeCreationAllowed(Collection<EObject> selection) {
		return false
	}

	protected def EObject createNode(EObject root, String name) {
		throw new IllegalStateException("createNode should only be called when isNodeCreationAllowed returns true!")
	}

	private def dispatch removeEdge(Relation element, EObject source, EObject target) {
		var command = new DeleteCommand(editingDomain, newArrayList(element))
		command.executeCommand
	}

	private def dispatch removeEdge(Connection<?> element, EObject source, EObject target) {
		var command = new DeleteCommand(editingDomain, newArrayList(element))
		command.executeCommand
	}

	private def dispatch removeEdge(Satisfies<?, ?> element, EObject source, EObject target) {
		var command = new DeleteCommand(editingDomain, newArrayList(element))
		command.executeCommand
	}

	private def dispatch removeEdge(Balancing element, EObject source, EObject target) {
		println("The deletion of sources for a balancing is questionable and thus disabled.")
		update(element)
		return false
	}

	private def dispatch removeEdge(Transition element, EObject source, EObject target) {
		var command = new DeleteCommand(editingDomain, newArrayList(element))
		command.executeCommand
	}

	private def dispatch removeEdge(EObject element, EObject source, EObject target) { return false }

	protected def executeCommand(Command command) {
		if (command.canExecute) {
			editingDomain.getCommandStack().execute(command)
			return true
		} else {
			return false
		}
	}

	private def dispatch fromPointer(APointer<? extends EObject> pointer) {
		throw new IllegalArgumentException
	}

	private def dispatch fromPointer(Void pointer) {
		null
	}

	private def dispatch fromPointer(ComponentReferencePointer<? extends EObject> pointer) {
		pointer.componentReference
	}

	private def dispatch fromPointer(DirectPointer<? extends EObject> pointer) {
		pointer.target
	}

	protected def boolean isAffectedByFilterQuery(Object element) {
		return SelectionHolder.instance.query.mode != QueryMode.HIGHLIGHT &&
			!SelectionHolder.instance.predicate.apply(element)
	}

	protected def String getLabelForEdge(IEdge<? extends INode, ? extends INode> edge)

	protected def Color getColorForEdge(IEdge<? extends INode, ? extends INode> edge)

	protected def boolean getBidirectionalForEdge(IEdge<? extends INode, ? extends INode> edge)

	/**
	 * @return whether an EObject is of a type that gets represented as a Node.
	 */
	protected def boolean isNode(EObject node)

	/**
	 * @return all the top-level nodes for the given selection
	 */
	protected def Collection<EObject> getNodes(Collection<? extends EObject> selection)

	/**
	 * @return all the different Relations in one List.
	 */
	protected def Collection<IEdge<? extends INode, ? extends INode>> getRelations(
		Collection<? extends EObject> selection)

	/**
	 * @return a name for the given EObject
	 */
	protected def String getName(EObject object)

	/**
	 * @return the direct children of the given EObject
	 */
	protected def Collection<? extends EObject> getChildren(EObject object)

	/**
	 * @return whether this node should be part of the secondary tree (i.e. function-tree)
	 */
	protected def boolean isSecondary(EObject object)

}
