/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.aspect.graphml.ui

import de.dlr.premise.util.urihandlers.URIHandlerHelper
import java.util.Collection
import java.util.List
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.resources.IFile
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.emf.ecore.xmi.XMIResource
import org.eclipse.emf.edit.command.ChangeCommand
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain
import org.eclipse.emf.edit.domain.IEditingDomainProvider
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.ui.IEditorPart
import org.eclipse.ui.part.FileEditorInput

import static extension org.eclipse.emf.common.util.URI.*
import static extension org.eclipse.ui.handlers.HandlerUtil.*

class GraphMLHandlerHelper {
	
	def static getGraphMLFileURI(URI originalFileURI) {
		originalFileURI
			.trimSegments(1)
			.appendSegment("export")
			.appendSegment(originalFileURI.lastSegment)
			.appendFileExtension("graphml")
	}
	
	def static getIFiles(ExecutionEvent event) {		
		// those casts are safe, because they mirror the rules for visibleWhen of the command (see plugin.xml)
		(event.currentSelectionChecked as StructuredSelection).toList as List<IFile>
	}
	
	def static getEditor(ExecutionEvent event) {
		event.IFiles.fold(null)[if ($0 != null) $0 else getEditor(event, $1)]
	}
	
	def static getGraphMLFileURI(Iterable<IFile> files) {
		files.get(0).URI.graphMLFileURI
	}
	
	def static findOrCreateResourceSet(IEditorPart editor, Collection<IFile> files) {
		var ResourceSet resSet
		
		if (editor instanceof IEditingDomainProvider) {
			resSet = editor.editingDomain.resourceSet
		}
		
		if (resSet == null) {
			resSet = createResourceSet
		}
		
		// ensure all files are loaded
		resSet.loadIFiles(files)
		
		// resolve all references
		EcoreUtil.resolveAll(resSet)
		
		resSet
	}
	
	def static createResourceSet() {
		new ResourceSetImpl => [
			loadOptions.put(XMIResource.OPTION_DISABLE_NOTIFY, Boolean.TRUE)
			URIHandlerHelper.registerInto(it.URIConverter)
		]
	}
	
	def static loadIFiles(ResourceSet resSet, Collection<IFile> files) {
		files.forEach[resSet.getResource(URI, true)]
	}
	
	def static getURI(IFile file) {
		file.fullPath.makeAbsolute.toString.createPlatformResourceURI(true)
	}
	
	def static executeInEditingDomain(ResourceSet resSet, ()=>void fn) {
		val editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(resSet)
		
		if (editingDomain != null) {
			editingDomain.commandStack.execute(new ChangeCommand(resSet) {
				override protected doExecute() {
					fn.apply
				}	
			})
		} else {				
			fn.apply
		}
	}
	
	
	def private static getEditor(ExecutionEvent event, IFile file) {
		val editorRef = event.activePart.site.page.editorReferences
			.findFirst[editorRef | 
				val input = editorRef.editorInput
				if (input instanceof FileEditorInput) {
					input.file == file
				} else {
					false
				}
			]
			
		editorRef?.getEditor(false)
	}
	

}