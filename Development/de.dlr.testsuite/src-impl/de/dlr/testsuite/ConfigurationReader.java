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
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationReader {

	private String configFile;
	
	ConfigurationReader(final String configFile) {
		this.configFile = configFile;
	}
	
	public List<String> getPlugins() {
		
		List<String> plugins = new ArrayList<String>();
		
	    try {			
	    	BufferedReader br = new BufferedReader(new FileReader(configFile));
	    	
	        String line = br.readLine();

	        while (line != null) {

		        // ignore comments and consider only names with "."
		        if ((line.contains("#")) || (!line.contains("."))) {
			        line = br.readLine();
		        	continue;
		        }

		        line = line.trim();

		        plugins.add(line);		        
		        line = br.readLine();
	        }
	        
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return plugins;
	}
}
