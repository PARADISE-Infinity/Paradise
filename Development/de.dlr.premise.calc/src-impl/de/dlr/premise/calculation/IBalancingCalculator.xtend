/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.calculation

import de.dlr.premise.states.data.State
import de.dlr.premise.system.Balancing
import java.util.function.Predicate

interface IBalancingCalculator {
	def void calculate(Balancing balancing, Predicate<State> targetStatePredicate, boolean sync)
}