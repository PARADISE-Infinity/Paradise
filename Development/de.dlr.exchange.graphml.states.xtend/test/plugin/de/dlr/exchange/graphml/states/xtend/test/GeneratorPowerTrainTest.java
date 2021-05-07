/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.states.xtend.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

import de.dlr.exchange.base.xtend.test.CharsetProvidingInMemoryFileSystemAccess;
import de.dlr.exchange.base.xtend.test.TestHelper;
import de.dlr.exchange.graphml.states.xtend.Generator;

public class GeneratorPowerTrainTest extends de.dlr.premise.util.TestHelper {

    private static final String PLUGIN_NAME = TestConstants.PLUGIN_NAME;
    
    private static String PATH_INPUT_FILE = locateFile(PLUGIN_NAME,
                                                       "test/data/TestingPowerTrainRepository.premise").getPath();
    private static String PATH_OUTPUT_SM_FILE = locateFile(PLUGIN_NAME,
                                                           "test/data/TestingPowerTrainRepository.statemachines.graphml").getPath();
    private static String PATH_OUTPUT_TREE_FILE = locateFile(PLUGIN_NAME,
                                                             "test/data/TestingPowerTrainRepository.statestree.graphml").getPath();

    @Test
    public void test() {
        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);
        List<Resource> selectedElements = new ArrayList<>();
        selectedElements.add(resource);

        CharsetProvidingInMemoryFileSystemAccess fsa = TestHelper.createInMemoryFileSystemAccess();
        Generator gen = new Generator();
        gen.doGenerateFromResources(resource.getResourceSet(), selectedElements, fsa); // call xtend script

        EList<String> list = new BasicEList<String>();
        list.add(PATH_OUTPUT_SM_FILE);
        list.add(PATH_OUTPUT_TREE_FILE);
        EList<File> files = TestHelper.saveFiles(fsa, list);
        assertEquals(44804, files.get(0).length());
        assertTrue(files.get(1).length() >= 46999 && files.get(1).length() <= 47002);
    }
}
