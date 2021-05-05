/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.io.Files;

import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.ModelVersion;
import de.dlr.premise.util.TestHelper;

public class MigrationModelTest {

    public static final String DE_DLR_PREMISE_MIGRATION = "de.dlr.premise.migration";
    
    private final static String FILE = TestHelper.locateFile(DE_DLR_PREMISE_MIGRATION, "test/data/MigrateModel/MigrateV111ToV112.premise").getPath();
    private final static String COPY_PATH = TestHelper.locateFile(DE_DLR_PREMISE_MIGRATION, "test/data/MigrateModel").getPath();
    private final static String COPY_NAME = "COPY.premise";
    private static String COPY;
    private final static String ENCODING = TestHelper.locateFile(DE_DLR_PREMISE_MIGRATION, "test/data/MigrateModel/ENCODING.premise").getPath();
	
	@BeforeClass
	public static void setUp () throws Exception {
		COPY = (new File(COPY_PATH,COPY_NAME)).toString();		
		Files.copy(new File(FILE), new File(COPY));
	}

	@AfterClass
	public static void tearDown () {
		File file = new File(COPY);
		if (file.exists()) {
			file.delete();
		}		
	}
	
	@Test
	public void testMigrationModel() throws Exception {
		MigrationModel miModel = new MigrationModel(FILE);
		assertNotNull(miModel);
		
		assertFalse(miModel.wasChanged());
		assertEquals(ModelVersion.V112.toString(), miModel.getVersion());
	}

	@Test
	public void testSetChangeAndWasChanged() throws Exception {
		MigrationModel miModel = new MigrationModel(FILE);
		assertNotNull(miModel);
		
		assertFalse(miModel.wasChanged());
		
		miModel.setChange();
		assertTrue(miModel.wasChanged());
	}

	@Test
	public void testGetModelRoot() throws Exception {
		MigrationModel miModel = new MigrationModel(FILE);
		assertNotNull(miModel);
		
		assertNotNull(miModel.getModelRoot());
	}

	@Test
	public void testGetReferencedFiles() throws Exception {
		MigrationModel miModel = new MigrationModel(FILE);
		assertNotNull(miModel);
		
		Map<String, Document> files = miModel.getReferencedFiles();
		assertEquals(2, files.size());
	}

	@Test
	public void testSetAndGetVersion() throws Exception {
		MigrationModel miModel = new MigrationModel(FILE);
		assertNotNull(miModel);

		assertEquals(ModelVersion.V112.toString(), miModel.getVersion());
		
		miModel.setVersion(ModelVersion.V111.toString());
		assertEquals(ModelVersion.V111.toString(), miModel.getVersion());
	}

	@Test
	public void testSave() throws Exception {
		
		MigrationModel miModel = new MigrationModel(COPY);
		assertNotNull(miModel);

		Element root = miModel.getModelRoot();
		NodeList elems = root.getElementsByTagName("projects");
		assertEquals(2, elems.getLength());
		
		Node prj = elems.item(0);
		Node name = prj.getAttributes().getNamedItem("name");
		assertNotNull(name);
		assertEquals("Alpha", name.getTextContent());
		
		name.setTextContent("Äquator");
		miModel.setChange();
		miModel.save();
		
		miModel = null;
		
		// check correct encoded file
		miModel = new MigrationModel(COPY);
		assertNotNull(miModel);

		elems = miModel.getModelRoot().getElementsByTagName("projects");
 		assertEquals(2, elems.getLength());
		
		name = elems.item(0).getAttributes().getNamedItem("name");
		assertNotNull(name);
		assertEquals("Äquator", name.getTextContent());

	}
	
	@Test
	public void testEncoding () throws Exception {
		
		MigrationModel miModel = new MigrationModel(ENCODING);
		assertNotNull(miModel);

		NodeList elems = miModel.getModelRoot().getElementsByTagName("projects");
		assertEquals(1, elems.getLength());
		
		Node name = elems.item(0).getAttributes().getNamedItem("name");
		assertNotNull(name);
		assertEquals("Äquator", name.getTextContent());
		
		miModel.setChange();
		miModel.save();		
		miModel = null;
		
		// check correct encoded file
		miModel = new MigrationModel(ENCODING);
		assertNotNull(miModel);

		elems = miModel.getModelRoot().getElementsByTagName("projects");
 		assertEquals(1, elems.getLength());
		
		name = elems.item(0).getAttributes().getNamedItem("name");
		assertNotNull(name);
		assertEquals("Äquator", name.getTextContent());		
	}
}
