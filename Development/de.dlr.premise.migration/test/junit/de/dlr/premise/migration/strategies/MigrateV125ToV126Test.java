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

public class MigrateV125ToV126Test {

    private static String NAME = "MigrateV125ToV126";

    private static String FILENAME_PREMISE = "My.system";
    private static String PATH_INPUT_PREMISE = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE);
    private static String PATH_OUTPUT_PREMISE = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE);
    private static String PATH_REFERENCE_PREMISE = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE);

    private static String FILENAME_USECASE = "My.function";
    private static String PATH_INPUT_USECASE = MigrationTestHelper.getInputPath(NAME, FILENAME_USECASE);
    private static String PATH_OUTPUT_USECASE = MigrationTestHelper.getOutputPath(NAME, FILENAME_USECASE);
    private static String PATH_REFERENCE_USECASE = MigrationTestHelper.getReferencePath(NAME, FILENAME_USECASE);

    private static String FILENAME_REGISTRY = "My.registry";
    private static String PATH_INPUT_REGISTRY = MigrationTestHelper.getInputPath(NAME, FILENAME_REGISTRY);
    private static String PATH_OUTPUT_REGISTRY = MigrationTestHelper.getOutputPath(NAME, FILENAME_REGISTRY);
    private static String PATH_REFERENCE_REGISTRY = MigrationTestHelper.getReferencePath(NAME, FILENAME_REGISTRY);

    private static String FILENAME_REGISTRY_2 = "My2.registry";
    private static String PATH_INPUT_REGISTRY_2 = MigrationTestHelper.getInputPath(NAME, FILENAME_REGISTRY_2);
    private static String PATH_OUTPUT_REGISTRY_2 = MigrationTestHelper.getOutputPath(NAME, FILENAME_REGISTRY_2);
    private static String PATH_REFERENCE_REGISTRY_2 = MigrationTestHelper.getReferencePath(NAME, FILENAME_REGISTRY_2);


    private static String FILENAME_FUNCTIONPOOL = "My.functionpool";
    private static String PATH_INPUT_FUNCTIONPOOL = MigrationTestHelper.getInputPath(NAME, FILENAME_FUNCTIONPOOL);
    private static String PATH_OUTPUT_FUNCTIONPOOL = MigrationTestHelper.getOutputPath(NAME, FILENAME_FUNCTIONPOOL);
    private static String PATH_REFERENCE_FUNCTIONPOOL = MigrationTestHelper.getReferencePath(NAME, FILENAME_FUNCTIONPOOL);

   @Test
    public void testMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
        MigrationTestHelper.fileCopy(PATH_INPUT_PREMISE, PATH_OUTPUT_PREMISE);
        MigrationTestHelper.fileCopy(PATH_INPUT_USECASE, PATH_OUTPUT_USECASE);
        MigrationTestHelper.fileCopy(PATH_INPUT_REGISTRY, PATH_OUTPUT_REGISTRY);
        MigrationTestHelper.fileCopy(PATH_INPUT_REGISTRY_2, PATH_OUTPUT_REGISTRY_2);
        MigrationTestHelper.fileCopy(PATH_INPUT_FUNCTIONPOOL, PATH_OUTPUT_FUNCTIONPOOL);

        MigrationModel model = new MigrationModel(PATH_OUTPUT_PREMISE);
        MigrationStrategy.migrate(model, "1.26");
        model.save();

        // test migration
        assertTrue(model.wasChanged());
        assertEquals("1.26", model.getModelRoot().getAttribute("metaModel"));

        assertTrue(Files.equal(new File(PATH_OUTPUT_PREMISE), new File(PATH_REFERENCE_PREMISE)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_USECASE), new File(PATH_REFERENCE_USECASE)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_REGISTRY), new File(PATH_REFERENCE_REGISTRY)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_REGISTRY_2), new File(PATH_REFERENCE_REGISTRY_2)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_FUNCTIONPOOL), new File(PATH_REFERENCE_FUNCTIONPOOL)));
    }

}
