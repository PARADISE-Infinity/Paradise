/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.aspect.graphml.ui

import de.dlr.aspect.graphml.transform.p2g.PremiseToGraphMLTransform
import de.dlr.aspect.graphml.transform.p2g.PremiseToGraphMLTransformException
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ARepository
import java.util.List
import java.util.Map
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.ui.IEditorRegistry
import org.eclipse.ui.part.FileEditorInput

import static extension com.google.common.collect.Maps.*
import static extension de.dlr.aspect.graphml.ui.GraphMLHandlerHelper.*
import static extension de.dlr.aspect.graphml.util.PremiseModelHelper.*
import static extension org.eclipse.emf.common.util.URI.*
import static extension org.eclipse.ui.handlers.HandlerUtil.*

class CreateGraphMLHandler extends AbstractHandler {

	override execute(ExecutionEvent event) {
		val files = event.IFiles
		val premiseResSet = findOrCreateResourceSet(event.editor, files)
		
		
		val root = try {
			doTransformation(premiseResSet)
		} catch (PremiseToGraphMLTransformException e) {
			MessageDialog.openError(event.activeWorkbenchWindow.shell, "Error while creating GraphML", e.message)
			return null
		}
				
		val graphMLResSet = new ResourceSetImpl
		
		// make sure we don't accidentally overwrite user data
		if (graphMLResSet.URIConverter.exists(files.graphMLFileURI, null)) {
			// deresolve the new file uri based on the file selected by the user (so she can understand the path)
			// selected element is always the first element in resource set
			val firstPremiseURI = premiseResSet.resources.get(0).URI
			val relativeURI = files.graphMLFileURI.deresolve(firstPremiseURI).toString.decode
			
			val overwrite = MessageDialog.openConfirm(event.activeWorkbenchWindow.shell, "File already exists",
				'''The file "«relativeURI»" already exists. Do you want to overwrite it?''')
				
			if (!overwrite) {
				return null
			}
		}
		
		// write out the file
		val graphMLResource = graphMLResSet.createResource(files.graphMLFileURI)
		graphMLResource.contents.add(root)
		graphMLResource.save(null)
		
		
		// open the system graphml editor (usually yEd)
		val page = event.activePart.site.page
		val file = ResourcesPlugin.workspace.root.findMember(files.graphMLFileURI.toPlatformString(false))
		if (file instanceof IFile) {	
			page.openEditor(new FileEditorInput(file), IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID)
		}
		
		return null
	}
	
	def doTransformation(ResourceSet premiseResSet) {
		// Map of resource uri to all root AElements
		val Map<String, List<? extends AElement>> elements = premiseResSet.resources
			.uniqueIndex[URI.toString]
			.mapValues[contents.get(0)]
			.filterValues[it instanceof ARepository]
			.mapValues[(it as ARepository).contained]
			
		// get options
		val affectedRepository = premiseResSet.resources.get(0).contents.get(0)
		val options = new CreateGraphMLOptions
		
		if (affectedRepository instanceof ARepository) {
			premiseResSet.executeInEditingDomain[options.setOptions(affectedRepository)]
		}
		val containment = options.getContainment(affectedRepository)
		
		val transform = new PremiseToGraphMLTransform(elements, containment)
		transform.run
		
		transform.root
	}

}
