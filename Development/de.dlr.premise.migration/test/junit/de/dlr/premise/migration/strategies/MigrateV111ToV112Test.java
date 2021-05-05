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

public class MigrateV111ToV112Test {

    private static String NAME = "MigrateV111ToV112";
    
    private static String FILENAME_PREMISE = "MigrateV111ToV112.premise";
    private static String FILENAME_REGISTRY = "MigrateV111ToV112.registry";
    private static String FILENAME_USECASE = "MigrateV111ToV112.usecase";

    private static String PATH_INPUT_PREMISE = MigrationTestHelper.getInputPath(NAME,FILENAME_PREMISE);
    private static String PATH_OUTPUT_PREMISE = MigrationTestHelper.getOutputPath(NAME,FILENAME_PREMISE);
    private static String PATH_REFERENCE_PREMISE = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE);

    private static String PATH_INPUT_REGISTRY = MigrationTestHelper.getInputPath(NAME,FILENAME_REGISTRY);
    private static String PATH_OUTPUT_REGISTRY = MigrationTestHelper.getOutputPath(NAME,FILENAME_REGISTRY);
    private static String PATH_REFERENCE_REGISTRY = MigrationTestHelper.getReferencePath(NAME, FILENAME_REGISTRY);
    
    private static String PATH_INPUT_USECASE = MigrationTestHelper.getInputPath(NAME,FILENAME_USECASE);
    private static String PATH_OUTPUT_USECASE = MigrationTestHelper.getOutputPath(NAME,FILENAME_USECASE);
    private static String PATH_REFERENCE_USECASE = MigrationTestHelper.getReferencePath(NAME, FILENAME_USECASE);

	@Test
	public void testMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
	    
        // open input files and assert the assumed file lengths
        File inputPremise = new File(PATH_INPUT_PREMISE);
        assertEquals(3203, inputPremise.length());

        File inputRegistry = new File(PATH_INPUT_REGISTRY);
        assertEquals(430, inputRegistry.length());

        File inputUseCase = new File(PATH_INPUT_USECASE);
        assertEquals(3048, inputUseCase.length());


        // copy files
        MigrationTestHelper.fileCopy(inputPremise, new File(PATH_OUTPUT_PREMISE));
        MigrationTestHelper.fileCopy(inputRegistry, new File(PATH_OUTPUT_REGISTRY));
        MigrationTestHelper.fileCopy(inputUseCase, new File(PATH_OUTPUT_USECASE));

		// migrate
		MigrationModel model = new MigrationModel(PATH_OUTPUT_PREMISE);
        MigrationStrategy.migrate(model, "1.12");
		model.save();

		// test migration
		assertTrue(model.wasChanged());
        assertEquals("1.12", model.getModelRoot().getAttribute("metaModel"));

		assertTrue(Files.equal(new File(PATH_OUTPUT_PREMISE), new File(PATH_REFERENCE_PREMISE)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_REGISTRY), new File(PATH_REFERENCE_REGISTRY)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_USECASE), new File(PATH_REFERENCE_USECASE)));
	}
}
