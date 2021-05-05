/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.calc.my

import de.dlr.premise.registry.AParameterDef
import org.eclipse.xtend.lib.annotations.Data

@Data class CalculatorParameter {
	
	val AParameterDef aParameterDefValue
	val Double doubleValue
	
	new(AParameterDef value) {
		aParameterDefValue = value
		doubleValue = null
	}
	
	new(Double value) {
		aParameterDefValue = null
		doubleValue = value
	}

    def isDouble() {
        doubleValue != null
    }
}
