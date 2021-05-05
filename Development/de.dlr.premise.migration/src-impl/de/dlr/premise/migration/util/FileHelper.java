/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.migration.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

public class FileHelper {

	/**
	 * this method is part of the HACK and can be removed if the implementation is fixed
	 * @param file
	 * @param type
	 * @throws IOException
	 */
	public static void removeString(File file, String type, String encoding) throws IOException {
	
	    // we need to store all the lines
	    List<String> lines = new ArrayList<String>();
	
	    // first, read the file and store the changes
	    BufferedReader in = createBufferedReader(file, encoding);
	    String line = in.readLine();
	    
	    // find and replace the given string
	    while (line != null) {
	    	if (line.indexOf(type) > 0) {        	
	    		line = line.replace(type, "");        		
	        }
	        lines.add(line);
	        line = in.readLine();
	    }
	    in.close();
	    
	    // write the considered lines into the file	
	    writeIntoFile(file, lines, encoding);
	}

    /**
	 * Remove empty lines from an file
	 * @param file
	 * @throws IOException
	 */
	public static void removeEmptyLines(File file, String encoding) throws IOException {
	    // we need to store all the lines
	    List<String> lines = new ArrayList<String>();
	
	    // first, read the file and store the changes
	    BufferedReader in = createBufferedReader(file, encoding);
	    String line = in.readLine();
	    
	    // add only lines with content
	    while (line != null) {
	    	if ((line.trim()).length() > 0) {        	
	            lines.add(line);        		
	        }
	        line = in.readLine();
	    }
	    in.close();
	
	    // write the considered lines into the file
	    writeIntoFile(file, lines, encoding);    	
	}

	private static BufferedReader createBufferedReader(File file, String encoding)
            throws UnsupportedEncodingException, FileNotFoundException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        return in;
    }

    /**
	 * Write a List of String into a given file
	 * @param file
	 * @param lines
     * @throws IOException 
	 */
	private static void writeIntoFile(File file, List<String> lines, String encoding) throws IOException {

		// now, write the file again with the changes
	    OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), encoding);
	    
	    String outStr = Joiner.on("\r\n").join(lines);
	    
	    out.write(outStr);
	    out.close();
	}
}
