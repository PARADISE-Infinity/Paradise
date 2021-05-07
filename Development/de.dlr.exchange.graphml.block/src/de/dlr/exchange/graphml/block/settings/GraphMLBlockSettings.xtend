/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.block.settings

import com.google.common.collect.Sets
import de.dlr.premise.component.ComponentPackage
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ElementPackage
import de.dlr.premise.graph.IEdge
import de.dlr.premise.graph.INode
import de.dlr.premise.registry.IMetaTypable
import de.dlr.premise.registry.MetaTypeDef
import de.dlr.premise.registry.RegistryPackage
import de.dlr.premise.system.SystemPackage
import de.dlr.premise.functions.UseCasePackage
import de.dlr.premise.util.scope.ScopedEObjectFactory
import java.util.Iterator
import java.util.Observable
import java.util.Set
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtend.lib.annotations.AccessorType
import org.eclipse.xtend.lib.annotations.Accessors

import static extension com.google.common.collect.Maps.*
import static extension de.dlr.exchange.graphml.block.PremiseExtensions.*
import static extension de.dlr.premise.util.PremiseHelper.*
import static extension de.dlr.premise.util.PremiseHelper.closure
import static extension de.dlr.premise.util.PremiseHelper.flatMap
import static extension org.eclipse.xtend.lib.annotations.AccessorType.*

class GraphMLBlockSettings extends Observable {
	
	// Charset of the file selected by the user (to be written into the graphml xml declaration)
	@Accessors String charset 
	
	// Roots as preselected in the tree editor
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<AElement> initialRoots = newHashSet
	
	// All possible root elements derived from initialRoots
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<AElement> allPossibleRoots = newHashSet
	
	// All MetaTypes of all possible roots
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<MetaTypeDef> allRootMetaTypes = newHashSet
	
	// Subset of allRootMetaTypes as selected by user
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<MetaTypeDef> selectedRootMetaTypes = newHashSet
	
	// Subset of allPossibleRoots as selected by user by setting selectedRootMetaTypes
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<AElement> selectedRoots = newHashSet
	
	// All elements that will be drawn (as derived from selected roots and depth)
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<AElement> selectedElements = newHashSet
	
	// All nodes that will be drawn (as derived from selected elements and additional node types)
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<EObject> selectedNodes = newHashSet
	
	// All nodes that will be drawn and all their children
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<EObject> allNodes
	
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) boolean restrictDepth = false
	
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) int depth = -1 
	
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<EClass> selectedAdditionalNodeTypes = newHashSet(
		ElementPackage.Literals.STATE_MACHINE,
		ElementPackage.Literals.MODE, 
		RegistryPackage.Literals.APARAMETER_DEF,
		UseCasePackage.Literals.REQUIRED_PARAMETER
	)	
	
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<EClass> selectedEdgeTypes = newHashSet(
		ElementPackage.Literals.CONNECTION, 
		ElementPackage.Literals.RELATION, 
		ComponentPackage.Literals.SATISFIES, 
		SystemPackage.Literals.ABALANCING, 
		ElementPackage.Literals.TRANSITION
	)
	
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) boolean moveEdgesToParents = true
	@Accessors boolean showEdgeCount = true
	@Accessors boolean showHierarchyAsGroups = true
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) boolean drawExternalDependencies = true
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) boolean drawSelfReferences = false
	
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<IEdge<?,?>> allPossibleEdges = newHashSet
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<IEdge<?,?>> selectedEdges = newHashSet
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<MetaTypeDef> allEdgeMetaTypes = newHashSet
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) Set<MetaTypeDef> selectedEdgeMetaTypes = newHashSet
	
		
	def setInitialRoots(Set<AElement> initialRoots) {
		this.initialRoots = initialRoots.map[ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(it)].toSet
		
		allPossibleRoots = this.initialRoots.iterator
			.flatMap[eAllContentsIncludingRoot]
			.filter(AElement)
			.toSet
		allRootMetaTypes = allPossibleRoots.filter(AElement).flatMap[metaTypes].toSet
		selectedRootMetaTypes = Sets.intersection(selectedRootMetaTypes, allRootMetaTypes)
		
		updateSelectedRoots()
		
		setChanged()
		notifyObservers()
	}
	
	def setSelectedRootMetaTypes(Set<MetaTypeDef> selectedRootMetaTypes) {
		this.selectedRootMetaTypes = selectedRootMetaTypes.map[ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(it)].toSet
		
		updateSelectedRoots()
		
		setChanged()
		notifyObservers()
	}
	
	def setSelectedEdgeMetaTypes(Set<MetaTypeDef> selectedEdgeMetaTypes) {
		this.selectedEdgeMetaTypes = selectedEdgeMetaTypes.map[ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(it)].toSet
		
		updateSelectedEdges()
		
		setChanged()
		notifyObservers()
	}
	
	def addSelectedEdgeType(EClass... eClasses) {
		selectedEdgeTypes.addAll(eClasses)
		
		updateAllPossibleEdges()
		
		setChanged()
		notifyObservers()
	}
	
	def removeSelectedEdgeType(EClass... eClasses) {
		selectedEdgeTypes.removeAll(eClasses)
		
		updateAllPossibleEdges()
		
		setChanged()
		notifyObservers()
	}
	
	def setRestictedDepth(boolean restrictDepth, int depth) {
		this.restrictDepth = restrictDepth
		this.depth = if (restrictDepth) depth else -1
		
		updateAllSelectedElements()
		
		setChanged()
		notifyObservers()
	}
	
	def setMoveEdgesToParents(boolean moveEdgesToParents) {
		this.moveEdgesToParents = moveEdgesToParents
		
		updateAllPossibleEdges()
		
		setChanged()
		notifyObservers()
	}
	
	def setDrawExternalDependencies(boolean drawExternalDependencies) {
		this.drawExternalDependencies = drawExternalDependencies
			
		updateAllPossibleEdges()
		
		setChanged()
		notifyObservers()
	}	
	
	def setDrawSelfReferences(boolean drawSelfReferences) {
		this.drawSelfReferences = drawSelfReferences
			
		updateAllPossibleEdges()
		
		setChanged()
		notifyObservers()
	}
	
	def addSelectedAdditionalNodeType(EClass... eClasses) {
		selectedAdditionalNodeTypes.addAll(eClasses)
		
		updateSelectedNodes()
		
		setChanged()
		notifyObservers()
	}
	
	def removeSelectedAdditionalNodeType(EClass... eClasses) {
		selectedAdditionalNodeTypes.removeAll(eClasses)
		
		updateSelectedNodes()
		
		setChanged()
		notifyObservers()
	}
	
	def private updateSelectedRoots() {
		if (selectedRootMetaTypes.isEmpty) {
			selectedRoots = initialRoots
		} else {
			selectedRoots = allPossibleRoots
				.filter[
					(it as AElement).metaTypes.exists[metaType|selectedRootMetaTypes.contains(metaType)]
				]
				.toSet
		}
		
		updateAllSelectedElements()	
	}
	
	private def updateAllSelectedElements() {
		if (!restrictDepth) {
			selectedElements = selectedRoots.iterator.flatMap[eAllContentsIncludingRoot].filter(AElement).toSet
		} else {
			selectedElements = selectedRoots
				.map[1 -> it]
				.closure[p | if (p.key >= depth) #[] else p.value.eContentsReferenced.filter(AElement).map[p.key + 1 -> it]]
				.map[value]
				.toSet
		}
		
		updateSelectedNodes()
	}
	
	private def updateSelectedNodes() {
		selectedNodes = newHashSet(selectedElements)
				
		val eClasses = (selectedElements.map[eClass] + selectedAdditionalNodeTypes).toSet
		
		val containmentTypes = eClasses.asMap[
			EAllContainments.filter[ref | 
				val type = ref.EType
				if (type instanceof EClass) {
					 selectedAdditionalNodeTypes.exists[isSuperTypeOf(type)]
				} else {
					false
				}
			]
		]
		
		selectedNodes += (selectedElements as Set<?> as Set<EObject>).closure[el | 
			containmentTypes.get(el.eClass)?.flatMap[
				el.eGet(it) as Iterable<EObject>
			]
		]
		
		allNodes = selectedNodes.iterator.flatMap[eAllContentsIncludingRoot].filter(INode).toSet
	
		updateAllPossibleEdges()
	}
	
	private def updateAllPossibleEdges() {		
		val allEdges = selectedRoots
			.map[eResource.resourceSet]
			.flatMap[resources]
			.flatMap[contents]
			.map[ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(it)]
			.iterator
			.flatMap[eAllContentsIncludingRoot]
			.filter(IEdge) as Iterator<?> as Iterator<IEdge<?,?>>
			
		//println(allEdges.toList)
		
		allPossibleEdges = allEdges
			.filter[selectedEdgeType]
			.flatMap[edge | 
				val sourceNodes = edge.referencedSources.map[drawable].filterNull.toSet
				val targetNodes = edge.referencedTargets.map[drawable].filterNull.toSet
				val sourceToTarget = Sets.cartesianProduct(sourceNodes, targetNodes).map[get(0) -> get(1)]
				sourceToTarget.map[it -> edge].iterator
			]
			.filter[
				val sourceToTarget = it.key
				
				if (sourceToTarget.key == sourceToTarget.value) {
					if (drawSelfReferences) {
						return selectedNodes.contains(sourceToTarget.key)
					} else {
						return false
					}
				}
				
				val ret = if (drawExternalDependencies) {
					selectedNodes.contains(sourceToTarget.key) || selectedNodes.contains(sourceToTarget.value)
				} else {
					selectedNodes.contains(sourceToTarget.key) && selectedNodes.contains(sourceToTarget.value)
				}
				
				return ret
			]
			.map[value]
			.toSet
		
		allEdgeMetaTypes = allPossibleEdges.filter(IMetaTypable).map[metaTypes].flatten.toSet
		selectedEdgeMetaTypes = Sets.intersection(selectedEdgeMetaTypes, allEdgeMetaTypes)
			
		updateSelectedEdges()
	}
	
	def private updateSelectedEdges() {
		if (selectedEdgeMetaTypes.isEmpty) {
			selectedEdges = allPossibleEdges
		} else {
			selectedEdges = allPossibleEdges.filter[
				(it as IMetaTypable).metaTypes.exists[metaType|selectedEdgeMetaTypes.contains(metaType)]
			].toSet
		}
	}
	
	def private isSelectedEdgeType(EObject n) {
		selectedEdgeTypes.exists[isSuperTypeOf(n.eClass)]
	}
	
	def getDrawable(EObject obj) {
		val ancestor = obj.ancestorInSelectedNodes
		if (moveEdgesToParents) {
			ancestor ?: obj
		} else {
			switch (ancestor) {
				case null: obj // obj is external dep
				case obj: obj // obj is in selected nodes
				default: null // obj is in all nodes, but not selected nodes
			}
		}
		
	}
		
	def public getAncestorInSelectedNodes(EObject startEl) {
		var el = startEl
		
		while (el != null && !selectedNodes.contains(el)) {
			el = el.eContainer
		}
		
		return el
	}
}