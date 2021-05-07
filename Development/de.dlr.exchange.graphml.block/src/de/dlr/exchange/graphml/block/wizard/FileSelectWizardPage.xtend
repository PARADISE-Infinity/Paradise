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

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Path
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.ui.dialogs.WizardNewFileCreationPage

class FileSelectWizardPage extends WizardNewFileCreationPage {

	val FILE_EXTENSIONS = #["graphml"]

	new() {
		super("file", StructuredSelection.EMPTY)

		description = "Select export file"
	}

	override validatePage() {
		if (!super.validatePage()) {
			return false
		}

		val fileExt = new Path(fileName).fileExtension
		if (fileExt == null || !FILE_EXTENSIONS.contains(fileExt)) {
			errorMessage = "File name must end in .graphml"
			return false
		}

		return true
	}

	def getFile() {
		ResourcesPlugin.getWorkspace().getRoot().getFile(containerFullPath.append(fileName));
	}
}
