/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.impl.my;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.registry.ADataItem;
import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.RegistryHelper;
import de.dlr.premise.util.TestHelper;

/**
 * @author hschum
 *
 */
public class PremiseFactoryImplMyTest {

	protected static String FILE_PATH = de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE, "test/data/testRepository.xmi").getPath();
    protected ProjectRepository repository;
    private String uuid1;

	@BeforeClass
	public static void startUp() {
		// register extension factory (if not done)
		RegistryHelper.registerExtensionFactory("xmi", new XMIResourceFactoryImpl());		
	}

	@AfterClass
	public static void afterClass() {
	    TestHelper.unifyID(FILE_PATH);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// check registration of resource factory for .xmi extensions
		Object resFactory = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().get("xmi");
		assertEquals(XMIResourceFactoryImpl.class, resFactory.getClass());

		/* create a ProjectRepository with following structure:
		 * repository
		 * 		SC1
		 * 			param1
		 * 			param2
		 * 		SC2
		 */
		repository = SystemFactory.eINSTANCE.createProjectRepository();

		SystemComponent newSC;
		newSC = SystemFactory.eINSTANCE.createSystemComponent();
		newSC.setName("SC1");
		repository.getProjects().add(newSC);
		newSC = SystemFactory.eINSTANCE.createSystemComponent();
		newSC.setName("SC2");
		repository.getProjects().add(newSC);
		SystemComponent sc1 = repository.getProjects().get(0);
		Parameter newParam;
		
		newParam = SystemFactory.eINSTANCE.createParameter();
		newParam.setName("param1");
		sc1.getParameters().add(newParam);
		newParam = SystemFactory.eINSTANCE.createParameter();
		newParam.setName("param2");
		sc1.getParameters().add(newParam);

		uuid1 = repository.getProjects().get(0).getId();
	}

	/**
	 * Test method for {@link system.impl.my.PremiseCalcFactoryImplMy}.
	 */
	@Test
	public void testAutoIdCreationOnNewADataItems() {
		
		// check that uuids exist and that they are different
		assertNotNull(uuid1);
		assertNotSame(uuid1, repository.getProjects().get(1).getId());

		ADataItem item;
		int uuidLen = 36;
		item = SystemFactory.eINSTANCE.createSystemComponent();
		assertEquals(uuidLen, item.getId().length());
		
		item = SystemFactory.eINSTANCE.createParameter();
		assertNotNull(item.getId());
		assertEquals(uuidLen, item.getId().length());

		item = ElementFactory.eINSTANCE.createMode();
		assertEquals(uuidLen, item.getId().length());
	}

	/**
	 * Test method for {@link system.impl.my.PremiseCalcFactoryImplMy}.
	 */
	@Test
	public void testAutoIdCreationOnResourceLoad() {
		// delete uuid1, remember uuid2, and save model
		repository.getProjects().get(0).setId(null);
		String uuid2 = repository.getProjects().get(1).getId();

		long fileLen = PremiseHelper.saveResource(FILE_PATH, repository);
        assertEquals(469, fileLen);

		// check automated creation of UUID Strings on...
		// ...on loading model, creating new uuid1 and keeping uuid2
		try {
            repository = (ProjectRepository) PremiseHelper.loadResource(FILE_PATH);
        } catch (IOException e) {
            fail(e.getMessage());
        }

		String uuidNew = repository.getProjects().get(0).getId();
		assertNotNull(uuidNew);
		assertEquals(36, uuidNew.length());
		assertNotSame(uuidNew, uuid1);
		assertEquals(uuid2, repository.getProjects().get(1).getId());
	}
}
