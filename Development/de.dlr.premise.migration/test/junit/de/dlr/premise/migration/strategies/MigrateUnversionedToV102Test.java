/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.migration.strategies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import de.dlr.premise.migration.MigrationMissingException;
import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.MigrationModelTest;
import de.dlr.premise.migration.MigrationStrategy;
import de.dlr.premise.migration.MigrationTestHelper;

public class MigrateUnversionedToV102Test extends MigrationModelTest{
    
    private static String PATH_INPUT_FILE = de.dlr.premise.util.TestHelper.locateFile(DE_DLR_PREMISE_MIGRATION, "test/data/MigrateToV102/input.premise").getPath().substring(1);
    private static String PATH_OUTPUT_FILE = de.dlr.premise.util.TestHelper.locateFile(DE_DLR_PREMISE_MIGRATION, "test/data/MigrateToV102/output.premise").getPath().substring(1);

	@Test
	public void testMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
		MigrationTestHelper.fileCopy(PATH_INPUT_FILE, PATH_OUTPUT_FILE);

		MigrationModel model = new MigrationModel(PATH_OUTPUT_FILE);
		MigrationStrategy.migrate(model, "1.02");
		model.save();

		assertTrue(model.wasChanged());
		assertEquals(model.getModelRoot().getAttribute("metaModel"), "1.02");

		assertEquals(7430, new File(PATH_OUTPUT_FILE).length());
	}	
}
