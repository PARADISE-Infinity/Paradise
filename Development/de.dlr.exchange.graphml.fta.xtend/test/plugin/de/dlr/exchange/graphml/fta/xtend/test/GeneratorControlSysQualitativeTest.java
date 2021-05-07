/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.fta.xtend.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.exchange.base.xtend.test.CharsetProvidingInMemoryFileSystemAccess;
import de.dlr.exchange.base.xtend.test.TestHelper;
import de.dlr.exchange.graphml.fta.xtend.Generator;
import de.dlr.exchange.graphml.fta.xtend.Options;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.util.PremiseHelper;

public class GeneratorControlSysQualitativeTest {

    private static String PATH_INPUT_FILE =
            de.dlr.premise.util.TestHelper.locateFile("de.dlr.exchange.graphml.fta.xtend", "test/data/ControlSystem.premise").getPath();
    private static String PATH_OUTPUT_FILE =
            de.dlr.premise.util.TestHelper.locateFile("de.dlr.exchange.graphml.fta.xtend", "test/data/ControlSystem.qual-fta.graphml")
                                          .getPath();
    private static String PATH_FUNCPOOL_FILE = de.dlr.premise.util.TestHelper
                                                                             .locateFile("de.dlr.exchange.graphml.fta.xtend",
                                                                                         "test/data/_GLOBAL/GlobalFuncDefs.premise.functionpool")
                                                                             .getPath();
    private static String PATH_EXCEL_FILE =
            de.dlr.premise.util.TestHelper.locateFile("de.dlr.exchange.graphml.fta.xtend", "test/data/_GLOBAL/GlobalCalcFunctions.xls")
                                          .getPath();

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestHelper.replaceFileString(PATH_FUNCPOOL_FILE, PATH_FUNCPOOL_FILE, "fileURI=\"test/data/_GLOBAL/GlobalCalcFunctions.xls\"",
                                     "fileURI=\"" + PATH_EXCEL_FILE.substring(1) + "\"");
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestHelper.replaceFileString(PATH_FUNCPOOL_FILE, PATH_FUNCPOOL_FILE, "fileURI=\"" + PATH_EXCEL_FILE.substring(1) + "\"",
                                     "fileURI=\"test/data/_GLOBAL/GlobalCalcFunctions.xls\"");
    }

    @Test
    public void test() {
        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);
        ArrayList<Resource> selectedFiles = new ArrayList<>();
        selectedFiles.add(resource);

        // remove all 3 TransitionBalancings
        ProjectRepository rep = (ProjectRepository) resource.getContents().get(0);
        SystemComponent sc = rep.getProjects().get(0);
        PremiseHelper.getTransitions(sc).get(0).getBalancings().clear();
        
        for (SystemComponent sysComp : sc.getReferencedChildren()) {
            PremiseHelper.getTransitions(sysComp).get(0).getBalancings().clear();
        }
        

        CharsetProvidingInMemoryFileSystemAccess fsa = TestHelper.createInMemoryFileSystemAccess();
        Generator gen = new Generator();
        gen.doGenerateFromResources(resource.getResourceSet(), selectedFiles, fsa); // call xtend script

        assertEquals(41360, TestHelper.saveFile(fsa, PATH_OUTPUT_FILE).length());

        // exclude "FailureRate" from considered parameters in option
        (new Options()).setOptions(rep);
        PremiseHelper.getMetaData(rep, Generator.OPT_PARAM_NAMES_NAME).setValue("FailureRate");
        gen.doGenerateFromResources(resource.getResourceSet(), selectedFiles, fsa); // call xtend script

        assertEquals(41240, TestHelper.saveFile(fsa, PATH_OUTPUT_FILE).length());

        System.out.println("Calculation errors expected due to a not resolved functionpool file.");
    }
}
