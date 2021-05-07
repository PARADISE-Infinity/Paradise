/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.element.impl.my.util

import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.IEdge
import de.dlr.premise.graph.INode
import org.eclipse.emf.common.util.ECollections
import org.eclipse.emf.common.util.EList

class IEdgeHelper {
	def static <S extends INode> EList<APointer<S>> getSourcePointers(IEdge<S, ?> it) {
		val feature = eClass.EAllReferences.findFirst[name == "sourcePointer"]
		(eGet(feature) as APointer<S>).singletonOrEmptyList
	}

	def static <T extends INode> EList<APointer<T>> getTargetPointers(IEdge<?, T> it) {
		val feature = eClass.EAllReferences.findFirst[name == "targetPointer"]
		(eGet(feature) as APointer<T>).singletonOrEmptyList
	}

	def static EList<INode> getSources(IEdge<? extends INode, ?> it) {
		return ECollections.asEList(sourcePointers.map[target])
	}

	def static EList<INode> getTargets(IEdge<?, ? extends INode> it) {
		return ECollections.asEList(targetPointers.map[target])
	}

	def static <T> EList<T> singletonOrEmptyList(T content) {
		if (content != null) {
			ECollections.singletonEList(content)
		} else {
			ECollections.emptyEList()
		}
	}
}
