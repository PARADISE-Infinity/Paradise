/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.states.reachability

import de.dlr.premise.states.data.State
import de.dlr.premise.states.data.StateGraph
import de.dlr.premise.states.reachability.StateReachabilityChecker.BackwardStep
import de.dlr.premise.states.reachability.mode.ModeReachabilityChecker
import java.util.Set
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

@FinalFieldsConstructor class CacheUsingReachabilityCheckerDelegate implements IStateReachabilityCheckerDelegate {
		val StateGraph graph
		val ModeReachabilityChecker modeReachabilityChecker
		
		override isKnownReachable(State state) {
			return graph.isMarkedReachable(state)
		}

		override isKnownUnreachable(State state) {
			// If a state contains a locally unreachable mode, the state itself is unreachable
			if (!modeReachabilityChecker.allModesReachable(state)) {
				return true
			}
			return graph.isMarkedReachable(state)
		}

		override newPreStates(BackwardStep backwardStep, Set<State> preStates) {
			for (currentStepPreState : preStates) {
				graph.add(currentStepPreState, backwardStep.transitionTargetState, backwardStep.transition)
			}
		}

		override markStateReachable(State state) {
			graph.markStateAndSuccessorsReachable(state)
		}

		override markStateUnreachable(State state) {
			graph.markStateAndPredecessorsUnreachable(state)
		}
	}