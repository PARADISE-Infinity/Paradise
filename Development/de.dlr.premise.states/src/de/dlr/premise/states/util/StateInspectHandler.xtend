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

import de.dlr.premise.element.AGuardCondition
import de.dlr.premise.element.AModeCombination
import de.dlr.premise.element.Mode
import de.dlr.premise.states.data.State
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.commands.ExecutionException
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.jface.dialogs.ProgressMonitorDialog
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.dialogs.ElementListSelectionDialog

import static extension de.dlr.premise.states.StateHelper.*
import static extension org.eclipse.ui.handlers.HandlerUtil.*
import de.dlr.premise.util.scope.ScopedEObjectFactory

class StateInspectHandler extends AbstractHandler {

	override execute(ExecutionEvent event) throws ExecutionException {
		val sel = event.currentSelection

		if (sel instanceof StructuredSelection) {
			val feTemp = sel.firstElement
			val firstElement = if (feTemp instanceof EObject) ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(feTemp) else feTemp

			val options = newHashMap()
			
			val root = switch(firstElement) {
				EObject: firstElement.eResource.resourceSet
				Resource: firstElement.resourceSet
				ResourceSet: firstElement
				default: null
			}
			
			if (root != null) {
				options.put("Show Graph", [
					var sga = root.findOrCreateStateCheckerAdapter
					if (sga != null) {
						if (sga.graph.allStates.size < 500) {
							println(sga.graph)
						} else {
							println("Too big for printing")
						}
					} else {
						println("NO ADAPTER")
					}
				])
				options.put("Get all valid states", [
					val sga = root.findOrCreateStateCheckerAdapter
						
			        new ProgressMonitorDialog(PlatformUI.workbench.activeWorkbenchWindow.shell).run(true, true)[monitor |
						println(sga.getAllValidStates(monitor))
					]
				        
					return null
				])
			}
		
			if (firstElement instanceof AModeCombination) {
				options.put("Validate", [
					val state = new State(firstElement.modes)
			
					var sga = firstElement.eResource.resourceSet.findOrCreateStateCheckerAdapter
					val valid = sga.isValid(state)
				
					println(state)
					println("Valid: " + valid)
					
					return null
				])
				options.put("Stable substates", [
					val state = new State(firstElement.modes)
					var sga = firstElement.eResource.resourceSet.findOrCreateStateCheckerAdapter
					
					println(state)
					println('''Stable substates: «sga.stableSubstatesGenerator.getStableSubstates(state)»''')
					
					return null
				])
			}
			if (firstElement instanceof AGuardCondition) {
				options.put("Get needed prestates", [println(StateCheckingHelper.getPossibleStates(firstElement))])
			}
			if (firstElement instanceof Mode) {
				options.put("Validate mode", [
					val reachable = root.findOrCreateStateCheckerAdapter.isModeReachable(firstElement)
		
					println("Reachable: " + reachable)
					
					return null
				])
			}

			val dialog = new ElementListSelectionDialog(event.activeEditor.site.shell, new LabelProvider())
			dialog.setElements(options.keySet)
			dialog.setTitle("Inspect states")
			dialog.open()

			if (dialog.returnCode != ElementListSelectionDialog.OK) {
				return null
			}

			options.get(dialog.firstResult).apply(null)
			println()
			
			return null
		}
	}
}
