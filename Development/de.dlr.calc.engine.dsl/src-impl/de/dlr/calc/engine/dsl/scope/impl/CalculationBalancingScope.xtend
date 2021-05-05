/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.dsl.scope.impl

import de.dlr.calc.engine.dsl.scope.IBiMapBackedUsedBalancingScope
import de.dlr.calc.engine.dsl.scope.ICalculationBalancingScope
import de.dlr.premise.util.SubstitutionMappingHelper
import de.dlr.premise.component.ComponentReferencePointer
import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.DirectPointer
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.util.scope.ScopedEObjectFactory
import org.eclipse.xtend.lib.annotations.Delegate

import static extension de.dlr.premise.util.SubstitutionMappingHelper.*

class CalculationBalancingScope implements ICalculationBalancingScope {
	
	@Delegate val extension IBiMapBackedUsedBalancingScope baseScope
	
	ComponentReference componentReference
	
	AParameterDef targetScoped

	
	new(IBiMapBackedUsedBalancingScope baseScope) {
		this.baseScope = baseScope
	}
	
	override needsPrepare() {
		val targetPointer = ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(targets.values.head)
		
		if (targetPointer instanceof ComponentReferencePointer<?>) {
			val compRefTargetPointer =  targetPointer as ComponentReferencePointer<? extends AParameterDef>
			componentReference = compRefTargetPointer.componentReference
			targetScoped = compRefTargetPointer.target
			
			if (targetScoped != null && !componentReference.substitutionMap.containsKey(targetScoped)) {
				return true
			}
		}
		
		return false
	}
	
	override prepareModel() {
		SubstitutionMappingHelper.updateMap(componentReference, #[], #[targetScoped])
	}
	
	override needsCalculation() {
		true
	}
	
	override getSourceParameter(String name) {
		sources.get(name)?.substitutedTarget
	}
	
	override getTargetParameter() {
		targets.values.head?.substitutedTarget
	}
	
	protected def dispatch substitutedTarget(APointer<? extends AParameterDef> pointer) {
		throw new IllegalArgumentException
	}
	
	protected def dispatch substitutedTarget(DirectPointer<? extends AParameterDef> pointer) {
		pointer.target
	}
	
	protected def dispatch substitutedTarget(ComponentReferencePointer<? extends AParameterDef> pointer) {
		val pointerScoped = ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(pointer)
		val pointerTargetScoped = pointerScoped.target
		
		if (pointer.componentReference.substitutionMap.containsKey(pointerTargetScoped)) {
			pointer.componentReference.substitutionMap.get(pointerTargetScoped) as AParameterDef
		} else {
			pointer.target
		}
	}
	
}