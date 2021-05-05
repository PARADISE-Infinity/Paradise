/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.testsuite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestClassGenerator {

    // plugin name
	private static final String TEST_PLUGIN = "de.dlr.testsuite";

	// configuration file
//	private static final String TEST_TEST_PLUGINS_CFG = "data\\ExchangePlugins.cfg";
//  private static final String TEST_TEST_PLUGINS_CFG = "data\\ModelPlugins.cfg";
	private static final String TEST_TEST_PLUGINS_CFG = "data\\TestPlugins.cfg";

	// source template and target test file
	private static final String TEST_TMPL = "data\\AllTestsRandom.tmpl";
    private static final String DEST = "test\\plugin\\de\\dlr\\testsuite\\AllTestsRandom.java";

    // 
	private final static String [] testDir = {"test/plugin", "tests/plugin","test/junit"};
	
	public static void main(String[] args) {
		
		// get all plug ins which shall be considered
		ConfigurationReader reader = new ConfigurationReader(TEST_TEST_PLUGINS_CFG);		
		List<String> plugs = reader.getPlugins();

		// get base directory		
		File tmp = new File(TEST_TEST_PLUGINS_CFG);
		String path = tmp.getAbsolutePath().replace(TEST_TEST_PLUGINS_CFG, "").replace(TEST_PLUGIN + "\\", "");

		// write the test classes
		TestClassWriter writer = new TestClassWriter(TEST_TMPL);
		
		try {
			writer.open(DEST);
		} catch (Exception e) {	
		}

		// define test class array
		List<String> testClasses = new ArrayList<String>();
		
		for (String plugin : plugs) {
			
			testClasses.clear();

			File pluginPath = new File (path, plugin);			
			for (String dir : testDir) {
				
				File file = new File(pluginPath, dir);				
				if (file.isDirectory() == false) {
					continue;
				}

				List<String> testFiles = new ArrayList<String>();
				testFiles = getTestFiles(file, testFiles);

				for(String test : testFiles) {
					test = test.replace(file.toString(), "");
					test = test.replace("\\", ".");
                    test = test.replaceFirst("\\.java$", ".class");
                    test = test.replaceFirst("\\.xtend$", ".class");
					if (test.charAt(0) == '.') {
						test = test.substring(1, test.length());
					}
					
					testClasses.add(test);
				}				
			}

			if (testClasses.size() > 0) {				
				writer.write(plugin, false, testClasses);
				System.out.println("Plugin : " + plugin + " cases added");
			} else {
                System.err.println("Plugin : " + plugin + " no cases found");
			}
		}

		writer.close();
		
		System.out.println("Generation of test cases done");
	}

	private static List<String> list(final File file, final List<String> tests) {
		
		List<String> nTests = tests;
		
        if (file.toString().endsWith(".java")) {
			nTests.add(file.toString());
		}

        if (file.toString().endsWith(".xtend")) {
            nTests.add(file.toString());
        }

	    File[] children = file.listFiles();	    
	    if (children == null) return nTests;
	    
	    for (File child : children) {
	    	nTests = list(child, nTests);
	    }

	    return nTests;
	}

	private static List<String> getTestFiles(final File file, final List<String> testFiles) {
		return list(file, testFiles);
	}	
}
