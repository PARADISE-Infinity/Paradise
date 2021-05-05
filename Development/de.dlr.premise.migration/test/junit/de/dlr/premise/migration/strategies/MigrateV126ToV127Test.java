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

public class MigrateV126ToV127Test {

    private static String NAME = "MigrateV126ToV127";

    private static String FILENAME_REPRESENTATION = "My.representation";
    private static String PATH_INPUT_REPRESENTATION = MigrationTestHelper.getInputPath(NAME, FILENAME_REPRESENTATION);
    private static String PATH_OUTPUT_REPRESENTATION = MigrationTestHelper.getOutputPath(NAME, FILENAME_REPRESENTATION);
    private static String PATH_REFERENCE_REPRESENTATION = MigrationTestHelper.getReferencePath(NAME, FILENAME_REPRESENTATION);

   @Test
    public void testMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
        MigrationTestHelper.fileCopy(PATH_INPUT_REPRESENTATION, PATH_OUTPUT_REPRESENTATION);

        MigrationModel model = new MigrationModel(PATH_OUTPUT_REPRESENTATION);
        MigrationStrategy.migrate(model, "1.27");
        model.save();

        // test migration
        assertTrue(model.wasChanged());
        assertEquals("1.27", model.getModelRoot().getAttribute("metaModel"));

        assertTrue(Files.equal(new File(PATH_OUTPUT_REPRESENTATION), new File(PATH_REFERENCE_REPRESENTATION)));
    }

}
