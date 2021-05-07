/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.json.structure

import com.google.gson.JsonSyntaxException
import de.dlr.exchange.json.structure.json.GsonFactory
import de.dlr.exchange.json.structure.model.JsonExport
import java.io.InputStreamReader
import java.util.Set
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.commands.ExecutionException
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Path
import org.eclipse.emf.common.command.AbortExecutionException
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.edit.command.ChangeCommand
import org.eclipse.emf.edit.domain.IEditingDomainProvider
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.ui.IEditorPart
import org.eclipse.ui.part.FileEditorInput

import static extension org.eclipse.ui.handlers.HandlerUtil.*
import org.eclipse.core.runtime.NullProgressMonitor

class JsonMergeHandler extends AbstractHandler {
	
	override execute(ExecutionEvent event) throws ExecutionException {
		val file = (event.currentSelectionChecked as StructuredSelection).firstElement as IFile
		
		if (!file.isSynchronized(IResource.DEPTH_ZERO)) {
			file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor)
		}
		
		
		val gson = GsonFactory.createGson()
		
		val jsonRoot = try {
			gson.fromJson(new InputStreamReader(file.contents), JsonExport)
		} catch (JsonSyntaxException e) {
			MessageDialog.openError(
				event.activeWorkbenchWindow.shell,
				"Error while reading JSON", 
				'''
					Failed to parse JSON file:
					«if (e.cause != null) e.cause.message else e.message»
				'''
			)
			return null
		}
		
		val uris = jsonRoot.children.map[uri].map[trimFragment].filterNull.toSet
		
		val editor = getEditor(event, uris)
		
		val resSet = editor.editingDomain.resourceSet
		
		val merge = new JsonIntoPremiseMerge(resSet, jsonRoot)
		
		try {
			merge.prepare()
		} catch (JsonIntoPremiseMerge.MergeException e) {
			MessageDialog.openError(
				event.activeWorkbenchWindow.shell,
				"Error while reading JSON", 
				'''
					Failed to parse JSON file:
					«e.message»
				'''
			)
			return null
		}
		
		editor.editingDomain.commandStack.execute(new ChangeCommand(resSet) {
			override protected doExecute() {
				merge.run()
				
				if (!merge.changed) {
					throw new AbortExecutionException()
				}
			}
		})
		
		event.activePart.site.page.activate(editor as IEditorPart)
		
		null
	}
	
	def private getEditor(ExecutionEvent event, Set<URI> uris) {
		val editor = event.activePart.site.page.editorReferences.map[getEditor(false)].filter(IEditingDomainProvider).findFirst[
			editingDomain.resourceSet.resources.exists[uris.contains(URI)]
		]
		
		if (editor != null) {
			return editor
		}
		
		// we don't have an editor, so open one now
		val page = event.activePart.site.page
		val modelFile = ResourcesPlugin.getWorkspace.root.findMember(new Path(uris.head.toPlatformString(true))) as IFile
		return page.openEditor(new FileEditorInput(modelFile), "de.dlr.premise.system.presentation.SystemEditorID") as IEditingDomainProvider
	}

}