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

import de.dlr.premise.migration.MigrationMissingException;
import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.MigrationStrategy;
import de.dlr.premise.migration.MigrationTestHelper;

public class MigrateV114ToV115Test {
	
    private static String NAME = "MigrateV114ToV115";

    private static String FILENAME_PREMISE_1 = "System1.premise";
    private static String FILENAME_PREMISE_2 = "System2.premise";
    private static String FILENAME_PREMISE_3 = "subfolder/System3.premise";
    private static String FILENAME_FNPOOL_1 = "Functionpool1.functionpool";
    private static String FILENAME_FNPOOL_2 = "Functionpool2.functionpool";
    private static String FILENAME_REPRESEN = "Representation.representation";

    private static String PATH_INPUT_PREMISE_1 = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE_1);
    private static String PATH_OUTPUT_PREMISE_1 = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE_1);
    private static String PATH_REFERENCE_PREMISE_1 = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE_1);

    private static String PATH_INPUT_PREMISE_2 = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE_2);
    private static String PATH_OUTPUT_PREMISE_2 = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE_2);
    private static String PATH_REFERENCE_PREMISE_2 = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE_2);

    private static String PATH_INPUT_PREMISE_3 = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE_3);
    private static String PATH_OUTPUT_PREMISE_3 = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE_3);
    private static String PATH_REFERENCE_PREMISE_3 = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE_3);

    private static String PATH_INPUT_FNPOOL_1 = MigrationTestHelper.getInputPath(NAME, FILENAME_FNPOOL_1);
    private static String PATH_OUTPUT_FNPOOL_1 = MigrationTestHelper.getOutputPath(NAME, FILENAME_FNPOOL_1);
    private static String PATH_REFERENCE_FNPOOL_1 = MigrationTestHelper.getReferencePath(NAME, FILENAME_FNPOOL_1);

    private static String PATH_INPUT_FNPOOL_2 = MigrationTestHelper.getInputPath(NAME, FILENAME_FNPOOL_2);
    private static String PATH_OUTPUT_FNPOOL_2 = MigrationTestHelper.getOutputPath(NAME, FILENAME_FNPOOL_2);
    private static String PATH_REFERENCE_FNPOOL_2 = MigrationTestHelper.getReferencePath(NAME, FILENAME_FNPOOL_2);

    private static String PATH_INPUT_REPRESEN = MigrationTestHelper.getInputPath(NAME, FILENAME_REPRESEN);
    private static String PATH_OUTPUT_REPRESEN = MigrationTestHelper.getOutputPath(NAME, FILENAME_REPRESEN);
    private static String PATH_REFERENCE_REPRESEN = MigrationTestHelper.getReferencePath(NAME, FILENAME_REPRESEN);

    @Test
    public void testMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
        // copy files
        MigrationTestHelper.fileCopy(PATH_INPUT_PREMISE_1, PATH_OUTPUT_PREMISE_1);
        MigrationTestHelper.fileCopy(PATH_INPUT_PREMISE_2, PATH_OUTPUT_PREMISE_2);
        MigrationTestHelper.fileCopy(PATH_INPUT_PREMISE_3, PATH_OUTPUT_PREMISE_3);
        MigrationTestHelper.fileCopy(PATH_INPUT_FNPOOL_1,  PATH_OUTPUT_FNPOOL_1 );
        MigrationTestHelper.fileCopy(PATH_INPUT_FNPOOL_2,  PATH_OUTPUT_FNPOOL_2 );
        MigrationTestHelper.fileCopy(PATH_INPUT_REPRESEN,  PATH_OUTPUT_REPRESEN );

        /**
         * Note: We do not use the usual pattern of comparing to a reference file here. We can't do this, because the migration assigns new
         * randomly generated UUIDs on each run. Thus, the generated files always differ, if only in that there are different values for the
         * UUIDs. Instead, we use file size as a proxy for file sameness.
         */

        // migrate System1
        MigrationModel model1 = new MigrationModel(PATH_OUTPUT_PREMISE_1);
        MigrationStrategy.migrate(model1, "1.15");
        model1.save();

        // test migration
        assertTrue(model1.wasChanged());
        assertEquals("1.15", model1.getModelRoot().getAttribute("metaModel"));

        // check that files were changed
        assertEquals(new File(PATH_OUTPUT_PREMISE_1).length(), new File(PATH_REFERENCE_PREMISE_1).length());
        assertEquals(new File(PATH_OUTPUT_FNPOOL_1).length(), new File(PATH_REFERENCE_FNPOOL_1).length());
        assertEquals(new File(PATH_OUTPUT_FNPOOL_2).length(), new File(PATH_REFERENCE_FNPOOL_2).length());
        assertEquals(new File(PATH_OUTPUT_REPRESEN).length(), new File(PATH_REFERENCE_REPRESEN).length());
        // check that System2 and System3 are still unchanged
        assertEquals(new File(PATH_OUTPUT_PREMISE_2).length(), new File(PATH_INPUT_PREMISE_2).length());
        assertEquals(new File(PATH_OUTPUT_PREMISE_3).length(), new File(PATH_INPUT_PREMISE_3).length());


        // migrate System2
        MigrationModel model2 = new MigrationModel(PATH_OUTPUT_PREMISE_2);
        MigrationStrategy.migrate(model2, "1.15");
        model2.save();

        // test migration
        assertTrue(model2.wasChanged());
        assertEquals("1.15", model2.getModelRoot().getAttribute("metaModel"));
        
        // check that files were changed
        assertEquals(new File(PATH_OUTPUT_PREMISE_1).length(), new File(PATH_REFERENCE_PREMISE_1).length());
        assertEquals(new File(PATH_OUTPUT_PREMISE_2).length(), new File(PATH_REFERENCE_PREMISE_2).length());
        assertEquals(new File(PATH_OUTPUT_FNPOOL_1).length(), new File(PATH_REFERENCE_FNPOOL_1).length());
        assertEquals(new File(PATH_OUTPUT_FNPOOL_2).length(), new File(PATH_REFERENCE_FNPOOL_2).length());
        assertEquals(new File(PATH_OUTPUT_REPRESEN).length(), new File(PATH_REFERENCE_REPRESEN).length());
        // check that System3 is still unchanged
        assertEquals(new File(PATH_OUTPUT_PREMISE_3).length(), new File(PATH_INPUT_PREMISE_3).length());


        // migrate System3
        MigrationModel model3 = new MigrationModel(PATH_OUTPUT_PREMISE_3);
        MigrationStrategy.migrate(model3, "1.15");
        model3.save();

        // test migration
        assertTrue(model3.wasChanged());
        assertEquals("1.15", model3.getModelRoot().getAttribute("metaModel"));

        // check that files were changed
        assertEquals(new File(PATH_OUTPUT_PREMISE_1).length(), new File(PATH_REFERENCE_PREMISE_1).length());
        assertEquals(new File(PATH_OUTPUT_PREMISE_2).length(), new File(PATH_REFERENCE_PREMISE_2).length());
        assertEquals(new File(PATH_OUTPUT_PREMISE_3).length(), new File(PATH_REFERENCE_PREMISE_3).length());
        assertEquals(new File(PATH_OUTPUT_FNPOOL_1).length(), new File(PATH_REFERENCE_FNPOOL_1).length());
        assertEquals(new File(PATH_OUTPUT_FNPOOL_2).length(), new File(PATH_REFERENCE_FNPOOL_2).length());
        assertEquals(new File(PATH_OUTPUT_REPRESEN).length(), new File(PATH_REFERENCE_REPRESEN).length());
    }
}
