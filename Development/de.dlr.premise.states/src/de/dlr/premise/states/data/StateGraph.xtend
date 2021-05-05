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

import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.SetMultimap
import de.dlr.premise.element.StateMachine
import de.dlr.premise.element.Transition
import java.util.Set

import static extension de.dlr.premise.states.util.StateCheckingHelper.*
import static extension de.dlr.premise.util.PremiseHelper.*

class StateGraph {
	SetMultimap<State, State> sourceToTargets = HashMultimap.create
	SetMultimap<State, State> targetToSources = HashMultimap.create
	
	SetMultimap<State, State> extendedToBase = HashMultimap.create
	
	SetMultimap<Pair<State, State>, Transition> transitions = HashMultimap.create
	
	Set<State> markedReachableStates = newHashSet
	Set<State> markedUnreachableStates = newHashSet
	
	SetMultimap<StateMachine, State> stateIndex = HashMultimap.create
		
	def synchronized add(State source, State baseTarget, Transition transition) {
		val target = State.createNewStateByReplacing(source, baseTarget)
		val changed = internalAdd(source -> target, transition)
		
		if (changed && baseTarget != target) {
			extendedToBase.put(target, baseTarget)
			copySubMachine(baseTarget, target)
		}
	}
	
	def synchronized removeStateMachine(StateMachine machine) {
		stateIndex.get(machine)
			.flatMap[closure[extendedToBase.get(it) + sourceToTargets.get(it) + targetToSources.get(it)]]
			.toList
			.forEach[internalRemove(it)]
		stateIndex.removeAll(machine)
	}
		
	def synchronized markStateAndSuccessorsReachable(State state) {
		addStateToIndex(state)
		val successors = state.closure[sourceToTargets.get(it)]
		markedReachableStates.addAll(successors)
	}
	
	def synchronized markStateAndPredecessorsUnreachable(State state) {
		addStateToIndex(state)
		val predecessors = state.closure[targetToSources.get(it)]
		markedUnreachableStates.addAll(predecessors)
	}
	
	def synchronized isMarkedReachable(State state) {
		if (markedReachableStates.contains(state)) {
			return true
		}
		if (markedReachableStates.exists[isCompatibleSuperstateOf(state)]) {
			return true
		}
		return false
	}
	
	def synchronized isMarkedUnreachable(State state) {
		if (markedUnreachableStates.contains(state)) {
			return true
		}
		if (markedUnreachableStates.exists[isCompatibleSubstateOf(state)]) {
			return true
		}
		return false
	}
	
	override synchronized toString() '''
		digraph StateGraph {
			«FOR state : markedReachableStates»
				"«state»" [color=green]
			«ENDFOR»
			«FOR state : markedUnreachableStates»
				"«state»" [color=red]
			«ENDFOR»
			«FOR state : allStates.filter[markedReachable]»
				"«state»" [fontcolor=green]
			«ENDFOR»
			«FOR state : allStates.filter[markedUnreachable]»
				"«state»" [fontcolor=red]
			«ENDFOR»
			«FOR it : extendedToBase.entries»
				"«value»" -> "«key»" [style=dashed]
			«ENDFOR»
			«FOR it : transitions.entries»
				"«key.key»" -> "«key.value»" [label="«value.stringRepr»"]
			«ENDFOR»
		}
	'''
	
	def synchronized getAllStates() {
		ImmutableSet.builder
			.addAll(sourceToTargets.asMap.keySet)
			.addAll(targetToSources.asMap.keySet)
			.addAll(markedReachableStates)
			.addAll(markedUnreachableStates)
			.build
	}
	
	def private addStateToIndex(State state) {
		for (stateMachine : state.modeByStateMachine.keySet) {
			stateIndex.put(stateMachine, state)
		}
	}
	
	def private void copySubMachine(State oldSource, State newSource) {
		for (oldTarget : sourceToTargets.get(oldSource)) {
			val newTarget = State.createNewStateByReplacing(newSource, oldTarget)
			extendedToBase.put(newTarget, oldTarget)
			
			val oldPair = oldSource -> oldTarget
			val newPair = newSource -> newTarget
			val oldTransitions = transitions.get(oldPair)
			val newTransitions = transitions.get(newPair)
			if (!newTransitions.empty && newTransitions != oldTransitions) {
				throw new IllegalArgumentException
			}
			val changed = oldTransitions.map[internalAdd(newPair, it)].reduce[r, l | r || l] ?: false
			if (changed) {
				copySubMachine(oldTarget, newTarget)
			}		
		}
	}
	
	def private internalRemove(State state) {
		for (target : sourceToTargets.get(state)) {
			transitions.removeAll(state -> target)
		}
		sourceToTargets.removeAll(state)
		for (source : targetToSources.get(state)) {
			transitions.removeAll(source -> state)
		}
		targetToSources.removeAll(state)
		
		extendedToBase.removeAll(state)
		
		markedReachableStates.remove(state)
		markedUnreachableStates.remove(state)
	}
	
	
	private def boolean internalAdd(Pair<State, State> sourceToTarget, Transition transition) {
		val existingTransitions = transitions.get(sourceToTarget)
		if (existingTransitions.contains(transition)) {
			return false
		}
		
		addStateToIndex(sourceToTarget.key)
		addStateToIndex(sourceToTarget.value)
				
		sourceToTargets.put(sourceToTarget.key, sourceToTarget.value)
		targetToSources.put(sourceToTarget.value, sourceToTarget.key)
		transitions.put(sourceToTarget, transition)
		
		return true
	}
}