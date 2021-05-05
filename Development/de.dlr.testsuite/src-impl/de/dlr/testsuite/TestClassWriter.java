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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

public class TestClassWriter {

	private final static String IDENT = "    ";

	private Writer writer;
	
	private BufferedReader br;
	private String template;
	
	public TestClassWriter(final String template) {		
		this.template = template;
	}

	public void open(final String dest) throws Exception {
		
		writer = new BufferedWriter(new OutputStreamWriter(
				 new FileOutputStream(dest), "utf-8"));

	    br = new BufferedReader(new FileReader(template));

        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

		writer.write(line + '\n');
		writer.flush();

        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
            
            if (line.contains("@InsertClasses")) {
            	break;            
            }
            
    		writer.write(line + '\n');
    		writer.flush();
        }
	}
		
	public void close() {

		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            
	    		writer.write(line + '\n');
	    		writer.flush();
	            
	    		line = br.readLine();
			}
			
			writer.close();			
	        br.close();

		} catch (Exception e) {
		}
	}

	public void write(final String name, boolean last, List<String> testClasses) {

		// write plug in comment 
		try {
			writer.write('\n' + IDENT +  "// "+ name + '\n');
			writer.flush();
		} catch (IOException e) {
			
		}
		
		// write all test classes
		for(String test : testClasses) {
			if (test.contains("package-info") || 
				test.contains("TestHelper") ||
				(test.contains("Test.") == false)) {
				continue;
			}
			
			String line = IDENT  + test;
			if ((testClasses.indexOf(test) < testClasses.size() - 1) || (last == false)) {
				line = line + ",";
			}

			try {
				writer.write(line + '\n');
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
