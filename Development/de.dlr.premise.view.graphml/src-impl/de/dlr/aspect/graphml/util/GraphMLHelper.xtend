/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.aspect.graphml.util

import java.util.List
import org.eclipse.emf.ecore.util.FeatureMap
import org.eclipse.emf.ecore.xml.type.XMLTypePackage
import org.graphdrawing.graphml.Data
import org.graphdrawing.graphml.GraphMLFactory
import org.graphdrawing.graphml.Key

class GraphMLHelper {
	
	/**
	 * Finds {@link Data} by a given key
	 */
	def static findByKey(List<Data> data, Key searchKey) {
		data.findFirst[key == searchKey]
	}
	
	/**
	 * Creates a new {@link Data} in the given list, and returns it
	 */
	def static createByKey(List<Data> data, Key searchKey) {
		GraphMLFactory.eINSTANCE.createData => [
			key = searchKey
			data.add(it)
		]
	}
	
	/**
	 * Finds {@link Data} by a given key or creates a new element, if none is found
	 */
	def static findOrCreateByKey(List<Data> data, Key searchKey) {
		data.findByKey(searchKey) ?: data.createByKey(searchKey)
	}
	
	/**
	 * Gets the Text from a {@link FeatureMap}
	 */
	def static getText(FeatureMap any) {
		any.get(XMLTypePackage.Literals.XML_TYPE_DOCUMENT_ROOT__TEXT, false) as List<String>
	}
		
	/**
	 * Gets the CDATA from a {@link FeatureMap}
	 */
	def static getCDATA(FeatureMap any) {
		any.get(XMLTypePackage.Literals.XML_TYPE_DOCUMENT_ROOT__CDATA, false) as List<String>
	}
}