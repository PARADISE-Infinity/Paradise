/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.trace.xtend

import de.dlr.exchange.base.xtend.test.CharsetProvidingInMemoryFileSystemAccess
import de.dlr.exchange.base.xtend.test.TestHelper
import de.dlr.premise.element.ARepository
import java.io.File
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals

import static extension de.dlr.premise.util.PremiseHelper.getMetaData

/** 
 * @author enge_do
 */
class GeneratorTest {
	static String PATH_TEST_FILES = de.dlr.premise.util.TestHelper.locateFile("de.dlr.exchange.excel.trace.xtend", "test/data/").path
	
	static String PATH_INPUT_FILE = PATH_TEST_FILES + "input/testReqTrace.premise"
	
	static String PATH_TRACE_OUTPUT_FILE =  PATH_TEST_FILES + "output/testReqTrace.xlsx"
	static String PATH_TRACE_REFERENCE_FILE =  PATH_TEST_FILES + "reference/testReqTrace.xlsx"
	
	static String PATH_TRACE_OUTPUT_FILE_2 =  PATH_TEST_FILES + "output/testReqTraceSumOn.xlsx"
	static String PATH_TRACE_REFERENCE_FILE_2 =  PATH_TEST_FILES + "reference/testReqTraceSumOn.xlsx"
	
	static String PATH_DSM_OUTPUT_FILE =  PATH_TEST_FILES + "output/testReqDSM.xlsx"
	static String PATH_DSM_REFERENCE_FILE =  PATH_TEST_FILES + "reference/testReqDSM.xlsx"
	
	var Resource resource
	var ARepository repository
	var List<Resource> selectedFiles
	var CharsetProvidingInMemoryFileSystemAccess fsa

	@Before
	def void before() {
		resource = TestHelper.loadResource(PATH_INPUT_FILE)
		selectedFiles = #[resource]
		EcoreUtil2.resolveAll(resource)
		fsa = TestHelper.createInMemoryFileSystemAccess()

		repository = resource.contents.get(0) as ARepository
	}

	@Test
	def void testTrace() {
		new TraceOptions().setOptions(repository)
		val group = repository.getMetaData(TraceOptions.OPT_TECH).getMetaData(
			TraceGenerator.OPT_DEPENDENCY_COUNT)
		group.setValue("off")

		var TraceGenerator gen = new TraceGenerator()
		gen.doGenerateFromResources(resource.getResourceSet(), selectedFiles, fsa)
		new File(PATH_TRACE_REFERENCE_FILE).length() <=> TestHelper.saveFile(fsa, PATH_TRACE_OUTPUT_FILE).length()
	}

	@Test
	def void testTraceSumOn() {
		new TraceOptions().setOptions(repository)
		val group = repository.getMetaData(TraceOptions.OPT_TECH).getMetaData(DSMGenerator.OPT_DEPENDENCY_COUNT)
		group.setValue("on")

		var TraceGenerator gen = new TraceGenerator()
		gen.doGenerateFromResources(resource.getResourceSet(), selectedFiles, fsa) // call xtend script
		new File(PATH_TRACE_REFERENCE_FILE_2).length() <=> TestHelper.saveFile(fsa, PATH_TRACE_OUTPUT_FILE_2).length()
	}

	@Test
	def void testDSM() {
		new DSMOptions().setOptions(repository)
		var DSMGenerator gen = new DSMGenerator()
		gen.doGenerateFromResources(resource.getResourceSet(), selectedFiles, fsa)
		new File(PATH_DSM_REFERENCE_FILE).length() <=> TestHelper.saveFile(fsa, PATH_DSM_OUTPUT_FILE).length()
	}

	def operator_spaceship(long expected, long actual) {
		assertEquals(expected, actual)
	}

}
