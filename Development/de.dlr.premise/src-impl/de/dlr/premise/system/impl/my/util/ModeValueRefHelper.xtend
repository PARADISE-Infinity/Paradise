/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.system.impl.my.util

import de.dlr.premise.graph.DirectPointer
import de.dlr.premise.system.ABalancing
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.ModeValueRef
import de.dlr.premise.system.Parameter
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.util.PremiseHelper

import static extension de.dlr.premise.util.PremiseHelper.*
import de.dlr.premise.component.ComponentReferencePointer

import static extension de.dlr.premise.system.impl.my.util.BalancingHelper.getValidReferenceParentComponents

class ModeValueRefHelper {
	def static queryValidModePointersList(ModeValueRef it) {
		getValidReferenceParentComponents(it.eContainer.eContainer as SystemComponent).filter(SystemComponent).flatMap[statemachines].flatMap[modes].toUnmodifieableEList
	}
	
	def static queryValidModePointersParentComponentReferenceList(ModeValueRef it) {
		getValidReferenceParentComponents(it.eContainer.eContainer as SystemComponent).filter(ComponentReference).toUnmodifieableEList
	}
	
	def static boolean isModesValid(ModeValueRef modeValueRef) {
		val parameter = modeValueRef.eContainer as Parameter
		if (PremiseHelper.getAll(parameter.rootNotifier, ABalancing).filter(ABalancing).exists[ABalancing<?> it | target == parameter]) {
			return true
		}
		val validModes = modeValueRef.queryValidModePointersList
		val validModeParentComponentReferences = modeValueRef.queryValidModePointersParentComponentReferenceList
				
		return modeValueRef.modePointers.forall[
			switch (it) {
				DirectPointer<?>: validModes.contains(target)
				ComponentReferencePointer<?>: validModeParentComponentReferences.contains(componentReference)
				default: false
			}
		]
	}
}