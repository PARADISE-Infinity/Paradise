/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.handler

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.commands.ExecutionException
import org.eclipse.emf.edit.domain.IEditingDomainProvider
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.ui.PlatformUI
import de.dlr.premise.system.SystemComponent

import static extension org.eclipse.ui.handlers.HandlerUtil.*
import org.eclipse.emf.edit.command.ChangeCommand

class ExtractComponentDefinitionHandler extends AbstractHandler {
	override execute(ExecutionEvent event) throws ExecutionException {
		val selection = event.currentSelectionChecked

		val sysComp = (selection as StructuredSelection).firstElement as SystemComponent
			
		val extractor = new ComponentDefinitionExtractor(sysComp)
		
		val error = extractor.prepare()
		if (error != null) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				error.title, error.message)
			return null
		}

		val activeEditor = event.activeEditor

		if (activeEditor instanceof IEditingDomainProvider) {
			val editingDomain = activeEditor.editingDomain
			editingDomain.commandStack.execute(new ChangeCommand(sysComp.eResource.resourceSet) {
				override protected doExecute() {
					extractor.execute()
				}
			});
		} else {
			System.err.println("Couldn't execute command!")
		}

		null
	}
	
}