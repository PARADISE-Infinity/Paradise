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

public class MigrateV123ToV124Test {

    private static String NAME = "MigrateV123ToV124";

    private static String FILENAME_PREMISE = "My.system";
    private static String PATH_INPUT_PREMISE = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE);
    private static String PATH_OUTPUT_PREMISE = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE);
    private static String PATH_REFERENCE_PREMISE = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE);

    private static String FILENAME_USECASE = "My.usecase";
    private static String PATH_INPUT_USECASE = MigrationTestHelper.getInputPath(NAME, FILENAME_USECASE);
    private static String PATH_OUTPUT_USECASE = MigrationTestHelper.getOutputPath(NAME, FILENAME_USECASE);
    private static String PATH_REFERENCE_USECASE = MigrationTestHelper.getReferencePath(NAME, FILENAME_USECASE);

    private static String FILENAME_COMPONENT = "My.component";
    private static String PATH_INPUT_COMPONENT = MigrationTestHelper.getInputPath(NAME, FILENAME_COMPONENT);
    private static String PATH_OUTPUT_COMPONENT = MigrationTestHelper.getOutputPath(NAME, FILENAME_COMPONENT);
    private static String PATH_REFERENCE_COMPONENT = MigrationTestHelper.getReferencePath(NAME, FILENAME_COMPONENT);

   @Test
    public void testMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
        MigrationTestHelper.fileCopy(PATH_INPUT_PREMISE, PATH_OUTPUT_PREMISE);
        MigrationTestHelper.fileCopy(PATH_INPUT_USECASE, PATH_OUTPUT_USECASE);
        MigrationTestHelper.fileCopy(PATH_INPUT_COMPONENT, PATH_OUTPUT_COMPONENT);

        MigrationModel model = new MigrationModel(PATH_OUTPUT_PREMISE);
        MigrationStrategy.migrate(model, "1.24");
        model.save();

        // test migration
        assertTrue(model.wasChanged());
        assertEquals("1.24", model.getModelRoot().getAttribute("metaModel"));

        assertTrue(Files.equal(new File(PATH_OUTPUT_PREMISE), new File(PATH_REFERENCE_PREMISE)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_USECASE), new File(PATH_REFERENCE_USECASE)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_COMPONENT), new File(PATH_REFERENCE_COMPONENT)));
    }

}
