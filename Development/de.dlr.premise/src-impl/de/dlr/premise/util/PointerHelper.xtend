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

import de.dlr.premise.component.ComponentReferencePointer
import de.dlr.premise.component.ISatisfying
import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.DirectPointer
import de.dlr.premise.graph.GraphFactory
import de.dlr.premise.graph.INode
import de.dlr.premise.util.scope.ScopedEObjectEListInvocationHandler
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.emf.ecore.InternalEObject

import static extension de.dlr.premise.util.PremiseHelper.eAllContentsIncludingRoot
import de.dlr.premise.component.ISatisfieable

class PointerHelper {	
	static def <T> delegateGetToPointer(EObject eObj, EStructuralFeature feature) {
		(eObj.eGet(feature) as APointer<T>)?.target
	}
	
	static def <T> delegateSetToPointer(EObject eObj, EStructuralFeature feature, T newValue) {
		val pointer = createPointer(newValue)
		eObj.eSet(feature, pointer)
	}
	
	private def static <T> DirectPointer<T> createPointer(T newValue) {
		switch(newValue) {
			case null: null
			default: GraphFactory.eINSTANCE.<T>createDirectPointer => [target = newValue]
		}
	}	
	
	static def <T> delegateGetToPointerCollection(InternalEObject eObj, EStructuralFeature feature) {
		return ScopedEObjectEListInvocationHandler.create(eObj.eGet(feature) as EList<APointer<T>>, [target], [createPointer])
	}
	
	static def <T extends INode> T getTarget(ComponentReferencePointer<T> pointer) {
		if (!pointer.valid) {
			return null
		}
		
		val candidates = pointer.componentReference.activeImplementation
			.eAllContentsIncludingRoot
			.filter(ISatisfying)
			.filter[ISatisfying<INode, ISatisfieable> it | satisfiedSatisfieables.contains(pointer.definition)]
			.toList
			
		if (candidates.size != 1) {
			return null
		}
		
		return candidates.head as T
	}

}