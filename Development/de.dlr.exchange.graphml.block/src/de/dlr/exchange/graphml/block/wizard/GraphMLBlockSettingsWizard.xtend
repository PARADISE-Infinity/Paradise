/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.block.wizard

import de.dlr.exchange.base.xtend.ExportFileHelper
import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.graphml.block.settings.GraphMLBlockSettings
import de.dlr.premise.element.AElement
import de.dlr.premise.provider.util.PremiseAdapterFactoryHelper
import java.util.Set
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.emf.edit.provider.ComposedAdapterFactory
import org.eclipse.jface.wizard.Wizard
import org.eclipse.swt.widgets.Composite
import org.eclipse.xtend.lib.annotations.AccessorType
import org.eclipse.xtend.lib.annotations.Accessors

import static extension org.eclipse.xtend.lib.annotations.AccessorType.*

class GraphMLBlockSettingsWizard extends Wizard {
	
	val static WIZARD_TITLE = "Export GraphML Block Diagram"
	
	@Accessors(value = #[AccessorType.PUBLIC_GETTER]) val GraphMLBlockSettings settings
	
	val IFolder initialFolder
	val Set<AElement> initialRoots
	
	val ComposedAdapterFactory adapterFactory
	
	val FileSelectWizardPage fileSelectWizardPage
	val SubtreeRootsWizardPage subtreeRootsPage
	val EdgesWizardPage edgesPage
	
	@Accessors(value = #[AccessorType.PUBLIC_GETTER])  var IFile file
	
	
	new(IFolder initialFolder, Set<AElement> initialRoots) {
		this.initialFolder = initialFolder
		this.initialRoots = initialRoots
		this.settings = new GraphMLBlockSettings
		
		adapterFactory = PremiseAdapterFactoryHelper.createComposedAdapterFactory()
		
		fileSelectWizardPage = new FileSelectWizardPage()
		subtreeRootsPage = new SubtreeRootsWizardPage(adapterFactory, settings)
		edgesPage = new EdgesWizardPage(adapterFactory, settings)	
		
		windowTitle = WIZARD_TITLE
		fileSelectWizardPage.title = WIZARD_TITLE
		subtreeRootsPage.title = WIZARD_TITLE
		edgesPage.title = WIZARD_TITLE
	}
		
	override addPages() {
		addPage(fileSelectWizardPage)
		addPage(subtreeRootsPage)		
		addPage(edgesPage)
		
		settings.addObserver(subtreeRootsPage)
		settings.addObserver(edgesPage)
	}
	
	override createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer)
		
		fileSelectWizardPage.containerFullPath = initialFolder.fullPath
		fileSelectWizardPage.fileName = createFileName(initialFolder)
		settings.initialRoots = initialRoots
	}
	
	override performFinish() {
		settings.deleteObserver(subtreeRootsPage)
		settings.deleteObserver(edgesPage)
		
		adapterFactory.dispose()
		
		this.file = fileSelectWizardPage.file
		settings.charset = file.charset
		
		return true
	}
	
	def private createFileName(IFolder folder) {
		val fileExt = ".graphml"
		val name = GeneratorHelper::encodeFileName(initialRoots.head.name)
		
		ExportFileHelper.findFileName(folder, name, fileExt)
	}
}