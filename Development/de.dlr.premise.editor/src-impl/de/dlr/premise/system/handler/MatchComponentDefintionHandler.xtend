/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.handler

import de.dlr.premise.component.ChildComponentDefinition
import de.dlr.premise.component.ComponentDefinition
import de.dlr.premise.component.ComponentFactory
import de.dlr.premise.component.IModeSatisfieable
import de.dlr.premise.component.IParameterSatisfieable
import de.dlr.premise.component.ISystemComponentSatisfieable
import de.dlr.premise.component.ModeDefinition
import de.dlr.premise.component.ParameterDefinition
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.element.Mode
import de.dlr.premise.provider.util.PremiseAdapterFactoryHelper
import de.dlr.premise.registry.RegistryFactory
import de.dlr.premise.system.Parameter
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.system.SystemFactory
import de.dlr.premise.system.presentation.SystemEditor
import java.util.Set
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.commands.ExecutionException
import org.eclipse.emf.edit.command.ChangeCommand
import org.eclipse.emf.edit.domain.IEditingDomainProvider
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.ui.dialogs.ElementListSelectionDialog

import static extension org.eclipse.ui.handlers.HandlerUtil.*

class MatchComponentDefintionHandler  extends AbstractHandler {
	override execute(ExecutionEvent event) throws ExecutionException {
		val selection = event.currentSelectionChecked
		
		val sysComp = (selection as StructuredSelection).firstElement as SystemComponent
		
		
		val satisfied = sysComp.satisfiedSatisfieables.filter(ComponentDefinition).toSet
		val notFullySatisfied = (satisfied).filter[!sysComp.getUnsatisfiedSatisfieablesFor(it).empty]
			
		val labelProvider = new AdapterFactoryLabelProvider(PremiseAdapterFactoryHelper.createComposedAdapterFactory())
		
		val definition = if (notFullySatisfied.isEmpty) {
			val dialog = new ElementListSelectionDialog(event.activeEditor.site.shell, labelProvider)
			dialog.setTitle("Select ComponentDefinition")
			dialog.setMessage("Select ComponentDefinition to add to the SystemComponent:")
			dialog.setElements(sysComp.eResource.resourceSet.allContents.filter(ComponentDefinition).filter[!satisfied.contains(it)].toList)
			dialog.open()
			dialog.firstResult as ComponentDefinition
		} else if (notFullySatisfied.length == 1) {
			val confirmation = MessageDialog.openConfirm(
			event.activeEditor.site.shell, 
			"Confirm ComponentDefinition",
            '''Add children to match "«labelProvider.getText(notFullySatisfied.head)»"?'''
           )
           if (confirmation) {
			notFullySatisfied.head
           }
		} else {
			val dialog = new ElementListSelectionDialog(event.activeEditor.site.shell, labelProvider)
			dialog.setTitle("Select ComponentDefinition")
			dialog.setMessage("Select ComponentDefinition for which to create children:")
			dialog.setElements(notFullySatisfied)
			dialog.open()
			dialog.firstResult as ComponentDefinition
		}
		
		if (definition == null) {
			return null
		}
		

		val activeEditor = event.activeEditor

		if (activeEditor instanceof IEditingDomainProvider) {
			val editingDomain = activeEditor.editingDomain
			editingDomain.commandStack.execute(new ChangeCommand(sysComp.eResource.resourceSet) {
				override protected doExecute() {
					val newChildren = createChildrenForUnsatisfiedSatisfieables(sysComp, definition)
					if (activeEditor instanceof SystemEditor) {
						activeEditor.selectionToViewer = newChildren
					}
				}
			});
		} else {
			System.err.println("Couldn't execute command!")
		}
		
		null
	}
	
	def private createChildrenForUnsatisfiedSatisfieables(SystemComponent sysComp, ComponentDefinition definition) {
		val newChildren = newArrayList()
		for (unsat : sysComp.getUnsatisfiedSatisfieablesFor(definition).toSet as Set<?> as Set<Object>) {
			switch (unsat) {
				ComponentDefinition: {
					val newSatisfies = ComponentFactory.eINSTANCE.<SystemComponent, ISystemComponentSatisfieable>createSatisfies => [
						target = unsat
					]
					newChildren.add(newSatisfies)
					sysComp.satisfies.add(newSatisfies)
				}
				ChildComponentDefinition: {
					val newChild = SystemFactory.eINSTANCE.createSystemComponent => [
						name = unsat.name
						it.description = unsat.description
						satisfies.add(ComponentFactory.eINSTANCE.<SystemComponent, ISystemComponentSatisfieable>createSatisfies => [
							target = unsat
						])
					]
					newChildren.add(newChild)
					sysComp.children.add(newChild)
				}								
				ParameterDefinition: {
					val newParameter = SystemFactory.eINSTANCE.createParameter => [
						name = unsat.name
						value = RegistryFactory.eINSTANCE.createValue
						it.unit = unsat.unit
						it.description = unsat.description
						satisfies.add(ComponentFactory.eINSTANCE.<Parameter, IParameterSatisfieable>createSatisfies => [
							target = unsat
						])
					]
					newChildren.add(newParameter)
					sysComp.parameters.add(newParameter)
				}
				ModeDefinition: {
					var sm = sysComp.statemachines.head
					if (sm == null) {
						sm =  ElementFactory.eINSTANCE.createStateMachine => [
							name = sysComp.name
						]
						newChildren.add(sm)
						sysComp.statemachines.add(sm)
					} 					
					
					val newMode = ElementFactory.eINSTANCE.createMode => [
						name = unsat.name
						description = unsat.description
						satisfies.add(ComponentFactory.eINSTANCE.<Mode, IModeSatisfieable>createSatisfies => [
							target = unsat
						])
					]
					newChildren.add(newMode)
					sm.modes.add(newMode)
				}
			}
		}
		return newChildren
	}
	
}