/**
* Copyright (C) 2011-2021 systemsdesign.de, Germany
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Holger Schumann
*
*/

package de.dlr.premise.util;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.Ignore;

import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.registry.ADataItem;

/**
 * @author hschum
 * 
 */
@Ignore
public class TestHelper {

    public static final String DE_DLR_PREMISE = "de.dlr.premise";
    public static final String DE_DLR_PREMISE_EDIT = "de.dlr.premise.edit";
    public static final String DE_DLR_PREMISE_EDITOR = "de.dlr.premise.editor";
    public static final String DE_DLR_PREMISE_VALIDATOR = "de.dlr.premise.validation.ui";

    
    public static final String UUID_FORMAT = "00000000-0000-0000-0000-000000000000";

    public static void unifyID(String absolutePathToPremise) {
        ProjectRepository repository;
        try {
            repository = (ProjectRepository) PremiseHelper.loadResource(absolutePathToPremise);
            int i = 0;
            for(EObject item : PremiseHelper.getAll(repository, ADataItem.class)) {
                int idxLen = String.valueOf(i).length();
                String uuid = UUID_FORMAT.substring(0, UUID_FORMAT.length() - idxLen) + i++;
                ((ADataItem) item).setId(uuid);
            }
            PremiseHelper.saveResource(absolutePathToPremise, repository);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Creates a resource from a given file and returns it
     * @param filePath path and name to input file
     * @return created resource or fails junit test
     */
    public static Resource loadResource(String filePath) {
        Resource resource = null;
        // try to load file
        try {
            resource = PremiseHelper.loadResource(filePath).eResource();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        // try to load contents
        try {
            resource.getContents(); // try not null access
        } catch (Exception e1) {
            fail("Input file for tests could not be identified, ensure execution as plugin-test!");
        }
        return resource;
    }
    
    /**
     * Creates a resource from a given file and returns it
     * @param filePath path and name to input file
     * @return created resource or fails junit test
     */
    public static Resource loadResource(ResourceSet resSet, String filePath) {
        Resource resource = null;
        // try to load file
        try {
            resource = PremiseHelper.loadResource(resSet, filePath).eResource();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        // try to load contents
        try {
            resource.getContents(); // try not null access
        } catch (Exception e1) {
            fail("Input file for tests could not be identified, ensure execution as plugin-test!");
        }
        return resource;
    }

    /**
     * Locate a file within a plugin and return the absolute path
     * 
     * @param bundle bundleID
     * @param fullPath relative path to the file
     * @return absolute path to the file
     */
    public static java.net.URI locateFile(String bundle, String fullPath) {
        try {
            URL url = FileLocator.find(Platform.getBundle(bundle), new Path(fullPath), null);
            if (url != null)
                return FileLocator.resolve(url).toURI();
        } catch (Exception e) {
        }
        return null;
    }
    
    /**
     * A variant of {@link TestHelper#locateFile(String, String)} that also works for JUnit tests
     * 
     * @param bundle bundleID
     * @param fullPath relative path to the file
     * @return absolute path to the file if running as plugin test, fullPath otherwise
     */
    public static String locateFileForJUnit(String bundle, String fullPath) {
        // allows to run as a junit test
        if (!Platform.isRunning()) {
            return fullPath;
        }

        java.net.URI uri = locateFile(bundle, fullPath);
        return uri.getPath().substring(1);
    }

    /**
     * Replaces all occurrences of oldText in inputFile by newText and stores the new content in outputFile
     * @param inputFile
     * @param outputFile
     * @param oldText
     * @param newText
     */
    public static void replaceFileString(String inputFile, String outputFile, String oldText, String newText) {
        File file = new File(inputFile);
        String line;
        String output = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while((line = reader.readLine()) != null) {
                output += line.replaceAll(oldText, newText) + System.getProperty("line.separator");
            }
            reader.close();
            FileWriter writer = new FileWriter(outputFile);
            writer.write(output);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Exchanges all adapters of given class of the given root object and its contained child objects by the new adapter recursively
     * @param root
     * @param adapterClass
     * @param newAdapter
     */
    public static void exchangeAdapters(EObject root, Class<? extends Adapter> adapterClass, Adapter newAdapter) {
        EList<Adapter> list = root.eAdapters();
        for (int i = 0; i < list.size(); i++) {
            if (adapterClass.isInstance(list.get(i))) {
                list.remove(i);
                list.add(i, newAdapter);
            }
        }
        for (EObject obj : root.eContents()) {
            exchangeAdapters(obj, adapterClass, newAdapter);
        }
    }
    
    /**
     * Copy files from a file location into a work space folder location
     * Exemplary usage:
     *   IProject project = TestHelper.createProject("TestProject");
     *   //IFolder folder = project.getFolder(new Path("testFolder"));
     *   //folder.create(true, true, null);
     *   File srcFolder = new File(new File(TEST_FILE).getParent());
     *   TestHelper.copyFiles(project, srcFolder, null);
     * @param project       workspace project
     * @param srcFolder     source folder
     * @param fullPath      folder in workspace project
     * @throws Exception
     */
    public static void copyFiles (IProject project, File srcFolder, IFolder fullPath) throws Exception {
        for (File f: srcFolder.listFiles()) {
            if (f.isDirectory()) {
                IFolder newFolder = fullPath.getFolder(new Path(f.getName()));
                newFolder.create(true, true, null);
                copyFiles(project, f, newFolder);
            } else {
                IPath path = new Path(f.getName());
                IFile newFile = project.getFile(path);
                newFile.create(new FileInputStream(f), true, null);
            }
        }
    }
    
    public static IProject createProject(String name) {
        IProject project = null;

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();        
        if (root!= null) {
            project = root.getProject(name);
            try {
                project.create(null);
                project.open(null);
            } catch (CoreException e) {
            }
        }
        return project;
    }

    /**
     * Dynamically create an EObject that has a 1-to-n-containment reference to the given EObjects
     * 
     * @param children An array of EObjects
     * @return A container EObject as described above
     */
    public static EObject createCommonRoot(EObject... children) {
        EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
    
        EClass container = EcoreFactory.eINSTANCE.createEClass();
        pkg.getEClassifiers().add(container);
    
        EReference containerChildren = EcoreFactory.eINSTANCE.createEReference();
        containerChildren.setEType(EcorePackage.Literals.EOBJECT);
        containerChildren.setContainment(true);
        containerChildren.setUpperBound(-1);
        container.getEStructuralFeatures().add(containerChildren);
    
        EObject containerInstance = pkg.getEFactoryInstance().create(container);
    
        // Reflective EMF is inherently unsafe, but in this case we can be sure to get back the correct list, as we just set it up
        @SuppressWarnings("unchecked")
        EList<EObject> containerInstanceChildren = (EList<EObject>) containerInstance.eGet(containerChildren);
        containerInstanceChildren.addAll(Arrays.asList(children));
    
        return containerInstance;
    }
    
    /**
     * Creates a common {@link ResourceSet}, in which all given EObjects are contained.
     *  
     * It creates a {@link Resource} for each of the objects it is given. Those objects are then put into a {@link ResourceSet}, which is
     * returned.
     * 
     * @param children An array of EObjects
     * @return A {@link ResourceSet} containing all the objects
     */
    
    public static ResourceSet createCommonResourceSet(EObject... children) {
        ResourceSet resSet = new ResourceSetImpl();
        resSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        
        int i = 0;
        for (EObject child : children) {
            Resource res = resSet.createResource(URI.createURI("synthetic:/file" + i + ".xmi"));
            res.getContents().add(child);
            i++;
        }

        return resSet;
    }

    /**
     * Creates a common {@link ResourceSet}, in which all given EObjects are contained and which is backed by a real project in the workspace.
     * 
     * This method can only be called from an eclipse context where the workspace is available. In particular, it <b>can't</b> be called
     * from JUnit tests!
     * 
     * It creates a {@link Resource} for each of the objects it is given. Those objects are then put into a {@link ResourceSet}, which is
     * returned.
     * 
     * @param children An array of EObjects
     * @return A {@link ResourceSet} containing all the objects
     */
    public static ResourceSet createCommonResourceSetInProject(EObject... children) {
        // get the platform workspace and warn if it is not available
        IWorkspace workspace;
        try {
            workspace = ResourcesPlugin.getWorkspace();
        } catch (IllegalStateException e) {
            throw new RuntimeException("Platform workspace is not available!", e);
        }
        // Create a dummy-project for the resources to live in. This is not strictly necessary, but a lot of PARADISE code relies on
        // resources having a location on disk. To satisfy them, we provide one.
        IProject proj = workspace.getRoot().getProject("dummy-project");
        try {
            // delete and recreate the project, to remove stuff that might be there from prior tests.
            proj.delete(true, true, null);
            proj.create(null);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        ResourceSet resSet = new ResourceSetImpl();
        
        int i = 0;
        for (EObject child : children) {
            Resource res = resSet.createResource(URI.createPlatformResourceURI("/dummy-project/file" + i + ".xmi", true));
            res.getContents().add(child);
            i++;
        }

        return resSet;
    }
}
