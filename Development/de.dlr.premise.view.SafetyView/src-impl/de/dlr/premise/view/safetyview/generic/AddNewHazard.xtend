/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.safetyview.generic

import de.dlr.premise.element.AElement
import de.dlr.premise.element.StateMachine
import de.dlr.premise.safety.impl.my.FailureHelper
import de.dlr.premise.view.safetyview.helper.SafetyViewHelper
import org.eclipse.emf.common.command.CompoundCommand
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain
import org.eclipse.emf.edit.domain.EditingDomain
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.swt.widgets.Display
import org.eclipse.jface.window.Window

class AddNewHazard {
	
	private AElement parent

	var shell = Display.current.activeShell

	var hazardName = "Failure";
	var description = "Add a description ..."
	var StateMachine harmfulEvent = null;
	
	new (AElement parent){
		this.parent = parent
	}
	
	public def void start() {

		// rename failure mode and state machine name if required
		var hazards = parent.eContents.filter[sm | sm instanceof StateMachine && FailureHelper.isHazard(sm as StateMachine)].toList	
		
		if (hazards.length > 0) {
			var hazardDlg = new HazardDialog(shell, parent);
			hazardDlg.create();
			if (hazardDlg.open() == Window.CANCEL) {
				return
			}
			
			hazardName = hazardDlg.name
			description = hazardDlg.description
			harmfulEvent = hazardDlg.harmFulEvent
		}
		
		// create a command that will hold all sub commands and executes as one
		var compound = new CompoundCommand
		
		// create a new default state machine
		var addStateMachine = SafetyViewHelper.buildStateMachine(parent, harmfulEvent, hazardName, description)
		compound.appendIfCanExecute(addStateMachine)

		// execute the command
		var EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(parent)
		editingDomain.getCommandStack().execute(compound)
	}
	
	/**
	 * This method displays a warning, informing the user that automatic model tree generation can only
	 * occur once.
	 */
	static def void displayWarning() {
		var display = Display.current
		var shell = display.activeShell
		MessageDialog.openWarning(shell, "Warning", "Hazards can only be added to functions or components.")
	}	
}
