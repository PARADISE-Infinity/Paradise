/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.migration;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

import de.dlr.premise.util.TestHelper;

public class MigrationTestHelper {

	private static String PLUGIN = "de.dlr.premise.migration";
	
	public static String getURIPath(String fullPath) {
        return TestHelper.locateFileForJUnit(PLUGIN, fullPath);
	}

	public static String getTestPath(final String testName)  {
		return "test/data/" + testName + "/";
	}
	
	public static String getInputPath(final String testName, final String fileName) {
		return MigrationTestHelper.getURIPath(getTestPath(testName)) + "input/" + fileName;		
	}
	
	public static String getOutputPath(final String testName, final String fileName) {
		return MigrationTestHelper.getURIPath(getTestPath(testName)) + "output/" + fileName;		
	}

	public static String getReferencePath(final String testName, final String fileName) {
		return MigrationTestHelper.getURIPath(getTestPath(testName)) + "reference/" + fileName;		
	}
	
	public static void fileCopy(String inPath, String outPath) throws IOException {
	    fileCopy(new File(inPath), new File(outPath));
	}
	
	public static void fileCopy(File in, File out) throws IOException {
	    Files.createParentDirs(out);
	    Files.copy(in, out);
	}
}
