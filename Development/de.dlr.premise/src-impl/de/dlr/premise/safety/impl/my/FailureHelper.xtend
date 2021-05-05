/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.safety.impl.my

import de.dlr.premise.element.AElement
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.element.Mode
import de.dlr.premise.element.ModeGuard
import de.dlr.premise.element.StateMachine
import de.dlr.premise.element.Transition
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.registry.MetaTypeDef
import de.dlr.premise.registry.RegistryFactory
import de.dlr.premise.registry.Value
import de.dlr.premise.system.SystemFactory
import de.dlr.premise.system.TransitionParameter
import de.dlr.premise.util.BaseRegistryHelper
import de.dlr.premise.util.LabelHelper

import java.util.HashMap
import java.util.Map

import org.eclipse.emf.ecore.EObject

import static extension de.dlr.premise.util.PremiseHelper.flatMap

class FailureHelper {

    public static val FAILURE_RATE_NAME = "Failure Rate"
    public static val FAILURE_PARENT = "Parent"

 	/**
 	 * If a mode is decorated with the HAZARD meta type the check returns true.
	 */
	def static isHazard(StateMachine sm) {
		return sm.metaTypes.exists[id == BaseRegistryHelper.METATYPE_HAZARD_ID || id == BaseRegistryHelper.METATYPE_HARMFUL_ID]
	}

 	/**
 	 * If a mode is decorated with the HAZARD meta type the check returns true.
	 */
	def static isHarmfulHazard(StateMachine sm) {
		return sm.metaTypes.exists[id == BaseRegistryHelper.METATYPE_HARMFUL_ID]
	}

 	/**
 	 * Check if a state machine has the meta type Phases
	 */
	def static isPhases(StateMachine sm) {
		return sm.metaTypes.exists[id == BaseRegistryHelper.METATYPE_PHASES_ID]
	}

	/**
	 * Generates a short name out of a given hazard name.
	 */
    def static String getShortHazardName(String name) {
        
        var String shortName = "";
        
        // extract first character of each word        
        var stArray = name.split(" ");
		for(token : stArray) {
			
			if (token.length > 0) {			
            	shortName = shortName + token.charAt(0);
            }
        }
        
        // change to upper case
        return shortName.toUpperCase();
    }


	/**
	 * Generates a hazard name. The hazard name is constructed after the rule
	 * FailureMode.name of Parent.name, thus Loss of Function can be generated if 
	 * FailureMode.name is "Loss" and Parent.name is "Function"  
	 */
    def static String getHazardName(Object element) {
        
        if (element instanceof Transition) {
      
            // get name of the failure mode
            var Transition trans = element;
            var Mode targetMode = trans.getTarget();

            // get parent state machine and cleaned parent name
            var StateMachine sm = trans.eContainer() as StateMachine;
            val parent = sm.eContainer() as AElement

			var String parentName = FAILURE_PARENT
            if (parent != null) {
            	parentName = LabelHelper.cleanName(parent.getName())     
            }   
            // construct hazard name
            var String name = targetMode.getName() +" of " + parentName;
            return name;
        }
        
        return null;
    }

		 
 	/**
 	 * If a mode is decorated with the FAILURE meta type the check returns true.
	 */
	public def static isFailureMode(Mode mode) {
		return mode.metaTypes.exists[id == BaseRegistryHelper.METATYPE_FAILURE_ID]
	}
	 
	
	/** 
	 * Check if a failure mode is the result of a basic event
	 */
	def static isBasicEvent(Mode mode) {		
		
		if (isFailureMode(mode) == false) {
			return false
		}
		
		// get parent of the FailureMode
		var StateMachine sm = mode.eContainer as StateMachine
		var trans = sm.transitions.findFirst[target == mode]
		
		if (trans == null) {
			return false
		}
		
		// check if it is a basic event
		if (trans.condition != null) {
			return false
		}
		
		return true
	}	 
	 
	 
	/**
	 * Validate a given state machine is a hazard. Therefore
	 */ 
	def static public boolean validFailureStateMachine(StateMachine sm) {

		var flag_oneEntryMode = false;
		var flag_everyModeOneTransition = false;

		// checks if the state machine is a hazard 
		if (sm.metaTypes.findFirst[id == BaseRegistryHelper.METATYPE_HAZARD_ID || id == BaseRegistryHelper.METATYPE_HARMFUL_ID] == null) {
			return false;
		}

		try {

			var Map<Mode, Integer> modeTargeted = new HashMap<Mode, Integer>(sm.getModes().size());
			var entryModeCounter = 0;

			for (Mode mode : sm.getModes()) {

				// only one entryMode
				if (mode.isEntryMode()) {
					entryModeCounter++;
					modeTargeted.put(mode, 1);
				}

				if (entryModeCounter == 1) {
					flag_oneEntryMode = true;
				} else {
					flag_oneEntryMode = false;
				}

				// add every mode to the Map
				modeTargeted.putIfAbsent(mode, 0);
			}

			// whenever a mode is targeted, its value is increased
			for (Transition trans : sm.getTransitions()) {
				val Mode targetMode = trans.getTarget();
				modeTargeted.put(targetMode, modeTargeted.get(targetMode) + 1);
			}

			// check that every mode has a value of "1" (implies that there are failure modes)
			for (Mode mode : modeTargeted.keySet()) {
				if (modeTargeted.get(mode) == 1) {
					flag_everyModeOneTransition = true;
				} else {
					flag_everyModeOneTransition = false;
					return false;
				}
			}
			
		} catch (Exception e) {
		}

		return (flag_everyModeOneTransition && flag_oneEntryMode);
	}
	
	/**
	 * Checks if a state machine is defined as hazard. The hazard is defined by a transition from a mode into a failure.
	 */
	def static boolean verifyValidHazard(StateMachine sm) {
		
		// checks if the state machine is a hazard 
		if (sm.metaTypes.findFirst[id == BaseRegistryHelper.METATYPE_HAZARD_ID] == null) {
			return false;
		}
		
		// find the first failure mode
		val failureMode = sm.modes.findFirst[metaTypes.exists[id == BaseRegistryHelper.METATYPE_FAILURE_ID]]
		if (failureMode == null) {
			return false;
		}

		// check if a transition contains an entry mode and the failure mode	
		return sm.transitions.exists[target == failureMode && source != null && source.entryMode]
	}	
	
	/**
	 * Returns a hazard after check from a given failure mode
	 */
	def static StateMachine getHazard(Mode mode) {
		
		val parent = mode.eContainer;
		
		if (parent instanceof StateMachine) {
			if (FailureHelper.isHazard(parent) == true) {
				return parent;
			}
		}
		
		return null	
	}
	
	/**
	 * Creates a hazard which is represented by a state machine
	 */
	def static StateMachine createHazard(AElement parent, MetaTypeDef mtHazard, String failureName, String desc) {

		// create empty state machine
        var StateMachine stateMachine = ElementFactory.eINSTANCE.createStateMachine()
        stateMachine.name = getFailureName(parent, failureName)
        stateMachine.metaTypes.add(mtHazard)
        stateMachine.description = desc

        // add nominal and failure mode
        var Mode dNormal = ElementFactory.eINSTANCE.createMode()
        dNormal.name = getNormalBehaviorName(parent)
        dNormal.entryMode = true
		
        var Mode dFailure = ElementFactory.eINSTANCE.createMode()
        dFailure.name = failureName
        dFailure.metaTypes.add(BaseRegistryHelper.getFailureMetaType(parent.eResource.resourceSet))

        // create failure event and add default failure rate
        var Transition dTrans = ElementFactory.eINSTANCE.createTransition();
        dTrans.name  getFailureEventName(parent, failureName)
        dTrans.target = dFailure
        dTrans.source = dNormal;

		// add failure rate to the transition
        var TransitionParameter dTransPara = SystemFactory.eINSTANCE.createTransitionParameter();
		dTransPara.name = FAILURE_RATE_NAME;
        var Value dValue = RegistryFactory.eINSTANCE.createValue();
        dValue.setValue("0");
        dTransPara.setValue(dValue);

		// add hazard to the parent	
        (parent as AElement).statemachines.add(stateMachine);
        stateMachine.modes.add(dNormal);
        stateMachine.modes.add(dFailure);
        stateMachine.transitions.add(dTrans);
        dTrans.parameters.add(dTransPara)

        return stateMachine;		
	}

	// returns the name of the failure mode				
	def static String getFailureName(EObject parent, String failureMode) {
		var name = "Failure"	
		if (failureMode != null) { 
			name = failureMode	
		}
		return name + " of " + (parent as ANameItem).getName()
	}
	
	// returns the generated event name of a failure
	def static String getFailureEventName(EObject parent, String failureMode) {
		val failureName = getFailureName(parent, failureMode)
		val shortName   = getShortHazardName(failureName)
		"E: [" + shortName + "] " + failureName
	}
	
	// returns the failure mode of a transition if the mode exists
	def static getFailureMode(StateMachine machine) {		
		return machine.modes?.findFirst[m | FailureHelper.isFailureMode(m)]
	}

	// returns the failure rate of a transition (event) 	
	def static TransitionParameter getFailureRate(Mode mode) {
		var parent = (mode.eContainer as StateMachine)
		var transitions = parent.transitions.filter[source == mode || target == mode]
		return transitions.flatMap[parameters].findFirst[name == FAILURE_RATE_NAME]
	} 
	
	// returns the failure rate of a transition (event) 
	def static TransitionParameter getFailureRate(ModeGuard guard) {
		return getFailureRate(guard.mode)		
	}
	
	// returns the name of the nominal behavior mode
	def static private String getNormalBehaviorName(EObject parent) {
		"Nominal " + (parent as ANameItem).getName()
	}
}
