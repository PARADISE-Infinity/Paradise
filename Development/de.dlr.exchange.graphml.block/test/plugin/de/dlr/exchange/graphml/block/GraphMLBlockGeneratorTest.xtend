/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.block

import com.google.common.io.Files
import de.dlr.exchange.graphml.block.settings.GraphMLBlockSettings
import de.dlr.premise.util.TestHelper
import java.io.File
import java.nio.charset.Charset
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.EcoreUtil
import org.junit.Before
import org.junit.Test
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent

import static org.junit.Assert.*

class GraphMLBlockGeneratorTest {
	val TEST_FOLDER = TestHelper.locateFileForJUnit("de.dlr.exchange.graphml.block", "test/data/GraphMLBlockGeneratorTest/");
	
	var ResourceSet resSet
	var Resource premiseRes
	var ProjectRepository repo
	
	var GraphMLBlockSettings settings
	
	@Before
	def void before() {
		resSet = new ResourceSetImpl()
		premiseRes = resSet.getResource(URI.createFileURI(TEST_FOLDER + "input/My.premise"), true)
		EcoreUtil.resolveAll(resSet)
		repo = premiseRes.contents.head as ProjectRepository
		
		settings = new GraphMLBlockSettings
	}
	
	@Test
	def void testExport() {
		settings.initialRoots = newHashSet(#[repo.projects.head] + repo.projects.head.children.filter(SystemComponent))
		settings.setRestictedDepth(true, 4)
		settings.charset = "cp1252"
		settings.drawSelfReferences = true
		
		val exporter = new GraphMLBlockGenerator(settings)
		val generated = exporter.generate
		
		val file = new File(TEST_FOLDER + "output/My.graphml")
		if (!file.exists) {
			Files.createParentDirs(file)
			file.createNewFile
		}
		Files.write(generated, file, Charset.forName("cp1252"));
		assertTrue(Files.equal(file, new File(TEST_FOLDER + "reference/My.graphml")))
	}
	
	@Test
	def void testExportWithoutGroups() {
		settings.initialRoots = newHashSet(#[repo.projects.head] + repo.projects.head.children.filter(SystemComponent))
		settings.setRestictedDepth(true, 4)
		settings.charset = "cp1252"
		settings.drawSelfReferences = true
		settings.showHierarchyAsGroups = false
		
		val exporter = new GraphMLBlockGenerator(settings)
		val generated = exporter.generate
		
		val file = new File(TEST_FOLDER + "output/My_without_groups.graphml")
		if (!file.exists) {
			Files.createParentDirs(file)
			file.createNewFile
		}
		Files.write(generated, file, Charset.forName("cp1252"));
		assertTrue(Files.equal(file, new File(TEST_FOLDER + "reference/My_without_groups.graphml")))
	}
}