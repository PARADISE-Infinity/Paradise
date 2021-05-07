/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.petrinet.snoopy.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

import de.dlr.exchange.base.xtend.test.CharsetProvidingInMemoryFileSystemAccess;
import de.dlr.exchange.base.xtend.test.TestHelper;
import de.dlr.exchange.petrinet.snoopy.xtend.Generator;

public class GeneratorTestSmall {

    private static String PATH_INPUT_FILE =
            de.dlr.premise.util.TestHelper.locateFile("de.dlr.exchange.petrinet.snoopy.xtend", "test/data/SmallNet.premise").getPath();
    private static String PATH_OUTPUT_FILE =
            de.dlr.premise.util.TestHelper.locateFile("de.dlr.exchange.petrinet.snoopy.xtend", "test/data/SmallNet.snoopy.spped").getPath();

    @Test
    public void test() {
        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);
        List<Resource> selectedFiles = new ArrayList<>();
        selectedFiles.add(resource);

        CharsetProvidingInMemoryFileSystemAccess fsa = TestHelper.createInMemoryFileSystemAccess();
        Generator gen = new Generator();
        gen.doGenerateFromResources(resource.getResourceSet(), selectedFiles, fsa); // call xtend script

        assertEquals(9637, TestHelper.saveFile(fsa, PATH_OUTPUT_FILE).length());
    }
}
