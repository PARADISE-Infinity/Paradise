/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.migration;

public class MigrationMissingException extends Exception {

    private static final long serialVersionUID = -2092638372560530400L;

    public MigrationMissingException(String version) {
        super("Migration " + version + " is missing");
    }    
}
