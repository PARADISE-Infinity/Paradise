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

class MigrateV122ToV123 extends AMigration {
	

	override getTargetVersion() {
		ModelVersion.V123.toString
	}
	
	override protected migrateRoot(MigrationModel model, Element modelRoot) {
		
		migrateElements(modelRoot,"usecases", model)
		migrateElements(modelRoot,"children", model)
		migrateElements(modelRoot,"projects", model)
		migrateElements(modelRoot,"components", model)
		


	}
	
	
	def private migrateElements(Element modelRoot, String elementType, MigrationModel model) {
		val repo = modelRoot.getElementsByTagName(elementType).listFromNodeList.filter(Element)
		for  ( element : repo) {
			
			// check element for Transitions or Modes
			var boolean hasModesOrTransitions = false
			for (child : element.getDirectChildElementsByTagName("modes")) {
				hasModesOrTransitions = true
			}
			for (child : element.getDirectChildElementsByTagName("transitions")) {
				child.removeAttribute("xsi:type")
				hasModesOrTransitions = true
			}
			
			
			
			if (hasModesOrTransitions) {
				// generate a Statemachine	
				val smchild = element.ownerDocument.createElement("statemachines")
				smchild.setAttribute("name",element.getAttribute("name"))
				element.appendChild(smchild)
				
				
				
				
				// move the old modes and transitions
				for (ch : element.getDirectChildElementsByTagName("modes")) {
					element.getDirectChildElementsByTagName("statemachines").get(0).appendChild(ch)
				}
				for (ch : element.getDirectChildElementsByTagName("transitions")) {
					element.getDirectChildElementsByTagName("statemachines").get(0).appendChild(ch)
				}
				model.setChange()
			}
				
		}
		
	}
	
	
	def private getDirectChildElementsByTagName(Element el, String name) {
		el.childNodes.listFromNodeList.filter(Element).filter[tagName == name]
	}
	
}