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

import de.dlr.premise.graph.APointer
import org.eclipse.emf.edit.command.ChangeCommand
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.SystemFactory
import de.dlr.premise.registry.AParameterDef
import de.dlr.calc.engine.dsl.scope.IBiMapBackedBalancingScope

class BalancingChangeCommandCreator {
	def static createChangeCommand(Balancing balancing, String functionText, IBiMapBackedBalancingScope scope) {		
		val sources = scope.sources
			.entrySet
			.map[entry | SystemFactory.eINSTANCE.createParameterNameMapping => [
				key = entry.key
				value = entry.value as APointer<?> as APointer<AParameterDef>
			]]
			
		val targets = scope.targets
			.entrySet
			.map[entry | SystemFactory.eINSTANCE.createParameterNameMapping => [
				key = entry.key
				value = entry.value as APointer<?> as APointer<AParameterDef>
			]]
		
		new ChangeCommand(balancing) {
			override protected doExecute() {
				balancing.actualSources.clear
				balancing.actualSources.addAll(sources)
				balancing.actualTarget = targets.head
				balancing.function = functionText
			}
		}
	}
}