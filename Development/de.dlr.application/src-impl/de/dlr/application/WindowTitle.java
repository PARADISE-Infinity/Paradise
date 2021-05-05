/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.application;

public class WindowTitle {

    public static final String VERSION = "1.25.0";
    public static final String NAME = "PARADISE";

    private static final String versionString = "V " + VERSION;
    private static final String title = "Parametric Architecture Designer for Integrated Systems Engineering";
    private static final String DELIM = " - ";

    /**
     * The short title includes only the application title and the version number. The long version shows also the long version of the
     * application acronym.
     * 
     * @param longVersion
     * @return Returns the selected application title.
     */

    public static String getTitle() {
        return NAME + DELIM + title + DELIM + versionString;
    }
}
