/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.safety.impl.my

import de.dlr.premise.safety.Analysis
import de.dlr.premise.safety.Assessment

class SafetyModelHelper {
	
	def static boolean isMitigatedLower(Assessment assessment) {
		return false	
	}
		
	def static boolean hasMitigationsEqualsMitigatedRisk(Assessment assessment) {
		return false	
	}
	
	def static boolean maxOneAssessmentPerHazard(Analysis analysis) {
		return false	
	}	
}