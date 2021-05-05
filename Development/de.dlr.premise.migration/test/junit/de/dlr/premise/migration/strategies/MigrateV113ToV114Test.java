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

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.common.io.Files;

import de.dlr.premise.migration.MigrationMissingException;
import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.MigrationStrategy;
import de.dlr.premise.migration.MigrationTestHelper;

public class MigrateV113ToV114Test {
	
	private static String NAME = "MigrateV113ToV114";

    private static String FILENAME_PREMISE  = NAME + ".premise";
    private static String FILENAME_USECASE  = NAME + ".usecase";
    private static String FILENAME_REGISTRY = NAME + ".registry";
    private static String FILENAME_FNPOOL   = NAME + ".functionpool";
    private static String FILENAME_REPRESEN = NAME + ".representation";

    private static String PATH_INPUT_PREMISE     = MigrationTestHelper.getInputPath(NAME, FILENAME_PREMISE);
    private static String PATH_OUTPUT_PREMISE    = MigrationTestHelper.getOutputPath(NAME, FILENAME_PREMISE);
    private static String PATH_REFERENCE_PREMISE = MigrationTestHelper.getReferencePath(NAME, FILENAME_PREMISE);
    
    private static String PATH_INPUT_USECASE     = MigrationTestHelper.getInputPath(NAME, FILENAME_USECASE);
    private static String PATH_OUTPUT_USECASE    = MigrationTestHelper.getOutputPath(NAME, FILENAME_USECASE);
    private static String PATH_REFERENCE_USECASE = MigrationTestHelper.getReferencePath(NAME, FILENAME_USECASE);

    private static String PATH_INPUT_REGISTRY     = MigrationTestHelper.getInputPath(NAME, FILENAME_REGISTRY);
    private static String PATH_OUTPUT_REGISTRY    = MigrationTestHelper.getOutputPath(NAME, FILENAME_REGISTRY);
    private static String PATH_REFERENCE_REGISTRY = MigrationTestHelper.getReferencePath(NAME, FILENAME_REGISTRY);
    
    private static String PATH_INPUT_REPRESEN = MigrationTestHelper.getInputPath(NAME, FILENAME_REPRESEN);
    private static String PATH_OUTPUT_REPRESEN = MigrationTestHelper.getOutputPath(NAME, FILENAME_REPRESEN);
    private static String PATH_REFERENCE_REPRESEN = MigrationTestHelper.getReferencePath(NAME, FILENAME_REPRESEN);

    private static String PATH_INPUT_FNPOOL = MigrationTestHelper.getInputPath(NAME, FILENAME_FNPOOL);
    private static String PATH_OUTPUT_FNPOOL = MigrationTestHelper.getOutputPath(NAME, FILENAME_FNPOOL);
    private static String PATH_REFERENCE_FNPOOL = MigrationTestHelper.getReferencePath(NAME, FILENAME_FNPOOL);
    
    private static File inputPremise = new File(PATH_INPUT_PREMISE);
    private static File inputUseCase = new File(PATH_INPUT_USECASE);
    private static File inputRegistry = new File(PATH_INPUT_REGISTRY);
    private static File inputFunctionpool = new File(PATH_INPUT_FNPOOL);
    private static File inputRepresentation = new File(PATH_INPUT_REPRESEN);

    @Before
    public void setup() throws IOException {
    	
        MigrationTestHelper.fileCopy(inputPremise, new File(PATH_OUTPUT_PREMISE));
        MigrationTestHelper.fileCopy(inputUseCase, new File(PATH_OUTPUT_USECASE));
        MigrationTestHelper.fileCopy(inputRegistry, new File(PATH_OUTPUT_REGISTRY));
        MigrationTestHelper.fileCopy(inputFunctionpool, new File(PATH_OUTPUT_FNPOOL));
        MigrationTestHelper.fileCopy(inputRepresentation, new File(PATH_OUTPUT_REPRESEN));
    }
    
	@Test
	public void testPremiseMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {

		// migrate premise model
		MigrationModel model = new MigrationModel(PATH_OUTPUT_PREMISE);
        MigrationStrategy.migrate(model, "1.14");
		model.save();

		// test migration
		File ref = new File(PATH_REFERENCE_PREMISE);
	    File out = new File(PATH_OUTPUT_PREMISE);
		assertEquals("1.14", model.getModelRoot().getAttribute("metaModel"));
		assertEquals(ref.length(), out.length());
	}
	
	@Test
	public void testUsecaseMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {

		// migrate premise model
		MigrationModel model = new MigrationModel(PATH_OUTPUT_USECASE);
        MigrationStrategy.migrate(model, "1.14");
		model.save();

		// test migration
        assertTrue(model.wasChanged());
		assertEquals("1.14", model.getModelRoot().getAttribute("metaModel"));

		File out = new File(PATH_OUTPUT_USECASE);
		File ref = new File(PATH_REFERENCE_USECASE);
		
		assertEquals(1846, out.length());
        assertEquals(ref.length(), out.length());
        assertTrue(Files.equal(ref, out));
	}

	@Test
	public void testRegistryMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
		
		// migrate premise model
		MigrationModel model = new MigrationModel(PATH_OUTPUT_REGISTRY);
        MigrationStrategy.migrate(model, "1.14");
		model.save();

		// test migration
		assertTrue(model.wasChanged());
        assertEquals("1.14", model.getModelRoot().getAttribute("metaModel"));
        assertEquals((new File(PATH_REFERENCE_REGISTRY)).length(), (new File(PATH_OUTPUT_REGISTRY)).length());
	}

	@Test
	public void testFunctionPoolMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
		
		// migrate premise model
		MigrationModel model = new MigrationModel(PATH_OUTPUT_FNPOOL);
        MigrationStrategy.migrate(model, "1.14");
		model.save();

		// test migration
        File out = new File(PATH_OUTPUT_FNPOOL);
        File ref = new File(PATH_REFERENCE_FNPOOL);

        assertTrue(model.wasChanged());
        assertEquals("1.14", model.getModelRoot().getAttribute("metaModel"));
        
        assertEquals(307, out.length());
        assertEquals(ref.length(), out.length());
	}
	
	@Test
	public void testRepresentationMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {

		// migrate premise model
		MigrationModel model = new MigrationModel(PATH_OUTPUT_REPRESEN);
        MigrationStrategy.migrate(model, "1.14");
		model.save();

		// test migration
		assertTrue(model.wasChanged());
        assertEquals("1.14", model.getModelRoot().getAttribute("metaModel"));
        assertEquals((new File(PATH_REFERENCE_REPRESEN)).length(), (new File(PATH_OUTPUT_REPRESEN)).length());
	}}
