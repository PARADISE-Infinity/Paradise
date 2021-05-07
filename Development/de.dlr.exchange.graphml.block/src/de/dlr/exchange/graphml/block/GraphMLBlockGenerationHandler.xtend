/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.block

import de.dlr.exchange.base.xtend.ExportFileHelper
import de.dlr.exchange.base.xtend.ui.AGenerationHandler
import de.dlr.exchange.graphml.block.wizard.GraphMLBlockSettingsWizard
import de.dlr.premise.component.ComponentRepository
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ARepository
import de.dlr.premise.functions.UseCaseRepository
import de.dlr.premise.system.ProjectRepository
import java.util.List
import java.util.Set
import org.eclipse.core.resources.IFolder
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.jface.wizard.WizardDialog
import org.eclipse.ui.IEditorRegistry
import org.eclipse.ui.part.FileEditorInput

import static extension org.eclipse.ui.handlers.HandlerUtil.*

class GraphMLBlockGenerationHandler extends AGenerationHandler {
		
	override generateFromAElements(IFolder srcGenFolder, ResourceSet input, List<AElement> selectedElements) {
		doGenerate(srcGenFolder, selectedElements.toSet)
	}
	
	override generateFromResources(IFolder srcGenFolder, ResourceSet input, List<Resource> selectedFiles) {
		val roots = selectedFiles
			.map[contents].flatten
			.filter(ARepository)
			.map[elements].flatten
			
		doGenerate(srcGenFolder, roots.toSet)
	}
	
	def private doGenerate(IFolder srcGenFolder, Set<AElement> initialSubtreeRoots) {		
		val wizard = new GraphMLBlockSettingsWizard(srcGenFolder, initialSubtreeRoots)
		val wizardDialog = new WizardDialog(event.activeShell, wizard)
		wizardDialog.setMinimumPageSize(700, 700)
		
		val result = wizardDialog.open()
		if (result != WizardDialog.OK) {
			return
		}
		
		val settings = wizard.settings
		
		val gen = new GraphMLBlockGenerator(settings)
		val generated = gen.generate()
		gen.dispose()
		val file = wizard.file
		
		ExportFileHelper.createOrUpdateFile(file, generated)
		ExportFileHelper.revealInNavigator(file)
		
		event.activeWorkbenchWindow.activePage.openEditor(
			new FileEditorInput(file), 
			IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID
		)
	}	
	
	def dispatch EList<? extends AElement> getElements(ProjectRepository pr) {
		pr.projects
	}
	def dispatch EList<? extends AElement> getElements(UseCaseRepository ur) {
		ur.usecases
	}
	def dispatch EList<? extends AElement> getElements(ComponentRepository cr) {
		cr.components
	}
	
}