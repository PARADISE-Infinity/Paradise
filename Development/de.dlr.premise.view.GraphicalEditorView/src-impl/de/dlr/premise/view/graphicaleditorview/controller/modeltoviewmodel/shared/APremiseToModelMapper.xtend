/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.shared

import de.dlr.premise.component.ChildComponentDefinition
import de.dlr.premise.component.ComponentDefinition
import de.dlr.premise.component.ComponentFactory
import de.dlr.premise.component.ParameterDefinition
import de.dlr.premise.component.Satisfies
import de.dlr.premise.element.AElement
import de.dlr.premise.element.Connection
import de.dlr.premise.element.Relation
import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.GraphFactory
import de.dlr.premise.graph.IEdge
import de.dlr.premise.graph.INode
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.functions.RequiredParameter
import de.dlr.premise.functions.UseCase
import de.dlr.premise.util.PremiseHelper
import de.dlr.premise.view.graphicaleditorview.view.GraphRegistry
import java.awt.Color
import java.util.Collection
import java.util.List
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject

import static de.dlr.premise.view.graphicaleditorview.view.GraphItemProvider.getRelationColor

/** 
 */
abstract class APremiseToModelMapper extends AFilteredRepositoryToModelMapper {

	val static nodeTypes = newArrayList(SystemComponent, ComponentReference, AParameterDef, UseCase, RequiredParameter,
		ComponentDefinition, ChildComponentDefinition, ParameterDefinition)
	val static secondaryNodeTypes = newArrayList(UseCase, RequiredParameter)
	val static edgeTypes = newArrayList(Relation, Connection, Balancing, Satisfies)

	override protected boolean isNode(EObject node) {
		for (n : nodeTypes) {
			if(n.isInstance(node)) return true
		}
		return false
	}

	override protected boolean isSecondary(EObject node) {
		for (n : secondaryNodeTypes) {
			if(n.isInstance(node)) return true
		}
		return false
	}

	override protected Collection<EObject> getNodes(Collection<? extends EObject> selection) {
		var result = newHashSet()
		result.addAll(selection.filter[node].filter[!affectedByFilterQuery])
		return result
	}

	override protected Collection<IEdge<? extends INode, ? extends INode>> getRelations(
		Collection<? extends EObject> selection) {
		var result = newHashSet()
		for (selected : selection) {
			val root = PremiseHelper.getRoot(selected as EObject)
			for (type : edgeTypes) {
				result.addAll(
					PremiseHelper.getAll(root, type) as EList<?> as EList<IEdge<? extends INode, ? extends INode>>)
			}
		}
		return result.filter[!affectedByFilterQuery || isNecessaryForEditing].toList
	}

	/**
	 * Allows for some elements to always be shown, even if they would be filtered out.
	 * I.e. always show Connections when the Connections view is active.
	 */
	def protected boolean isNecessaryForEditing(IEdge<? extends INode, ? extends INode> edge)

	override protected String getLabelForEdge(IEdge<? extends INode, ? extends INode> edge) {
		switch (edge) {
			Relation,
			Connection<?>: {
				return edge.name
			}
		}
		return ""
	}

	override protected Color getColorForEdge(IEdge<? extends INode, ? extends INode> edge) {
		switch (edge) {
			Relation: {
				return getRelationColor(Relation)
			}
			Connection<?>: {
				return getRelationColor(Connection)
			}
			Balancing: {
				return getRelationColor(Balancing)
			}
			Satisfies<?, ?>: {
				return getRelationColor(Satisfies)
			}
		}
	}

	override protected boolean getBidirectionalForEdge(IEdge<? extends INode, ? extends INode> edge) {
		if (edge instanceof Connection<?>) {
			return edge.bidirectional
		}
		return false
	}

	def dispatch getName(ANameItem it) {
		if (hasChildren && GraphRegistry.groupsEnabled) {
			return name?.replace("\\n", "") ?: ""
		}
		return name?.withLineBreaks
	}

	def dispatch getName(Relation it) {
		return name?.withLineBreaks
	}

	def dispatch getName(Connection<?> it) {
		return name?.withLineBreaks
	}

	def dispatch getName(EObject it) {
		return class.simpleName
	}

	private def withLineBreaks(String name) {
		return name?.replace("\\n", "\n") ?: ""
	}

	private def hasChildren(EObject element) {
		return !element.children.nullOrEmpty
	}

	override protected Collection<? extends EObject> getChildren(EObject object) {
		var List<EObject> combinedChildren = newArrayList()
		switch object {
			SystemComponent: {
				combinedChildren.addAll(object.children)
				combinedChildren.addAll(object.parameters)
			}
			UseCase: {
				combinedChildren.addAll(object.children)
				combinedChildren.addAll(object.requiredParameters)
			}
			ComponentDefinition: {
				combinedChildren.addAll(object.children)
				combinedChildren.addAll(object.parameters)
			}
		}
		return combinedChildren.filter[node].filter[!affectedByFilterQuery].toList
	}

	override boolean isSelectionEmptyForMapper(Collection<?> selection) {
		return !selection.filter(EObject).exists[node]
	}

	def protected <T> toPointer(T element) {
		if (element instanceof ComponentReference) {
			(ComponentFactory.eINSTANCE.<SystemComponent>createComponentReferencePointer => [
				componentReference = element
				definition = element.componentDefinition
			]) as APointer<?> as APointer<AElement>
		} else {
			GraphFactory.eINSTANCE.createDirectPointer => [
				target = element
			]
		}
	}
}
