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

class MigrateV128ToV129 extends AMigration {

	override protected migrateRoot(MigrationModel model, Element modelRoot) {
		val transitions = modelRoot.getElementsByTagName("transitions").listFromNodeList.filter(Element)
		
		for (trans : transitions) {
			if (trans.hasAttribute("externallyTriggered")) {
				if (trans.hasCondition) {
					trans.setAttribute("behavior", "EXTERNAL")
				}
				trans.removeAttribute("externallyTriggered")
				model.setChange()
			}
		} 
	}

	override getTargetVersion() {
		return ModelVersion.V129.toString
	}
	
	def private hasCondition(Element it) {
		childNodes.listFromNodeList.filter(Element).exists[tagName == "condition"]
	}
}
