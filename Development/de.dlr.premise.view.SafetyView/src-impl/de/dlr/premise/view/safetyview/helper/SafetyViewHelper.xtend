/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.safetyview.helper

import de.dlr.premise.element.AElement
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.element.ElementPackage
import de.dlr.premise.element.GuardCombination
import de.dlr.premise.element.Mode
import de.dlr.premise.element.ModeGuard
import de.dlr.premise.element.StateMachine
import de.dlr.premise.element.Transition
import de.dlr.premise.safety.SafetyHelper
import de.dlr.premise.functions.UseCase
import de.dlr.premise.functions.UseCaseFactory
import de.dlr.premise.util.BaseRegistryHelper
import de.dlr.premise.util.PremiseHelper
import de.dlr.premise.view.safetyview.commands.BuildStateMachineCommand
import java.util.HashMap
import java.util.List
import java.util.Map
import org.eclipse.emf.common.command.CompoundCommand
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.edit.command.AddCommand
import org.eclipse.emf.edit.command.ChangeCommand
import org.eclipse.emf.edit.command.RemoveCommand
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain

class SafetyViewHelper {
		
	/**
	 * Copy state machine to the selected Element
	 */
	def static public copyStateMachineTo(AElement target, StateMachine sourceSM) {
	
		// create new StateMachine
		var StateMachine sm = ElementFactory.eINSTANCE.createStateMachine
		sm.name = sourceSM.name
		sm.description = sourceSM.description
		sm.metaTypes.addAll(sourceSM.metaTypes)
	
		// add StateMachine
		var feature = ElementPackage.Literals.AELEMENT__STATEMACHINES;
		var editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(target);
	
		var compoundCommand = new CompoundCommand()
		var command = new AddCommand(editingDomain, target, feature, sm);
		compoundCommand.appendAndExecute(command)
	

		// <sourceMode,newMode>
		var Map<Mode, Mode> modemap = new HashMap<Mode, Mode>()

		// Setup Modes
		for (sourceMode : sourceSM.getModes) {
			var oneMode = ElementFactory.eINSTANCE.createMode
			oneMode.name = sourceMode.name
			oneMode.description = sourceMode.description
			oneMode.entryMode = sourceMode.entryMode
	
			modemap.put(sourceMode, oneMode)
	
			// add mode
			feature = ElementPackage.Literals.STATE_MACHINE__MODES;
			editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(sm);
	
			command = new AddCommand(editingDomain, sm, feature, oneMode);
			compoundCommand.appendAndExecute(command)
		}
	
		// Setup Transitions
		for (sourceTransition : sourceSM.getTransitions) {
			var oneTransition = ElementFactory.eINSTANCE.createTransition
			oneTransition.name = sourceTransition.name
			oneTransition.description = sourceTransition.name
			oneTransition.behavior = sourceTransition.behavior
	
			oneTransition.source = modemap.get(sourceTransition.getSource)
			oneTransition.target = modemap.get(sourceTransition.getTarget)
	
			// add Transition
			feature = ElementPackage.Literals.STATE_MACHINE__TRANSITIONS;
			editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(sm);
	
			command = new AddCommand(editingDomain, sm, feature, oneTransition);
			compoundCommand.appendAndExecute(command)
		}
		
		if (compoundCommand.canExecute()) {
			editingDomain.getCommandStack().execute(compoundCommand);
		}
	}
	

	/**
	 * Delete the given state machine from the model
	 */
	def static public deleteStateMachine(StateMachine sm) {

		var stateMachine = sm as StateMachine
		var feature = ElementPackage.Literals.AELEMENT__STATEMACHINES;
		
		val modelRoot = PremiseHelper.getRoot(sm)
		var editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(modelRoot);
		var List<EObject> modeguards = PremiseHelper.getAll(modelRoot, ModeGuard)
		var compoundCommand = new CompoundCommand()
		

		for (mg : modeguards) {
			if (mg instanceof ModeGuard) {
				for (mode : sm.getModes) {
					if (mg.mode != null && mode.id == mg.mode.id) {
						// if a referencing mode guard is found, the parent type has to be identified
						val container = mg.eContainer
						if (container instanceof GuardCombination) {
							val mgFeature = ElementPackage.Literals.GUARD_COMBINATION__CHILDREN

							var removeMGCom = new RemoveCommand(editingDomain, container, mgFeature, mg)
							compoundCommand.appendIfCanExecute(removeMGCom)
						} else if (container instanceof Transition) {
							var changeCom = new ChangeCommand(container) {
								override protected doExecute() {
									container.condition = null
								}
							}
							compoundCommand.appendIfCanExecute(changeCom)
						}
					}
				}
			}
		}
	
		
		var removeCommand = new RemoveCommand(editingDomain, stateMachine.eContainer, feature, stateMachine)
		compoundCommand.appendIfCanExecute(removeCommand)
		
		editingDomain.getCommandStack().execute(compoundCommand);
		
	}
	
	
	
	/**
	 * Generates a new state machine and add this state machine to the given object.
	 */
	def static ChangeCommand buildStateMachine(AElement parent, StateMachine harmfulEvent, String failureName, String description) {
		return new BuildStateMachineCommand(parent, harmfulEvent, failureName, description)
    }


	/** Builds a UseCase representing a safety significant Function */
	def static public buildSSF(EObject parent) {
		
		if (parent instanceof UseCase){
			
			val mtSSF = BaseRegistryHelper.getSSFMetaType(parent.eResource.resourceSet)

			val cchange = new ChangeCommand(parent){
				override protected doExecute() {
					
					// generate a SSF UseCase
					val ssf = UseCaseFactory.eINSTANCE.createUseCase
					ssf.name = "Safety Significant Function"
					ssf.metaTypes.add(mtSSF)
					parent.children.add(ssf)
					
					// generate FHA MetaData
					SafetyHelper.createFHAMetadata(ssf)
				}
			}
			return cchange;
		}
		return null;
	}	
}