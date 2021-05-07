/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.safetyview.commands;

import de.dlr.premise.element.AElement
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.element.GuardCombination
import de.dlr.premise.element.Mode
import de.dlr.premise.element.StateMachine
import de.dlr.premise.registry.Junction
import de.dlr.premise.registry.MetaTypeDef
import de.dlr.premise.safety.SafetyHelper
import de.dlr.premise.system.SystemFactory
import de.dlr.premise.functions.UseCase
import de.dlr.premise.util.BaseRegistryHelper
import de.dlr.premise.safety.impl.my.FailureHelper
import org.eclipse.emf.common.notify.Notifier
import org.eclipse.emf.edit.command.ChangeCommand

class BuildStateMachineCommand extends ChangeCommand {

	private var String modeName  
	private var String desc
	private var StateMachine smHarmfulEvent

	public var StateMachine hazard
	
	private var AElement parent

	new(Notifier notifier, StateMachine harmfulEvent, String failureName, String description) {

		super(notifier)
		
		if (notifier instanceof AElement) {
			parent = notifier as AElement	
		}

		modeName = failureName
		desc 	 = description
		smHarmfulEvent = harmfulEvent

	}
	
	override protected doExecute() {

		val mtHazard = getHazardMetaType(parent)

        // create a hazard state machine
        hazard = FailureHelper.createHazard(parent, mtHazard, modeName, desc)     	           
        var dFailure = FailureHelper.getFailureMode(hazard)

        // check if we don't have a harmful event in the function or component
        if (smHarmfulEvent != null) {

            // link to harmful event
            if (smHarmfulEvent.transitions.size > 0) {
				var event = smHarmfulEvent.transitions.get(0)
				if (event.condition == null) {
					var gc = ElementFactory.eINSTANCE.createGuardCombination
					gc.junction = Junction.OR
					event.condition = gc			
					
					var tb = SystemFactory.eINSTANCE.createTransitionBalancing
					event.balancings.add(tb)				
					var Mode mode = FailureHelper.getFailureMode(smHarmfulEvent)
					tb.target = FailureHelper.getFailureRate(mode)						
				}

				// create mode guard
				var modeGuard = ElementFactory.eINSTANCE.createModeGuard
				modeGuard.mode = dFailure
	
				// in case we have a guard combination add the failure mode
				// otherwise we need a new guard combination and then adding 
				// the existing and the new mode guard
				if (event.condition instanceof GuardCombination) {
					(event.condition as GuardCombination).children.add(modeGuard)
				} else {
					var storedModeGuard = event.condition						
					var gc = ElementFactory.eINSTANCE.createGuardCombination
						gc.junction = Junction.OR
						event.condition = gc
					gc.children.add(storedModeGuard)				
					gc.children.add(modeGuard)
				}					
			}
		}
		
        // TODO should be moved to FHA
        // subroutine for upgrading UseCases to SSF
		val mtSSF = BaseRegistryHelper.getSSFMetaType(parent.eResource.resourceSet)
        
        if (parent instanceof UseCase) {
        	if (parent.metaTypes.filter[mt|mt.id == BaseRegistryHelper.METATYPE_SSF_ID].nullOrEmpty) {
	    		parent.metaTypes.add(mtSSF)
        		
	    		// generate FHA MetaData, check if the meta data is required                    		
	    		SafetyHelper.createFHAMetadata(parent)                    		
        	}
        }			
	}

	/**
	 * Returns the meta type for a hazard. In case the no harmful event exists the MetaType 
	 * harmful event is returned, otherwise the meta type hazard. 
	 */	
	def private MetaTypeDef getHazardMetaType(AElement parent) {
	    if (smHarmfulEvent == null) {
        	return BaseRegistryHelper.getHarmfulHazardMetaType(parent.eResource.resourceSet)
        }
        
        return BaseRegistryHelper.getHazardMetaType(parent.eResource.resourceSet)	
	}
}
