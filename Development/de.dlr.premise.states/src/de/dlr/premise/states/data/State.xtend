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

import com.google.common.collect.ImmutableBiMap
import de.dlr.premise.element.Mode
import de.dlr.premise.element.StateMachine
import java.util.Map
import org.eclipse.xtend.lib.annotations.Data

import static extension com.google.common.collect.Maps.uniqueIndex
import static extension de.dlr.premise.states.util.StateCheckingHelper.*

@Data class State {
	val public static EMPTY_STATE = new State()
	
	ImmutableBiMap<StateMachine, Mode> modeByStateMachine
	
	def static State createNewStateOrNull(Mode... modes) {
		createNewStateOrNull(modes.toList)
	}
	
	def static State createNewStateOrNull(Iterable<Mode> modes) {
		try {
			new State(modes)
		} catch (InvalidStateException e) {
			// ignore invalid states
			null
		}
	}
	
	def static State createNewStateByCombiningOrNull(State... states) {
		createNewStateByCombiningOrNull(states.toList)
	}
	
	def static State createNewStateByCombiningOrNull(Iterable<State> states) {
		try {
			new State(states.map[modes].flatten)
		} catch (InvalidStateException e) {
			// ignore invalid states
			null
		}
	}
		
	def static State createNewStateByReplacing(State... states) {
		createNewStateByReplacing(states.toList)
	}
	
	def static State createNewStateByReplacing(Iterable<State> states) {
		val Map<StateMachine, Mode> modeByStateMachine = newHashMap
		for (State s : states) {
			modeByStateMachine.putAll(s.modeByStateMachine)
		}
		return new State(modeByStateMachine.values)
	}
	
	new(Mode... modes) {
		this(modes.toList)
	}
	
	new(Iterable<Mode> modes) {
		if (modes.isEmpty) {
			throw new InvalidStateException("Must specify at least one mode!")
		}
		try {
			modeByStateMachine = ImmutableBiMap.copyOf(modes.toSet.uniqueIndex[stateMachine])
		} catch (IllegalArgumentException e) {
			throw new InvalidStateException("Modes must be from distinct state machines", e)
		} catch (NullPointerException e) {
			throw new InvalidStateException("Modes must belong to a state machine", e)
		}
	}
	
	private new() {
		modeByStateMachine = ImmutableBiMap.of()
	}
	
	def getModes() {
		modeByStateMachine.values
	}
	
	def isEntry() {
		modes.forall[entryMode]
	}
	
	def isCompatibleSubstateOf(State other) {
		other.modes.containsAll(this.modes)
	}
	
	def isCompatibleSuperstateOf(State other) {
		other.isCompatibleSubstateOf(this)
	}
	
	override toString() '''
		State [«modeByStateMachine.entrySet.sortBy[key.scopedName].map['''«key.scopedName»=«value.name»'''].join(", ")»]'''
		
}