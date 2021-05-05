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
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.Before;
import org.junit.Test;

import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.util.TestHelper;

/**
 * @author steh_ti
 */
public class SatisfiesValidationTest {

    private static String PATH_INPUT_FILE = de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_VALIDATOR, "test/data/SatisfiesValidationTest/System.premise").getPath();

	private static ProjectRepository repository = null;

	private Diagnostician diagnostician;
	private BasicDiagnostic diagnostic;

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
}
