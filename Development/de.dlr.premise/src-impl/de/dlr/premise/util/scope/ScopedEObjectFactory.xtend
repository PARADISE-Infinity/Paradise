/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.util.scope

import com.google.common.collect.ImmutableSet
import de.dlr.premise.component.ComponentReferencePointer
import de.dlr.premise.system.ComponentReference
import org.eclipse.emf.common.notify.Notifier
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet

import static extension de.dlr.premise.util.PremiseHelper.eAllContentsIncludingRoot
import static extension de.dlr.premise.util.PremiseHelper.flatMap
import static extension de.dlr.premise.util.PremiseHelper.getRootNotifier
import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.*

/**
 * This factory creates proxies around model element that resolves instances of {@link ComponentReference} and makes the referenced implementations unique.
 * 
 * A {@link ComponentReference} allows to reference a model subtree that is physically stored in any location in the model (in the same file 
 * or even in another file). By this reference the referenced subtree becomes logically located at the place of the ComponentReference. The 
 * same subtree can be referenced multiple times to allow reuse of model parts. With a {@link ComponentReferencePointer}, the referenced 
 * subtree can be connected with the model at large at the site of reference.
 * 
 * The proxies created by this class provides a view of the model that behaves as if the logically referenced subtrees were physically located a the models location.
 * 
 * The factory methods (except {@link #createScopedEObjectUnchecked}) guarantee that all returned instances are valid a the time of instantiation an will throw an 
 * IllegalArgumentException otherwise. Note that instances might become invalid at any time afterwards as the model is modified. Clients are advised not to keep 
 * instances around during long running operations or to lock the model for exclusive use and not to modify it to guarantee their instances stay valid.
 */
class ScopedEObjectFactory {
	val public static NULLSCOPE = ImmutableSet.<ComponentReference>of()
	
	val public static INSTANCE = new ScopedEObjectFactory
	
	// CREATION METHODS
	
	def <T extends EObject> T createScopedEObjectForScope(T toWrap, Iterable<ComponentReference> inputScope) {		
		val scope = ImmutableSet.copyOf(inputScope)
		if (toWrap.isScopedEObject) {
			if (toWrap.scope != inputScope) {
				throw new IllegalArgumentException('''Trying to create already scoped EObject «toWrap» in differing scope «inputScope»''')
			}
			return toWrap
		}

		if (!scope.valid) {
			throw new IllegalArgumentException('''Scope not valid''')
		}
		if (!scope.isEmpty && !scope.last.containsInImplementation(toWrap) && !toWrap.isExistingSubstitution(scope)) {
			throw new IllegalArgumentException('''Object to wrap is not in contents of activeImplementation''')
		}

		createScopedEObjectUnchecked(toWrap, scope)
	}

	def <T extends EObject> T createScopedEObjectForNullscope(T toWrap) {
		// TODO rename to createScopedEObjectForNaturalScope
		if (toWrap.isScopedEObject) {
			return toWrap
		}
		// TODO substitution
		createScopedEObjectUnchecked(toWrap, NULLSCOPE)
	}

	def <T extends EObject> Iterable<T> createAllScopedEObjects(T toWrap) {
		if (toWrap.isScopedEObject) {
			return #[toWrap]
		}
		
		toWrap.rootNotifier.allContentsScoped.filter[wrappedElement == toWrap].map[it as T]
		
//		val scopedWrappers = toWrap.activeImplementationUsingComponentReferences
//			.map[ImmutableSet.of(it)]
//			.closure[extendScope]
//			.map[createScopedEObjectUnchecked(toWrap, it)]
//
//		#[toWrap.createScopedEObjectForNullscope] + scopedWrappers
	}

	def Iterable<EObject> createAllScopedEObjectsByType(Notifier root, EClass filterEClass) {
		root.allContentsScoped.filter[filterEClass.isSuperTypeOf(eClass)]
	}

	def <T extends EObject> Iterable<T> createAllScopedEObjectsByType(Notifier root, Class<T> filterClass) {
		root.allContentsScoped.filter[filterClass.isInstance(wrappedElement)] as Iterable<T>
	}

	def <T extends EObject> T createScopedEObjectUnchecked(T toWrap, ImmutableSet<ComponentReference> scope) {
		ScopedEObjectInvocationHandler.createScopedEObject(toWrap, scope)
	}
	
	// HELPERS
		
	def private dispatch Iterable<EObject> getAllContentsScoped(ResourceSet root) {
		root.resources.flatMap[allContentsScoped]
	}
	
	def private dispatch Iterable<EObject> getAllContentsScoped(Resource root) {
		root.contents.flatMap[allContentsScoped]
	}
	
	def private dispatch Iterable<EObject> getAllContentsScoped(EObject root) {
		// root.createAllScopedEObjects.flatMap[eAllContentsIncludingRoot.toIterable]
		createScopedEObjectForNullscope(root).eAllContentsIncludingRoot.toIterable
	}
	

	def private static containsInImplementation(ComponentReference compRef, EObject toWrap) {
		compRef.activeImplementation != null && compRef.activeImplementation.eAllContentsIncludingRoot.exists [
			it == toWrap
		]
	}
	
    def private static isExistingSubstitution(EObject substitution, ImmutableSet<ComponentReference> scope) {
        val mapping = scope.head.substitutionMap.findFirst[mapping | mapping.substitution == substitution]
        if (mapping == null) {
            return false
        }
        val mappingScope = ImmutableSet.copyOf(#[scope.head] + mapping.relativeScope)
        if (mappingScope != scope) {
            return false
        }
        return scope.last.containsInImplementation(mapping.original)
    }
	

	def private static isValid(ImmutableSet<ComponentReference> scope) {
		for (i : 0 ..< (scope.length - 1)) {
			val parent = scope.get(i)
			val child = scope.get(i + 1)

			if (!parent.containsInImplementation(child)) {
				return false
			}
		}

		return true
	}

//	def private static extendScope(ImmutableSet<ComponentReference> scope) {
//		scope.head.activeImplementationUsingComponentReferences// prevent cycles in scope (valid scope can't contain component reference multiple times)
//		.filter[!scope.contains(it)].map [
//			ImmutableSet.builder.add(it).addAll(scope).build
//		]
//	}
//
//	def private static getActiveImplementationUsingComponentReferences(EObject toWrap) {
//		val allParents = toWrap.closure[#[eContainer]]
//
//		findAll(allParents, toWrap.rootNotifier).values.flatten.map[getEObject].filter(ComponentReference)
//	}
//
//	def private static findAll(Collection<?> eObjectsOfInterest, Notifier notifier) {
//		ActiveImplementationCrossReferencer.findAll(eObjectsOfInterest, notifier)
//	}
//	
//	private static class ActiveImplementationCrossReferencer extends EcoreUtil.UsageCrossReferencer {	
//		protected new(Resource notifier) {
//			super(notifier)
//		}
//		
//		protected new(ResourceSet notifier) {
//			super(notifier)
//		}
//		
//		protected new(EObject notifier) {
//			super(notifier)
//		}
//	
//		override boolean crossReference(EObject eObject, EReference eReference, EObject crossReferencedEObject) {
//     		return eReference == SystemPackage.Literals.COMPONENT_REFERENCE__ACTIVE_IMPLEMENTATION
//      	}
//      	
//      	def static findAll(Collection<?> eObjectsOfInterest, Notifier notifier) {
//			switch (notifier) {
//				ResourceSet: new ActiveImplementationCrossReferencer(notifier).findAllUsage(eObjectsOfInterest)
//				Resource: new ActiveImplementationCrossReferencer(notifier).findAllUsage(eObjectsOfInterest)
//				EObject: new ActiveImplementationCrossReferencer(notifier).findAllUsage(eObjectsOfInterest)
//			}
//		}
//	}
}
