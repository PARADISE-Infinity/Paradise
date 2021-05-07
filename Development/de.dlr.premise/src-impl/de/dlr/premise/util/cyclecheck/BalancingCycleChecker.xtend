/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.util.cyclecheck

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import de.dlr.premise.system.ABalancing
import de.dlr.premise.util.cyclecheck.tarjan.AdjacencyList
import de.dlr.premise.util.cyclecheck.tarjan.Node
import de.dlr.premise.util.cyclecheck.tarjan.Tarjan
import de.dlr.premise.util.scope.ScopedEObjectFactory
import java.util.List
import org.eclipse.emf.common.notify.Notifier
import org.eclipse.emf.ecore.EObject

class BalancingCycleChecker implements IBalancingCycleChecker {
	
	val Notifier root
	val ABalancing<?> startBal
	
	var int nodeId
	
	var AdjacencyList adjacencyList
	var BiMap<EObject, Node> nodeMap
	
	var List<? extends List<Node>> stronglyConnectedComponents = null
	
	new(Notifier root, ABalancing<?> startBal) {
		this.root = root
		this.startBal = startBal
	}
	
	override hasCycle() {		
		for (stronglyConnectedComponent : getStronglyConncectedComponents()) {
			if (stronglyConnectedComponent.size > 1) {
				return true
			}
		}
		
		return false
	}
	
	def public getCycles() {		
		getStronglyConncectedComponents()
			.filter[size > 1]
			.map[scc | scc.map[findEObject]]
			.toList
	}
	
	def private getStronglyConncectedComponents() {
		if (stronglyConnectedComponents == null) {
			performTarjan()
		}
		
		stronglyConnectedComponents
	}
	
	def private performTarjan() {
		if (adjacencyList == null) {
			createAdjacencyList(root)
		}
		val algo = new Tarjan()
		stronglyConnectedComponents = algo.tarjan(startBal.findOrCreateNode, adjacencyList)
	}
	
	def private createAdjacencyList(Notifier notifier) {
		nodeId = 0
		nodeMap = HashBiMap.create
		adjacencyList = new AdjacencyList
		
		val allBals = ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(notifier, ABalancing) as Iterable<?> as Iterable<ABalancing<?>>
		
		for (bal : allBals) {
			for (source : bal.referencedSources) {
				adjacencyList.addEdge(source.findOrCreateNode, bal.findOrCreateNode)
			}
			for (target : bal.referencedTargets) {
				adjacencyList.addEdge(bal.findOrCreateNode, target.findOrCreateNode)
			}
		}
	}
	
	def private findOrCreateNode(EObject modelObject) {
		if (!nodeMap.containsKey(modelObject)) {
			nodeMap.put(modelObject, createNode())
		}
		nodeMap.get(modelObject)
	}
	
	def private findEObject(Node node) {
		nodeMap.inverse.get(node)
	}
	
	def private createNode() {
		new Node(nodeId++)
	}
}