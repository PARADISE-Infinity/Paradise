/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
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

public class MigrateV104ToV105Test {
	private static String NAME = "MigrateV104ToV105";

	private static String FILENAME_PREMISE = "MigrateV104ToV105.premise";
	private static String FILENAME_REGISTRY = "MigrateV104ToV105.registry";
	
	private static String PATH_INPUT_PREMISE = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE);
	private static String PATH_INPUT_REGISTRY = MigrationTestHelper.getInputPath(NAME, FILENAME_REGISTRY);
	private static String PATH_OUTPUT_PREMISE = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE);
	private static String PATH_OUTPUT_REGISTRY = MigrationTestHelper.getOutputPath(NAME, FILENAME_REGISTRY);
	private static String PATH_REFERENCE_PREMISE = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE);
	private static String PATH_REFERENCE_REGISTRY = MigrationTestHelper.getReferencePath(NAME, FILENAME_REGISTRY);

	@Test
	public void testMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
		MigrationTestHelper.fileCopy(PATH_INPUT_PREMISE, PATH_OUTPUT_PREMISE);
		MigrationTestHelper.fileCopy(PATH_INPUT_REGISTRY, PATH_OUTPUT_REGISTRY);

		MigrationModel model = new MigrationModel(PATH_OUTPUT_PREMISE);
		MigrationStrategy.migrate(model, "1.05");
		model.save();

		assertTrue(model.wasChanged());
		assertEquals("1.05", model.getModelRoot().getAttribute("metaModel"));
		assertEquals(1517, new File(PATH_OUTPUT_PREMISE).length());
		assertTrue(Files.equal(new File(PATH_OUTPUT_PREMISE), new File(PATH_REFERENCE_PREMISE)));
		assertEquals(414, new File(PATH_OUTPUT_REGISTRY).length());
		assertTrue(Files.equal(new File(PATH_OUTPUT_REGISTRY), new File(PATH_REFERENCE_REGISTRY)));
	}
}
