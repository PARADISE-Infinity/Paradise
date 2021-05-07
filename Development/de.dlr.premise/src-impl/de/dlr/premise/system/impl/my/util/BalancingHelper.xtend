/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.system.impl.my.util

import de.dlr.premise.component.ComponentDefinition
import de.dlr.premise.component.ComponentReferencePointer
import de.dlr.premise.element.Connection
import de.dlr.premise.element.ElementPackage
import de.dlr.premise.element.Relation
import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.DirectPointer
import de.dlr.premise.graph.GraphFactory
import de.dlr.premise.graph.GraphPackage
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.registry.Constant
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.IComponent
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.system.SystemFactory
import de.dlr.premise.system.SystemPackage
import de.dlr.premise.util.PremiseHelper
import java.util.Set
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.InternalEObject
import org.eclipse.emf.ecore.util.EcoreEList
import org.eclipse.emf.ecore.util.EcoreUtil

import static extension de.dlr.premise.system.impl.my.util.BalancingParameterProprotionalityAdapter.findOrCreateParameterProprotionalityAdapter
import static extension de.dlr.premise.util.PremiseHelper.closure
import static extension de.dlr.premise.util.PremiseHelper.flatMap
import static extension de.dlr.premise.util.PremiseHelper.toUnmodifieableEList

class BalancingHelper {
	
	def static void setTarget(Balancing it, AParameterDef newTarget) {
		targetPointer = GraphFactory.eINSTANCE.<AParameterDef>createDirectPointer => [
			target = newTarget
		]
	}
	
	def static EList<APointer<AParameterDef>> getSourcePointers(Balancing it) {
		val l = it.actualSources.map[value]
		
		return new EcoreEList.UnmodifiableEList<APointer<AParameterDef>>(it as InternalEObject,
			SystemPackage.Literals.BALANCING__SOURCE_POINTERS, l.size(), l.toArray());
	}
	
	def static APointer<AParameterDef> getTargetPointer(Balancing it) {
		it.actualTarget?.value
	}
	
	def static void setTargetPointer(Balancing it, APointer<AParameterDef> newTargetPointer) {
		if (actualTarget == null) {
			actualTarget = SystemFactory.eINSTANCE.createParameterNameMapping
		}
		actualTarget.value = newTargetPointer
	}
	
	def static EList<ComponentReference> queryValidSourcesParentComponentReferenceList(Balancing it) {
		it.validSourceParentComponents.filter(ComponentReference).toUnmodifieableEList
	}
	
	def static EList<ComponentReference> queryValidTargetParentComponentReferenceList(Balancing it) {
		containerSystemComponent.children.filter(ComponentReference).toUnmodifieableEList
	}	
	
	def static EList<AParameterDef> queryValidSourcesList(Balancing it) {
		val params = it.validSourceParentComponents.filter(SystemComponent).flatMap[allParameters]
		val constants = it.eResource.resourceSet.allContents.filter(Constant).toIterable
		
		return (params + constants).toUnmodifieableEList
	}
	
	def static EList<AParameterDef> queryValidTargetList(Balancing it) {
		containerSystemComponent.allParameters.toUnmodifieableEList
	}
	
	def static boolean isSourcesValid(Balancing it) {
		val validSources = queryValidSourcesList()
		val validSourcesParentComponentReferences = queryValidSourcesParentComponentReferenceList()
		
		sourcePointers
			.forall[APointer<AParameterDef> sourcePointer |
				sourcePointer.target != null 
					&& switch(sourcePointer) {
						DirectPointer<?>: validSources.contains(sourcePointer.eGet(GraphPackage.Literals.DIRECT_POINTER__TARGET))
						ComponentReferencePointer<?>: sourcePointer.valid &&  validSourcesParentComponentReferences.contains(sourcePointer.componentReference)
						default: throw new IllegalArgumentException()
					}
			]
	}
	
	def static boolean isTargetValid(Balancing it) {		
		val validTargets = queryValidTargetList
		val validTargetParentComponentRerefences = queryValidTargetParentComponentReferenceList
		val targetPointer = it.targetPointer
		
		switch(targetPointer) {
			case null: false
			DirectPointer<?>: validTargets.contains(targetPointer.eGet(GraphPackage.Literals.DIRECT_POINTER__TARGET))
			ComponentReferencePointer<?>: validTargetParentComponentRerefences.contains(targetPointer.componentReference)
			default: throw new IllegalArgumentException()
		}
	}
	
	def static getSourceParameterProportionality(Balancing bal, AParameterDef param) {
		bal.findOrCreateParameterProprotionalityAdapter().getParameterProportionality(param)
	}
	
	def private static getContainerSystemComponent(EObject it) {
		val eContainer = it.eContainer
		
		switch (eContainer) {
			SystemComponent: eContainer
			default: null
		}
	}
	
	def private static getValidSourceParentComponents(Balancing it) {
		de.dlr.premise.system.impl.my.util.BalancingHelper.getValidReferenceParentComponents(it.containerSystemComponent)
	}
	
	def static Set<IComponent> getValidReferenceParentComponents(SystemComponent component) {
		val downward = (component as IComponent).closure[switch (it) {
			SystemComponent: children
			default: #[]
		}]
		
		val upward = component.closure[#[containerSystemComponent]]
		
		val references = EcoreUtil.UsageCrossReferencer.find(component, component.eResource.resourceSet)
			.filter[
				EStructuralFeature == GraphPackage.Literals.DIRECT_POINTER__TARGET && (EObject.eContainer instanceof Connection<?> || EObject.eContainer instanceof Relation)
			]
			.map[
				val feature = switch(EObject.eContainingFeature) {
					case ElementPackage.Literals.CONNECTION__SOURCE_POINTER: ElementPackage.Literals.CONNECTION__TARGET_POINTER
					case ElementPackage.Literals.CONNECTION__TARGET_POINTER: ElementPackage.Literals.CONNECTION__SOURCE_POINTER
					case ElementPackage.Literals.RELATION__SOURCE_POINTER: ElementPackage.Literals.RELATION__TARGET_POINTER
					case ElementPackage.Literals.RELATION__TARGET_POINTER: ElementPackage.Literals.RELATION__SOURCE_POINTER
					default: throw new IllegalArgumentException()
				}
				
				val pointer = EObject.eContainer.eGet(feature)
				
				switch (pointer) {
					DirectPointer<?>: pointer.target
					ComponentReferencePointer<?> case pointer.definition instanceof ComponentDefinition: pointer.componentReference
				}
			]
			.filter(IComponent)
		
		return (downward + upward + references).toSet
	}
	
	def private static getAllParameters(SystemComponent sc) {
		PremiseHelper.getParameters(sc)
	}
}
