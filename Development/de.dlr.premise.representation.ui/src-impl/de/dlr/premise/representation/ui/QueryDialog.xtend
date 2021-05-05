/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.representation.ui

import de.dlr.premise.presentation.util.DefaultSizeDialog
import de.dlr.premise.query.ui.OCLEditor
import org.eclipse.emf.ecore.EClassifier
import org.eclipse.jface.dialogs.IDialogConstants
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Shell

class QueryDialog extends DefaultSizeDialog {

	var OCLEditor editor
	val String initialQuery
	val String featureName
	val EClassifier context

	new(Shell parentShell, String initialQuery, String featureName, EClassifier context) {
		super(parentShell)
		this.initialQuery = initialQuery ?: ""
		this.featureName = featureName
		this.context = context
	}

	override protected Control createDialogArea(Composite parent) {

		var Composite composite = new Composite(parent, SWT::NONE)
		var GridLayout layout = new GridLayout()

		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants::VERTICAL_MARGIN)
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants::HORIZONTAL_MARGIN)
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants::VERTICAL_SPACING)
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants::HORIZONTAL_SPACING)
		composite.setLayout(layout)
		composite.setLayoutData(new GridData(GridData::FILL_BOTH))

		editor = OCLEditor.createEditor(composite, context)
		editor.text = initialQuery

		return composite
	}

	override boolean isResizable() {
		return true
	}

	override configureShell(Shell shell) {
		super.configureShell(shell)
		shell.text = '''Enter «featureName»'''
	}

	override getDefaultSize() {
		return getShell().computeSize(600, 300, true);
	}

	override okPressed() {
		setReturnCode(OK)
		close()
	}

	def getContent() {
		return editor.text
	}

}
