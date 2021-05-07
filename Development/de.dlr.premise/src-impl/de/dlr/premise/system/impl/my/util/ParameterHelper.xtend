/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.system.impl.my.util

import com.google.common.cache.CacheBuilder
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.registry.IQuantity
import de.dlr.premise.system.ABalancing
import de.dlr.premise.system.ComponentReferenceSubstitutionMapping
import de.dlr.premise.system.Parameter
import de.dlr.premise.system.SystemPackage
import de.dlr.premise.system.impl.ParameterImpl
import de.dlr.premise.functions.RequiredParameter
import de.dlr.premise.util.PremiseHelper
import de.dlr.premise.util.scope.ScopedEObjectFactory
import java.util.Collections
import java.util.concurrent.TimeUnit
import org.eclipse.emf.common.notify.Notifier
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreEList

import static extension de.dlr.premise.util.PremiseHelper.eAllContentsIncludingRoot
import static extension de.dlr.premise.util.PremiseHelper.flatMap
import static extension de.dlr.premise.util.PremiseHelper.getRootNotifier
import static extension de.dlr.premise.util.PremiseHelper.toUnmodifieableEList
import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.getWrappedElement

class ParameterHelper {
	
	def static EList<RequiredParameter> getSatisfiesRequiredParameters(ParameterImpl it) {
		val satisfiesRequiredParameters = satisfiedSatisfieables.filter(RequiredParameter).toUnmodifieableEList
		
		return new EcoreEList.UnmodifiableEList<RequiredParameter>(it, SystemPackage.Literals.PARAMETER__SATISFIES_REQUIRED_PARAMETERS,
			satisfiesRequiredParameters.size, satisfiesRequiredParameters.toArray)
	}
	

	
	def static boolean isUniqueTarget(Parameter it) {	
		multipleTargetingBalancings.empty
	}
	
	def static String getErrorMessageForUniqueTarget(Parameter it) {	
		multipleTargetingBalancings
			.map["Parameter is target of multiple balancings: " + map[PremiseHelper.getMeaningfulName(it)].join(", ") + "."]
			.join(" ")
	}
	
	private def static getMultipleTargetingBalancings(Parameter param) {
		multipleTargetingBalancingsCache.get(param)
	}
	
	val private static multipleTargetingBalancingsCache = CacheBuilder.newBuilder()
		.expireAfterWrite(500, TimeUnit.MILLISECONDS)
		.build[Parameter param |
			if (param.eContainer instanceof ComponentReferenceSubstitutionMapping) {
				return Collections.emptyList
			}
			val sp = param.allScopedToValidate
			sp
				.map[paramScoped | 
					param.rootNotifier.allBals.filter[target == paramScoped]
				]
				.filter[size > 1]
	 	]
	
	private def static getAllBals(Notifier root) {
		allBalsCache.get(root)
	}
	
	val private static allBalsCache = CacheBuilder.newBuilder()
		.expireAfterWrite(500, TimeUnit.MILLISECONDS)
		.build[Notifier root |
			ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(root, ABalancing) as Iterable<?> as Iterable<ABalancing<?>>
	 	]
	 	
	 private static def getAllScopedToValidate(EObject unscopedEObj) {
	 	val resSet = unscopedEObj.eResource?.resourceSet
	 	
 		if (resSet != null) {
			val roots = resSet.resourcesToValidate.flatMap[contents].map[ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(it)]
			val candidates = roots.iterator.flatMap[eAllContentsIncludingRoot]
			candidates.filter[wrappedElement == unscopedEObj].toList
		} else {
			ScopedEObjectFactory.INSTANCE.createAllScopedEObjects(unscopedEObj).toList
		}
	 }
	 
	 private static def getResourcesToValidate(ResourceSet resSet) {
	 	#[resSet.resources.head] + resSet.resources.tail.filter[URI.fileExtension != "component"]
	 }
	
	def static boolean isValidSatisfiedUnit(Parameter it) {
		unitViolatedSatisfieables.empty
	}
	
	def static String getErrorMessageForValidSatisfiedUnit(Parameter it) {
		"Parameter should have the same unit as " + unitViolatedSatisfieables.filter(ANameItem).map [
			PremiseHelper.getMeaningfulName(it) + " (" + ((it as IQuantity).unit?.symbol ?: "none") + ")"
		].join(", ")
	}
	
	private def static getUnitViolatedSatisfieables(Parameter it) {
		unitViolatedSatisfieablesCache.get(it)
	}
	
	val private static unitViolatedSatisfieablesCache = CacheBuilder.newBuilder().expireAfterWrite(500,
		TimeUnit.MILLISECONDS).build [ Parameter it |
		satisfiedSatisfieables.filter(IQuantity).filter[unit != null].filter[satisfied|satisfied.unit != unit].toList
	]
}