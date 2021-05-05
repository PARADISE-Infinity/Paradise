/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.migration.strategies

import de.dlr.premise.migration.MigrationModel
import de.dlr.premise.migration.ModelVersion
import org.w3c.dom.Element

import static extension de.dlr.premise.migration.util.MigrationHelper.*

/**
 * Convert ModeGuard.mode and AModeCombination.modes to pointers
 */
class MigrateV129ToV130 extends AMigration {

	override protected migrateRoot(MigrationModel model, Element modelRoot) {
		val modeGuards = modelRoot.getElementsByTagName("*")
			.listFromNodeList.filter(Element).filter[getAttribute("xsi:type") == "elem:ModeGuard"]
			
		modeGuards.forEach[convertRefToDirectPointer("mode", "modePointer", "elem:Mode", model)]
		
		val modeValueRefs = modelRoot.getElementsByTagName("modeValues").listFromNodeList
		val modeValueConstraints = modelRoot.getElementsByTagName("modeValueConstraints").listFromNodeList
		val modeCombinations = (modeValueConstraints + modeValueRefs).filter(Element)
		
		modeCombinations.forEach[convertRefToDirectPointer("modes", "modePointers", "elem:Mode", model)]
		
		if (model.wasChanged) {
			modelRoot.setAttribute("xmlns:graph", "http://www.dlr.de/premise/graph/2017/")
			modelRoot.setAttribute("xmlns:elem", "http://www.dlr.de/premise/element/2014/")
		}
	}

	override getTargetVersion() {
		return ModelVersion.V130.toString
	}
	
	def convertRefToDirectPointer(Element parent, String refName, String pointerName, String defaultTargetType, MigrationModel model) {
		if (parent.hasAttribute(refName)) {
			val ids = parent.getAttribute(refName).split(" ")
			for (id : ids) {
				parent.appendChild(parent.ownerDocument.parse(
					'''<«pointerName» xsi:type="graph:DirectPointer" target="«id»"/>'''
				))
			}
			parent.removeAttribute(refName)
			model.setChange()
		} 
		
		val children = parent.childNodes.listFromNodeList.filter(Element).filter[tagName == refName]
		for (child : children) {
			val href = child.getAttribute("href")
			val xsiType = if (child.hasAttribute("xsi:type")) child.getAttribute("xsi:type") else defaultTargetType
			parent.replaceChild(parent.ownerDocument.parse('''
				<«pointerName» xsi:type="graph:DirectPointer">
					<target xsi:type="«xsiType»" href="«href»"/>
				</«pointerName»>
			'''), child)
			model.setChange()
		}
	}
}
