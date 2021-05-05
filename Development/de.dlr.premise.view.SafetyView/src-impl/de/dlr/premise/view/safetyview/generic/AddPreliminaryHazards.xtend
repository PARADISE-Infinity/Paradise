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
import de.dlr.premise.element.StateMachine
import de.dlr.premise.safety.impl.my.FailureHelper
import de.dlr.premise.view.safetyview.commands.BuildStateMachineCommand
import org.eclipse.emf.common.command.CompoundCommand
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain
import org.eclipse.emf.edit.domain.EditingDomain
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.swt.widgets.Display

class AddPreliminaryHazards {
	
	private AElement parent;
	
	val hazards = GetDefaultPreliminaryHazardNames.defaultHazards 
	val hazDescription = GetDefaultPreliminaryHazardNames.defaultDescription 
	
	var EditingDomain editingDomain
	
	new (AElement parent){
		this.parent = parent;
		 editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(parent);
	}
	
	public def void start() {
	
		// check if the preliminary hazards are added already
		var elemHazards = parent.statemachines.filter[sm | FailureHelper.isHazard(sm)].toList

		// create a command that will hold all sub commands and executes as one
		var compound = new CompoundCommand
		
		// get harmfulEvent in case no event exists a generic Loss of ... will be created
		var harmfulEvent = elemHazards.findFirst[sm | FailureHelper.isHarmfulHazard(sm as StateMachine)] as StateMachine
		if (harmfulEvent == null) {
			val failureMode = "Loss"
			val desc = failureMode + " of " + parent.name
			
			// the command needs to be executed 
			var buildstateMachine = new BuildStateMachineCommand(parent, null, failureMode, desc)
			editingDomain.getCommandStack().execute(buildstateMachine);
			
			// get the hazard because this is the new top level event and update hazard list
			harmfulEvent = buildstateMachine.hazard
			elemHazards = parent.statemachines.filter[sm | FailureHelper.isHazard(sm)].toList 	
		}
		
		// create number of required state machines to assess all common failures
		for (var i=0; i < hazards.length; i++) {

			// get the current normalized hazard name
			val failureMode = hazards.get(i)
			var name = FailureHelper.getFailureName(parent, failureMode)
			
			// check preliminary hazard names
			var included = false
			for (var j=0; j < elemHazards.length && included != true; j++) {

				// check if the normalized preliminary hazard name is given				
				var curName = (elemHazards.get(j) as StateMachine).name.toLowerCase							
				if (name.toLowerCase.equals(curName) == true) {
					included = true
				}
			}

			// include only new hazards
			if (!included) {
				// add preliminary description
				var desc = hazDescription.get(i).replace(GetDefaultPreliminaryHazardNames.FUNCTION_NAME, parent.name)
				compound.appendIfCanExecute(new BuildStateMachineCommand(parent, harmfulEvent, failureMode, desc))
			}
		}
				
		editingDomain.getCommandStack().execute(compound);
	}
	
	
	/**
	 * This method displays a warning, informing the user that automatic model tree generation can only
	 * occur once.
	 */
	def protected void displayWarning(){
		var display = Display.current
		var shell = display.activeShell
		MessageDialog.openWarning(shell, "Warning", "A default fault tree can only be generated once.")
	}
}