/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.states.util

import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import de.dlr.premise.element.AGuardCondition
import de.dlr.premise.element.GuardCombination
import de.dlr.premise.element.Mode
import de.dlr.premise.element.ModeGuard
import de.dlr.premise.element.StateMachine
import de.dlr.premise.element.Transition
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.states.data.State
import java.util.List
import java.util.Set

import static extension com.google.common.collect.Maps.toMap
import static extension de.dlr.premise.util.PremiseHelper.*

import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.*


class StateCheckingHelper {
	def static stringRepr(Transition transition) {
		'''«(transition.eContainer as ANameItem).scopedName»=«transition.source?.name ?: "ALL"»->«transition.target.name»'''
	}
	
	def static getScopedName(ANameItem nameItem) {
		if (nameItem.isScopedEObject) {
			(nameItem.scope.map[name] + #[nameItem.name]).join('.')
		} else {
			nameItem.name
		}
	}
	
	def static getSources(Transition trans) {
		if (trans.source == null) {
			trans.stateMachine.modes.toSet
		} else {
			#{trans.source}
		}
	}
	
	def static getStateMachine(Mode mode) {
		mode.eContainer as StateMachine
	}
	
	def static getStateMachine(Transition transition) {
		transition.eContainer as StateMachine
	}
	
	def static Iterable<Transition> getIncomingTransitions(Mode mode) {
		(mode.eContainer as StateMachine).transitions.filter[target == mode]
	}
		
	def static Iterable<Transition> getOutgoingTransitions(Mode mode) {
		(mode.eContainer as StateMachine).transitions.filter[sources.contains(mode)]
	}
	
	/**
	 * Gets all the states which satisfy a AGuardCondition.
	 * 
	 * Formally this set can be interpreted as the disjunctive normal form of the logical formula represented by the 
	 * guard condition. 
	 * 
	 * Note that negation is expressed as the complement of modes of the state machine
	 * 
	 * Examples: 
	 * 
	 * <pre>
	 * StateMachine1 = {A, B, C}
	 * StateMachine2 = {1, 2}
	 * 
	 * GuardCondition1 = (A or B) and (1 or 2)
	 * possibleStates(GuardCondition1) = {(A, 1), (A, 2), (B, 1), (B, 2)}
	 * 
	 * GuardCondition2 = not A
	 * possibleStates(GuardCondition2) = {(B), (C)}
	 * <pre>
	 */
	def static Set<State> getPossibleStates(AGuardCondition gc) {
		return doGetPossibleStates(gc)
	}
	
	def private static dispatch Set<State> doGetPossibleStates(AGuardCondition gc) {
		throw new UnsupportedOperationException
	}
	
	def private static dispatch Set<State> doGetPossibleStates(Void gc) {
		return #{State.EMPTY_STATE}
	}
	
	def private static dispatch Set<State> doGetPossibleStates(GuardCombination gc) {
		switch (gc.junction) {
			case OR: gc.children.flatMap[possibleStates].toSet.redundanciesFilteredStateSet
			case AND: {
				val childrenPossibleStates = gc.children.map[possibleStates]
				Sets.cartesianProduct(childrenPossibleStates)
					.map[State.createNewStateByCombiningOrNull(it)]
					.filterNull.toSet
					.redundanciesFilteredStateSet
			}
			case NOT: {
				if (gc.children.length != 1) {
					System.err.println("Invalid NOT junction: " + gc)
					ImmutableSet.of()
				} else {
					val childPossibleStates = gc.children.head.possibleStates
					if (childPossibleStates.length == 0) {
						// negation of nothing is everything
						ImmutableSet.of(State.EMPTY_STATE)
					} else {
						childPossibleStates.flatMap[createNegatedStates].toSet.redundanciesFilteredStateSet
					}
				}
			}
			case XOR: {
				val possibleStates = gc.children.toMap[possibleStates]
				val negatedPossibleStates = possibleStates.mapValues[flatMap[createNegatedStates].toSet]
				
				gc.children
					.flatMap[currentChild |
						val states = gc.children.map[
							if (it == currentChild) {
								possibleStates.get(it)
							} else {
								negatedPossibleStates.get(it)
							}
						]
						
						return Sets.cartesianProduct(states).map[State.createNewStateByCombiningOrNull(it)].filterNull
					]
					.toSet
					.redundanciesFilteredStateSet
			}
			case VOTE: {
				val childrenPossibleStates = gc.children.map[possibleStates]
				val threshold = childrenPossibleStates.size / 2 + 1 // ceil
				
				val quorumChildCombinations = childrenPossibleStates.combinations(threshold)
				
				quorumChildCombinations
					.flatMap[combinationStates |
						Sets.cartesianProduct(combinationStates).map[State.createNewStateByCombiningOrNull(it)].filterNull
					]
					.toSet
					.redundanciesFilteredStateSet
			}
			default: {
				System.err.println("Unsupported Junction: " + gc.junction  + " " + gc)
				ImmutableSet.of()
			}
		}
	}

	def private static dispatch Set<State> doGetPossibleStates(ModeGuard mg) {
		if (mg.mode == null) {
			return ImmutableSet.of(State.EMPTY_STATE)
		}
		return ImmutableSet.of(new State(mg.mode))
	}
	
	def static Iterable<State> createNegatedStates(State state) {
		Sets.cartesianProduct(state.modeByStateMachine.keySet.map[modes.toSet].toList).map[modes | new State(modes)].filter[it != state]
	}
	
	/**
	 * Creates a simplified state set by combining states that cover all modes of a state machine.
	 * 
	 * Example: 
	 * 
	 * <pre>
	 * StateMachine1 = {A, B, C}
	 * StateMachine2 = {1, 2}
	 * 
	 * states = {(A, 1), (A, 2), (B, 1), (C, 2)}
	 * simplifiedStateSet(states) = {(A), (B, 1), (C, 2)}
	 * <pre>
	 */
	def static getSimplifiedStateSet(Set<State> inputStates) {
		val stateMachines = inputStates.flatMap[modeByStateMachine.keySet].toSet
		val states = newHashSet()
		states += inputStates
		for (stateMachine : stateMachines) {
			states
				.groupBy[State.createNewStateOrNull(modes.filter[mode | mode.stateMachine != stateMachine]) ?: State.EMPTY_STATE]
				.filter[key, group | stateMachine.modes.forall[mode | group.exists[modes.contains(mode)]]]
				.forEach[k, v| 
					states.removeAll(v)
					states.add(k)
				]
		}
		return states
	}
		
	/**
	 * Removes redundancies from a state set by removing states that are substates of other states
	 * 
	 * Example: 
	 * 
	 * <pre>
	 * StateMachine1 = {A, B, C}
	 * StateMachine2 = {1, 2}
	 * 
	 * states = {(A, 1), (A), (1), (C, 2)}
	 * redundanciesFilteredStateSet(states) = {(A), (1), (C, 2)}
	 * <pre>
	 */
	def static getRedundanciesFilteredStateSet(Set<State> inputStates) {
		inputStates.filter[state | !inputStates.exists[state.isCompatibleSuperstateOf(it) && it != state]].toSet
	}
	
	def private static <T> Set<List<T>> combinations(List<T> elements, int combinationSize) {
		val results = newHashSet
		
		// indexes of elements to include in last combinations
		val lastIndexes = ((elements.size-combinationSize) ..< elements.size).toList
		
		// current indexes of elements to include in combination
		val indexes = (0 ..< combinationSize).toList
		
		results.add(elements.filterByIndex(indexes))
		
		while (indexes != lastIndexes) {
			// find biggest index that can be incremented
			val k = (0..<combinationSize).toList.reverseView.findFirst[indexes.get(it) < lastIndexes.get(it)]
			// value of that index
			val indexK = indexes.get(k)
			// increment it and all following by one
			for (i : 0 ..< (combinationSize-k)) {
				indexes.set(k+i, indexK+i+1)
			}
		
			results.add(elements.filterByIndex(indexes))
		}
		
		return results
	}
	
	def private static <T> List<T> filterByIndex(List<T> elements, List<Integer> indexes) {
		newArrayList(indexes.map[elements.get(it)])
	} 
}