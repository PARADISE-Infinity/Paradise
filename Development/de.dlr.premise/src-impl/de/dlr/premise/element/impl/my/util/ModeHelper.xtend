/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.element.impl.my.util

import de.dlr.premise.element.AModeCombination
import de.dlr.premise.element.Mode
import org.eclipse.core.runtime.Platform

class ModeHelper {
	
	static interface IModeValidator {
		def boolean isModeReachable(Mode mode)
		def boolean isModeCombinationValid(AModeCombination modeCombination)
	}
	
	private static IModeValidator cachedModeValidator = null
	
	def static getModeValidator() {
		if (cachedModeValidator == null) {
			val config = Platform.getExtensionRegistry().getConfigurationElementsFor("de.dlr.premise.modeValidator");
	        try {
	          cachedModeValidator = config.head.createExecutableExtension("class") as IModeValidator
	        } catch (Throwable e) {
	        	System.err.println("Couldn't create IModeValidator")
	        	e.printStackTrace
	        	return null
	        }
		}
		return de.dlr.premise.element.impl.my.util.ModeHelper.cachedModeValidator
	}
	
	def static isReachable(Mode it) {
		if (modeValidator != null) {
			return modeValidator.isModeReachable(it)
		}
		return true
	}
}