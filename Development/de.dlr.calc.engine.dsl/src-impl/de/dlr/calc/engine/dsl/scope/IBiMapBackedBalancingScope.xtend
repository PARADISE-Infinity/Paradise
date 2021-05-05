/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.dsl.scope

import com.google.common.collect.ImmutableBiMap
import de.dlr.premise.graph.APointer
import de.dlr.premise.functionpool.AFnDef
import de.dlr.premise.registry.AParameterDef
import com.google.common.collect.BiMap

/**
 * Uses and exposes a {@link BiMap} to store system elements visible to a balancing. 
 * 
 * 
 * It must hold for all strings str
 * 
 * <code>
 *    getSourceParameter(str) == getSources().get(str).getTarget()
 * && getTargetParameter(str) == getTargets().get(str).getTarget()
 * && getFunction(str) == getFunctions().get(str)
 * <code>
 */
interface IBiMapBackedBalancingScope extends IBalancingScope {
	def ImmutableBiMap<String, APointer<? extends AParameterDef>> getSources()
	
	def ImmutableBiMap<String, APointer<? extends AParameterDef>> getTargets()
	
	def ImmutableBiMap<String, AFnDef> getFunctions()
}