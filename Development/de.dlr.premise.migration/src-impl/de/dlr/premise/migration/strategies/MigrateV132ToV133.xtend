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
 * Rename ComponentReferenceSubstitiutionMapping.subsitution to substitution
 */
class MigrateV132ToV133 extends AMigration {

	override protected migrateRoot(MigrationModel model, Element modelRoot) {
		val substitiutionElements = modelRoot.getElementsByTagName("subsitution").listFromNodeList.filter(Element).toList
		substitiutionElements.forEach [
			renameElement("substitution")
			model.setChange
		]
	}

	override getTargetVersion() {
		return ModelVersion.V133.toString
	}

}
