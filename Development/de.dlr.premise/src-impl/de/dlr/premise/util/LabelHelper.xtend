/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.util

class LabelHelper {
	
	
	// remove all space from a given label
	def	static public String cleanSpaces(String label) {
	    return label.replaceAll("\\s+", "");
    }

	// trims the label and reduces the spaces between words to a single space
	def	static public String singleSpaces(String label) {
	    return label.replaceAll("\\s+", " ").trim;
    }

    /** 
     * Removes "\\*" type of control characters 
     * @param name
     * @return
     */
    def static public String cleanName(String label) {        

		val delims = newArrayList("\n","\\n","\t","\\t")

        var String cleanName = label;
        
		for (delim : delims) {		
	        var idx = cleanName.indexOf(delim)
	        while (idx > 0) {
	            cleanName = cleanName.replace(cleanName.subSequence(idx, idx + delim.length),"");
	            idx = cleanName.indexOf(delim);            
	        }			
		}
        return cleanName;  
    }
    
    def static public String cleanQualifiedName(String name) {

		val delim = '.'
        var String cleanName = name   
	    var idx = cleanName.indexOf(delim)

		while (idx > 0) {
		    cleanName = cleanName.replace(cleanName.subSequence(idx, idx + delim.length),'');
		    idx = cleanName.indexOf(delim);            
		}			
		
		return cleanName;
	}
	
}
