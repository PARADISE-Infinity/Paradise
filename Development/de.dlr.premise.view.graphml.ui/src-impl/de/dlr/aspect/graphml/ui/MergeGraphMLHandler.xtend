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

import de.dlr.aspect.graphml.compare.DuplicateAwareIdentifierEObjectMatcher
import de.dlr.aspect.graphml.compare.GraphMLPremiseComparePostProcessor
import de.dlr.aspect.graphml.transform.g2p.GraphMLToPremiseTransform
import de.dlr.aspect.graphml.transform.g2p.GraphMLToPremiseTransformException
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ARepository
import de.dlr.premise.functions.UseCase
import de.dlr.premise.functions.UseCaseFactory
import de.dlr.premise.functions.UseCaseRepository
import java.util.List
import java.util.Map
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.emf.common.util.BasicMonitor
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.compare.EMFCompare
import org.eclipse.emf.compare.match.DefaultComparisonFactory
import org.eclipse.emf.compare.match.DefaultEqualityHelperFactory
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl
import org.eclipse.emf.compare.merge.BatchMerger
import org.eclipse.emf.compare.merge.IBatchMerger
import org.eclipse.emf.compare.merge.IMerger
import org.eclipse.emf.compare.postprocessor.PostProcessorDescriptorRegistryImpl
import org.eclipse.emf.compare.scope.DefaultComparisonScope
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.handlers.HandlerUtil
import org.eclipse.ui.part.FileEditorInput
import org.graphdrawing.graphml.Graphml
import de.dlr.premise.system.SystemFactory
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.system.presentation.SystemEditor

import static extension de.dlr.aspect.graphml.ui.GraphMLHandlerHelper.*
import static extension org.eclipse.emf.common.util.URI.createURI
import static extension org.eclipse.ui.handlers.HandlerUtil.*
import de.dlr.premise.component.ComponentRepository
import de.dlr.premise.component.ComponentFactory

class MergeGraphMLHandler extends AbstractHandler {
	
	
	val EMFCompare comparator
	val IBatchMerger batchMerger
	
	new() {
		comparator = (EMFCompare.builder => [
			matchEngineFactoryRegistry = new MatchEngineFactoryRegistryImpl => [
				val matcher = DuplicateAwareIdentifierEObjectMatcher.create()
				val comparisonFactory = new DefaultComparisonFactory(new DefaultEqualityHelperFactory)
				val matchEngineFactory = new MatchEngineFactoryImpl(matcher, comparisonFactory) => [
					ranking = 20
				]
				add(matchEngineFactory)
			]
			postProcessorRegistry = new PostProcessorDescriptorRegistryImpl<String> => [
				(new GraphMLPremiseComparePostProcessor).register(it)
			]
		]).build
		
		batchMerger = new BatchMerger(IMerger.RegistryImpl.createStandaloneInstance)
	}
	
	override execute(ExecutionEvent event) {
		var editor = event.editor
		val files = event.IFiles
			
		if (editor == null) {
			// we don't have an editor, so open one now
			val page = event.activePart.site.page
			val file = files.head
			val desc = PlatformUI.workbench.editorRegistry.getDefaultEditor(file.name);
			editor = page.openEditor(new FileEditorInput(file), desc.id)
		}
		
		val graphmlResSet = createResourceSet
			
		val premiseResSet = findOrCreateResourceSet(editor, files)

		val in = graphmlResSet.getResource(files.graphMLFileURI, true)
		val graphmlRoot = in.contents.get(0) as Graphml

		val transform = new GraphMLToPremiseTransform(graphmlRoot)
		try {
			transform.run
		} catch (GraphMLToPremiseTransformException e) {
			MessageDialog.openError(HandlerUtil.getActiveWorkbenchWindow(event).shell,
				"Error while merging GraphML", e.message)
			return null
		}

		val transformedResSet = createResourceSet

		// create resources for the retransformed premise, so we can then match on resource set level
		buildResourceSets(premiseResSet, transformedResSet, transform.roots)

		// do comparison
		val comparison = comparator.compare(new DefaultComparisonScope(premiseResSet, transformedResSet, premiseResSet))

		// merge the comparison
		if (comparison.differences.size > 0) {
			// we try to do this in the editing domain, so we don't force the editor to reload
			premiseResSet.executeInEditingDomain[batchMerger.copyAllRightToLeft(comparison.differences, new BasicMonitor)]
		}
				
		// set selection to changed/updated elements		
		if (editor instanceof SystemEditor) {
			val diffleft = comparison.differences
				.map[match.left]
				.filter[it != null]
				.toList
			
			editor.setSelectionToViewer(diffleft)
		}
		
		return null
	}
	
	private def buildResourceSets(ResourceSet premiseResSet, ResourceSet transformedResSet, Map<String, List<AElement>> roots) {
		// create equivalents of all the resources in the premiseResSet
		premiseResSet.resources.forEach[transformedResSet.findOrCreateResource(it.URI)]
		
		// as a base for possible newly created files, we use the filename of the first input file
		val baseURI = premiseResSet.resources.get(0).URI.trimFileExtension
		
		// elements that can't be processed by their identification data
		val remainingElems = newArrayList
		
		roots
			.forEach[identification, elems | 
				if (identification == null || !identification.createURI.isPlatformResource || elems.size == 0) {
					remainingElems += elems
					return
				}
				
				val uri = identification.createURI
				
				// create something to merge into, if it isn't there already
				val premiseRes = premiseResSet.findOrCreateResource(uri)
				
				// create a new resource to house the transformed elements
				val transformedRes = transformedResSet.findOrCreateResource(uri)
	
				val components = elems.filter(SystemComponent)
				val usecases = elems.filter(UseCase)
								
				switch (uri.fileExtension) {
					case "usecase": {
						createUseCaseRepostitory(premiseRes, transformedRes, usecases)
						remainingElems += components
					}
					case "component": {
						createComponentRepostitory(premiseRes, transformedRes, components)
						remainingElems += usecases
					}
					default: {
						createProjectRepostitory(premiseRes, transformedRes, components)
						remainingElems += usecases
					}
				}
			]

	
		// now, look at all the elements left (e.g. newly create components/usecases)
		// first, we try to lump them into an existing resource, if that fails, we create a new one
		val components = remainingElems.filter(SystemComponent)
		if (components.length > 0) {
			var resource = transformedResSet.resources
				.findFirst[URI.fileExtension == "premise" && (contents.size == 0 || contents.get(0) instanceof ProjectRepository)]
			
			if (resource == null) {
				premiseResSet.findOrCreateResource(baseURI.appendFileExtension("premise")) => [
					if (repository == null) repository = SystemFactory.eINSTANCE.createProjectRepository
				]
				resource = transformedResSet.findOrCreateResource(baseURI.appendFileExtension("premise"))
			}
			
			if (resource.repository == null) {
				resource.repository = SystemFactory.eINSTANCE.createProjectRepository
			}
			
			(resource.repository as ProjectRepository).projects += components
		}
		
		val usecases = remainingElems.filter(UseCase)
		if (usecases.length > 0) {
			var resource = transformedResSet.resources
				.findFirst[URI.fileExtension == "usecase" && (contents.size == 0 || contents.get(0) instanceof UseCaseRepository)]
			
			if (resource == null) {
				premiseResSet.findOrCreateResource(baseURI.appendFileExtension("usecase")) => [
					if (repository == null) repository = UseCaseFactory.eINSTANCE.createUseCaseRepository
				]
				resource = transformedResSet.findOrCreateResource(baseURI.appendFileExtension("usecase"))
			} 				
			
			if (resource.repository == null) {
				resource.repository = UseCaseFactory.eINSTANCE.createUseCaseRepository
			}
			
			(resource.repository as UseCaseRepository).usecases += usecases
		}
	}
	
	private def createProjectRepostitory(Resource premiseResource, Resource transformedResource, Iterable<SystemComponent> elems) {
		if (!(premiseResource.repository instanceof ProjectRepository)) {
			premiseResource.repository = SystemFactory.eINSTANCE.createProjectRepository
		}
		transformedResource.repository = SystemFactory.eINSTANCE.createProjectRepository => [
			projects += elems
		]
	}
	
	private def createComponentRepostitory(Resource componentResource, Resource transformedResource, Iterable<SystemComponent> elems) {
		if (!(componentResource.repository instanceof ComponentRepository)) {
			componentResource.repository = ComponentFactory.eINSTANCE.createComponentRepository
		}
		transformedResource.repository = ComponentFactory.eINSTANCE.createComponentRepository => [
			components += elems
		]
	}
	
	private def createUseCaseRepostitory(Resource usecaseResource, Resource transformedResource, Iterable<UseCase> elems) {
		if (!(usecaseResource.repository instanceof UseCaseRepository)) {
			usecaseResource.repository = UseCaseFactory.eINSTANCE.createUseCaseRepository
		}
		transformedResource.repository = UseCaseFactory.eINSTANCE.createUseCaseRepository => [
			usecases += elems
		]
	}
	
	private def findOrCreateResource(ResourceSet resSet, URI uri) {
		var resource = resSet.getResource(uri, false)
		
		if (resource == null) {
			resource = resSet.createResource(uri);
		}
		
		return resource;
	}
	
	private def setRepository(Resource res, ARepository repository) {
		if (res.contents.size > 1) {
			throw new RuntimeException("Multiple root elements in one resource!")
		}
		
		res.contents.clear
		res.contents += repository
	}
	
	private def getRepository(Resource res) {
		switch (res.contents.size) {
			case 0: null
			case 1: res.contents.get(0) as ARepository
			default: throw new RuntimeException("Multiple root elements in one resource!")
		}
	}
	
}