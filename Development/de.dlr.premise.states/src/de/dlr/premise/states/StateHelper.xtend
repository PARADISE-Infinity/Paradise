/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.states

import de.dlr.premise.element.AGuardCondition
import de.dlr.premise.element.AModeCombination
import de.dlr.premise.element.Mode
import de.dlr.premise.graph.GraphFactory
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.registry.RegistryFactory
import de.dlr.premise.states.data.State
import de.dlr.premise.system.ModeValueRef
import de.dlr.premise.system.Parameter
import de.dlr.premise.system.SystemFactory
import de.dlr.premise.util.scope.ScopedEObjectFactory
import java.util.Comparator
import java.util.Set
import org.eclipse.emf.common.util.ECollections
import org.eclipse.emf.ecore.resource.ResourceSet

import static extension de.dlr.premise.states.data.State.*
import static extension de.dlr.premise.states.util.StateCheckingHelper.*
import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.*
import de.dlr.premise.states.util.StateCheckingHelper

class StateHelper {
		
	static val MODE_VALUE_REF_MODES_COMPARATOR = Comparator
		.<Mode, String>comparing[if (isScopedEObject) scope.last?.name ?: "" else ""]
		.<String>thenComparing[stateMachine.name]
		.<String>thenComparing[name]
		
	static def setValue(AParameterDef param, State valueState, String newValue) {
		if (valueState == null || valueState == State.EMPTY_STATE) {
			if (param.value == null) {
				param.value = RegistryFactory.eINSTANCE.createValue
			}
			if (param.value.value != newValue) {
				param.value.value = newValue
			}
		} else {
			if (param instanceof Parameter) {
				var modeValue = param.modeValues.findFirst[state == valueState]
				if (modeValue == null) {
					if (param.isScopedEObject) {
						modeValue = ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(SystemFactory.eINSTANCE.createModeValueRef)
						modeValue.modes += valueState.modes
					} else {
						modeValue = SystemFactory.eINSTANCE.createModeValueRef
						modeValue.modePointers += valueState.modes.map[mode |
							GraphFactory.eINSTANCE.<Mode>createDirectPointer => [target = mode]
						]
					}
					param.modeValues += modeValue
				}
				
				ECollections.sort(modeValue.modes, MODE_VALUE_REF_MODES_COMPARATOR)
				if (modeValue.value != newValue) {
					modeValue.value = newValue
				}
			} else {
				throw new IllegalArgumentException("Can't set state value if argument is not a Parameter")
			}
		}
	}
	
	 static def String getValue(AParameterDef param, State valueState) {
	 	if (valueState != null && valueState != State.EMPTY_STATE && param instanceof Parameter) {
			var ModeValueRef bestMatchingModeValue = null
			for (modeValue : (param as Parameter).modeValues.reverseView) {
				val modeValueState = modeValue.state
				if (modeValueState != null 
					&& modeValueState.isCompatibleSubstateOf(valueState) 
					&& (bestMatchingModeValue == null || modeValueState.modes.length > bestMatchingModeValue.modes.length)
				) {
					bestMatchingModeValue = modeValue
				}
				if (bestMatchingModeValue != null) {
					return bestMatchingModeValue.value
				}
			}
	 	}
	 	return param.value?.value
	 }
	

	static def getState(AModeCombination mc) {
		mc.modes.createNewStateOrNull
	}
	
	 def static Set<State> getSimplifiedCondition(AGuardCondition gc) {
	 	return StateCheckingHelper.getPossibleStates(gc)
	 }
	
	static def StateCheckerAdapter findOrCreateStateCheckerAdapter(ResourceSet root) {
		var sca = root.eAdapters.filter(StateCheckerAdapter).head
		if (sca == null) {
			sca = new StateCheckerAdapter(root)
		}
		return sca
	}
}