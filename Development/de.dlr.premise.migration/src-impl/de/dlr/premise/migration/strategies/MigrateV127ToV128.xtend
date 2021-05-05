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

class MigrateV127ToV128 extends AMigration {

	override protected migrateRoot(MigrationModel model, Element modelRoot) {
		val relations = modelRoot.getElementsByTagName("relations").listFromNodeList
		val connections = modelRoot.getElementsByTagName("connections").listFromNodeList
		val elements = (relations + connections).filter(Element).filter[hasAttribute("type")]
		elements.forEach[renameAttribute("type", "name"); model.setChange]
	}

	override getTargetVersion() {
		return ModelVersion.V128.toString
	}
}
