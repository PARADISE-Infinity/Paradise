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

import com.google.common.collect.ImmutableBiMap
import de.dlr.calc.engine.dsl.scope.IBiMapBackedVisibleBalancingScope
import de.dlr.premise.graph.APointer
import de.dlr.premise.functionpool.AFnDef
import java.util.Map
import org.eclipse.xtend.lib.annotations.Accessors
import de.dlr.premise.registry.AParameterDef

class BalancingScope implements IBiMapBackedVisibleBalancingScope {
	@Accessors val ImmutableBiMap<String, APointer<? extends AParameterDef>> sources
	@Accessors val ImmutableBiMap<String, APointer<? extends AParameterDef>> targets
	@Accessors val ImmutableBiMap<String, AFnDef> functions
	
	new(Map<String, APointer<? extends AParameterDef>> sources, Map<String, APointer<? extends AParameterDef>> targets, Map<String, AFnDef> functions) {
		this.sources = ImmutableBiMap.copyOf(sources)
		this.targets = ImmutableBiMap.copyOf(targets)
		this.functions = ImmutableBiMap.copyOf(functions)
	}
	
	override getSourceParameter(String name) {
		sources.get(name)?.target
	}
	
	override getTargetParameter(String name) {
		targets.get(name)?.target
	}
	
	override getFunction(String name) {
		functions.get(name)
	}
	
	override getSourceNames() {
		sources.keySet
	}
	
	override getTargetNames() {
		targets.keySet
	}
	
	override getFunctionNames() {
		functions.keySet
	}
}