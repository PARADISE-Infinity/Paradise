/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.fha

import FHA.Analysis
import de.dlr.exchange.base.xtend.ICharsetProvidingFileSystemAccess
import de.dlr.exchange.base.xtend.IGeneratorMy
import de.dlr.exchange.excel.fha.util.FHAExcelGenerator
import de.dlr.exchange.excel.fha.util.FHAMerge
import de.dlr.exchange.excel.fha.util.FHAModelGenerator
import de.dlr.exchange.excel.fha.util.FHAPremiseImporter
import de.dlr.exchange.excel.fha.util.FileBackup
import de.dlr.exchange.excel.fha.wizard.MyFHAWizard
import de.dlr.premise.element.AElement
import de.dlr.premise.registry.impl.RegistryImpl
import de.dlr.premise.functions.UseCaseRepository
import de.dlr.premise.util.PremiseHelper
import java.util.List
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.jface.wizard.WizardDialog
import org.eclipse.ui.PlatformUI

/**
 * Entry point for FHA synchronization
 */
class SyncModelGenerator implements IGeneratorMy {
	
	private boolean isUpdate = false
	private boolean isExport = true
	private boolean createBackup = true
	private boolean partialGeneration = false
	private String dataPath = "noDATAPass"
	private String usecaseFilePath = ""
	private RegistryImpl basereg
	
	
	override doGenerateFromAElements(ResourceSet resSet, List<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa) {
	
		partialGeneration = true
	
		setupRegistry(resSet)
		
		usecaseFilePath  = PremiseHelper.getResourceAbsPath(selectedElements.get(0))

		// call the wizard
		var wizardDialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), new MyFHAWizard(this))
		var dialogResult = wizardDialog.open()
		if (dialogResult == WizardDialog.CANCEL){
			return
		}
		
		fsa.compile(selectedElements.get(0), resSet)
	}
	
	override doGenerateFromResources(ResourceSet resSet, List<Resource> selectedFiles, ICharsetProvidingFileSystemAccess fsa) {
		
		partialGeneration = false
		
		if (!resSet.resources.nullOrEmpty && resSet.resources.head.contents.head instanceof UseCaseRepository) {

			setupRegistry(resSet)
			var usecaseRep = resSet.resources.head.contents.head as UseCaseRepository
			var selectedElement = usecaseRep.usecases.head

			usecaseFilePath = PremiseHelper.getResourceAbsPath(selectedElement)

			// call the wizard
			var wizardDialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				new MyFHAWizard(this))
			var dialogResult = wizardDialog.open()
			if (dialogResult == WizardDialog.CANCEL) {
				return
			}

			fsa.compile(selectedElement, resSet)

		}
			
	}

	
	def compile(ICharsetProvidingFileSystemAccess fsa, AElement selectedElement, ResourceSet resSet){
		
		// setup generators
		var modelGen = new FHAModelGenerator
		var premiseGen = new FHAPremiseImporter(basereg)
		var excelGen = new FHAExcelGenerator(dataPath)
		
		
		// procedure differs by wizard settings
		if (!isUpdate && isExport) {
			
			// simple export to excel 
			// create backup
			if (createBackup){
				var backupper = new FileBackup
				backupper.createDefaultBackup(resSet, dataPath)
			}
			
			// pick export method
			var Analysis premiseAnalysis
			if (partialGeneration){
				premiseAnalysis = modelGen.doGeneratePartialModelFromPremise(selectedElement)
			} else {
				premiseAnalysis = modelGen.doGenerateModelFromPremise(selectedElement)
			}
			
			// generate file
			excelGen.doGenerateNewExcelFromModel(fsa, premiseAnalysis)
			
		} else if (!isExport) {
			
			// import to premise from excel
			// create backup
			if (createBackup){
				var backupper = new FileBackup
				backupper.createBackup(resSet)
			}
			
			// get the analysis ready
			// when importing, the whole premise model is needed
			var excelAnalysis = modelGen.doGenerateModelFromExcel(dataPath)
			var premiseAnalysis = modelGen.doGenerateModelFromPremise(selectedElement)
			if (excelAnalysis == null){
				System.err.println("WARNING! An error occurred reading the file from path: " + dataPath + "\n")
				return;
			}
			
			// merge models and generate
			var mergedAnalysis = mergeE2P(excelAnalysis,premiseAnalysis)
			premiseGen.doGeneratePremiseFromModel(selectedElement, mergedAnalysis)
			
		} else if (isUpdate && isExport) {
			
			// export to excel, but merge with existing analysis
			// create backup
			if (createBackup){
				var backupper = new FileBackup
				backupper.createBackup(dataPath)
			}
			
			// get the excel model
			var fhaExcel = modelGen.doGenerateModelFromExcel(dataPath)
			if (fhaExcel == null){
				println("WARNING! An error occurred reading the file from path: "+dataPath+"\n")
				return;
			}
			
			// pick export method
			var Analysis fhaPremise
			if (partialGeneration){
				fhaPremise = modelGen.doGeneratePartialModelFromPremise(selectedElement)
			} else {
				fhaPremise = modelGen.doGenerateModelFromPremise(selectedElement)
			}

			// merge and export
			var fhaMerged = mergeP2E(fhaExcel,fhaPremise)
			excelGen.doGenerateNewExcelFromModel(fsa, fhaMerged)
		}
	}
	
	// For tests only
	def public compile(ICharsetProvidingFileSystemAccess fsa, AElement selectedElement){
		createBackup = false
		partialGeneration = false
		compile(fsa, selectedElement, null)
	}
	
	
	/** Sets up the Base.Registry for MetaTypeDef access */
	def setupRegistry(ResourceSet resSet){
		var baseregRes = resSet.getResource(URI.createURI("premise:/Base.registry"), true)
		basereg = baseregRes.contents.get(0) as RegistryImpl
	}
	
	def mergeP2E(Analysis excel, Analysis premise){
		val merger = new FHAMerge
		return merger.mergeP2E(premise, excel)
	}
	
	def mergeE2P(Analysis excel, Analysis premise){
		val merger = new FHAMerge
		return merger.mergeE2P(excel, premise)
	}
	
	public def saveWizardInput(boolean update, boolean export, boolean backup, String dataPath){
		this.isUpdate = update
		this.isExport = export
		this.dataPath = dataPath
		this.createBackup = backup
	}
	
	public def getCurrentFilePath(){
		return usecaseFilePath
	}
	
	
}
