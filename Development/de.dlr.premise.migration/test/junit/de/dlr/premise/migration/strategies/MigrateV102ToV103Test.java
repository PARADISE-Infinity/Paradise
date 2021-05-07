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

import de.dlr.premise.migration.MigrationMissingException;
import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.MigrationModelTest;
import de.dlr.premise.migration.MigrationStrategy;
import de.dlr.premise.migration.MigrationTestHelper;

public class MigrateV102ToV103Test extends MigrationModelTest {

	private static String PATH_INPUT = "test/data/MigrateV102ToV103/input/";
	private static String PATH_OUTPUT = "test/data/MigrateV102ToV103/output/";
	
	private static String INPUT_PREMISE = de.dlr.premise.util.TestHelper.locateFile(DE_DLR_PREMISE_MIGRATION, PATH_INPUT + "E-Car.premise").getPath().substring(1);
	private static String INPUT_REGISTRY = de.dlr.premise.util.TestHelper.locateFile(DE_DLR_PREMISE_MIGRATION, PATH_INPUT + "Functions/E-Car.registry").getPath().substring(1);
	private static String INPUT_FNPOOL = de.dlr.premise.util.TestHelper.locateFile(DE_DLR_PREMISE_MIGRATION, PATH_INPUT + "Functions/E-Car.functionpool").getPath().substring(1);
	
	private static String OUTPUT_PREMISE = de.dlr.premise.util.TestHelper.locateFile(DE_DLR_PREMISE_MIGRATION, PATH_OUTPUT + "E-Car.premise").getPath().substring(1);
	private static String OUTPUT_REGISTRY = de.dlr.premise.util.TestHelper.locateFile(DE_DLR_PREMISE_MIGRATION, PATH_OUTPUT + "Functions/E-Car.registry").getPath().substring(1);
	private static String OUTPUT_FNPOOL = de.dlr.premise.util.TestHelper.locateFile(DE_DLR_PREMISE_MIGRATION, PATH_OUTPUT + "Functions/E-Car.functionpool").getPath().substring(1);
	
	@Test
	public void testMigration() throws IOException, SAXException, ParserConfigurationException, MigrationMissingException {
		MigrationTestHelper.fileCopy(INPUT_PREMISE, OUTPUT_PREMISE);
		MigrationTestHelper.fileCopy(INPUT_REGISTRY, OUTPUT_REGISTRY);
		MigrationTestHelper.fileCopy(INPUT_FNPOOL, OUTPUT_FNPOOL);

		MigrationModel model = new MigrationModel(OUTPUT_PREMISE);
		MigrationStrategy.migrate(model, "1.03");
		model.save();

		assertTrue(model.wasChanged());
		assertEquals(model.getModelRoot().getAttribute("metaModel"), "1.03");
		
		assertEquals(8852, new File(OUTPUT_PREMISE).length());
		assertEquals(438, new File(OUTPUT_REGISTRY).length());
		assertEquals(1590, new File(OUTPUT_FNPOOL).length());
	}
}
