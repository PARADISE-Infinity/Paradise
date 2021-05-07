/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.aspect.graphml

import com.yworks.yed.YedPackage
import com.yworks.ygraphml.YGraphMLPackage
import com.yworks.ygraphml.util.my.YGraphMLResourceFactoryImplMy
import de.dlr.premise.component.ComponentPackage
import de.dlr.premise.component.impl.my.ComponentFactoryImplMy
import de.dlr.premise.functions.UseCasePackage
import de.dlr.premise.functions.impl.my.UseCaseFactoryImplMy
import de.dlr.premise.util.RegistryHelper
import de.dlr.premise.util.TestHelper
import org.eclipse.core.runtime.Platform
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.graphdrawing.graphml.GraphMLPackage
import de.dlr.premise.system.SystemPackage
import de.dlr.premise.system.impl.my.SystemFactoryImplMy
import de.dlr.premise.registry.RegistryPackage
import de.dlr.premise.registry.impl.my.RegistryFactoryImplMy

class GraphMLTestHelper {

    def static ResourceSet createResourceSet() {
    	if (Platform.isRunning) {
			return new ResourceSetImpl();
		}

		RegistryHelper.registerFactory(SystemPackage.eNS_URI, new SystemFactoryImplMy)
		RegistryHelper.registerFactory(ComponentPackage.eNS_URI, new ComponentFactoryImplMy)
		RegistryHelper.registerFactory(RegistryPackage.eNS_URI, new RegistryFactoryImplMy)
		RegistryHelper.registerFactory(UseCasePackage.eNS_URI, new UseCaseFactoryImplMy)

		GraphMLPackage.eINSTANCE.eClass
		YGraphMLPackage.eINSTANCE.eClass
		YedPackage.eINSTANCE.eClass
		
		SystemPackage.eINSTANCE.eClass
		ComponentPackage.eINSTANCE.eClass
		RegistryPackage.eINSTANCE.eClass
		UseCasePackage.eINSTANCE.eClass
    
        new ResourceSetImpl => [
        	packageRegistry => [
				put(GraphMLPackage.eNS_URI, GraphMLPackage.eINSTANCE)
				put(YGraphMLPackage.eNS_URI, YGraphMLPackage.eINSTANCE)
				put(YedPackage.eNS_URI, YedPackage.eINSTANCE)
				
				put(SystemPackage.eNS_URI, SystemPackage.eINSTANCE)
				put(ComponentPackage.eNS_URI, ComponentPackage.eINSTANCE)
				put(RegistryPackage.eNS_URI, RegistryPackage.eINSTANCE)
				put(UseCasePackage.eNS_URI, UseCasePackage.eINSTANCE)
			]
	        
	        resourceFactoryRegistry.extensionToFactoryMap => [
				put(GraphMLPackage.eNAME, new YGraphMLResourceFactoryImplMy)
				
				put(SystemPackage.eNAME, new XMIResourceFactoryImpl)
				put(ComponentPackage.eNAME, new XMIResourceFactoryImpl)
				put(RegistryPackage.eNAME, new XMIResourceFactoryImpl)
				put(UseCasePackage.eNAME, new XMIResourceFactoryImpl)
			]
		]
    }
    
    def static getFile(String path) {
    	// allows running as junit test standalone or plugin test in the testsuite
    	if (Platform.isRunning) {
    		TestHelper.locateFile("de.dlr.premise.view.graphml", path).path
    	} else {
    		path
    	}
    }
}