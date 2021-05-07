/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.safetyview.generic

import de.dlr.premise.element.AElement
import de.dlr.premise.element.Connection
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.element.GuardCombination
import de.dlr.premise.element.ModeGuard
import de.dlr.premise.element.StateMachine
import de.dlr.premise.registry.Junction
import java.util.Set
import org.eclipse.emf.edit.command.ChangeCommand
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain
import org.eclipse.emf.edit.domain.EditingDomain

import static extension de.dlr.premise.util.PremiseHelper.flatMap
import static extension de.dlr.premise.safety.impl.my.FailureHelper.*

class GenericFaultGeneration {

	var AElement parent

	new (AElement parent) {
		this.parent = parent		
	}

	def public void start() {

		var cchange = new ChangeCommand(parent) {

			override doExecute() {

				// collect all external failure modes get harmful hazards linked by a connection				
				val connections = parent.eResource.resourceSet.resources.flatMap[contents].iterator.flatMap[eAllContents].filter(Connection).toSet as Set<?> as Set<Connection<?>>
				var elem = connections.filter[target == parent].map[source].toSet
				var connected = elem.flatMap[statemachines].filter[harmfulHazard]
				var eEvents = connected.map[modes.findFirst[isFailureMode]].toSet

				// get connected Elements by bidirectional connections
				var conElem = connections.filter[source == parent && bidirectional].map[target].toSet
				var cEvents = conElem.flatMap[statemachines].filter[harmfulHazard].map[modes.findFirst[isFailureMode]].toSet

				// add failure modes of bidirectional connections 
				eEvents.addAll(cEvents)

				// get and check the harmful event of the component
				var harmfulEvent = parent.statemachines.findFirst[sm | isHarmfulHazard(sm)]
				if (harmfulEvent == null) {
					return
				}

				// get the harmful event and get the guard condition for adding new internal and external hazards 
				var transition = harmfulEvent.transitions.get(0)
				var GuardCombination gc
				var gcCreated = false
				if (transition.condition == null) {
					gc = ElementFactory.eINSTANCE.createGuardCombination
					gc.junction = Junction.OR
					gcCreated = true
				} else {
					if (transition.condition instanceof GuardCombination)
					gc = transition.condition as GuardCombination			 
				}
		
				// consider internal external events
				for (harmfulHazard : eEvents) {

					// create mode guards
					var modeGuard = ElementFactory.eINSTANCE.createModeGuard
					modeGuard.mode = harmfulHazard
						
					// add only new stuff
					if (gc.children.findFirst[g | (g instanceof ModeGuard) && (g as ModeGuard).mode == harmfulHazard] == null) {
						gc.children.add(modeGuard)
					}
				}

				// consider all internal events
				var internalEvents = parent.statemachines.filter[sm | isHazard(sm) && (isHarmfulHazard(sm) == false)].toSet
				for (stateMachine : internalEvents) {
					// create mode guards
					val extMode = getFailureMode(stateMachine as StateMachine)
					var modeGuard = ElementFactory.eINSTANCE.createModeGuard
					modeGuard.mode = extMode
					
					// add only new stuff
					if (gc.children.findFirst[g | (g instanceof ModeGuard) && (g as ModeGuard).mode == extMode] == null)
						gc.children.add(modeGuard)
				}
				
				// add created guard condition only if the external and internal events exists
				if (gcCreated) {
					if (eEvents.size > 0 || internalEvents.size > 0) {
						transition.condition = gc						
					}				
				}
			}			
		}

		// execute commands
		var EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(parent)
		editingDomain.getCommandStack().execute(cchange)
	}
}
