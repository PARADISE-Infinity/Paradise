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

import de.dlr.premise.representation.Selector
import de.dlr.premise.representation.provider.SelectorItemProvider
import de.dlr.premise.system.extensionpoints.ICustomCellEditor
import de.dlr.premise.representation.ui.QueryDialog
import org.eclipse.emf.common.ui.celleditor.ExtendedDialogCellEditor
import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClassifier
import org.eclipse.jface.dialogs.Dialog
import org.eclipse.jface.viewers.CellEditor
import org.eclipse.jface.viewers.ILabelProvider
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control

abstract class ASelectorCustomCellEditor implements ICustomCellEditor {

	def abstract protected EAttribute getSupportedFeature()

	def abstract protected EClassifier getContext(Selector selector)

	override boolean appliesForFeature(Object feature) {
		return feature === getSupportedFeature()
	}

	override CellEditor createPropertyEditor(Object object, Composite composite, ILabelProvider propertyLabelProvider) {
		val Selector selector = (object as Selector)
		val String featureDescription = selector.eAdapters.filter(SelectorItemProvider).map [
			getPropertyDescriptor(selector, getSupportedFeature())
		].map[getDescription(null).toFirstLower].head
		val String query = (selector.eGet(getSupportedFeature()) as String)

		return new ExtendedDialogCellEditor(composite, propertyLabelProvider) {

			override protected Object openDialogBox(Control cellEditorWindow) {
				val QueryDialog xtextDialog = new QueryDialog(cellEditorWindow.getShell(), query, featureDescription,
					selector.context)
				val int returnCode = xtextDialog.open()
				if (returnCode === Dialog::CANCEL) {
					return null
				}
				return xtextDialog.getContent()
			}
		}
	}
}
