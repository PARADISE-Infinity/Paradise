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

public class MigrateV116ToV117Test {
    
    private static String NAME = "MigrateV116ToV117";

    private static String FILENAME_PREMISE = "Migrate.premise";
    private static String FILENAME_PREMISE_COPY = "CopyMigrate.premise";
    private static String FILENAME_FNPOOL= "GlobalFuncDefs.premise.functionpool";
    private static String FILENAME_REGISTRY_1 = "Registry.premise.registry";
    private static String FILENAME_REGISTRY_2 = "Other.registry";

    private static String PATH_INPUT_PREMISE = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE);
    private static String PATH_OUTPUT_PREMISE = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE);
    private static String PATH_REFERENCE_PREMISE = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE);
    
    private static String PATH_INPUT_PREMISE_COPY = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE_COPY);
    private static String PATH_OUTPUT_PREMISE_COPY = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE_COPY);
    private static String PATH_REFERENCE_PREMISE_COPY = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE_COPY);
    
    private static String PATH_INPUT_FNPOOL = MigrationTestHelper.getInputPath(NAME, FILENAME_FNPOOL);
    private static String PATH_OUTPUT_FNPOOL = MigrationTestHelper.getOutputPath(NAME, FILENAME_FNPOOL);
    private static String PATH_REFERENCE_FNPOOL = MigrationTestHelper.getReferencePath(NAME, FILENAME_FNPOOL);

    private static String PATH_INPUT_REGISTRY_1 = MigrationTestHelper.getInputPath(NAME, FILENAME_REGISTRY_1);
    private static String PATH_OUTPUT_REGISTRY_1 = MigrationTestHelper.getOutputPath(NAME, FILENAME_REGISTRY_1);
    private static String PATH_REFERENCE_REGISTRY_1 = MigrationTestHelper.getReferencePath(NAME, FILENAME_REGISTRY_1);
    
    private static String PATH_INPUT_REGISTRY_2 = MigrationTestHelper.getInputPath(NAME, FILENAME_REGISTRY_2);
    private static String PATH_OUTPUT_REGISTRY_2 = MigrationTestHelper.getOutputPath(NAME, FILENAME_REGISTRY_2);
    private static String PATH_REFERENCE_REGISTRY_2 = MigrationTestHelper.getReferencePath(NAME, FILENAME_REGISTRY_2);
    
    @Test
    public void testMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
        MigrationTestHelper.fileCopy(PATH_INPUT_PREMISE, PATH_OUTPUT_PREMISE);
        MigrationTestHelper.fileCopy(PATH_INPUT_PREMISE_COPY, PATH_OUTPUT_PREMISE_COPY);
        MigrationTestHelper.fileCopy(PATH_INPUT_FNPOOL, PATH_OUTPUT_FNPOOL);
        MigrationTestHelper.fileCopy(PATH_INPUT_REGISTRY_1, PATH_OUTPUT_REGISTRY_1);
        MigrationTestHelper.fileCopy(PATH_INPUT_REGISTRY_2, PATH_OUTPUT_REGISTRY_2);
        
        MigrationModel model = new MigrationModel(PATH_OUTPUT_PREMISE);
        MigrationStrategy.migrate(model, "1.17");
        model.save();
        
        // test migration
        assertTrue(model.wasChanged());
        assertEquals("1.17", model.getModelRoot().getAttribute("metaModel"));
        
        // test migration of copy (this should not add another SumOverAllModes to the functionpool)
        MigrationModel modelCopy = new MigrationModel(PATH_OUTPUT_PREMISE_COPY);
        MigrationStrategy.migrate(modelCopy, "1.17");
        modelCopy.save();
        

        assertTrue(Files.equal(new File(PATH_OUTPUT_PREMISE), new File(PATH_REFERENCE_PREMISE)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_PREMISE_COPY), new File(PATH_REFERENCE_PREMISE_COPY)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_FNPOOL), new File(PATH_REFERENCE_FNPOOL)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_REGISTRY_1), new File(PATH_REFERENCE_REGISTRY_1)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_REGISTRY_2), new File(PATH_REFERENCE_REGISTRY_2)));
    }
}
