/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.element.impl.my.util

import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.IEdge
import de.dlr.premise.graph.INode
import de.dlr.premise.util.PremiseHelper
import java.util.Collection
import java.util.HashSet
import java.util.List
import java.util.Set
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject

import static extension de.dlr.premise.util.PremiseHelper.closure

class INodeHelper {
	/**
	 * Returns an {@link EList} with all direct contents, all outgoing and incoming edges and their targets / sources.
	 */
	def static EList<EObject> getConnectedElements(INode root, int depth) {
		val Set<EObject> elements = new HashSet
		val contents = (root as EObject).closure([eContents], depth)
		elements.addAll(contents)
		val inOrOutEdges = elements.inOrOutEdges
		elements.addAll(inOrOutEdges)
		elements.addAll(inOrOutEdges.targets)
		return PremiseHelper.toUnmodifieableEList(elements)
	}

	def static List<IEdge<INode, INode>> getInOrOutEdges(Collection<EObject> roots) {
		if (roots.nullOrEmpty) {
			return newArrayList()
		}
		val Iterable<IEdge<INode, INode>> allEdges = PremiseHelper.getAll(roots.get(0).eResource.resourceSet, IEdge).
			filter(IEdge) as Iterable<?> as Iterable<IEdge<INode, INode>>
		return allEdges.filter [
			val pointers = newArrayList()
			pointers.addAll(sourcePointers)
			pointers.addAll(targetPointers)
			return pointers.map[target].exists[roots.contains(it)]
		].toList
	}

	private static def List<INode> getTargets(Iterable<IEdge<INode, INode>> edges) {
		return edges.map [
			val List<APointer<INode>> targets = newArrayList()
			targets.addAll(sourcePointers)
			targets.addAll(targetPointers)
			return targets
		].flatten.map[target].toList
	}

}
