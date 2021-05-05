/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.states.reachability.mode

import com.google.common.collect.Sets
import de.dlr.premise.element.Mode
import de.dlr.premise.element.Transition
import de.dlr.premise.states.data.State
import java.util.ArrayDeque
import java.util.Set
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static extension de.dlr.premise.states.util.StateCheckingHelper.*
import static extension de.dlr.premise.util.PremiseHelper.*

/**
 * Checks whether a given mode is reachable in its state machine.
 * 
 * Checking only happens locally for a state machine, modes that are reachable locally may still be unreachable due to 
 * the global combination of conditions.
 */
@FinalFieldsConstructor class ModeReachabilityChecker {
	val extension IModeReachabilityCheckerDelegate validatorDelegate
	
	def boolean allModesReachable(State state) {
		state.modes.forall[reachable]
	}
	
	def boolean isReachable(Mode mode) {
		if (mode.knownReachable) {
			return true
		}
		if (mode.knownUnreachable) {
			return false
		}
		if (mode.entryMode) {
			return true
		}

		val seenModes = newHashSet(mode)
		
		val modeQueue = new ArrayDeque()
		modeQueue.add(mode)
		
		while (!modeQueue.empty) {
			val currentMode = modeQueue.poll()
			
			val preModes = currentMode.preModes
			
			val entryMode = preModes.findFirst[entryMode]
			if (entryMode != null) {
				markModeReachable(mode)
				return true
			}
			
			val newPreModes = Sets.difference(preModes, seenModes)
			modeQueue.addAll(newPreModes)
			seenModes.addAll(newPreModes)
		}
				
		markModeUnreachable(mode)
		
		return false
	}
	
	private def Set<Mode> getPreModes(Mode mode) {
		mode.incomingTransitions.filter[transitionConditionPossible].flatMap[sources].toSet
	}
	
	private def transitionConditionPossible(Transition trans) {
		if (trans.condition == null) {
			return true
		}

		for (state : trans.condition.possibleStates) {
			val modeInTransitionStateMachine = state.modeByStateMachine.get(trans.stateMachine)
			if (modeInTransitionStateMachine == null || trans.sources.contains(modeInTransitionStateMachine)) {
				return true
			}
		}
		
		return false
	}
}