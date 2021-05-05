/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.base.xtend

import java.lang.reflect.InvocationTargetException
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.swt.widgets.Display
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.actions.WorkspaceModifyOperation
import org.eclipse.ui.part.ISetSelectionTarget
import org.eclipse.xtext.util.StringInputStream
import java.io.InputStream

class ExportFileHelper {
	def static findFileName(IFolder folder, String name, String fileExt) {
		if (!folder.exists(new Path(name + fileExt))) {
			return name + fileExt
		}
		
		var counter = 1
		while (folder.exists(new Path(name + counter + fileExt))) {
			counter += 1
		}
		
		return name + counter + fileExt
	}	
	
	def static createOrUpdateFile(IFile file, String generated) {
		val inputStream = new StringInputStream(generated, file.charset)
		createOrUpdateFile(file, inputStream)
	}

	def static createOrUpdateFile(IFile file, InputStream inputStream) {
		new WorkspaceModifyOperation() {
			override protected execute(
				IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				if (!file.exists) {
					file.create(inputStream, true, monitor)
				} else {
					file.setContents(inputStream, true, true, monitor)
				}
			}

		}.run(new NullProgressMonitor)
	}
	
	def static revealInNavigator(IFile file) {
		Display.getDefault().asyncExec[
			val part = PlatformUI.getWorkbench().activeWorkbenchWindow.activePage.findView("premise.rcp.navigator")
			if (part instanceof ISetSelectionTarget) {
				val selection = new StructuredSelection(file)
					part.selectReveal(selection)
			}
		]
	}
}