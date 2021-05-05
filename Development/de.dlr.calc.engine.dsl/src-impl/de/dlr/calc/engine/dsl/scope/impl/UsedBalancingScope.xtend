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
import de.dlr.premise.graph.APointer
import de.dlr.premise.functionpool.AFnDef
import java.util.Map
import de.dlr.premise.registry.AParameterDef

class UsedBalancingScope extends BalancingScope implements IBiMapBackedUsedBalancingScope {
	
	new(Map<String, APointer<? extends AParameterDef>> sources, Map<String, APointer<? extends AParameterDef>> targets, Map<String, AFnDef> functions) {
		super(sources, targets, functions)
		
		if (targets.size > 1) {
			throw new IllegalArgumentException("Can only have one target!")
		}
	}
	
	override getTargetParameter() {
		targets.values.head.target
	}
	
	override getTargetName() {
		targets.keySet.head
	}
	
}