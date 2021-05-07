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
import static org.junit.Assert.fail;

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

public class MigrateV119ToV120Test {

    private static String NAME = "MigrateV119ToV120";

    private static String FILENAME_PREMISE = "States.premise";
    private static String PATH_INPUT_PREMISE = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE);
    private static String PATH_OUTPUT_PREMISE = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE);
    private static String PATH_REFERENCE_PREMISE = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE);

    private static String FILENAME_PREMISE_IT = "StatesWithIllegalTransitions.premise";
    private static String PATH_INPUT_PREMISE_IT = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE_IT);
    private static String PATH_OUTPUT_PREMISE_IT = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE_IT);

    @Test
    public void testMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
        MigrationTestHelper.fileCopy(PATH_INPUT_PREMISE, PATH_OUTPUT_PREMISE);

        MigrationModel model = new MigrationModel(PATH_OUTPUT_PREMISE);
        MigrationStrategy.migrate(model, "1.20");
        model.save();

        // test migration
        assertTrue(model.wasChanged());
        assertEquals("1.20", model.getModelRoot().getAttribute("metaModel"));

        assertTrue(Files.equal(new File(PATH_OUTPUT_PREMISE), new File(PATH_REFERENCE_PREMISE)));
    }

    @Test
    public void testMigrationWithIllegalTransition()
            throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
        MigrationTestHelper.fileCopy(PATH_INPUT_PREMISE_IT, PATH_OUTPUT_PREMISE_IT);

        MigrationModel model = new MigrationModel(PATH_OUTPUT_PREMISE_IT);

        try {
            MigrationStrategy.migrate(model, "1.20");
            model.save();
            fail("Migration should have failed.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Transition"));
            assertEquals("1.19", model.getModelRoot().getAttribute("metaModel"));
        }
    }
}
