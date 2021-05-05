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

public class MigrateV118ToV119Test {

    private static String NAME = "MigrateV118ToV119";

    private static String FILENAME_PREMISE = "System.premise";
    private static String PATH_INPUT_PREMISE = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE);
    private static String PATH_OUTPUT_PREMISE = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE);
    private static String PATH_REFERENCE_PREMISE = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE);
    
    private static String FILENAME_REGISTRY = "Registry.registry";
    private static String PATH_INPUT_REGISTRY = MigrationTestHelper.getInputPath(NAME, FILENAME_REGISTRY);
    private static String PATH_OUTPUT_REGISTRY = MigrationTestHelper.getOutputPath(NAME, FILENAME_REGISTRY);
    private static String PATH_REFERENCE_REGISTRY = MigrationTestHelper.getReferencePath(NAME, FILENAME_REGISTRY);
    
    private static String FILENAME_USECASE = "Problem.usecase";
    private static String PATH_INPUT_USECASE = MigrationTestHelper.getInputPath(NAME, FILENAME_USECASE);
    private static String PATH_OUTPUT_USECASE = MigrationTestHelper.getOutputPath(NAME, FILENAME_USECASE);
    private static String PATH_REFERENCE_USECASE = MigrationTestHelper.getReferencePath(NAME, FILENAME_USECASE);
    
    private static String FILENAME_PREMISE_NO_XSI = "NoXSINamespace.premise";
    private static String PATH_INPUT_PREMISE_NO_XSI = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE_NO_XSI);
    private static String PATH_OUTPUT_PREMISE_NO_XSI = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE_NO_XSI);
    private static String PATH_REFERENCE_PREMISE_NO_XSI = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE_NO_XSI);
    
    private static String FILENAME_PREMISE_NO_UC = "NoUCNamespace.premise";
    private static String PATH_INPUT_PREMISE_NO_UC = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE_NO_UC);
    private static String PATH_OUTPUT_PREMISE_NO_UC = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE_NO_UC);
    private static String PATH_REFERENCE_PREMISE_NO_UC = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE_NO_UC);
    
    private static String FILENAME_USECASE_NO_UC = "NoUCNamespace.usecase";
    private static String PATH_INPUT_USECASE_NO_UC = MigrationTestHelper.getInputPath(NAME, FILENAME_USECASE_NO_UC);
    private static String PATH_OUTPUT_USECASE_NO_UC = MigrationTestHelper.getOutputPath(NAME, FILENAME_USECASE_NO_UC);
    private static String PATH_REFERENCE_USECASE_NO_UC = MigrationTestHelper.getReferencePath(NAME, FILENAME_USECASE_NO_UC);
    
    @Test
    public void testMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
        MigrationTestHelper.fileCopy(PATH_INPUT_PREMISE, PATH_OUTPUT_PREMISE);
        MigrationTestHelper.fileCopy(PATH_INPUT_USECASE, PATH_OUTPUT_USECASE);
        MigrationTestHelper.fileCopy(PATH_INPUT_REGISTRY, PATH_OUTPUT_REGISTRY);


        MigrationModel model = new MigrationModel(PATH_OUTPUT_PREMISE);
        MigrationStrategy.migrate(model, "1.19");
        model.save();

        // test migration
        assertTrue(model.wasChanged());
        assertEquals("1.19", model.getModelRoot().getAttribute("metaModel"));

        assertTrue(Files.equal(new File(PATH_OUTPUT_PREMISE), new File(PATH_REFERENCE_PREMISE)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_USECASE), new File(PATH_REFERENCE_USECASE)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_REGISTRY), new File(PATH_REFERENCE_REGISTRY)));
    }
    
    @Test
    public void testMigrationNoXsi() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
        MigrationTestHelper.fileCopy(PATH_INPUT_PREMISE_NO_XSI, PATH_OUTPUT_PREMISE_NO_XSI);

        MigrationModel model = new MigrationModel(PATH_OUTPUT_PREMISE_NO_XSI);
        MigrationStrategy.migrate(model, "1.19");
        model.save();

        // test migration
        assertTrue(model.wasChanged());
        assertEquals("1.19", model.getModelRoot().getAttribute("metaModel"));

        assertTrue(Files.equal(new File(PATH_OUTPUT_PREMISE_NO_XSI), new File(PATH_REFERENCE_PREMISE_NO_XSI)));
    }
    
    @Test
    public void testMigrationNoUc() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
        MigrationTestHelper.fileCopy(PATH_INPUT_PREMISE_NO_UC, PATH_OUTPUT_PREMISE_NO_UC);
        MigrationTestHelper.fileCopy(PATH_INPUT_USECASE_NO_UC, PATH_OUTPUT_USECASE_NO_UC);
        
        MigrationModel model = new MigrationModel(PATH_OUTPUT_PREMISE_NO_UC);
        MigrationStrategy.migrate(model, "1.19");
        model.save();

        // test migration
        assertTrue(model.wasChanged());
        assertEquals("1.19", model.getModelRoot().getAttribute("metaModel"));

        assertTrue(Files.equal(new File(PATH_OUTPUT_PREMISE_NO_UC), new File(PATH_REFERENCE_PREMISE_NO_UC)));
        assertTrue(Files.equal(new File(PATH_OUTPUT_USECASE_NO_UC), new File(PATH_REFERENCE_USECASE_NO_UC)));
    }
}
