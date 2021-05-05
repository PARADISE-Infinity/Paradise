/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.calc.engine.matlab;

/**
 * The Class MatlabTestHelper.
 */
public class MatlabTestHelper {
    
    /**
     * Delay.
     *
     * @param ms the ms
     */
    protected void delay(int ms) {
    	
    	long cTime = System.currentTimeMillis();
    	long tTime = System.currentTimeMillis() + ms;
    
    	while (cTime < tTime) {
    		cTime = System.currentTimeMillis();
    	}		
    }
}

