/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.handler

import com.google.common.collect.Lists
import java.util.Collection
import java.util.Set
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.commands.ExecutionException
import org.eclipse.emf.common.command.Command
import org.eclipse.emf.common.command.CompoundCommand
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.util.EcoreUtil.CrossReferencer
import org.eclipse.emf.edit.command.RemoveCommand
import org.eclipse.emf.edit.domain.IEditingDomainProvider
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.ui.PlatformUI

import static extension org.eclipse.emf.ecore.util.EcoreUtil.*
import static extension org.eclipse.ui.handlers.HandlerUtil.*

class UnloadResourceHandler extends AbstractHandler {
	
	override execute(ExecutionEvent event) throws ExecutionException {
		val selection = event.currentSelectionChecked as StructuredSelection	
		val activeEditor = event.activeEditor as IEditingDomainProvider
		
		val editingDomain = activeEditor.editingDomain
		val resources = selection.toList.filter(Resource).toList
		
		val referencingResources = ResourcesUsedCrossReferencer.findResources(resources)
		
		if (!referencingResources.empty) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Can't unload", 
				'''
					Resources to be unloaded are referenced by: 
					
					«FOR res : referencingResources»
						- «res.URI»
					«ENDFOR»
				'''
			)
			
			return null
		}

		val commands = Lists.newArrayList(resources.map[new RemoveCommand(editingDomain, resourceSet.resources, it) as Command])
		
		editingDomain.commandStack.execute(new CompoundCommand(commands))

		null
	}
	
	
	static class ResourcesUsedCrossReferencer extends CrossReferencer {
		
		Collection<Resource> resources
		Set<EObject> roots
		
	
		new(Collection<Resource> resources) {
			super(resources.map[resourceSet].toSet)
			
			this.resources = resources
			roots = resources.map[contents].flatten.toSet
		}
	
		override protected boolean containment(EObject eObject) {
			!roots.contains(eObject)
		}

		override protected boolean crossReference(EObject eObject, EReference eReference, EObject crossReferencedEObject) {
			roots.exists[isAncestor(crossReferencedEObject)]  && !roots.exists[isAncestor(eObject)] && !eReference.derived 
		}
		
		def static findResources(Collection<Resource> resources) {
			val result = new ResourcesUsedCrossReferencer(resources)
			result.crossReference()
			result.done()
			
			result.values.flatten.map[EObject.eResource].toSet
		}
	}
}