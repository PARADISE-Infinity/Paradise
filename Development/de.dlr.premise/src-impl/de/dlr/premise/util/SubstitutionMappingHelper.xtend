/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.util

import com.google.common.collect.ImmutableSet
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.ComponentReferenceSubstitutionMapping
import de.dlr.premise.system.SystemFactory
import de.dlr.premise.util.PremiseHelper
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil

import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.*

class SubstitutionMappingHelper {	
	def static updateMap(ComponentReference componentReference, Iterable<AParameterDef> parametersToRemove, Iterable<AParameterDef> parametersToAdd) {
		parametersToRemove.forEach[componentReference.substitutionMap.removeKey(it)]
		parametersToAdd.forEach[parameterScoped | 
			createSubstitution(componentReference, parameterScoped)
		]
	}
	
	def static ComponentReferenceSubstitutionMapping createSubstitution(ComponentReference componentReference, AParameterDef parameterScoped) {
		val param = parameterScoped.wrappedElement
		
		val copy = EcoreUtil.copy(param)
		copy.id = PremiseHelper.createId()
		
		componentReference.putInSubsitution(parameterScoped, copy)
	}
	
	def static containsKey(EList<ComponentReferenceSubstitutionMapping> mappings, AParameterDef key) {
		mappings.exists[mappingListFilterPredicate(key)]
	}
	
	private def static removeKey(EList<ComponentReferenceSubstitutionMapping> mappings, AParameterDef key) {
		val toRemove = mappings.filter[mappingListFilterPredicate(key)].filter[!pinned]
		mappings.removeAll(toRemove)
	}
	
	def static get(EList<ComponentReferenceSubstitutionMapping> mappings, AParameterDef key) {
		mappings.findFirst[mappingListFilterPredicate(key)].substitution
	}
	
	def static putInSubsitution(ComponentReference compRef, AParameterDef originalScoped, EObject value) {
		if (compRef != originalScoped.scope.head) {
			throw new IllegalArgumentException
		}
		
		val mapping = SystemFactory.eINSTANCE.createComponentReferenceSubstitutionMapping
		mapping.original = originalScoped.wrappedElement
		mapping.relativeScope.addAll(originalScoped.scope.tail)
		mapping.substitution = value
		
		compRef.substitutionMap.add(mapping)
		
		return mapping
	}
	
	def private static mappingListFilterPredicate(ComponentReferenceSubstitutionMapping mapping, AParameterDef key) {
		key != null && mapping.fullScope == key.scope && mapping.original == key.wrappedElement
	}
	
	def private static getFullScope(ComponentReferenceSubstitutionMapping mapping) {
		ImmutableSet.builder.add(mapping.eContainer as ComponentReference).addAll(mapping.relativeScope).build
	}
}
