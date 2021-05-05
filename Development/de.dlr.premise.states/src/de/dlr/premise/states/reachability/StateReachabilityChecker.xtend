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

import com.google.common.collect.Sets
import de.dlr.premise.element.Mode
import de.dlr.premise.element.Transition
import de.dlr.premise.states.data.State
import java.util.HashSet
import java.util.PriorityQueue
import java.util.Set
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static extension de.dlr.premise.states.util.StateCheckingHelper.*
import static extension de.dlr.premise.util.PremiseHelper.*

/**
 * Checks whether a given state is reachable in a model of interconnected state machines.
 * 
 * Checking is done by backwards search from the given state, trying to find an entry state (a state where all modes 
 * are entry modes) or a state already known to be reachable.
 */
@FinalFieldsConstructor class StateReachabilityChecker {
	@Data
	static class BackwardStep implements Comparable<BackwardStep> {
		def static createNewBackwardStepOrNull(State transitionTargetState, Transition transition, Mode transitionSourceMode) {
			try {
				new BackwardStep(transitionTargetState, transition, transitionSourceMode)
			} catch (IllegalArgumentException e) {
				// ignore invalid backward step
				null
			}
		}
		
		val State transitionTargetState
		val Transition transition
		val Mode transitionSourceMode
		
		new(State transitionTargetState, Transition transition, Mode transitionSourceMode) {
			if (transition.stateMachine != transitionSourceMode.stateMachine 
				|| (transition.source != null && transitionSourceMode != transition.source)
				|| !transitionTargetState.modes.contains(transition.target)
			) {
				throw new IllegalArgumentException
			}
				
			this.transitionTargetState = transitionTargetState
			this.transition = transition
			this.transitionSourceMode = transitionSourceMode
		}
		
		override compareTo(BackwardStep other) {
			return Double.compare(this.rating, other.rating)
		}
		
		def getRating() {
			val notEntryModeCount = transitionTargetState.modes.filter[!entryMode].length
			
			if (transition.target != null && transition.target.entryMode) {
				notEntryModeCount + 0.25
			} else if (transitionSourceMode.entryMode) {
				notEntryModeCount - 0.25
			} else {
				notEntryModeCount
			}
		}
		
		override toString() '''
			BackwardStep [«transitionTargetState», «transition.stringRepr», «transitionSourceMode.name», rating=«rating»]
		'''
	}
	
	val extension IStateReachabilityCheckerDelegate validatorDelegate
	
	def boolean isReachable(State state) {
		if (state.directlyReachable) {
			return true
		}
		if (state.knownUnreachable) {
			return false
		}
				
		val steps = state.backwardSteps
		
		// we keep track of all steps we have seen before, to avoid considering a step twice
		val Set<BackwardStep> seenSteps = newHashSet(steps)
		// backwards steps are ordered, so states most likely to lead to an entry state are processed first
		val stepQueue = new PriorityQueue<BackwardStep>()
		stepQueue.addAll(steps)
		
		while (!stepQueue.empty) {
			val currentStep = stepQueue.poll()
			val currentStepPreStates = currentStep.preStates
			newPreStates(currentStep, currentStepPreStates)
			
			// if one of the prestates of the backwards steps if reacheable, the given states is reachable
			val entryState = currentStepPreStates.findFirst[directlyReachable]
			if (entryState != null) {
				markStateReachable(entryState)
				return true
			}
			
			// filter out prestates that are known to be invalid
			val notMarkedInvalidCurrentStepPreStates = currentStepPreStates.filter[!knownUnreachable]
			
			// create the new steps
			val currentStepPreSteps = notMarkedInvalidCurrentStepPreStates.flatMap[backwardSteps].toSet
			val newCurrentStepPreSteps = Sets.difference(currentStepPreSteps, seenSteps)
			stepQueue.addAll(newCurrentStepPreSteps)
			seenSteps.addAll(newCurrentStepPreSteps)
			
			
			if (stepQueue.size > 15000) {
				System.err.println("Couldn't show reachability for state " + state)
				return false
			}
		}
		
		markStateUnreachable(state)
		return false
	}
	
	private def isDirectlyReachable(State state) {
		state.entry || state.knownReachable
	}
	
	private def Set<BackwardStep> getBackwardSteps(State state) {
		state.modes
			.flatMap[incomingTransitions]
			.flatMap[trans | trans.sources.map[BackwardStep.createNewBackwardStepOrNull(state, trans, it)].filterNull]
			.toSet
	}
	
	/**
	 * Gets all states for which the transition of a backwards step can activate
	 */
	private def Set<State> getPreStates(BackwardStep it) {
		// replace the transition target mode with the source mode
		val newStateModes = new HashSet(transitionTargetState.modes)
		newStateModes.remove(transition.target)
		newStateModes.add(transitionSourceMode)
		val basePreState = new State(newStateModes)

		// combine with the conditions under which the transition can activate
		val possibleStates = transition.condition?.possibleStates
		if (possibleStates == null) {
			#{basePreState}
		} else {
			possibleStates.map[State.createNewStateByCombiningOrNull(it, basePreState)].filterNull.toSet
		}
	}
}