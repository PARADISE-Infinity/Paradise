/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.states.reachability.mode

import de.dlr.premise.element.Mode

class BaseModeReachabilityCheckerDelegate implements IModeReachabilityCheckerDelegate {
	
	override isKnownReachable(Mode mode) {
		return false
	}
	
	override isKnownUnreachable(Mode mode) {
		return false
	}
	
	override markModeReachable(Mode mode) {
		// meh
	}
	
	override markModeUnreachable(Mode mode) {
		// why oh why, didn't i take the blue pill
	}
	
}