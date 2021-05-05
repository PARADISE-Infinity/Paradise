/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.tradeoff.xtend;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xtext.EcoreUtil2;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.exchange.base.xtend.test.CharsetProvidingInMemoryFileSystemAccess;
import de.dlr.exchange.base.xtend.test.TestHelper;

/**
 * @author enge_do
 *
 */
public class GeneratorTest {

    private static final String PLUGIN_NAME = "de.dlr.exchange.excel.tradeoff.xtend";
    
    private static final long GEN_FILE_ABSOLUT_LENGTH = 15853l;
    private static final long GEN_FILE_RELATIVE_LENGTH = 15963l;

    private static java.net.URI[] PATHS_PREMISE =
            { de.dlr.premise.util.TestHelper.locateFile(PLUGIN_NAME, "test/data/Jubula.premise"),
              de.dlr.premise.util.TestHelper.locateFile(PLUGIN_NAME, "test/data/RCPTestingTool.premise"),
              de.dlr.premise.util.TestHelper.locateFile(PLUGIN_NAME, "test/data/RedDeer.premise"),
              de.dlr.premise.util.TestHelper.locateFile(PLUGIN_NAME, "test/data/Squish.premise"),
              de.dlr.premise.util.TestHelper.locateFile(PLUGIN_NAME, "test/data/SWTBot.premise") };

    private static java.net.URI PATH_USECASE_FILE =
            de.dlr.premise.util.TestHelper.locateFile(PLUGIN_NAME, "test/data/TestTool.usecase");
    private static java.net.URI PATH_TEMPLATE_FILE =
            de.dlr.premise.util.TestHelper.locateFile(PLUGIN_NAME, "test/data/TradeOffTemplate.xlsx");
    private static java.net.URI PATH_REFERENCE_FILE =
            de.dlr.premise.util.TestHelper.locateFile(PLUGIN_NAME, "test/data/Reference.premise");

    private static ResourceSet resSet;
    private static IWorkspaceRoot wsRoot;

    @Test
    public void testAbsolute() {

        AbsoluteTradeoffGenerator gen = new AbsoluteTradeoffGenerator();
        CharsetProvidingInMemoryFileSystemAccess fsa = TestHelper.createInMemoryFileSystemAccess();

        gen.doGenerateFromResources(resSet, new ArrayList<Resource>(), fsa); // call xtend script

        final String fileName = wsRoot.getRawLocation().toString() + "/trdff/" + 
                                    AbsoluteTradeoffGenerator.NAME_OUTPUT_FILE;
        
        assertEquals(GEN_FILE_ABSOLUT_LENGTH, TestHelper.saveFile(fsa, fileName).length(), 5);
    }

    @Test
    public void testRelative() {

        RelativeTradeoffGenerator gen = new RelativeTradeoffGenerator();
        CharsetProvidingInMemoryFileSystemAccess fsa = TestHelper.createInMemoryFileSystemAccess();

        gen.doGenerateFromResources(resSet, new ArrayList<Resource>(), fsa); // call xtend script
        
        final String fileName = wsRoot.getRawLocation().toString() + "/trdff/" + 
                            RelativeTradeoffGenerator.NAME_OUTPUT_FILE;
        
        assertEquals(GEN_FILE_RELATIVE_LENGTH, TestHelper.saveFile(fsa, fileName).length(), 5);
    }

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        resSet = new ResourceSetImpl();
        wsRoot = ResourcesPlugin.getWorkspace().getRoot();

        // create project in junit workspace
        IProject proj = wsRoot.getProject("trdff");
        proj.create(null);
        proj.open(null);

        // copy premise files into junit workspace
        for (java.net.URI uri : PATHS_PREMISE) {
            String filename = uri.toString();
            filename = filename.substring(Math.max(filename.lastIndexOf('\\'), filename.lastIndexOf('/')) + 1);
            IFile prRepoPlatformResource = wsRoot.getFile(new Path("trdff/" + filename));
            prRepoPlatformResource.create(new FileInputStream(new File(uri)), IResource.NONE, null);
            // load model from resource
            // Resource prRepoEMFResource =
            resSet.getResource(URI.createPlatformResourceURI("trdff/" + filename, true), true);
            // prRepoEMFResource.load(Collections.EMPTY_MAP);
        }

        // copy usecase file into junit workspace
        IFile ucRepoPlatformResource = wsRoot.getFile(new Path("trdff/TestTool.usecase"));
        ucRepoPlatformResource.create(new FileInputStream(new File(PATH_USECASE_FILE)), IResource.NONE, null);
        // copy template file into junit workspace
        IFile tpRepoPlatformResource = wsRoot.getFile(new Path("trdff/template.xlsx"));
        tpRepoPlatformResource.create(new FileInputStream(new File(PATH_TEMPLATE_FILE)), IResource.NONE, null);
        // copy reference file into junit workspace
        IFile refRepoPlatformResource = wsRoot.getFile(new Path("trdff/Reference.premise"));
        refRepoPlatformResource.create(new FileInputStream(new File(PATH_REFERENCE_FILE)), IResource.NONE, null);

        for (Resource r : (Resource[]) resSet.getResources().toArray()) {
            r.load(Collections.EMPTY_MAP);
            EcoreUtil2.resolveAll(resSet);
        }
    }
}
