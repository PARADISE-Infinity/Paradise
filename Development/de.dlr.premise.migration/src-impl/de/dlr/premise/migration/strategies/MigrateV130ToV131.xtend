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
 * Convert MetaData.children to MetaData.metaData
 */
class MigrateV130ToV131 extends AMigration{
	
	override protected migrateRoot(MigrationModel model, Element modelRoot) {
		val metaDatas = modelRoot.getElementsByTagName("metaData").listFromNodeList.filter(Element).toList
		
		for (md : metaDatas) {
			migrateMetaDataChildren(model, md)
		}
	}
	
	private def void migrateMetaDataChildren(MigrationModel model, Element md) {
		val children = md.childNodes.listFromNodeList.filter(Element).filter[tagName == "children"]
		for (ch : children) {
            md.getOwnerDocument().renameNode(ch, ch.getNamespaceURI(), "metaData");
            model.setChange
            migrateMetaDataChildren(model, ch)
		}
	}
	
	override getTargetVersion() {
		return ModelVersion.V131.toString
	}
	
}