/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.validation

import de.dlr.premise.component.ComponentPackage
import de.dlr.premise.element.ElementPackage
import de.dlr.premise.functionpool.FunctionpoolPackage
import de.dlr.premise.graph.GraphPackage
import de.dlr.premise.registry.RegistryPackage
import de.dlr.premise.representation.RepresentationPackage
import de.dlr.premise.system.SystemPackage
import de.dlr.premise.functions.UseCasePackage
import de.dlr.premise.validation.ocl.ResourceSetAwareOCLDelegateDomainFactory
import org.eclipse.emf.common.util.BasicDiagnostic
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EValidator
import org.eclipse.emf.ecore.EValidator.Registry
import org.eclipse.ocl.xtext.oclinecore.validation.OCLinEcoreEObjectValidator

import static extension de.dlr.premise.util.PremiseHelper.*

/** 
 * OCL Registration as a Singleton, because the registration should only be done once.
 */
class OCLRegistration {
	/**
	 * All ePackages of the PREMISE model
	 */
	val static PREMISE_PACKAGES = #[
		ComponentPackage.eINSTANCE, 
		ElementPackage.eINSTANCE, 
		FunctionpoolPackage.eINSTANCE,
		GraphPackage.eINSTANCE, 
		RegistryPackage.eINSTANCE, 
		RepresentationPackage.eINSTANCE, 
		SystemPackage.eINSTANCE,
		UseCasePackage.eINSTANCE
	].closure[ESubpackages]
	
	
	static boolean hasRegistered = false

	def static synchronized void register() {
		if (hasRegistered) {
			return;
		}
		hasRegistered = true
		
		// register ocl delegate domain
		ResourceSetAwareOCLDelegateDomainFactory.register()
		
		// overwrite default validators with ones that can read ocl warnings
		val oclInEcoreEObjectValidator = new OCLinEcoreEObjectValidator()
		val Registry instance = EValidator.Registry.INSTANCE
		for (ePackage : de.dlr.premise.validation.OCLRegistration.PREMISE_PACKAGES) {
			instance.put(ePackage, oclInEcoreEObjectValidator)
		}
		
		// Warm up constraints
		warmUpOCLConstraints()
	}
	
	/**
	 * Executing OCL constraints is in general thread safe. However during the first execution of a constraint, 
	 * internal caches are filled up, which are not thread safe (see https://wiki.eclipse.org/OCL/FAQ#OCL_Thread_Safety).
	 * 
	 * To prevent errors with parallel validation, we warm up caches by executing every constraint once.
	 */
	def static warmUpOCLConstraints() {
		val validator = new OCLinEcoreEObjectValidator()
		
		val eClasses = de.dlr.premise.validation.OCLRegistration.PREMISE_PACKAGES.flatMap[EClassifiers].filter(EClass).filter[!abstract && !interface]
		
		val diagnostics = new BasicDiagnostic()
		val context = newHashMap
		for (eClass : eClasses) {
			val instance = eClass.EPackage.EFactoryInstance.create(eClass)
			try {
				validator.validate(eClass, instance, diagnostics, context)
			} catch (Throwable t) {
				System.err.println(t)
			}
		}
	}
}
