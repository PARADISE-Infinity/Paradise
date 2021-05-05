/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.handler

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.commands.ExecutionException
import org.eclipse.emf.common.ui.viewer.IViewerProvider
import org.eclipse.jface.viewers.AbstractTreeViewer
import org.eclipse.jface.viewers.StructuredSelection

import static extension org.eclipse.ui.handlers.HandlerUtil.*

class ExpandAllHandler extends AbstractHandler {
	
	override execute(ExecutionEvent event) throws ExecutionException {
		val part = event.activePart
		if (part instanceof IViewerProvider) {
			val viewer = part.viewer
			if (viewer instanceof AbstractTreeViewer) {
				val selection = event.currentSelectionChecked
				if (selection instanceof StructuredSelection) {
					viewer.control.redraw = false
					selection.toList.forEach[viewer.expandToLevel(it, AbstractTreeViewer.ALL_LEVELS)]
					viewer.control.redraw = true
				}
			}
		}
		
		null
	}
	
}