package de.dlr.premise.system.handler

import de.dlr.premise.system.presentation.my.query.QueryableTree
import de.dlr.premise.system.presentation.my.search.SearchDialog
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.commands.ExecutionException
import org.eclipse.swt.widgets.Display
import org.eclipse.ui.part.MultiPageEditorPart

import static extension org.eclipse.ui.handlers.HandlerUtil.*
import org.eclipse.jface.dialogs.Dialog

/** 
 * Copyright (c) DLR Institute of Flight Systems, Braunschweig, Germany All rights reserved
 * @version SVN: $Id$
 */
class OpenSearchHandler extends AbstractHandler {
	override execute(ExecutionEvent event) throws ExecutionException {
		val editor = event.activeEditor
		if (editor instanceof MultiPageEditorPart) {
			val page = editor.selectedPage
			if (page instanceof QueryableTree) {
				openSearchDialog(page)
			}
		}
		null
	}

	protected def void openSearchDialog(QueryableTree view) {
		try {
			var Display display = Display.getCurrent()
			var Dialog searchDialog = new SearchDialog(display, view)
			searchDialog.open()
		} catch (Exception e) {
			throw new RuntimeException(e)
		}
	}
}
