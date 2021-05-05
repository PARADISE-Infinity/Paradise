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

import com.google.common.io.Files;

import de.dlr.premise.migration.MigrationMissingException;
import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.MigrationStrategy;
import de.dlr.premise.migration.MigrationTestHelper;

public class MigrateV105ToV106Test {
	private static String NAME = "MigrateV105ToV106";
	private static String FILENAME_PREMISE = "MigrateV105ToV106.premise";

    private static String PATH_INPUT = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE);
    private static String PATH_OUTPUT = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE);
    private static String PATH_REFERENCE = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE);

	@Test
	public void testMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
		MigrationTestHelper.fileCopy(PATH_INPUT, PATH_OUTPUT);

		MigrationModel model = new MigrationModel(PATH_OUTPUT);
		MigrationStrategy.migrate(model, "1.06");
		model.save();

		assertTrue(model.wasChanged());
		assertEquals("1.06", model.getModelRoot().getAttribute("metaModel"));

		assertEquals(1272, new File(PATH_OUTPUT).length());
		assertEquals(new File(PATH_REFERENCE).length(), new File(PATH_OUTPUT).length());
		assertTrue(Files.equal(new File(PATH_OUTPUT), new File(PATH_REFERENCE)));
	}
}
