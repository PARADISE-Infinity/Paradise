/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.states.test

import de.dlr.premise.element.Mode
import de.dlr.premise.element.StateMachine
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.states.data.State
import java.util.Set
import org.eclipse.emf.common.util.EList

class StateTestHelper {
	def static <T extends ANameItem> named(EList<T> list, String searchName) {
		val results = list
			.filter[name == searchName]
			.toList
			
		switch(results.size) {
			case 0: throw new RuntimeException('''No element with name «searchName»''')
			case 1: results.get(0)
			default: throw new RuntimeException('''Multiple elements with name «searchName»''')
		}
	}
	
	def static State createState(Pair<StateMachine, String>... stateSpec) {
		new State(stateSpec.map[p | p.key.getMode(p.value)])
	}
	
	def static Mode getMode(StateMachine sm, String modeName) {
		sm.modes.named(modeName)
	}
	
	def static Set<State> createStates(Set<Pair<StateMachine, String>>... stateSpecs) {
		stateSpecs.map[createState].toSet
	}
}
