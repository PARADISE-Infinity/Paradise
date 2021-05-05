/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel

import de.dlr.premise.query.persistent.PersistableQuery
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.system.presentation.my.query.CollectionBasedPredicate
import de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl

/**
 * Holds the currently selected elements and gets notified when it changes.
 */
class SelectionHolder {
	var static SelectionHolder instance = null

	var List<EObject> selection
	var PersistableQuery query
	var CollectionBasedPredicate predicate

	private new() {
		selection = newArrayList()
	}

	def static synchronized SelectionHolder getInstance() {
		if (instance == null) {
			instance = new SelectionHolder()
		}
		return instance
	}

	def getSelection() {
		return selection
	}

	def void setSelection(List<EObject> selection) {
		this.selection = selection
	}

	def getQuery() {
		return query
	}

	def setQuery(PersistableQuery query) {
		this.query = query
	}

	def getPredicate() {
		return predicate
	}

	def setPredicate(CollectionBasedPredicate predicate) {
		this.predicate = predicate
	}

	/**
	 * @return an OutputStream for saving the selected element as graphml.
	 */
	def getExportOutputStream() {
		if (!selection.nullOrEmpty) {
			var selected = selection.get(0)
			if (selected instanceof ANameItem) {
				var filename = '''«selected.name.replace("\\n", "").replaceAll("\\W+", "_")»_«MapperRegistry.currentContext»'''
				var uri = selected.eResource.getURI.trimSegments(1)
				uri = uri.appendSegment("export").appendSegment("view").appendSegment(filename).
					appendFileExtension("graphml")
				return new ExtensibleURIConverterImpl().createOutputStream(uri)
			}
		}
	}
}
