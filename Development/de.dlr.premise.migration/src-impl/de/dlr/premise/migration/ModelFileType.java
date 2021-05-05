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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public enum ModelFileType {
    
    FUNCTION("usecase", "function"),
    SYSTEM("system", "premise"),
    FUNCTIONPOOL("functionpool"),
    REGISTRY("registry"),
    REPRESENTATION("representation"),
    COMPONENT("component"),
    UNKNOWN("unknown");

    private Set<String> extensions = new LinkedHashSet<>();
    private ModelFileType(String... extensions) {
        Collections.addAll(this.extensions, extensions);
    }

    public Set<String> getExtensions() {
        return Collections.unmodifiableSet(extensions);
    }
    
    public static ModelFileType getModelFileType(final String fileName) {
    	String extension = "";

    	int i = fileName.lastIndexOf('.');
    	if (i > 0) {
    	    extension = fileName.substring(i+1);
    	}
    	
    	
        for(ModelFileType type : ModelFileType.values()){
        	if (type.getExtensions().contains(extension)) {
        		return type;
        	}
        }
        
        return UNKNOWN;
    }    
}
