/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.excel.fha.util

import FHA.Analysis
import FHA.Classification
import FHA.Probability
import com.smartxls.WorkBook
import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.excel.fha.temp.FHATemplate
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.URI
import java.net.URL
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Platform
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.InMemoryFileSystemAccess
import org.eclipse.xtext.util.RuntimeIOException
import org.osgi.framework.Bundle

class FHAExcelGenerator {

    private static String PLUGIN_NAME = "de.dlr.exchange.excel.fha"
	
	private String dataPath

	new (String dataPath){
		this.dataPath = dataPath
	}
	
	/** Generates an Excel Workbook based on a Template and a provided FHA Model */
	def doGenerateNewExcelFromModel(IFileSystemAccess fsa, Analysis analysis){
	
		// load the template workbook
		val workbook = new WorkBook
		var URI uri
	
		var Bundle bundle = Platform.getBundle(PLUGIN_NAME);
		var URL fileURL = bundle.getEntry(FHATemplate.NAME)
	
		var resolvedURL = FileLocator.toFileURL(fileURL)
		uri = new URI(resolvedURL.protocol, resolvedURL.path, null)
		var file = new File(uri)
		
		var FileInputStream streamIn
        if (file != null && file.absolutePath != null) {
        	streamIn = new FileInputStream(file.absolutePath)
			workbook.readXLSX(streamIn)
        } else {
        	System.err.println("WARNING! Could not load Template File")
        }
        
        // set sheet number and sheet name of functions
		workbook.sheet = FHATemplate.FUNCTION_SHEET_IDX
		workbook.setSheetName(FHATemplate.FUNCTION_SHEET_IDX, FHATemplate.FUNCTION_SHEET_NAME)
				
		// set the function list content
		var funcIndex = FHATemplate.FUNCTION_SHEET_START_IDX		
		for (function : analysis.functions) {
			
			workbook.setText(funcIndex, FHATemplate.FUN_F_ID_COL_IDX, function.fid)
			workbook.setText(funcIndex, FHATemplate.FUN_NAME_COL_IDX, function.name)
			workbook.setText(funcIndex, FHATemplate.FUN_DESC_COL_IDX, function.description)
			workbook.setText(funcIndex, FHATemplate.FUN_UUID_COL_IDX, function.uuid)
			workbook.setText(funcIndex, FHATemplate.FUN_SAFE_COL_IDX, 
				getNoneClean(function.safetyobjective.literal))
			workbook.setText(funcIndex, FHATemplate.FUN_MITI_COL_IDX, 
				getNoneClean(function.mitigationobjective.literal))
			funcIndex++;
		}
		
		// set the hazard list content
		workbook.sheet = FHATemplate.HAZARD_SHEET_IDX
		var hazIndex = FHATemplate.HAZARD_SHEET_START_IDX
		
		for (hazard : analysis.hazards) {
			
			workbook.setText(hazIndex, FHATemplate.HAZ_F_ID_COL_IDX, hazard.function.fid)
			workbook.setText(hazIndex, FHATemplate.HAZ_FNAM_COL_IDX, hazard.function.name)
			workbook.setText(hazIndex, FHATemplate.HAZ_UUID_COL_IDX, hazard.uuid)
			workbook.setText(hazIndex, FHATemplate.HAZ_DESC_COL_IDX, hazard.description)
			workbook.setText(hazIndex, FHATemplate.HAZ_NAME_COL_IDX, hazard.name)
			workbook.setText(hazIndex, FHATemplate.HAZ_PHAS_COL_IDX, hazard.phases.phase)
			workbook.setText(hazIndex, FHATemplate.HAZ_COND_COL_IDX, hazard.conditions)
			workbook.setText(hazIndex, FHATemplate.HAZ_EFFE_COL_IDX, hazard.effects)
			
			if (hazard.riskassessment != null){
				workbook.setText(hazIndex,FHATemplate.HAZ_CLAS_COL_IDX, 
					handleClassfication(hazard.riskassessment.classification))
				workbook.setText(hazIndex,FHATemplate.HAZ_PROB_COL_IDX, 
					handleProbability(hazard.riskassessment.probability))
				workbook.setText(hazIndex,FHATemplate.HAZ_FAIL_COL_IDX, 
					getNoneClean(hazard.riskassessment.failsafe.literal))
			}
			
			workbook.setText(hazIndex, FHATemplate.HAZ_COMP_COL_IDX, hazard.compliance)
			workbook.setText(hazIndex, FHATemplate.HAZ_DOCU_COL_IDX, hazard.documentation)

			// filling mitigation sheet with data
			var mitIndex = hazIndex
			workbook.sheet = FHATemplate.MITIGATION_SHEET_IDX
					
			workbook.setText(mitIndex, FHATemplate.MIT_F_ID_COL_IDX, hazard.function.fid)
			workbook.setText(mitIndex, FHATemplate.MIT_NAME_COL_IDX, hazard.name)
			workbook.setText(mitIndex, FHATemplate.MIT_PHAS_COL_IDX, hazard.phases.phase)
			workbook.setText(mitIndex, FHATemplate.MIT_MITI_COL_IDX, hazard.mitigations)
			
			if (hazard.mitigationassessment != null){
				workbook.setText(mitIndex, FHATemplate.MIT_CLAS_COL_IDX, 
					handleClassfication(hazard.mitigationassessment.classification))
				workbook.setText(mitIndex, FHATemplate.MIT_PROB_COL_IDX, 
					handleProbability(hazard.mitigationassessment.probability))				
				workbook.setText(mitIndex, FHATemplate.MIT_FAIL_COL_IDX, 
					getNoneClean(hazard.mitigationassessment.failsafe.literal))
			}
			
			// switch back to hazard sheet
			workbook.sheet = FHATemplate.HAZARD_SHEET_IDX
			hazIndex++;
		}
		
		// save the file
		try {
			
			var out = new ByteArrayOutputStream
			workbook.writeXLSX(out)
			streamIn.close
			
			// check the name
			if (dataPath.length > 4 && dataPath.substring(dataPath.length-5) == ".xlsx"){
				dataPath = dataPath.substring(dataPath.lastIndexOf('\\')+1,dataPath.length-5)
			} else if (dataPath.length < 1) {
				dataPath = "Export"
			}
			
			if (fsa instanceof InMemoryFileSystemAccess) { // still needed, the GenerationHandler uses it
				val inputStream = new ByteArrayInputStream(out.toByteArray)
				fsa.generateFile(GeneratorHelper::encodeFileName(dataPath) + ".xlsx",inputStream)
			} else if (fsa instanceof EclipseResourceFileSystemAccess2) {
				val inputStream = new ByteArrayInputStream(out.toByteArray)
				fsa.generateFile(GeneratorHelper::encodeFileName(dataPath) + ".xlsx",inputStream)
			}
			System.out.println("MESSAGE! Successfully generated File \""+ dataPath +"\" from Template\n")
			
		} catch (RuntimeIOException e) {
			System.err.println("Could not write file: " + (e.cause as CoreException).status.exception.message)
		} catch (FileNotFoundException e) {
			System.err.println(e.message)
		}
	}
		
	private def String getNoneClean(String text) {			
		if (text == "none") {
			return ""
		} 			
		
	 	return text 
	}

	/** Overwrites an existing Excel WorkBook with newly created information
	 * 
	 * The Excel Workbook has to conform some model guidelines to work
	 * TODO: This function is currently bugged when it comes to deleting functions or hazards in premise
	 */
	def doOverrideExistingExcelFromModel(IFileSystemAccess fsa, Analysis analysis){

		// load the users workbook
		val workbook = new WorkBook
		val FileInputStream streamIn = new FileInputStream(dataPath)
		workbook.readXLSX(streamIn)
		
		var cellMap = FHAMerge.setupMap(workbook)
		if (cellMap == null){
			return
		}
		
		workbook.sheet = FHATemplate.FUNCTION_SHEET_IDX
		
		// generate the function list
		var funcIndex = 1;
		for (function : analysis.functions){
			workbook.setText(funcIndex,cellMap.get("fid"),function.fid)
			workbook.setText(funcIndex,cellMap.get("function"), function.name)
			workbook.setText(funcIndex,cellMap.get("fuuid"),function.uuid)
			workbook.setText(funcIndex,cellMap.get("fsobj"),function.safetyobjective.literal)
			workbook.setText(funcIndex,cellMap.get("fmobj"),function.mitigationobjective.literal)
			funcIndex++;
		}
		
		workbook.setSheetName(0, FHATemplate.FUNCTION_SHEET_NAME)
		
		// generate the hazard and mitigation list
		var hazIndex = 2;
		workbook.sheet = 1
		
		for (hazard : analysis.hazards){
			workbook.setText(hazIndex,cellMap.get("hfid"),hazard.function.fid)
			workbook.setText(hazIndex,cellMap.get("hfunction"),hazard.function.name)
			workbook.setText(hazIndex,cellMap.get("huuid"),hazard.uuid)
			workbook.setText(hazIndex,cellMap.get("hazard"),hazard.name)
			workbook.setText(hazIndex,cellMap.get("hphase"),hazard.phases.phase)
			workbook.setText(hazIndex,cellMap.get("hconditions"),hazard.conditions)
			workbook.setText(hazIndex,cellMap.get("heffects"),hazard.effects)
			if (hazard.riskassessment != null){
				var setText = hazard.riskassessment.classification.literal
				if (setText=="none"){
					workbook.setText(hazIndex,cellMap.get("hclassification"),"")
				} else{
					workbook.setText(hazIndex,cellMap.get("hclassification"),hazard.riskassessment.classification.literal)
				}
				setText = hazard.riskassessment.probability.literal
				if (setText=="none"){
					workbook.setText(hazIndex,cellMap.get("hprobability"),"")
				} else{
					workbook.setText(hazIndex,cellMap.get("hprobability"),hazard.riskassessment.probability.literal)
				}
				setText = hazard.riskassessment.failsafe.literal
				if (setText=="none"){
					workbook.setText(hazIndex,cellMap.get("hfailsafe"),"")
				} else{
					workbook.setText(hazIndex,cellMap.get("hfailsafe"),hazard.riskassessment.failsafe.literal)
				}
			}
			workbook.setText(hazIndex,cellMap.get("hobjcomp"),hazard.compliance)
			workbook.setText(hazIndex,cellMap.get("hdoc"),hazard.documentation)
			
			// mitigation
			var mitIndex = hazIndex-1
			workbook.sheet = 2
			workbook.setText(mitIndex,cellMap.get("mfid"),hazard.function.fid)
			workbook.setText(mitIndex,cellMap.get("mhaz"),hazard.name)
			workbook.setText(mitIndex,cellMap.get("mitigation"),hazard.mitigations)
			if (hazard.mitigationassessment != null){
				var setText = hazard.mitigationassessment.classification.literal
				if (setText=="none"){
					workbook.setText(mitIndex,cellMap.get("mclassification"),"")
				} else{
					workbook.setText(mitIndex,cellMap.get("mclassification"),hazard.mitigationassessment.classification.literal)
				}
				setText = hazard.mitigationassessment.probability.literal
				if (setText=="none"){
					workbook.setText(mitIndex,cellMap.get("mprobability"),"")
				} else{
					workbook.setText(mitIndex,cellMap.get("mprobability"),hazard.mitigationassessment.probability.literal)
				}
				setText = hazard.mitigationassessment.failsafe.literal
				if (setText=="none"){
					workbook.setText(mitIndex,cellMap.get("mfailsafe"),"")
				} else{
					workbook.setText(mitIndex,cellMap.get("mfailsafe"),hazard.mitigationassessment.failsafe.literal)
				}
			}
			
			// switch back
			workbook.sheet = 1
			hazIndex++;
		}
		
		
		// save the file, Note that we can't extract this method 
		try {
			
			var out = new ByteArrayOutputStream
			workbook.writeXLSX(out)
			streamIn.close
			
			// check the name
			if (dataPath.length > 4 && dataPath.substring(dataPath.length-5) == ".xlsx"){
				dataPath = dataPath.substring(dataPath.lastIndexOf('\\')+1,dataPath.length-5)
			} else if (dataPath.length < 1) {
				dataPath = "Export"
			}
			
			if (fsa instanceof InMemoryFileSystemAccess) { // still needed, the GenerationHandler uses it
				val inputStream = new ByteArrayInputStream(out.toByteArray)
				fsa.generateFile(GeneratorHelper::encodeFileName(dataPath) + ".xlsx",inputStream)
			} else if (fsa instanceof EclipseResourceFileSystemAccess2) {
				val inputStream = new ByteArrayInputStream(out.toByteArray)
				fsa.generateFile(GeneratorHelper::encodeFileName(dataPath) + ".xlsx",inputStream)
			}
			System.out.println("Successfully merged to file: " + dataPath + "\n")
			
		} catch (RuntimeIOException e) {
			System.err.println("Could not write file: " + (e.cause as CoreException).status.exception.message)
		} catch (FileNotFoundException e) {
			System.err.println(e.message)
		}
	}
	

	private def handleClassfication(Classification classification){
		switch (classification){
			case Classification.NONE: 			return ""
			case Classification.NO_EFFECT: 		return "No Effect"
			default: 							return classification.literal
		}	
	}
	
	private def handleProbability(Probability probability){
		switch (probability){
			case Probability.NONE: 					return ""
			case Probability.REASONABLY_PROBABLE:	return "Reasonably Probable"
			case Probability.EXTREMELY_REMOTE: 		return "Extremely Remote"
			case Probability.EXTREMELY_IMPROBABLE: 	return "Extremely Improbable"
			default: 								return probability.literal
		}
	}	
}