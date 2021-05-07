/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.constraints.valueconstraints

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Sets
import de.dlr.premise.registry.AValueDef
import de.dlr.premise.states.data.State
import de.dlr.premise.system.Parameter
import de.dlr.premise.functions.AConstraint
import de.dlr.premise.functions.AModeValueConstraint
import de.dlr.premise.functions.AValueConstraint
import de.dlr.premise.functions.RequiredParameter
import de.dlr.premise.functions.UseCase
import java.util.Set

import static extension com.google.common.collect.Maps.toMap
import static extension de.dlr.premise.states.StateHelper.getState
import static extension de.dlr.premise.states.StateHelper.getSimplifiedCondition

/**
 * Provides facilities to deal with Values of Parameters that depend on modes.
 * 
 * Parameters are linked to state machines via their ModeValueRefs. A ModeValueRef contains a collection of modes. It describes the value
 * that the parameter takes if all the modes it contains are active.
 * 
 * For any given combination of active Modes, a parameter is meant to have at least one value (it might have multiple, if the combination
 * does not contain a Mode for all state machines that are referenced by the ModeValueRefs). These values will either be ModeValueRefs
 * specifically defined for the given combination, or if none such exist the Parameter's Value.
 * 
 * @author steh_ti
 */
class ConstraintToValueMapper  {
	/**
     * Map instances of {@link AConstraint} to the {@link AValueDef} instances for which it needs to be checked.
     * 
     * A constraint can be mapped to multiple values and a value can be mapped to multiple constraints.
     * 
     * @param reqParam
     * @param param
     * @return
     */
    def Multimap<AConstraint, AValueDef> createConstraintToValueMap(RequiredParameter reqParam, Parameter param) {
    	val constraintToValueMap = HashMultimap.<AConstraint, AValueDef>create();

      	val useCaseCondition = (reqParam.eContainer as UseCase).condition.simplifiedCondition
    	val constraints = <AConstraint>newHashSet => [    		
    		if (reqParam.valueConstraint != null) {
    			add(reqParam.valueConstraint)
    		}
    		// ignore invalid modeValueConstraints (at least one mode required)
    		addAll(reqParam.modeValueConstraints.filter[state != null])

    	]
    	
    	val allConstraintConditions = constraints.toMap[createConstraintCondition(useCaseCondition)]
    	val fallbackConstraint = constraints.findFirst[allConstraintConditions.get(it).contains(State.EMPTY_STATE)]
    	val constraintConditions = allConstraintConditions.filter[k, v| k != fallbackConstraint && !v.empty]
    	
    	// find matching constraints for modevals
    	for (modeVal : param.modeValues.filter[state != null]) {
    		val matchingConstraints = constraintConditions.filter[constr, cond | cond.exists[cs | cs.isCompatibleSubstateOf(modeVal.state)]].keySet
    		if (!matchingConstraints.empty) {
    			matchingConstraints.forEach[constraintToValueMap.put(it, modeVal)]
    		} else if (fallbackConstraint != null) {
    			constraintToValueMap.put(fallbackConstraint, modeVal)
    		}
    		
    	}
    	
    	if (param.value != null) {
    		// constraints that don't have a matching modeValue constrain the value
    		val unmatchedConstraints = Sets.difference(constraints, constraintToValueMap.keySet)
    		unmatchedConstraints.forEach[constraintToValueMap.put(it, param.value)]
    		
    		if (fallbackConstraint != null) {
    			constraintToValueMap.put(fallbackConstraint, param.value)
    		}
    	}
    	
    	return constraintToValueMap
    }
    
   	def private dispatch createConstraintCondition(AValueConstraint constr, Set<State> useCaseCondition) {
    	return useCaseCondition
    }
    
    def private dispatch createConstraintCondition(AModeValueConstraint constr, Set<State> useCaseCondition) {
		val constrState = constr.state
		// combine every state in the use case condition with the state of the constraint
		useCaseCondition.map[State.createNewStateByCombiningOrNull(it, constrState)].filterNull.toSet
    }
}