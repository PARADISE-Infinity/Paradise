/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.safetyview.generic

class GetDefaultPreliminaryHazardNames {

	static public val FUNCTION_NAME = "[Function name]"

	static def getDefaultHazards() {
		return newArrayList('Fails to operate', 
							'Degradation', 
							'Wrong Timing ', 
							'Out of Sequence', 
							'Stop operation')
	}
	
	static def getDefaultDescription() {
		return newArrayList('Unable to perform of ' + FUNCTION_NAME, 
							'Degraded performance of ' + FUNCTION_NAME, 
							'To late or to early timing by performing of ' + FUNCTION_NAME, 
							'Out of Sequence by performing of ' + FUNCTION_NAME, 
							'Unable to stop the operation of ' + FUNCTION_NAME)	
	}	
}