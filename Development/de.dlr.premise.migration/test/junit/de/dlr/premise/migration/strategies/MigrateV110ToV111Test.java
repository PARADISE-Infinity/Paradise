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

public class MigrateV110ToV111Test {

    private static String NAME = "MigrateV110ToV111";
    
    private static String FILENAME_PREMISE = "MigrateV110ToV111.premise";
    private static String FILENAME_REGISTRY = "MigrateV110ToV111.registry";

    private static String PATH_INPUT_PREMISE = MigrationTestHelper.getInputPath(NAME,FILENAME_PREMISE);
    private static String PATH_OUTPUT_PREMISE = MigrationTestHelper.getOutputPath(NAME,FILENAME_PREMISE);
    private static String PATH_REFERENCE_PREMISE = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE);

    private static String PATH_INPUT_REGISTRY = MigrationTestHelper.getInputPath(NAME,FILENAME_REGISTRY);
    private static String PATH_OUTPUT_REGISTRY = MigrationTestHelper.getOutputPath(NAME,FILENAME_REGISTRY);
    private static String PATH_REFERENCE_REGISTRY = MigrationTestHelper.getReferencePath(NAME, FILENAME_REGISTRY);

	@Test
	public void testMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
	    
        // open input files and assert the assumed file lengths
        File inputPremise = new File(PATH_INPUT_PREMISE);
        assertEquals(2668, inputPremise.length());

        File inputRegistry = new File(PATH_INPUT_REGISTRY);
        assertEquals(491, inputRegistry.length());

        // copy files
        MigrationTestHelper.fileCopy(inputPremise, new File(PATH_OUTPUT_PREMISE));
        MigrationTestHelper.fileCopy(inputRegistry, new File(PATH_OUTPUT_REGISTRY));

		// migrate
		MigrationModel model = new MigrationModel(PATH_OUTPUT_PREMISE);
        MigrationStrategy.migrate(model, "1.11");
		model.save();

		// test migration
		assertTrue(model.wasChanged());
        assertEquals("1.11", model.getModelRoot().getAttribute("metaModel"));

        assertEquals(2545, new File(PATH_OUTPUT_PREMISE).length());
		assertTrue(Files.equal(new File(PATH_OUTPUT_PREMISE), new File(PATH_REFERENCE_PREMISE)));

        assertEquals(417, new File(PATH_OUTPUT_REGISTRY).length());
        assertTrue(Files.equal(new File(PATH_OUTPUT_REGISTRY), new File(PATH_REFERENCE_REGISTRY)));
	}
}
