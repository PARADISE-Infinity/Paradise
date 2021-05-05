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

import de.dlr.aspect.graphml.transform.g2p.GraphMLToPremiseTransform
import de.dlr.aspect.graphml.transform.g2p.GraphMLToPremiseTransformException
import de.dlr.premise.functions.UseCase
import de.dlr.premise.functions.UseCaseFactory
import java.io.File
import java.io.IOException
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.util.EcoreUtil
import org.graphdrawing.graphml.Graphml
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import de.dlr.premise.system.SystemFactory
import de.dlr.premise.system.SystemComponent

import static de.dlr.aspect.graphml.GraphMLTestHelper.*
import static org.junit.Assert.*

import static extension com.google.common.collect.Iterators.*
import org.eclipse.emf.ecore.resource.ResourceSet
import org.junit.Before

class GraphMLToPremiseTest {	
	val static PREFIX = "file:/"
	val static PATH = getFile("test/data/GraphMLToPremise/")
	
	val static INPUT = "tree.graphml"
	val static OUTPUT_PREMISE = "tree.premise"
	val static OUTPUT_USECASE = "tree.usecase"
	
	val static INPUT_NO_TREE = "noTree.graphml"
	val static INPUT_WRONG_RELATION = "wrongRelation.graphml"
	val static INPUT_WRONG_CONTIANMENT = "wrongContainment.graphml"
	
  	@Rule
  	val public ExpectedException exception = ExpectedException.none()

	var ResourceSet resSet

	@Before 
	def void before() {
		resSet = createResourceSet
		resSet.URIConverter.URIMap.put(URI.createURI(PREFIX), URI.createFileURI(PATH))
	}

	@Test
	def void testTransform() throws IOException {
		val in = resSet.getResource(URI.createURI(PREFIX + INPUT), true)
		EcoreUtil.resolveAll(resSet)
		
		val graphml = in.contents.get(0) as Graphml

		val transform = new GraphMLToPremiseTransform(graphml)
		transform.run

		// should generate one entry each for premise and usecase file
		transform.roots.keySet => [
			assertEquals(2, length)
			assertTrue(contains(PREFIX + OUTPUT_PREMISE))
			assertTrue(contains(PREFIX + OUTPUT_USECASE))
		]
		

		
		// assert basic facts about model
		transform.roots.get(PREFIX + OUTPUT_PREMISE) => [ roots | 
			// 1 root and ...
			assertEquals(1, roots.length)
			// ... 16 children, for a total of 17 SystemComponents
			assertEquals(16, roots.iterator.map[eAllContents.filter(SystemComponent)].concat.size)
		]
		
		transform.roots.get(PREFIX + OUTPUT_USECASE) => [ roots | 
			// 1 root and ...
			assertEquals(1, roots.length)
			// ... 8 children, for a total of 9 UseCases
			assertEquals(7, roots.iterator.map[eAllContents.filter(UseCase)].concat.size)
		]
		
		
		// now save it and do size comparison
		val outPr = resSet.createResource(URI.createURI(PREFIX + OUTPUT_PREMISE))
		val outUc = resSet.createResource(URI.createURI(PREFIX + OUTPUT_USECASE))
		
		outPr.contents.add(SystemFactory.eINSTANCE.createProjectRepository => [
			projects += transform.roots.get(PREFIX + OUTPUT_PREMISE).filter(SystemComponent)
		])
		outUc.contents.add(UseCaseFactory.eINSTANCE.createUseCaseRepository => [
			usecases += transform.roots.get(PREFIX + OUTPUT_USECASE).filter(UseCase)
		])
		
		outPr.save(null)
		outUc.save(null)
		
		assertEquals(3089, new File(PATH + OUTPUT_PREMISE).length)
		assertEquals(1439, new File(PATH + OUTPUT_USECASE).length)
	}
	
	/**
	 * Test an input file with a non-tree-like-structure
	 */
	@Test
	def void testNoTree() throws IOException {
		val in = resSet.getResource(URI.createURI(PREFIX + INPUT_NO_TREE), true)
		EcoreUtil.resolveAll(resSet)
		
		val graphml = in.contents.get(0) as Graphml
		
		val transform = new GraphMLToPremiseTransform(graphml)
		
		exception.expect(GraphMLToPremiseTransformException)
		exception.expectMessage('''Node "1" has multiple parents, Input is not a tree''')
		
		transform.run
	}
	
	/**
	 * Test input file with a Relation from UseCase to SystemComponent (not possible in PREMISE)
	 */
	@Test
	def void testWrongRelation() throws IOException {
		val in = resSet.getResource(URI.createURI(PREFIX + INPUT_WRONG_RELATION), true)
		EcoreUtil.resolveAll(resSet)
		
		val graphml = in.contents.get(0) as Graphml
		
		val transform = new GraphMLToPremiseTransform(graphml)
		
		exception.expect(GraphMLToPremiseTransformException)
		exception.expectMessage("Can't create Relation from UseCase to SystemComponent")
		
		transform.run
	}
	
	/**
	 * Test input file with an UseCase contained in a SystemComponent
	 */
	@Test
	def void testWrongContainment() throws IOException {
		val in = resSet.getResource(URI.createURI(PREFIX + INPUT_WRONG_CONTIANMENT), true)
		EcoreUtil.resolveAll(resSet)
		
		val graphml = in.contents.get(0) as Graphml
		
		val transform = new GraphMLToPremiseTransform(graphml)
		
		exception.expect(GraphMLToPremiseTransformException)
		exception.expectMessage("SystemComponent can't contain UseCase")
		
		transform.run
	}
}