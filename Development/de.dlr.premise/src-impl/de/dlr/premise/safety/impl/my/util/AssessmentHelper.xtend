/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.safety.impl.my.util

import de.dlr.premise.element.Mode
import de.dlr.premise.element.StateMachine
import de.dlr.premise.safety.Analysis
import de.dlr.premise.safety.Assessment

import static extension de.dlr.premise.safety.impl.my.FailureHelper.*

class AssessmentHelper {
	
	/**
	 * Checks if the mode contained in the phases state machine.
	 */
	static def queryIsValidPhaseValue(Assessment it, Mode mode) {	
		
		if (mode == null) {
			 return true
	 	}
			 	
		(mode.eContainer as StateMachine).isPhases
	}
	
	/**
	 * Checks if a given state machine is an hazard.
	 */
	static def queryIsValidHazardValue(Assessment it, StateMachine hazard) {
		
		if (hazard == null) {
			return true
		}
		
		hazard.isHazard
	}
	
	/**
	 * Check if a mitigation improves the risk.
	 */
	static def isMitigatedRiskLowerValid(Assessment it) {

		// only if a base risk exists a mitigation is required
		if (baseRisk == null) {
			return true
		}
	
		// check if a mitigation is lower
		if (mitigatedRisk != null) {
			baseRisk.objective >= mitigatedRisk.objective return true
		}
		
		return false
	}
	
	/**
	 * For a analysis only one assessment per hazard is be allowed.
	 */
	static def isMaxOneAssessmentPerHazardValid(Assessment it) {

		// get the list of assessments which assess the given hazard
		val current = hazard
		val assessments = (eContainer as Analysis).assessments.filter[hazard == current]

		// check if there only is a hazard mode of the hazard and the assessments 		
		if (assessments != null && assessments.size < 2) return true;
		
		// check if the modes of the assessments are different
		val modes = assessments.map[phase].toList
		for(Mode m : modes) {
			val cnt = modes.filter[e | e == m].toList.size
			if (cnt > 1) return false
		}
		
		return true
	}	
	
	/**
	 * Returns the mode of an assessment.
	 */
	def Mode getPhase(Assessment it) {
		return phase
	}	
}