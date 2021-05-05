/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.calc.engine.dsl.scope

import com.google.common.collect.ImmutableSet
import de.dlr.premise.functionpool.AFnDef
import de.dlr.premise.registry.AParameterDef

/**
 * Describes the system elements that are visible to a balancing in a certain context.
 */
interface IBalancingScope {
	
	def AParameterDef getSourceParameter(String name)
	
	def AFnDef getFunction(String name)
	
	def ImmutableSet<String> getSourceNames()
	
	def ImmutableSet<String> getFunctionNames()
}