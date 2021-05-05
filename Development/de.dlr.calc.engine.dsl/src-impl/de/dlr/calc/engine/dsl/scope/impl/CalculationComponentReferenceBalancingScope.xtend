/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.calc.engine.dsl.scope.impl

import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import de.dlr.calc.engine.dsl.scope.IBiMapBackedUsedBalancingScope
import de.dlr.calc.engine.dsl.scope.ICalculationBalancingScope
import de.dlr.premise.util.SubstitutionMappingHelper
import de.dlr.premise.component.ComponentReferencePointer
import de.dlr.premise.graph.APointer
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.system.ABalancing
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.SystemPackage
import de.dlr.premise.util.scope.ScopedEObjectFactory
import java.util.Set
import org.eclipse.emf.ecore.util.EcoreUtil.UsageCrossReferencer
import org.eclipse.xtend.lib.annotations.Delegate
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static extension de.dlr.premise.util.SubstitutionMappingHelper.*
import static extension de.dlr.premise.util.PremiseHelper.closure

@FinalFieldsConstructor class CalculationComponentReferenceBalancingScope implements ICalculationBalancingScope {
	
	@Delegate val extension IBiMapBackedUsedBalancingScope baseScope
	val ImmutableSet<ComponentReference> balancingComponentReferenceScope
	
	Set<AParameterDef> parametersToRemove
	Set<AParameterDef> parametersToAdd
		
	override needsPrepare() {
		val componentReference = balancingComponentReferenceScope.head
		val componentReferenceScoped = ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(componentReference)
		
		if (componentReferenceScoped.activeImplementation != null) {
			val implScoped = componentReferenceScoped.activeImplementation 
			
			val allBalsInImpl = implScoped.eAllContents.filter(ABalancing).toSet as Set<?> as Set<ABalancing<?>>
			
			val incomingComponentReferencePointersForBalancingTarget = UsageCrossReferencer.find(componentReference, componentReference.eResource.resourceSet)
				.map[getEObject]
				.filter(ComponentReferencePointer)
				.filter[ComponentReferencePointer<?> it | eContainer?.eContainingFeature == SystemPackage.Literals.BALANCING__ACTUAL_TARGET]
				.toSet
			
			val externallySetParametersScoped = incomingComponentReferencePointersForBalancingTarget
				.map[ComponentReferencePointer<?> it | ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(it)]
				.map[target]
				.filter(AParameterDef)
				.toSet
				
			val pinnedParametersCurrentlyInMapping = componentReferenceScoped.substitutionMap
				.filter[pinned]
				.map[original]
				.filter(AParameterDef)
				.toSet
			
			val substitutionSourceParameters = (externallySetParametersScoped + pinnedParametersCurrentlyInMapping).toSet
			
			val parametersToIncludeInMapping = substitutionSourceParameters.closure[p | 
				allBalsInImpl.filter[sources.exists[it == p]].map[target]
			].toSet
			
			val parametersCurrentlyInMapping = componentReferenceScoped.substitutionMap
				.map[original]
				.filter(AParameterDef)
				.toSet
		
			parametersToRemove = parametersCurrentlyInMapping - parametersToIncludeInMapping
			parametersToAdd = parametersToIncludeInMapping - parametersCurrentlyInMapping
			
			if (!parametersToAdd.isEmpty || !parametersToRemove.isEmpty) {
				return true
			}
		}
		
		return false
	}
	
	override prepareModel() {
		SubstitutionMappingHelper.updateMap(balancingComponentReferenceScope.head, parametersToRemove, parametersToAdd)
	}
	
	override needsCalculation() {
		targetParameter != baseScope.targetParameter
	}
	
	override getSourceParameter(String name) {
		sources.get(name)?.substitutedTarget
	}
	
	override getTargetParameter() {
		targets.values.head?.substitutedTarget
	}
	
	
	def private <E> operator_minus(Set<E> set1, Set<?> set2) {
		Sets.difference(set1, set2)
	}
	
	def private substitutedTarget(APointer<? extends AParameterDef> pointer) {
		val pointerScoped = ScopedEObjectFactory.INSTANCE.createScopedEObjectForScope(pointer, balancingComponentReferenceScope)
		val target = pointerScoped.target
		
		val componentReference = balancingComponentReferenceScope.head
		
		if (componentReference.substitutionMap.containsKey(target)) {
			componentReference.substitutionMap.get(target) as AParameterDef
		} else {
			pointer?.target
		}
	}
}