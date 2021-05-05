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

import de.dlr.premise.migration.IPremiseMigration
import de.dlr.premise.migration.MigrationModel
import java.util.HashMap
import java.util.Map
import org.w3c.dom.Document

abstract class ATwoPhaseMigration implements IPremiseMigration {
	override void migrate(MigrationModel model) {
		val Map<String, Document> refFiles = new HashMap(model.referencedFiles)
		refFiles.put(model.path, model.modelDocument)

		refFiles.forEach[path, document | prepareRoot(model, path, document)]
		
		refFiles.forEach[path, document | migrateRoot(model, path, document)]
	}
	
	def protected void prepareRoot(MigrationModel model, String path, Document document) {
		// Default: do nothing
	}
	
	def protected void migrateRoot(MigrationModel model, String path, Document document)
}