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

import de.dlr.exchange.base.xtend.ExportFileHelper
import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.base.xtend.ui.AGenerationHandler
import de.dlr.exchange.json.structure.json.GsonFactory
import de.dlr.premise.component.ComponentRepository
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ARepository
import de.dlr.premise.functions.UseCaseRepository
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.presentation.my.query.QueryableTree
import java.util.List
import java.util.function.Predicate
import org.eclipse.core.resources.IFolder
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.part.MultiPageEditorPart

import static extension de.dlr.premise.util.PremiseHelper.*

class JsonGenerationHandler extends AGenerationHandler {

	public static String EXTENSION = ".json"

	override void generateFromAElements(IFolder srcGenFolder, ResourceSet editorResSet, List<AElement> selectedElements) {
		val transform = new PremiseToJsonTransform(selectedElements, predicate)
		transform.run()
		
		val gson = GsonFactory.createGson()
		val generated = gson.toJson(transform.jsonRoot)
		
		val encoded_name = GeneratorHelper.encodeFileName(selectedElements.head.name)
		
		val name = ExportFileHelper.findFileName(srcGenFolder, encoded_name, EXTENSION)
		val file = srcGenFolder.getFile(name)
		
		ExportFileHelper.createOrUpdateFile(file, generated)
		ExportFileHelper.revealInNavigator(file)
	}

	override void generateFromResources(IFolder srcGenFolder, ResourceSet resultSet, List<Resource> selectedFiles) {
		val list = selectedFiles
			.flatMap[contents]
			.filter(ARepository)
			.flatMap[elements as EList<AElement>]
			.toList
			
		generateFromAElements(srcGenFolder, resultSet, list)
	}
	
			
	def dispatch getElements(ProjectRepository pr) {
		pr.projects
	}
	
	def dispatch getElements(UseCaseRepository ur) {
		ur.usecases
	}
	
	def dispatch getElements(ComponentRepository cr) {
		cr.components
	}
	
	private def Predicate<Object> getPredicate() {
		val editor = PlatformUI.workbench.activeWorkbenchWindow.activePage.activeEditor
		if (editor instanceof MultiPageEditorPart) {
			val page = editor.selectedPage
			if (page instanceof QueryableTree) {
				return page.predicate
			}
		}
		return [true]
	}
}