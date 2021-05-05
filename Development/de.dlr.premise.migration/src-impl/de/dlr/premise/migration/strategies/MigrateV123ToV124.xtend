/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.migration.strategies

import de.dlr.premise.migration.MigrationModel
import de.dlr.premise.migration.ModelVersion
import org.w3c.dom.Element

import static extension de.dlr.premise.migration.util.MigrationHelper.*

/**
 * Move APointer and DirectPointer from Component to Graph
 */
class MigrateV123ToV124 extends AMigration {
	
	override protected migrateRoot(MigrationModel model, Element modelRoot) {
		val dps = modelRoot.getElementsByTagName("*").listFromNodeList.filter(Element).filter[getAttribute("xsi:type") == "comp:DirectPointer"]
		
		if (!dps.isEmpty) {
			dps.forEach[setAttribute("xsi:type", "graph:DirectPointer")]
			modelRoot.setAttribute("xmlns:graph", "http://www.dlr.de/premise/graph/2017/")
			model.setChange()
			
		}
	}
	
	override getTargetVersion() {
		return ModelVersion.V124.toString
	}
	
}