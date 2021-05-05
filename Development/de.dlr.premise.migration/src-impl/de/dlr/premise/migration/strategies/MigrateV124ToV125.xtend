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
import org.w3c.dom.Element
import de.dlr.premise.migration.ModelVersion
import static extension de.dlr.premise.migration.util.MigrationHelper.*


class MigrateV124ToV125 extends AMigration{
		
	override getTargetVersion() {
		ModelVersion.V125.toString
	}
	
	// get rid of TransitionBalancing Sources and FunctionDefs
	
	override protected migrateRoot(MigrationModel model, Element modelRoot) {	
		val repo = modelRoot.getElementsByTagName("*").listFromNodeList.filter(Element).filter[tagName=="transitions"]
		
		// this should be all transitions
		for (element : repo) {
			// check transition for Balancings inside them
			for (bal : element.getDirectChildElementsByTagName("balancings")) {
				// remove sources
				bal.removeAttribute("sources")
				// remove function defs
				for (fdef : (bal.getDirectChildElementsByTagName("functionDefOr") + bal.getDirectChildElementsByTagName("functionDefAnd"))) {
					bal.removeChild(fdef)
				}
				
			}
		}

		model.setChange();
	}
	
		
		
	def private getDirectChildElementsByTagName(Element el, String name) {
		el.childNodes.listFromNodeList.filter(Element).filter[tagName == name]
	}
}