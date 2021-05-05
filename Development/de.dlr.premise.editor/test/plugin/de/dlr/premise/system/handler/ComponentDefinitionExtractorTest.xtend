/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.system.handler

import de.dlr.premise.component.ComponentPackage
import de.dlr.premise.component.impl.my.ComponentFactoryImplMy
import de.dlr.premise.registry.RegistryPackage
import de.dlr.premise.registry.impl.my.RegistryFactoryImplMy
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.system.SystemPackage
import de.dlr.premise.system.impl.my.SystemFactoryImplMy
import de.dlr.premise.functions.UseCasePackage
import de.dlr.premise.functions.impl.my.UseCaseFactoryImplMy
import de.dlr.premise.util.RegistryHelper
import de.dlr.premise.util.TestHelper
import java.io.File
import org.eclipse.core.runtime.Platform
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

class ComponentDefinitionExtractorTest {
	val static PREFIX = "file:/"
	val static PATH = getFile("test/data/ComponentDefinitionExtractorTest/")
	
	val static INPUT_FOLDER = "input/"
	val static OUTPUT_FOLDER = "output/"
	val static REFERENCE_FOLDER = "reference/"
	
	val static VALID_PREMISE = "ValidSystem.premise"
	val static OUTGOING_REFERENCES_PREMISE = "OutgoingReferencesSystem.premise"
	val static COMPONENT = "Components.component"
	val static REGISTRY = "Registry.registry"
	val static USECASE = "Problem.usecase"

	var ResourceSet resSet

	@Before 
	def void before() {
		resSet = createResourceSet
		resSet.URIConverter.URIMap.put(URI.createURI(PREFIX), URI.createFileURI(PATH + INPUT_FOLDER))
	}
	
	@Test(expected=IllegalStateException)
	def void testNotPrepared() {
		val premiseRes = resSet.getResource(VALID_PREMISE.toURI, true)
		EcoreUtil.resolveAll(resSet)
		
		val sysComp = premiseRes.getEObject("toExtract") as SystemComponent
		
		val extactor = new ComponentDefinitionExtractor(sysComp)
		
		extactor.execute()
	}

	@Test
	def void testNoComponentRepository() {
		val premiseRes = resSet.getResource(VALID_PREMISE.toURI, true)
		EcoreUtil.resolveAll(resSet)
		
		val sysComp = premiseRes.getEObject("toExtract") as SystemComponent
		
		val extactor = new ComponentDefinitionExtractor(sysComp)
		
		val error = extactor.prepare()
		assertNotNull(error)
		assertEquals("No ComponentRepository", error.title)
	}
	
	@Test
	def void testRootExtraction() {
		val premiseRes = resSet.getResource(VALID_PREMISE.toURI, true)
		
		// there is no reference between premise and component, so we have to load it directly here
		resSet.getResource(COMPONENT.toURI, true)
		
		EcoreUtil.resolveAll(resSet)
		
		val sysComp = premiseRes.getEObject("root") as SystemComponent
		
		val extactor = new ComponentDefinitionExtractor(sysComp)
		
		val error = extactor.prepare()
		assertNotNull(error)
		assertEquals("Selected SystemComponent is a root", error.title)
	}
	
	@Test
	def void testOutgoingReferences() {
		val premiseRes = resSet.getResource(OUTGOING_REFERENCES_PREMISE.toURI, true)
		
		// there is no reference between premise and component, so we have to load it directly here
		resSet.getResource(COMPONENT.toURI, true)
		
		EcoreUtil.resolveAll(resSet)
		
		val sysComp = premiseRes.getEObject("toExtract") as SystemComponent
		
		val extactor = new ComponentDefinitionExtractor(sysComp)
		
		val error = extactor.prepare()
		assertNotNull(error)
		assertEquals("Selected SystemComponent references external elements", error.title)
	}	
	
	@Test
	def void testValid() {
		val premiseRes = resSet.getResource(VALID_PREMISE.toURI, true)
		
		// there is no reference between premise and component, so we have to load it directly here
		resSet.getResource(COMPONENT.toURI, true)
		
		EcoreUtil.resolveAll(resSet)
		
		val sysComp = premiseRes.getEObject("toExtract") as SystemComponent
		
		val extactor = new ComponentDefinitionExtractor(sysComp)
		
		val error = extactor.prepare()
		assertNull(error)
		
		extactor.execute()
		
		// change path to output folder
		resSet.URIConverter.URIMap.put(URI.createURI(PREFIX), URI.createFileURI(PATH + OUTPUT_FOLDER))
		resSet.resources.forEach[save(null)]
		
		assertOutputSizeEqualsReferenceSize(VALID_PREMISE)
		assertOutputSizeEqualsReferenceSize(COMPONENT)
		assertOutputSizeEqualsReferenceSize(REGISTRY)
		assertOutputSizeEqualsReferenceSize(USECASE)
	}
	
	def private static toURI(String path) {
		return URI.createURI(PREFIX + path);
	}
	
		
	def private static assertOutputSizeEqualsReferenceSize(String name) {
		assertEquals(new File(PATH + REFERENCE_FOLDER + name).length, new File(PATH + OUTPUT_FOLDER + name).length)
	}

	def private static getFile(String path) {

		// allows running as junit test standalone or plugin test in the testsuite
		if (Platform.isRunning) {
			TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_EDITOR, path).path
		} else {
			path
		}
	}

	def static ResourceSet createResourceSet() {
		if (Platform.isRunning) {
			return new ResourceSetImpl();
		}
				
		RegistryHelper.registerFactory(SystemPackage.eNS_URI, new SystemFactoryImplMy)
		RegistryHelper.registerFactory(ComponentPackage.eNS_URI, new ComponentFactoryImplMy)
		RegistryHelper.registerFactory(RegistryPackage.eNS_URI, new RegistryFactoryImplMy)
		RegistryHelper.registerFactory(UseCasePackage.eNS_URI, new UseCaseFactoryImplMy)
		
		SystemPackage.eINSTANCE.eClass
		ComponentPackage.eINSTANCE.eClass
		RegistryPackage.eINSTANCE.eClass
		UseCasePackage.eINSTANCE.eClass

		new ResourceSetImpl => [
			packageRegistry => [
				put(SystemPackage.eNS_URI, SystemPackage.eINSTANCE)
				put(ComponentPackage.eNS_URI, ComponentPackage.eINSTANCE)
				put(RegistryPackage.eNS_URI, RegistryPackage.eINSTANCE)
				put(UseCasePackage.eNS_URI, UseCasePackage.eINSTANCE)
			]
			resourceFactoryRegistry.extensionToFactoryMap => [
				put(SystemPackage.eNAME, new XMIResourceFactoryImpl)
				put(ComponentPackage.eNAME, new XMIResourceFactoryImpl)
				put(RegistryPackage.eNAME, new XMIResourceFactoryImpl)
				put(UseCasePackage.eNAME, new XMIResourceFactoryImpl)
			]
		]
	}
}
