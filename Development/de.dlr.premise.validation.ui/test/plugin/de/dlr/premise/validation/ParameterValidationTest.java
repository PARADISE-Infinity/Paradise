/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.util.TestHelper;

/**
 * @author steh_ti
 */
public class ParameterValidationTest {

    private static String PATH_INPUT_FILE = de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_VALIDATOR, "test/data/ParameterValidationTest.premise").getPath();
    private static String PATH_FUNCPOOL_FILE = de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_VALIDATOR, "test/data/_GLOBAL/GlobalFuncDefs.premise.functionpool").getPath();
    private static String PATH_EXCEL_FILE = de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_VALIDATOR, "test/data/_GLOBAL/GlobalCalcFunctions.xls").getPath();
   

	private static ProjectRepository repository = null;

	private Diagnostician diagnostician;
	private BasicDiagnostic diagnostic;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	    TestHelper.replaceFileString(PATH_FUNCPOOL_FILE, PATH_FUNCPOOL_FILE, "fileURI=\"test/data/_GLOBAL/GlobalCalcFunctions.xls\"", "fileURI=\"" + PATH_EXCEL_FILE.substring(1) + "\"");
	    ValdationTestHelper.registerOCL();
	}
	
    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestHelper.replaceFileString(PATH_FUNCPOOL_FILE, PATH_FUNCPOOL_FILE, "fileURI=\"" + PATH_EXCEL_FILE.substring(1)  + "\"", "fileURI=\"test/data/_GLOBAL/GlobalCalcFunctions.xls\"");
    }

	@Before
	public void setUp() throws Exception {
		Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);
        EcoreUtil.resolveAll(resource.getResourceSet());
		repository = (ProjectRepository) resource.getContents().get(0);

		diagnostician = new Diagnostician();
		diagnostic = diagnostician.createDefaultDiagnostic(repository);
	}

	@Test
	public void testValid() {
		assertTrue(diagnostician.validate(repository, diagnostic));
		assertEquals(0, diagnostic.getChildren().size());
	}

	@Test
	public void testMissingSourceValue() {
		// remove value of source parameter
		repository.getProjects().get(0).getParameters().get(0).setValue(null);

		assertFalse(diagnostician.validate(repository, diagnostic));
		assertEquals(1, diagnostic.getChildren().size());
	}
}
