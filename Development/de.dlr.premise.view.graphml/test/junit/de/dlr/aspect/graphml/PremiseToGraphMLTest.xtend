/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.aspect.graphml

import de.dlr.aspect.graphml.transform.p2g.PremiseToGraphMLTransform
import de.dlr.premise.functions.UseCaseRepository
import java.io.File
import java.io.IOException
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreUtil
import org.junit.Test
import de.dlr.premise.system.ProjectRepository

import static org.junit.Assert.*

import static extension de.dlr.aspect.graphml.GraphMLTestHelper.*
import static extension de.dlr.aspect.graphml.util.PremiseModelHelper.*
import org.junit.Before

class PremiseToGraphMLTest {
	val static PREFIX = "file:/"
	val static PATH = getFile("test/data/PremiseToGraphML/")
	
	val static OUTPUT = "tree.graphml"
	val static INPUT_PREMISE = "tree.premise"
	val static INPUT_USECASE = "tree.usecase"
	
	var ResourceSet resSet

	@Before 
	def void before() {
		resSet = createResourceSet
	}

	@Test
	def void testTransform() throws IOException {
		resSet.URIConverter.URIMap.put(URI.createURI(PREFIX), URI.createFileURI(PATH))

		val inPr = resSet.getResource(URI.createURI(PREFIX + INPUT_PREMISE), true)
		val inUc = resSet.getResource(URI.createURI(PREFIX + INPUT_USECASE), true)

		EcoreUtil.resolveAll(resSet)

		val ucRepo = inUc.contents.get(0) as UseCaseRepository
		val prRepo = inPr.contents.get(0) as ProjectRepository

		val roots = #{PREFIX + INPUT_PREMISE -> prRepo.contained, PREFIX + INPUT_USECASE -> ucRepo.contained}

		val transform = new PremiseToGraphMLTransform(roots)
		transform.run

		// first, some basic facts about the generated file
		transform.root.graph.get(0) => [
			// 28 edges  (containment + relation)
			assertEquals(28, edge.length)
			// 25 nodes (usecase + premise)
			assertEquals(25, node.length)
		]
		

		val out = resSet.createResource(URI.createURI(PREFIX + OUTPUT))
		out.contents.add(transform.root)
		out.save(null)
		
		assertEquals(44420, new File(PATH + OUTPUT).length)
	}
}
