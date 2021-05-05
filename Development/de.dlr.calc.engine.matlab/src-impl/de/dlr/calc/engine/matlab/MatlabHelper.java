/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.matlab;

import java.util.Random;

public class MatlabHelper {

    static Random _random = new Random();
    
    /**
     * @param length length of the variable name but minimum 6 char long
     * @return
     *      Returns a temporary MATLAB variable name string.
     */
    public static String getTmpVarName(int length) {
    
        // check minimum length
        if (length < 6) {
            length = 6;
        }

        // check maximum length
        if (length > 32) {
            length = 32;
        }
        
        // define set of allowed characters
        char[] chars = "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ".toCharArray();
    
        // initialize
        StringBuilder sb = new StringBuilder();            
        for (int i = 0; i < length; i++) {
            char c = chars[_random.nextInt(chars.length)];
            sb.append(c);
        }
    
        // convert into string
        String output = sb.toString();
        return output;
    }
}
