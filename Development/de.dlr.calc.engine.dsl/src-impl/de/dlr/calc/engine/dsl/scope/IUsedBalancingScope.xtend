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

import de.dlr.premise.registry.AParameterDef

/**
 *  Describes the system elements that are used by a balancing in its calculation. These are derived from the balancing itself.
 * 
 * Since a balancing can only have one target at any time, the target is exposed specially. Implementations should guarantee that:
 */
interface IUsedBalancingScope extends IBalancingScope {
	def AParameterDef getTargetParameter()
	
	def String getTargetName()
}