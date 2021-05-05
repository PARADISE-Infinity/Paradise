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

import FHA.Analysis
import FHA.Classification
import FHA.FHAFactory
import FHA.Failsafe
import FHA.Function
import FHA.Objective
import FHA.Phase
import FHA.Probability
import com.smartxls.WorkBook
import de.dlr.exchange.excel.fha.temp.FHATemplate
import de.dlr.premise.element.AElement
import de.dlr.premise.element.StateMachine
import de.dlr.premise.functions.UseCase
import de.dlr.premise.util.PremiseHelper

import de.dlr.premise.safety.impl.my.FailureHelper
import org.eclipse.emf.common.util.BasicEList
import org.eclipse.emf.ecore.EObject
import de.dlr.premise.util.LabelHelper
import de.dlr.premise.util.BaseRegistryHelper

/** 
 * This class generates instances of FHA meta models given either a UseCase or Excel Sheet
 */
class FHAModelGenerator {

	private static val ffac = FHAFactory.eINSTANCE;

	/** 
	 * Generates an FHA Model instance based on the selected model element. This element must be an AElement like
	 * SystemComponent or UseCase to get the root element*/
	def doGenerateModelFromPremise(AElement element) {
		
		val premiseRoot = PremiseHelper::getRoot(element)
		return getAnalysis(premiseRoot)		
	}

	/** 
	 * Generates an FHA Model instance based on the specified element and all its children, but uses the systems 
	 * Phase-StateMachine
	 */
	def doGeneratePartialModelFromPremise(AElement root){
				
		val premiseRoot = PremiseHelper::getRoot(root)
		return getAnalysis(premiseRoot);
	}
	
	/** 
	 * Generates an FHA model instance based on the provided Excel Template
	 */
	def doGenerateModelFromExcel(String path){
		
		// read the excel file to import
		val workbook = new WorkBook
		try{
			workbook.readXLSX(path)
		} catch (Exception e){
			return null;
		}
		
		// setup cell map
		var cellMap = FHAMerge.setupMap(workbook)
		if (cellMap == null) {
			return null;
		}

		// generate a FHA model
		val analysis = ffac.createAnalysis
		
		// start by gathering functions
		workbook.sheet = FHATemplate.FUNCTION_SHEET_IDX;
		var startRow = FHATemplate.FUNCTION_SHEET_START_IDX;
		
		var fidstr = "placeholder-string-to-start"
		while (fidstr != "") {
			fidstr = workbook.getText(startRow,cellMap.get("fid"))
			if (fidstr != "") {
				
				//when there is a function to model
				val function = ffac.createFunction
				function.fid = fidstr
				function.name = workbook.getText(startRow,cellMap.get("function"))
				function.uuid = workbook.getText(startRow,cellMap.get("fuuid"))
				
				val safetystr = workbook.getText(startRow,cellMap.get("fsobj"))
				function.safetyobjective = getObjective(safetystr)
								
				val mitigationstr = workbook.getText(startRow,cellMap.get("fmobj"))
				function.mitigationobjective = getObjective(mitigationstr)
				
				analysis.functions.add(function)
			}
			startRow++;
		}
		
		// next up generate the hazard lists info
		workbook.sheet = FHATemplate.HAZARD_SHEET_IDX
		startRow = FHATemplate.HAZARD_SHEET_START_IDX
		
		fidstr = "placeholder-string-to-start"
		
		while (fidstr != "") {
			
			fidstr = workbook.getText(startRow, cellMap.get("hfid"))

			if (fidstr != "") {
				
				val hazard = ffac.createHazard
				val fidfinal = fidstr
				hazard.function = analysis.functions.findFirst[f|f.fid == fidfinal]
				if (analysis.functions.findFirst[f|f.fid == fidfinal] == null){
					println("WARNING! could not find F#ID: "+fidfinal)
				}
				
				hazard.uuid = workbook.getText(startRow,cellMap.get("huuid"))
				hazard.name = workbook.getText(startRow,cellMap.get("hazard"))
				
				val phasename = workbook.getText(startRow,cellMap.get("hphase"))
				if (analysis.eAllContents.filter[p|p instanceof Phase].filter[m|(m as Phase).phase == phasename].nullOrEmpty){
					// create a new phase
					val phase = ffac.createPhase
					phase.phase = phasename
					hazard.phases = phase
				} else {
					//use existing phase
					val phase = analysis.eAllContents.filter[p|p instanceof Phase].findFirst[m|(m as Phase).phase == phasename]
					hazard.phases = phase as Phase;
				}
				
				hazard.conditions = workbook.getText(startRow,cellMap.get("hconditions"))
				hazard.effects = workbook.getText(startRow,cellMap.get("heffects"))
				hazard.compliance = workbook.getText(startRow,cellMap.get("hobjcomp"))
				hazard.documentation = workbook.getText(startRow,cellMap.get("hdoc"))
				
				// now to the safety assessment
				val safetyass = ffac.createAssessment
				
				var strClassification = workbook.getText(startRow,cellMap.get("hclassification"))
				safetyass.classification = getClassification(strClassification)
				
				var strProbability = workbook.getText(startRow,cellMap.get("hprobability"))
				safetyass.probability = getProbability(strProbability)
				
				var strFailsafe = workbook.getText(startRow,cellMap.get("hfailsafe"))
				safetyass.failsafe = getFailsafe(strFailsafe)

				var strObjective = workbook.getText(startRow,cellMap.get("hsobj"))
				safetyass.objective = getObjective(strObjective)
				hazard.riskassessment = safetyass
				
				// mitigation assessment				
				// switch to mitigation sheet and get the corresponding hazard function
				workbook.sheet = FHATemplate.MITIGATION_SHEET_IDX
				val mitRow = startRow			

				// create a new mitigation assessment
				val mitigationass = ffac.createAssessment
				
				// get values
				strClassification = workbook.getText(mitRow, cellMap.get("mclassification"))
				mitigationass.classification = getClassification(strClassification)

				strProbability = workbook.getText(mitRow,cellMap.get("mprobability"))
				mitigationass.probability = getProbability(strProbability)
				
				strFailsafe = workbook.getText(mitRow,cellMap.get("mfailsafe"))
				mitigationass.failsafe = getFailsafe(strFailsafe)

				strObjective = workbook.getText(mitRow,cellMap.get("msobj"))
				mitigationass.objective = getObjective(strObjective)
				
				hazard.mitigationassessment = mitigationass
				hazard.mitigations = workbook.getText(mitRow, cellMap.get("mitigation"))

				// add the hazard to the analysis and switch back to the hazard sheet
				analysis.hazards.add(hazard)
				workbook.sheet = FHATemplate.HAZARD_SHEET_IDX
			}

			startRow++
		}
		
		return analysis
	}

	/**
	 * Get the whole FHA analysis from a given model
	 */
	private def Analysis getAnalysis(EObject root) {

		// get all system states by searching all state machine with a meta type PHASE
		val statesWithMT = root.eAllContents.filter[sm|sm instanceof StateMachine && !(sm as StateMachine).metaTypes.nullOrEmpty].toList
		var StateMachine systemstates 
		for (sm : statesWithMT) {
			if (FHAMerge.hasObjectMetaType(sm as StateMachine, BaseRegistryHelper.METATYPE_PHASES_ID)){
				systemstates = sm as StateMachine
			}
		}
		
		// create phases based on system states
		val phases = new BasicEList<Phase>()
		if (systemstates == null || systemstates.modes.nullOrEmpty) {
			val defaultPhase = ffac.createPhase
			defaultPhase.phase = "default"
			phases.add(defaultPhase)
		} else {
			for (mode : systemstates.modes) {
				val phase = ffac.createPhase
				phase.phase = mode.name
				phases.add(phase)
			}
		}
		
		// create the analysis model from a PREMISE model
		val analysis = FHAFactory.eINSTANCE.createAnalysis
		return analysis.addFunctionsAndHazards(root, phases)
	}

	/**
	 * Analyze the given model and create the FHA analysis model
	 */
	private def addFunctionsAndHazards(Analysis analysis, EObject parent, BasicEList<Phase> phases){
				
		for (phase : phases) {
			
			for (uce : parent.eAllContents.toList.filter[elm|elm instanceof UseCase]) {

				val uc = uce as UseCase
				if (!uc.metaData.filter(md|md.name == "FHA").nullOrEmpty) {
					
					var Function function 
					
					// create the function from a use case if it isn't already present
					if (analysis.functions.filter[f|f.uuid == uc.id].nullOrEmpty) {
						function = ffac.createFunction
						function.name = LabelHelper.cleanName(uc.name) // 
						function.uuid = uc.id
						function.description = uc.description
						
						function.fid = uc.metaData.findFirst[md|md.name == "FHA"].metaData.findFirst[mdc|mdc.name == "F#ID"].value
					} else {
						function = analysis.functions.findFirst[f|f.uuid == uc.id]
					}
					
					// set the safety and mitigation objective
					var sobj = uc.metaData.findFirst[md|md.name == "FHA"].metaData.findFirst[mdc|mdc.name == "Safety Objective"]
					if (sobj != null) {
						function.safetyobjective = getObjective(sobj.value)
					}			
					
					var mobj = uc.metaData.findFirst[md|md.name == "FHA"].metaData.findFirst[mdc|mdc.name == "Mitigation Objective"]
					if (mobj != null) {
						function.mitigationobjective = getObjective(mobj.value)					
					}

					// add the function
					analysis.functions.add(function)

					// handle each failure
					for (sm : uc.statemachines) {

						// verify failure state machines (hazard)										
						if (FailureHelper.verifyValidHazard(sm)) {

							var modes = sm.modes.filter[mo|FHAMerge.hasObjectMetaType(mo, BaseRegistryHelper.METATYPE_FAILURE_ID)]
	
							for (mode : modes) {
	
								// skip modes, which are targeted by transitions with conditions inside their state machine
								// there is no transition that A) Has this mode as target and B) has no condition
								// 
								if (sm.transitions.exists[target == mode]) {
									val hazard = ffac.createHazard
									hazard.name = mode.name
									hazard.uuid = mode.id
									hazard.function = function
									hazard.phases = phase
									hazard.description = sm.description
									analysis.hazards.add(hazard)
								}
							}
						}
					}
				}
			}
		}
		
		return analysis;
	}
		
	/**
	 * Returns the type of a given safety objective string
	 */
	private def Objective getObjective(String value) {
		
		var Objective objective = Objective.NONE;
		switch (value){
			case "PASSED": objective = Objective.PASSED
			case "MAYBE":  objective = Objective.MAYBE
			case "FAILED": objective = Objective.FAILED
			default:       objective = Objective.NONE
		}
		
		return objective;
	}
	
	/**
	 * Returns the type of a given fail safe string
	 */
	private def Failsafe getFailsafe(String value) {
		
		var Failsafe failsafe = Failsafe.NONE
		switch (value){
			case "Yes": failsafe = Failsafe.YES
			case "No":  failsafe = Failsafe.NO
			default:    failsafe = Failsafe.NONE
		}
		
		return failsafe
	}	
	
	/**
	 * Returns the type of a given classification string
	 */
	private def Classification getClassification(String value) {
	
		var classification = Classification.NONE			
		switch (value){
			case "Catastrophic": classification = Classification.CATASTROPHIC
			case "Hazardous":    classification = Classification.HAZARDOUS
			case "Major":        classification = Classification.MAJOR
			case "Minor":        classification = Classification.MINOR
			case "No Effect":    classification = Classification.NO_EFFECT
			default:             classification = Classification.NONE
		}
		
		return classification
	}	
	
	/**
	 * Returns the type of a given probability string
	 */
	 private def Probability getProbability(String value) {
	 	
	 	var Probability probability = Probability.NONE
		switch (value){
			case "Frequent": 			 probability = Probability.FREQUENT
			case "Reasonably Probable":  probability = Probability.REASONABLY_PROBABLE
			case "Remote": 				 probability = Probability.REMOTE
			case "Extremely Remote": 	 probability = Probability.EXTREMELY_REMOTE
			case "Extremely Improbable": probability = Probability.EXTREMELY_REMOTE
			default: 					 probability = Probability.NONE
		}
		
		return probability
	}	
}