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

import org.w3c.dom.Element;

public enum ModelVersion {
    
    // is treated as sorted list
    V102("1.02"),
    V103("1.03"),
    V104("1.04"),
    V105("1.05"),
    V106("1.06"),
    V107("1.07"),
    V108("1.08"),
    V109("1.09"),
    V110("1.10"),
    V111("1.11"),
    V112("1.12"),
    V113("1.13"),
    V114("1.14"),
    V115("1.15"),
    V116("1.16"),
    V117("1.17"),
    V118("1.18"),
    V119("1.19"),
    V120("1.20"),
    V121("1.21"),
    V122("1.22"),
    V123("1.23"),
    V124("1.24"),
    V125("1.25"),
    V126("1.26"),
    V127("1.27"),
    V128("1.28"),
    V129("1.29"),
    V130("1.30"),
    V131("1.31"),
    V132("1.32"),
    V133("1.33");

    private String _version;
    private ModelVersion(String version) {
        _version = version;
    }
    
    @Override
    public String toString()  {
        return _version;
    }
    
    public static String getLatestVersion() {        
        ModelVersion[] versions = ModelVersion.values();
        return versions[versions.length-1].toString();
    }   
    
    public static boolean isVersion(final String version) {
        for(ModelVersion ver : ModelVersion.values()){
            if (ver.toString().equals(version))  {
                return true;
            }
        }
        
        return false;
    }
    
    public static String getModelVersion(final Element root) {        
        String version = "";
        version = root.getAttribute("metaModel");
        return version;
    }
}
