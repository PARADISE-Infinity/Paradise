/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.states.reachability

import de.dlr.premise.states.data.State
import de.dlr.premise.states.reachability.StateReachabilityChecker.BackwardStep
import java.util.Set

class BaseStateReachabilityCheckerDelegate implements IStateReachabilityCheckerDelegate {
	
	override isKnownReachable(State state) {
		return false
	}
	
	override isKnownUnreachable(State state) {
		return false
	}
	
	override newPreStates(BackwardStep backwardStep, Set<State> preStates) {
		// do nothing
	}
	
	override markStateReachable(State state) {
		// do nothing
	}
	
	override markStateUnreachable(State state) {
		// ich putz hier nur
	}
	
}