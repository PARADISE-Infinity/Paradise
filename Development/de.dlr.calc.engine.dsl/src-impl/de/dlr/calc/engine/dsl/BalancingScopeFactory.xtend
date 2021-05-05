/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.dsl

import com.google.common.collect.ImmutableBiMap
import com.google.common.collect.ImmutableMap
import com.google.inject.Inject
import de.dlr.calc.engine.dsl.scope.IBiMapBackedBalancingScope
import de.dlr.calc.engine.dsl.scope.IBiMapBackedUsedBalancingScope
import de.dlr.calc.engine.dsl.scope.IBiMapBackedVisibleBalancingScope
import de.dlr.calc.engine.dsl.scope.ICalculationBalancingScope
import de.dlr.calc.engine.dsl.scope.impl.BalancingScope
import de.dlr.calc.engine.dsl.scope.impl.CalculationBalancingScope
import de.dlr.calc.engine.dsl.scope.impl.CalculationComponentReferenceBalancingScope
import de.dlr.calc.engine.dsl.scope.impl.UsedBalancingScope
import de.dlr.calc.engine.dsl.xtext.calcDsl.Model
import de.dlr.calc.engine.dsl.xtext.calcDsl.ParameterLiteral
import de.dlr.premise.component.ComponentFactory
import de.dlr.premise.functionpool.AFnDef
import de.dlr.premise.functionpool.FunctionPool
import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.GraphFactory
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.Parameter
import de.dlr.premise.system.ParameterNameMapping
import de.dlr.premise.util.scope.ScopedEObjectFactory
import de.dlr.premise.xtext.Activator
import java.util.Map
import org.eclipse.emf.common.util.EList
import org.eclipse.xtext.naming.IQualifiedNameConverter
import org.eclipse.xtext.naming.IQualifiedNameProvider

import static extension com.google.common.collect.Maps.filterKeys
import static extension com.google.common.collect.Maps.transformEntries
import static extension com.google.common.collect.Maps.uniqueIndex
import static extension de.dlr.premise.util.PremiseHelper.eAllContentsIncludingRoot
import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.getScope

class BalancingScopeFactory {		
	@Inject IQualifiedNameProvider qualifiedNameProvider
	@Inject IQualifiedNameConverter qualifiedNameConverter
	
	new() {
		val injector = Activator::getDefault().getInjector()
		injector.injectMembers(this)
	}
	
	def IBiMapBackedVisibleBalancingScope createScope(Balancing bal) {
		new BalancingScope(bal.reachableSourceMap, bal.reachableTargetMap, bal.reachableFunctionMap)
	}	
	
	def IBiMapBackedUsedBalancingScope createUsedScope(Balancing bal) {
		new UsedBalancingScope(bal.usedSourceMap, bal.usedTargetMap, bal.reachableFunctionMap)
	}
	
	def ICalculationBalancingScope createCalculationScope(Balancing bal) {		
		val baseScope = bal.createUsedScope
		
		if (bal.scope != null && bal.scope != ScopedEObjectFactory.NULLSCOPE) {
			new CalculationComponentReferenceBalancingScope(baseScope, bal.scope)
		} else {
			new CalculationBalancingScope(baseScope)
		}
	}
	
	def IBiMapBackedVisibleBalancingScope createFilteredScope(IBiMapBackedBalancingScope baseScope, Model model) {
		val sourceNames = model.body.eAllContentsIncludingRoot
			.filter(ParameterLiteral)
			.map[value.name]
			.toSet
					
		val targetNames = newHashSet(model.target.name)
		
		val filteredSources = baseScope.sources.filterKeys[sourceNames.contains(it)]
		val filteredTargets = baseScope.targets.filterKeys[targetNames.contains(it)]
		
		return new BalancingScope(filteredSources, filteredTargets, baseScope.functions)
	}	
	
	def IBiMapBackedUsedBalancingScope createRenamedScope(IBiMapBackedUsedBalancingScope usedScope, IBiMapBackedBalancingScope scope) {		
		val newSources = ImmutableBiMap.copyOf(usedScope.sources.inverse.replaceEntries(scope.sources.inverse)).inverse
		val newTargets = ImmutableBiMap.copyOf(usedScope.targets.inverse.replaceEntries(scope.targets.inverse)).inverse
		
		val renamedScope = new UsedBalancingScope(newSources, newTargets, usedScope.functions)
		renamedScope
	}
	
	def private getReachableSourceMap(Balancing bal) {	
		val directPointers = bal.queryValidSourcesList().convertParametersToNameMap()
		val componentReferencePointers =  bal.queryValidSourcesParentComponentReferenceList().convertComponentReferencesToNameMap()
		
		return ImmutableBiMap.builder.putAll(directPointers).putAll(componentReferencePointers).build
	}
	
	def private getReachableTargetMap(Balancing bal) {
		val directPointers = bal.queryValidTargetList().convertParametersToNameMap()
		val componentReferencePointers = bal.queryValidTargetParentComponentReferenceList().convertComponentReferencesToNameMap()
			
		return ImmutableBiMap.<String, APointer<? extends AParameterDef>>builder.putAll(directPointers).putAll(componentReferencePointers).build
	}
	
	def private getReachableFunctionMap(Balancing bal) {
		val functions = bal.eResource.resourceSet.resources
			.map[contents].flatten
			.filter(FunctionPool)
			.map[functions].flatten
			.filter[qualifiedNameProvider.getFullyQualifiedName(it) != null]
			.uniqueIndex[qualifiedNameConverter.toString(qualifiedNameProvider.getFullyQualifiedName(it))]
			
		return ImmutableBiMap.<String, AFnDef>builder.putAll(functions).build		
	}
	
	def private getUsedSourceMap(Balancing bal) {
		val builder = ImmutableBiMap.<String, APointer<? extends AParameterDef>>builder
		bal.actualSources.forEach[builder.put(it)]
		return builder.build
	}
	
	def private getUsedTargetMap(Balancing bal) {
		val builder = ImmutableBiMap.<String, APointer<? extends AParameterDef>>builder
		if (bal.actualTarget != null) {
			builder.put(bal.actualTarget)
		}
		return builder.build
	}
	
	def private put(ImmutableMap.Builder<String, APointer<? extends AParameterDef>> builder, ParameterNameMapping pnm) {
		builder.put(pnm.key, pnm.value)
	}
	
	private def convertParametersToNameMap(EList<AParameterDef> list) {
		list
			.map[parameter | GraphFactory.eINSTANCE.<AParameterDef>createDirectPointer => [
				target = parameter
			]]
			.toSet
			.uniqueIndex[
				qualifiedNameConverter.toString(qualifiedNameProvider.getFullyQualifiedName(target))
			]
	}
	
	private def convertComponentReferencesToNameMap(EList<ComponentReference> list) {
		list
			.filter[componentDefinition != null]
			.map[compRef |
				compRef.componentDefinition.parameters.map[paramDef | 
					 ComponentFactory.eINSTANCE.<Parameter>createComponentReferencePointer => [
						componentReference = compRef
						definition = paramDef	
					]
				]
			]
			.flatten
			.toSet
			.uniqueIndex[
				qualifiedNameConverter.toString(qualifiedNameProvider.getFullyQualifiedName(componentReference).append(qualifiedNameProvider.getFullyQualifiedName(definition)))
			]
	}
	
	private def <K, V1, V2> replaceEntries(Map<K, V1> sourceMap, Map<K, V2> replacementMap) {
		sourceMap.transformEntries[k, v | replacementMap.get(k)]
	}
}