/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.functionpool.provider.my;

import de.dlr.premise.registry.Unit;

public class LabelHelper {

	public static String getUnitSymbol(final Unit unit) {		
		if (unit != null) {
			return unit.getSymbol();
		}
		return null;
	}
	
	public static String cleanSpaces(final String label) {
	    return label.replaceAll("\\s+", "");
    }
}