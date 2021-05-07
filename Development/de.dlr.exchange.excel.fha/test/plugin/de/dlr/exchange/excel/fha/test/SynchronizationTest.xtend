/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.excel.fha.test

import de.dlr.exchange.base.xtend.test.CharsetProvidingInMemoryFileSystemAccess
import de.dlr.exchange.base.xtend.test.TestHelper
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ARepository
import de.dlr.premise.functions.UseCase
import de.dlr.premise.util.urihandlers.PremiseLibraryURIHandler
import de.dlr.premise.util.urihandlers.URIHandlerHelper
import java.io.File
import java.io.FileOutputStream
import org.junit.Test

import static org.junit.Assert.assertEquals

import de.dlr.exchange.excel.fha.SyncModelGenerator

class SynchronizationTest {
	
	val TEST_FOLDER = de.dlr.premise.util.TestHelper.locateFileForJUnit("de.dlr.exchange.excel.fha", "test/data/")
	
	// due to smartXLS behavior we need a delta
	val delta = 10;
	
	@Test
	def void testExport(){
		
		// define test files
		val input     = TEST_FOLDER + "input/Export.usecase";
		val output    = TEST_FOLDER + "output/Export.xlsx";
		val reference = TEST_FOLDER + "reference/Export.xlsx"
		
		// test a static export to the predefined Template
		val resource = TestHelper.loadResource(input)
		val uc = resource.contents.filter[c|c instanceof ARepository].head.eAllContents.findFirst[t|t instanceof UseCase]
		val CharsetProvidingInMemoryFileSystemAccess fsa = TestHelper.createInMemoryFileSystemAccess()
		
		// manually add base.registry
		var resSet = resource.resourceSet
		URIHandlerHelper.registerInto(resSet.URIConverter);
		resSet.getResource(PremiseLibraryURIHandler.PREMISE_GLOBAL_REGISTRY_URI, true);
		
		// skip the wizard, set flags according to test and compile file
		val gen = new SyncModelGenerator
		gen.saveWizardInput(false,true,false,"Export.xlsx")
		gen.compile(fsa, uc as AElement)
		
		// save the generated file
		TestHelper.saveFile(fsa, output)
		
		// check the output
		// as excel stores additional data, check the length with a delta
		assertEquals(new File(reference).length, new File(output).length, delta)
	}
	
	@Test
	def void testImport(){
		
		// define test files
		val input     = TEST_FOLDER + "input/FreshImport.usecase";
		val output    = TEST_FOLDER + "output/FreshImport.usecase";
		val reference = TEST_FOLDER + "reference/FreshImport.usecase";
		
		// test a merge import from an existing FHA Excel file to a *.usecase file
		// some functions are already contained, others need to be added
		val resource = TestHelper.loadResource(input)
		val uc = resource.contents.filter[c|c instanceof ARepository].head.eAllContents.findFirst[t|t instanceof UseCase]
		val CharsetProvidingInMemoryFileSystemAccess fsa = TestHelper.createInMemoryFileSystemAccess()
		
		// manually add base.registry
		var resSet = resource.resourceSet
		URIHandlerHelper.registerInto(resSet.URIConverter);
		resSet.getResource(PremiseLibraryURIHandler.PREMISE_GLOBAL_REGISTRY_URI, true);
		
		// skip the wizard, set flags according to test and compile file
		val gen = new SyncModelGenerator

		gen.setupRegistry(resSet)
		val dataPath = de.dlr.premise.util.TestHelper.locateFile("de.dlr.exchange.excel.fha", "test/data/input/FreshImport.xlsx").path
		gen.saveWizardInput(true,false,false,dataPath)
		gen.compile(fsa, uc as AElement)
		
		// save the premise file
		resource.save(new FileOutputStream(output),null)
		
		// as we generate new ids one can only check the file length
		assertEquals(new File(reference).length, new File(output).length, delta)
	}
	
	@Test
	def void testMergeExport(){
		
		// test a merge export to an existing FHA Excel file with changes made
		// start with input/Export.usecase
		// remove Function charlie, rename Phase-A -> Phase-1, add Phase-Z, Add Gamma Failure to Function Alpha
		// remove Bravo Failure, set Bravo Objectives to PASSED

		// define test files
		val input     = TEST_FOLDER + "input/ExportMerge.usecase"
		val output    = TEST_FOLDER + "output/PostMerge.xlsx"; 
		val reference = TEST_FOLDER + "reference/PostMerge.xlsx";

		// get input file		
		val resource = TestHelper.loadResource(input)
		val uc = resource.contents.filter[c|c instanceof ARepository].head.eAllContents.findFirst[t|t instanceof UseCase]
		val CharsetProvidingInMemoryFileSystemAccess fsa = TestHelper.createInMemoryFileSystemAccess()
		
		// manually add base.registry
		var resSet = resource.resourceSet
		URIHandlerHelper.registerInto(resSet.URIConverter);
		resSet.getResource(PremiseLibraryURIHandler.PREMISE_GLOBAL_REGISTRY_URI, true);
		
		// skip the wizard, set flags according to test and compile file
		val gen = new SyncModelGenerator
		val dataPath = de.dlr.premise.util.TestHelper.locateFile("de.dlr.exchange.excel.fha", "test/data/input/PreMerge.xlsx").path
		gen.saveWizardInput(true,true,false,dataPath)
		gen.compile(fsa, uc as AElement)
		
		// save the generated file
		TestHelper.saveFile(fsa, output)
		
		// check the output
		// as excel stores additional data, check the length with a delta
		assertEquals(new File(reference).length,new File(output).length, delta)
	}
}