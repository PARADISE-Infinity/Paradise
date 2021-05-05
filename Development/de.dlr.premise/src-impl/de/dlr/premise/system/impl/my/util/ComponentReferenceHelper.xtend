/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.system.impl.my.util

import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.SystemComponent

class ComponentReferenceHelper {
	static def queryIsValidActiveImplementationValue(ComponentReference ref, SystemComponent sc) {
		sc != null && sc.satisfiedSatisfieables.contains(ref.componentDefinition)
	}
	
	static def isActiveImplementationValid(ComponentReference ref) {
		ref.queryIsValidActiveImplementationValue(ref.activeImplementation)
	}
}