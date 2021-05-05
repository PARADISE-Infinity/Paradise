/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.graphml.cfta.xtend.test

import de.dlr.exchange.base.xtend.test.CharsetProvidingInMemoryFileSystemAccess
import de.dlr.exchange.base.xtend.test.TestHelper;
import java.util.ArrayList
import org.eclipse.emf.ecore.resource.Resource
import org.junit.Test
import de.dlr.exchange.graphml.cfta.xtend.Generator
import com.google.common.io.Files
import java.io.File
import static org.junit.Assert.assertTrue
import de.dlr.premise.element.ARepository
import de.dlr.premise.element.AElement

class CFTAExporterTest {
	
	val TEST_FOLDER = de.dlr.premise.util.TestHelper.locateFileForJUnit("de.dlr.exchange.graphml.cfta.xtend", "test/data/");
	
	@Test
	def void testExport(){
		
		val Resource resource = TestHelper.loadResource(TEST_FOLDER + "input/MTLE.premise")
		var ArrayList<Resource> selectedFiles = new ArrayList<Resource>()
		selectedFiles.add(resource)
		
		val CharsetProvidingInMemoryFileSystemAccess fsa = TestHelper.createInMemoryFileSystemAccess()
		val Generator gen = new Generator()
		gen.doGenerateFromResources(resource.resourceSet, selectedFiles, fsa)
		
		// full tree generation
		TestHelper.saveFile(fsa, TEST_FOLDER+"output/Top_Component.cfta.graphml")
		assertTrue(Files.equal(new File(TEST_FOLDER+"reference/Top_Component.cfta.graphml"), new File(TEST_FOLDER+"output/Top_Component.cfta.graphml")))
		
		// single component generation
		
		val resSet = resource.resourceSet
		if (!resSet.resources.nullOrEmpty && resSet.resources.get(0).contents.get(0) instanceof ARepository) {
			
			// create resources list of premise and usecase files
			var resources = selectedFiles.filter(r|"premise".equals(r.URI.fileExtension)).toList
			
			var selectedElements = new ArrayList<AElement>
			for (Resource res : resources) {
				selectedElements.addAll((res.contents.get(0) as ARepository).eAllContents.toList.filter(AElement))
			}

			// check selected elements and compile
			if (!selectedElements.nullOrEmpty) {
				var selectedElement = selectedElements.get(0)
				selectedElements.clear
				selectedElements.add(selectedElement)
				
				gen.doGenerateFromAElements(resSet, selectedElements, fsa)
				TestHelper.saveFile(fsa, TEST_FOLDER+"output/Top_Component.scfta.graphml")
				assertTrue(Files.equal(new File(TEST_FOLDER+"reference/Top_Component.scfta.graphml"), new File(TEST_FOLDER+"output/Top_Component.scfta.graphml")))
			}
		}
					
		
	}
	
}