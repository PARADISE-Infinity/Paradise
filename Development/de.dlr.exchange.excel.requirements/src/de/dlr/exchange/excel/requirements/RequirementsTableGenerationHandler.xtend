/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.requirements

import de.dlr.exchange.base.xtend.ExportFileHelper
import de.dlr.exchange.base.xtend.ui.AGenerationHandler
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ARepository
import de.dlr.premise.util.PremiseHelper
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.List
import org.eclipse.core.resources.IFolder
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet

import static extension de.dlr.premise.util.PremiseHelper.*

class RequirementsTableGenerationHandler extends AGenerationHandler {

	override generateFromAElements(IFolder srcGenFolder, ResourceSet input, List<AElement> selectedElements) {
		val optRoot = selectedElements.map[root].filter(ARepository).head?.initOptions
		val isVisible = computeVisibleElements(selectedElements)
		val out = createOutputStream(srcGenFolder, input)
		new RequirementsTableGenerator(optRoot).generateRequirementsTable(srcGenFolder, input, isVisible, out)
	}

	private def computeVisibleElements(List<AElement> selectedElements) {
		val visibleElements = selectedElements.flatMap[connectedElements].toSet
		return [visibleElements.contains(it)]
	}

	override generateFromResources(IFolder srcGenFolder, ResourceSet input, List<Resource> selectedFiles) {
		val optRoot = selectedFiles.map[contents.filter(ARepository).head].head?.initOptions
		val isVisible = [true]
		val out = createOutputStream(srcGenFolder, input)
		new RequirementsTableGenerator(optRoot).generateRequirementsTable(srcGenFolder, input, isVisible, out)
	}

	private def OutputStream createOutputStream(IFolder srcGenFolder, ResourceSet input) {
		val file = input.resources.get(0).file
		val name = ExportFileHelper.findFileName(srcGenFolder, file.fullPath.removeFileExtension.lastSegment,
			".requirements.xlsx")
		val exportFile = srcGenFolder.getFile(name)
		val out = new PipedOutputStream()
		val in = new PipedInputStream(out)
		new Thread [
			ExportFileHelper.createOrUpdateFile(exportFile, in)
			ExportFileHelper.revealInNavigator(exportFile)
		].start()
		return out
	}

	private def initOptions(ARepository repository) {
		new Options().setOptions(repository)
		return PremiseHelper.getMetaData(repository, Options.OPT_TECH)
	}
}
