/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.matlab.struct.xtend;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

import de.dlr.exchange.base.xtend.test.CharsetProvidingInMemoryFileSystemAccess;
import de.dlr.exchange.base.xtend.test.TestHelper;
import de.dlr.premise.util.urihandlers.URIHandlerHelper;

public class GeneratorTest {

    private static String PATH_INPUT_FILE =
            de.dlr.premise.util.TestHelper.locateFile("de.dlr.exchange.matlab.struct.xtend", "test/data/MatlabData.premise").getPath();
    private static String PATH_OUTPUT_FILE =
            de.dlr.premise.util.TestHelper.locateFile("de.dlr.exchange.matlab.struct.xtend", "test/data/MatlabData.m").getPath();

    @Test
    public void test() {
        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);
        URIHandlerHelper.registerInto(resource.getResourceSet().getURIConverter());
        List<Resource> selectedFiles = new ArrayList<>();
        selectedFiles.add(resource);

        CharsetProvidingInMemoryFileSystemAccess fsa = TestHelper.createInMemoryFileSystemAccess();
        Generator gen = new Generator();
        gen.doGenerateFromResources(resource.getResourceSet(), selectedFiles, fsa); // call xtend script

        assertEquals(1569, TestHelper.saveFile(fsa, PATH_OUTPUT_FILE).length());
    }

}
