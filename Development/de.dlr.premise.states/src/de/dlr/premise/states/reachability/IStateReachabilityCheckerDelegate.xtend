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

interface IStateReachabilityCheckerDelegate {
	def boolean isKnownReachable(State state) 
	
	def boolean isKnownUnreachable(State state) 
	
	def void newPreStates(BackwardStep backwardStep, Set<State> preStates)
	
	def void markStateReachable(State state)
	
	def void markStateUnreachable(State state)
}