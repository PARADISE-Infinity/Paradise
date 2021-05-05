/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.view

import de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry
import java.util.Observable
import org.eclipse.swt.widgets.Display
import y.base.GraphEvent
import y.base.GraphListener

/**
 * Listener for GraphEvents. <br>
 * Handles edge and node creation and deletion.
 */
class GraphListenerImpl extends Observable implements GraphListener {

	int listening = 0

	new() {
		addObserver(MapperRegistry.modelToGraphMapper)
	}

	override onGraphEvent(GraphEvent e) {
		if (listening != 0) {
			return
		}
		Display.^default.asyncExec [
			setChanged()
			notifyObservers(e)
		]
	}

	def suspendListening() {
		listening++
	}

	def resumeListening() {
		listening--
	}

}
