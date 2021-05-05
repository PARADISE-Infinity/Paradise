/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.states.util

import de.dlr.premise.element.AModeCombination
import de.dlr.premise.element.Mode
import de.dlr.premise.element.impl.my.util.ModeHelper.IModeValidator
import de.dlr.premise.states.data.InvalidStateException
import de.dlr.premise.states.data.State

import static extension de.dlr.premise.states.StateHelper.*
import de.dlr.premise.util.scope.ScopedEObjectFactory

class ModeValidatorImpl implements IModeValidator {
	
	override isModeReachable(Mode mode) {
		val sca = mode.eResource.resourceSet.findOrCreateStateCheckerAdapter
		return sca.isModeReachable(mode)
	}
	
	override isModeCombinationValid(AModeCombination modeCombination) {
		val state = try {
			 new State(ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(modeCombination).modes)
		} catch (InvalidStateException e) {
			return false
		}
		
		val sca = modeCombination.eResource.resourceSet.findOrCreateStateCheckerAdapter
		return sca.isValid(state)
	}
	
}