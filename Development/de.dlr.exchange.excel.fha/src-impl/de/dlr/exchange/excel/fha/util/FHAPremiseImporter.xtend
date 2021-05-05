/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.fha.util

import de.dlr.premise.element.AElement
import FHA.Analysis
import de.dlr.premise.util.PremiseHelper
import org.eclipse.emf.edit.command.ChangeCommand
import de.dlr.premise.functions.UseCase
import de.dlr.premise.element.StateMachine
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.common.util.BasicEList
import FHA.Phase
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.registry.MetaData
import de.dlr.premise.registry.RegistryFactory
import de.dlr.premise.element.Mode
import org.eclipse.emf.edit.domain.EditingDomain
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain
import de.dlr.premise.registry.MetaTypeDef
import de.dlr.premise.functions.UseCaseFactory
import FHA.Function
import FHA.Hazard
import de.dlr.premise.registry.impl.RegistryImpl
import de.dlr.premise.functions.UseCaseRepository
import de.dlr.premise.util.BaseRegistryHelper

/** This class is used to modify a PrEMISE model when importing data from another fha model */
class FHAPremiseImporter {
	
	private RegistryImpl basereg
	
	new (RegistryImpl basereg){
		this.basereg = basereg
	}
	
	/** Modifies a given PrEMISE Model based on a provided FHA Model */
	def doGeneratePremiseFromModel(AElement aelement, Analysis analysis){

		val premiseRoot = PremiseHelper::getRoot(aelement)
		
		val cchange = new ChangeCommand(premiseRoot) {
			
			override protected doExecute() {
				if (premiseRoot instanceof UseCaseRepository){
					
					// create a container
					// rootUC is the container for new elements, as it is derived by selection
					val rootUC = aelement as UseCase
					
					// check for a system state machine
					val statesWithMT = premiseRoot.eAllContents.filter [sm|
						sm instanceof StateMachine && 
						!(sm as StateMachine).metaTypes.nullOrEmpty].toList
					var StateMachine syssm
					for (sm : statesWithMT) {
						if (FHAMerge.hasObjectMetaType(sm as StateMachine, BaseRegistryHelper.METATYPE_PHASES_ID)) {
							syssm = sm as StateMachine
						}
					}

					if (syssm == null){
						
						// if there is no state machine yet, just create one
						syssm = createNewSystemSateMachine(analysis)
						rootUC.statemachines.add(syssm)
						
					} else {
						
						// if there is a matching state machine, merge the phases
						// create all states
						var EList<Phase> phases = new BasicEList<Phase>()
						for (hazard : analysis.hazards) {
							val phase = hazard.phases
							if (phases.filter[p|p.phase == phase.phase].nullOrEmpty) {
								phases.add(phase)
							}
						}
						
						// add phases only if they aren't already contained
						for (phase : phases){
							if (syssm.modes.filter[m|m.name == phase.phase].nullOrEmpty){
								val newMode = ElementFactory.eINSTANCE.createMode
								newMode.name = phase.phase
								syssm.modes.add(newMode)
								for (existingMode : syssm.modes){
									if (newMode.id != existingMode.id) {
										// create a Transition to and from this mode
										val tIn = ElementFactory.eINSTANCE.createTransition
										tIn.source = existingMode
										tIn.target = newMode
										syssm.transitions.add(tIn)
										val tOut = ElementFactory.eINSTANCE.createTransition
										tOut.source = newMode
										tOut.target = existingMode
										syssm.transitions.add(tOut)
									}
								}
							}
						}
					}			
					
					// generate functions now
					for (function : analysis.functions){
						
						// check if a function exists using fid (or uuid?)
						var metaFID = premiseRoot.eAllContents.findFirst[md|
							md instanceof MetaData
							&& (md as MetaData).name == "F#ID"
							&& (md as MetaData).value == function.fid
						]
						
						if (metaFID == null){
							
							//there is no such function yet, add a new one and add the corresponding hazards as well
							val uc = createNewFunctionWithHazards(analysis, function)
							rootUC.children.add(uc)
							
						} else {
							
							// there is already a corresponding function
							val uc = metaFID.eContainer.eContainer as UseCase
							
							// update data
							// uc.name = function.name
							// if (function.uuid != null && function.uuid != "")
							//	uc.id = function.uuid
							
							// update meta data
							val mdSafety = uc.metaData.findFirst[md|md.name == "FHA"]
							var mdSafetyObjective = mdSafety.metaData.findFirst[md|md.name == "Safety Objective"]
							if (mdSafetyObjective == null) {
								mdSafetyObjective = RegistryFactory.eINSTANCE.createMetaData
								mdSafetyObjective.name = "Safety Objective"
								mdSafety.metaData.add(mdSafetyObjective)
							}
							mdSafetyObjective.value = function.safetyobjective.literal
							var mdMitigationObjective = mdSafety.metaData.findFirst[md|md.name == "Mitigation Objective"]
							if (mdMitigationObjective == null) {
								mdMitigationObjective = RegistryFactory.eINSTANCE.createMetaData
								mdMitigationObjective.name = "Mitigation Objective"
								mdSafety.metaData.add(mdMitigationObjective)
							}
							mdMitigationObjective.value = function.mitigationobjective.literal
							
							//  check if there are new hazards
							val functionHazards = analysis.hazards.filter[h|h.function == function]
							for (hazard : functionHazards){
								
								// if possible, get the mode corresponding the hazard
								val hazardInUC = uc.eAllContents.filter[elm|PremiseHelper::getRootElement(elm) == uc]
								.filter[m|m instanceof Mode].findFirst[m|(m as Mode).id == hazard.uuid]
								
								// if the mode does not exist, create a new state machine
								if (hazardInUC == null){
									val sm = createHazardStateMachine(hazard)
									uc.statemachines.add(sm)
								}
							}
						}	
					}
				}
			}
		}
		
		// execute the changes
		if (cchange.canExecute){
			var EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(aelement);
			if (editingDomain != null){	
				editingDomain.getCommandStack().execute(cchange);
			} else {
				// this is solely for JUnit tests, as they don't feature an editing domain
				cchange.execute
			}
			println("MESSAGE! Successfully imported File to PrEMISE Model\n")
		}
	}
	
	/** This function creates a new StateMachine that contains all hazard phases */
	def createNewSystemSateMachine(Analysis analysis) {

		val syssm = ElementFactory.eINSTANCE.createStateMachine
		syssm.name = "System States"
		syssm.metaTypes.add(basereg.eAllContents.findFirst[mt|mt instanceof MetaTypeDef 
			&& (mt as MetaTypeDef).id == BaseRegistryHelper.METATYPE_PHASES_ID
			] as MetaTypeDef)

		// create all states
		var EList<Phase> phases = new BasicEList<Phase>()
		for (hazard : analysis.hazards) {
			val phase = hazard.phases
			if (phases.filter[p|p.phase == phase.phase].nullOrEmpty) {
				phases.add(phase)
			}
		}

		// should there be an entry mode set?
		var entryMode = true
		for (phase : phases) {
			val mode = ElementFactory.eINSTANCE.createMode
			mode.name = (phase as Phase).phase
			mode.entryMode = entryMode
			entryMode = false
			syssm.modes.add(mode)
		}

		// link all states
		for (mode : syssm.modes) {
			for (othermode : syssm.modes) {
				// create outgoing transitions to every mode to map all phases
				if (mode.id != othermode.id) {
					val tout = ElementFactory.eINSTANCE.createTransition
					tout.source = mode
					tout.target = othermode
					syssm.transitions.add(tout)
				}
			}
		}
		return syssm
	}
	
	/** This functions creates a new UseCase and adds all hazard state machines belonging to it */
	def createNewFunctionWithHazards(Analysis analysis, Function function) {
		
		// the basic function
		val uc = UseCaseFactory.eINSTANCE.createUseCase
		uc.name = function.name
		if (function.uuid != null && function.uuid != ""){
			uc.id = function.uuid
		}
		val mtSSF = basereg.eAllContents.findFirst[mt| mt instanceof MetaTypeDef 
            	&& (mt as MetaTypeDef).id == BaseRegistryHelper.METATYPE_SSF_ID] as MetaTypeDef
        uc.metaTypes.add(mtSSF)

		// safety meta data
		val mdsafety = RegistryFactory.eINSTANCE.createMetaData
		mdsafety.name = "FHA"
		uc.metaData.add(mdsafety)
		val mdfid = RegistryFactory.eINSTANCE.createMetaData
		mdfid.name = "F#ID"
		mdfid.value = function.fid
		mdsafety.metaData.add(mdfid)
		val mdSafetyObj = RegistryFactory.eINSTANCE.createMetaData
		mdSafetyObj.name = "Safety Objective"
		mdSafetyObj.value = function.safetyobjective.toString
		mdsafety.metaData.add(mdSafetyObj)
		val mdMitigation = RegistryFactory.eINSTANCE.createMetaData
		mdMitigation.name = "Mitigation Objective"
		mdMitigation.value = function.mitigationobjective.toString
		mdsafety.metaData.add(mdMitigation)

		// create a list of hazards corresponding to this function
		val hazList = analysis.hazards.filter[h|h.function.fid == function.fid]
		var uqHazList = new BasicEList<Hazard>()
		for (hazard : hazList) {
			if (uqHazList.filter[h|h.name == hazard.name].nullOrEmpty) {
				uqHazList.add(hazard)
			}
		}

		// generate statemachines for each hazard
		for (hazard : uqHazList) {
			val sm = createHazardStateMachine(hazard)
			uc.statemachines.add(sm)

		}
		
		return uc;
	}
	
	/** Creates a new Hazard StateMachine representing the given Hazard*/
	def createHazardStateMachine(Hazard hazard) {
		val sm = ElementFactory.eINSTANCE.createStateMachine
		val mtypeHaz = basereg.eAllContents.findFirst[mt|mt instanceof MetaTypeDef && (mt as MetaTypeDef).id == BaseRegistryHelper.METATYPE_HAZARD_ID] as MetaTypeDef
		val mtypeFail = basereg.eAllContents.findFirst[mt|mt instanceof MetaTypeDef && (mt as MetaTypeDef).id == BaseRegistryHelper.METATYPE_FAILURE_ID] as MetaTypeDef

		sm.name = hazard.name + " State"
		sm.metaTypes.add(mtypeHaz)

		val mNormal = ElementFactory.eINSTANCE.createMode
		mNormal.entryMode = true
		mNormal.name = "Normal"
		sm.modes.add(mNormal)

		val mFail = ElementFactory.eINSTANCE.createMode
		mFail.name = hazard.name
		mFail.metaTypes.add(mtypeFail)
		if (hazard.uuid != null && hazard.uuid != ""){
			mFail.id = hazard.uuid
		}
		sm.modes.add(mFail)
		

		val tFail = ElementFactory.eINSTANCE.createTransition
		tFail.source = mNormal
		tFail.target = mFail
		sm.transitions.add(tFail)
		return sm;
	}
}