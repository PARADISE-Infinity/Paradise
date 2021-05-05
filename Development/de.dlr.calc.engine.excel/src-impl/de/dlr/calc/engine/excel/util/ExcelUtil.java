/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.excel.util;

public class ExcelUtil {

    /**
     * Converts an excel cell name string into a integer row and column coordinate. For example A1 will be converted into [0,0].
     * 
     * @param cellName
     * @return A integer array of row and column
     */
    public static int[] convert(String cellName) {

        // check input
        if (cellName.length() < 2 || cellName.length() > 8) {
            return null;
        }

        // split column and row, therefore find index of first number
        int index = 0;
        for (int i = 0; i < cellName.length(); i++) {
            int digitval = (((int) cellName.charAt(i)) - 64);
            if (digitval < 0) {
                index = i;
                break;
            }
        }

        // store the row and the column
        String row = cellName.substring(index);
        String col = cellName.replace(row, "");

        // converting row name into a number
        index = 0;
        for (int i = 0; i < col.length() - 1; i++) {
            int digitval = (((int) col.charAt(i)) - 64);
            double columnval = Math.pow(26, (col.length() - i - 1));
            index += digitval * columnval;
        }

        index += (((int) cellName.charAt(col.length() - 1)) - 64);

        // create cell coordinate
        int[] cellIndex = new int[2];
        cellIndex[0] = index - 1;
        cellIndex[1] = Integer.parseInt(row) - 1;

        return cellIndex;
    }
}
