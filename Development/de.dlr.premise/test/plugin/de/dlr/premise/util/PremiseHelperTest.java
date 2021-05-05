/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;

/**
 *
 */
public class PremiseHelperTest {

    private static String PATH_INPUT_FILE = de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE,
            "test/data/PremiseHelperTestCases.premise").getPath();
    
    private static ProjectRepository rep;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // load model from .premise file
        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);

        // initialize top components
        rep = ((ProjectRepository) resource.getContents().get(0));
    }

    @Test
    public void testPremiseHelper() {
    	assertNotNull(new PremiseHelper());
    }
    
    @Test
    public void testGetQualifyingNamePrefix() {
        String prefix;
        SystemComponent root = rep.getProjects().get(0);

        // test if one of duplicated components has a null name parent
        SystemComponent scWithNoNameParent = root.getReferencedChildren().get(0).getReferencedChildren().get(0);
        prefix = PremiseHelper.getQualifyingNamePrefix(scWithNoNameParent);
        assertEquals(".", prefix);

        // test if duplicated component has a prefix
        SystemComponent scWithNameParent = root.getReferencedChildren().get(1).getReferencedChildren().get(0);
        prefix = PremiseHelper.getQualifyingNamePrefix(scWithNameParent);
        assertEquals("ParentWithName.", prefix);

        // test if duplicated component with same parent has a prefix
        SystemComponent scWithSameParent = root.getReferencedChildren().get(1).getReferencedChildren().get(1);
        prefix = PremiseHelper.getQualifyingNamePrefix(scWithSameParent);
        assertEquals("getQualifyingNamePrefix.ParentWithName.", prefix);
    }

    /**
     *
     * This method is meant to test the getResourceAbsPath method, which returns the absolute path to a the folder a model elements resource
     * is stored in. Since this method is inherently dependent on the execution environment, it is rather difficult to test.
     *
     * In this test, the method is tested with a resource that resides inside an eclipse project and is physically located in the workspace.
     * The following things are tested:
     * <ul>
     * <li>The path returned should end in with the path used for the test project
     * <li>The path should start with the path to the workspace the test is run in. Note that we can't test against a static string value
     * here, since we can't predict the location of the JUnit workspace.
     * </ul>
     *
     * Note: This test in its current implementation is platform dependent. It WILL fail if run in a non-windows context.
     *
     * @throws CoreException
     */
    @Test
    public void testGetResourceAbsPathForPlatformResource() throws CoreException {
        // create a project structure to work with
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("TestProject");
        project.create(null);
        project.open(null);

        IFolder folder = project.getFolder("testFolder");
        folder.create(true, true, null);

        // create resource set and add model into resource
        ResourceSet resSet = new ResourceSetImpl();
        Resource res = resSet.createResource(URI.createPlatformResourceURI("TestProject/testFolder/test.premise", true));
        EObject model = SystemFactory.eINSTANCE.createProjectRepository();
        res.getContents().add(model);

        // do the model path getting
        String resourceAbsPath = PremiseHelper.getResourceAbsPath(model);

        // check path beginning
        assertTrue(resourceAbsPath.startsWith(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString()));

        // check path end
        assertTrue(resourceAbsPath.endsWith("TestProject\\testFolder"));

        // check if file exists in file system
        assertTrue((new File(resourceAbsPath)).exists());

        // clean up
        project.delete(true, null);
    }

    /**
     * This method is meant to test the getResourceAbsPath method, which returns the absolute path to a the folder a model elements resource
     * is stored in. Since this method is inherently dependent on the execution environment, it is rather difficult to test.
     *
     * In this test, the method is tested with a model that is not stored in any resource. Therefore, its resouceAbsPath should be null.
     */
    @Test
    public void testGetResourceAbsPathForNoResource() {
        EObject model = SystemFactory.eINSTANCE.createProjectRepository();
        String resourceAbsPath = PremiseHelper.getResourceAbsPath(model);
        assertNull(resourceAbsPath);
    }

    /**
     * This method is meant to test the getResourceAbsPath method, which returns the absolute path to a the folder a model elements resource
     * is stored in. Since this method is inherently dependent on the execution environment, it is rather difficult to test.
     *
     * In this test, the method is tested with a model that resides in a resource somewhere in the file system, but not inside a workspace.
     */
    @Test
    public void testGetResoureAbsPathFileSystemResource() {
        String resourceAbsPath = PremiseHelper.getResourceAbsPath(rep);

        // check if file exists in file system
        assertTrue((new File(resourceAbsPath)).exists());

        // check if the returned path is a the path to the parent of the given file
        assertTrue(new File(PATH_INPUT_FILE).getParent().equals(resourceAbsPath));
    }

    @Test
    public void testSaveResource() {
        String FILE_PATH = TestHelper.locateFile(TestHelper.DE_DLR_PREMISE, "test/data/refRepository.xmi").getPath();

        ProjectRepository rep = SystemFactory.eINSTANCE.createProjectRepository();
        long fileLen = PremiseHelper.saveResource(FILE_PATH, rep);
        assertEquals(185, fileLen);

    }

    @Test
    public void testSameID() {
        
        SystemComponent componentA = SystemFactory.eINSTANCE.createSystemComponent();
        SystemComponent componentB = componentA;
        SystemComponent componentC = SystemFactory.eINSTANCE.createSystemComponent();
        
        assertTrue(PremiseHelper.sameID(componentA, componentB));
        assertFalse(PremiseHelper.sameID(componentA, componentC));
    }
}
