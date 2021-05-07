/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.states.stability

import com.google.common.collect.Sets
import de.dlr.premise.element.Transition
import de.dlr.premise.element.TransitionActivationBehavior
import de.dlr.premise.states.data.State
import java.util.ArrayDeque
import java.util.HashSet

import static extension de.dlr.premise.states.util.StateCheckingHelper.*
import static extension de.dlr.premise.util.PremiseHelper.*

class StableSubstatesGenerator {
	
	/**
	 * Finds all substates of a given state which are stable.
	 * 
	 * A state is stable if the system can stay in that state for an amount of time, that is the state must not be left 
	 * immediately. 
	 * 
	 * A state is not stable if there exists a transition which has one of the states modes as source, can activate 
	 * according to its condition and must activate according to its behavior.
	 * 
	 * Stable substates are found by starting with the input state and successively adding more modes until the state 
	 * is stable.
	 */
	def getStableSubstates(State state) {
		val confirmedStable = new HashSet()
		
		var candidates = new ArrayDeque()
		val seenStates = new HashSet()
		candidates.add(state)
		seenStates.add(state)
		
		while (!candidates.empty) {
			val candidate = candidates.poll()
			val derivedCandidates = candidate.stableSubstateCandidates
			// a candidate is stable if creating its stable substate candidates yields only the candidate itself
			if (derivedCandidates.size == 1 && derivedCandidates.head == candidate) {
				confirmedStable.add(candidate)
			} else {
				val newCandidates = Sets.difference(derivedCandidates, seenStates)
				candidates.addAll(newCandidates)
				seenStates.addAll(newCandidates)
			}
		}
		
		return confirmedStable
	}
	
	def private getStableSubstateCandidates(State state) {
		val transitions = state.modes.flatMap[outgoingTransitions].toSet
		
		var Iterable<State> stableCandidates = #[state]
		for (transition : transitions) {
			stableCandidates = stableCandidates.flatMap[getStableStubstatesForTransition(transition)]
		}
		
		return stableCandidates.toSet
	}
	
	/**
	 * Finds all substates of a given state for which a given transition will not change the state.
	 * 
	 * This means that for all those retuned states the transition will either not activate at all, or not change the 
	 * state when activating (that is, the transition may loop a mode onto itself).
	 * 
	 * The stability depends on the activation behavior of the transition (see 
	 * {@link de.dlr.premise.element.Transition#getBehavior()}). It is considered as follows:
	 * 
	 * <ul>
	 * 	<li>a) behavior == EXTERNAL: Transition can activate at any time after its condition is met
	 *  <li>b) behavior == IMMEDIATE: Transition must activate immediately after its condition is met
	 *  <li>c) behavior == DEFAULT && condition == null: Like EXTERNAL
	 *  <li>d) behavior == DEFAULT && condition != null: Like IMMEDIATE
	 * </ul>
	 */
	def private getStableStubstatesForTransition(State state, Transition transition) {
		// Loop transition: This is stable, as the state after the transition is the same as the state before the transition
		if (state.modes.contains(transition.target)) {
			return #{state}
		}
		
		// a): Transitions with behavior "external" are always stable
		if (transition.behavior == TransitionActivationBehavior.EXTERNAL) {
			return #{state}
		} 
		
		// b) and c): If condition is null, it is always true
		if (transition.condition == null) {
			if (transition.behavior == TransitionActivationBehavior.IMMEDIATE) {
				// b): For IMMEDIATE, we will always leave the state via the transition, so no stable substates
				return #{}
			} else {	
				// c): Behaves like external, so state itself is stable
				return #{state}
			}
		}
		
		// b) and d) Transition must activate immediately after its condition is met
		
		// Get states of conditions of transition
		val conditionStates = transition.condition.possibleStates.simplifiedStateSet
		
		// Create all substates of state, for which none of the conditions are true
		var Iterable<State> stableCandidates = #[state]
		for (conditionState : conditionStates) {
			stableCandidates = stableCandidates.flatMap[stableCandidate |
				val combination = State.createNewStateByCombiningOrNull(stableCandidate, conditionState)
				if (combination == null) {
					// condition isn't true for the candiate itself, so it can stay
					return #{stableCandidate}
				}
				// condition is true for at least some of the substates, find all others
				val modesOnlyInConditionState = Sets.difference(conditionState.modes, stableCandidate.modes)
				if (modesOnlyInConditionState.size == 0) {
					return #{}
				}
				val allOthers = new State(modesOnlyInConditionState).createNegatedStates
				allOthers.map[State.createNewStateByCombiningOrNull(it, stableCandidate)]
			]
		}
		
		return stableCandidates
	}
	
}