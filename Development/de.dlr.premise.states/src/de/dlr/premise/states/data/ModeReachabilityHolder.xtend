/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.states.data

import de.dlr.premise.element.Mode
import de.dlr.premise.element.StateMachine
import de.dlr.premise.states.reachability.mode.IModeReachabilityCheckerDelegate
import java.util.Set

import static extension de.dlr.premise.states.util.StateCheckingHelper.*

class ModeReachabilityHolder implements IModeReachabilityCheckerDelegate {
	Set<Mode> markedReachableModes = newHashSet
	Set<Mode> markedUnreachableModes = newHashSet
		
	override synchronized isKnownReachable(Mode mode) {
		markedReachableModes.contains(mode)
	}
	
	override synchronized isKnownUnreachable(Mode mode) {
		markedUnreachableModes.contains(mode)
	}
	
	override synchronized markModeReachable(Mode mode) {
		markedReachableModes.add(mode)
	}
	
	override synchronized markModeUnreachable(Mode mode) {
		markedUnreachableModes.add(mode)
	}
	
	def synchronized removeStateMachine(StateMachine machine) {
		markedReachableModes.removeIf[mode | mode.stateMachine == machine]
		markedUnreachableModes.removeIf[mode | mode.stateMachine == machine]
	}

}