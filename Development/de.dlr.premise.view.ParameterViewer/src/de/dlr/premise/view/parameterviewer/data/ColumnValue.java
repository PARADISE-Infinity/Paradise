/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.parameterviewer.data;

import java.io.Serializable;

public class ColumnValue implements Serializable {

    private static final long serialVersionUID = 1L;

    public String capture = "";
    public int width = 75;
    public final ID id;

    public ColumnValue(String captuer, int width, ID id) {
        this.capture = captuer;
        this.width = width;
        this.id = id;
    }

    public enum ID {
        NAME, VALUE, UNIT, OLD_VALUE, CHANGES_ABS, UNCERTAINTY, SATISFIES, SOURCE, FULL_NAME, CHANGES_PERCENTAGE, TARGET
    }
}