/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.aspect.graphml.transform.g2p

import com.yworks.ygraphml.LineType
import de.dlr.aspect.graphml.transform.p2g.PremiseToGraphMLTransform
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.functions.UseCase
import de.dlr.premise.functions.UseCaseFactory
import java.util.ArrayList
import java.util.Collection
import java.util.List
import java.util.Map
import java.util.Set
import org.eclipse.emf.ecore.EObject
import org.graphdrawing.graphml.Edge
import org.graphdrawing.graphml.Graph
import org.graphdrawing.graphml.Graphml
import org.graphdrawing.graphml.Key
import org.graphdrawing.graphml.KeyFor
import org.graphdrawing.graphml.KeyType
import org.graphdrawing.graphml.Node
import de.dlr.premise.system.SystemFactory
import de.dlr.premise.system.SystemComponent

import static extension com.google.common.collect.Lists.*
import static extension com.google.common.collect.Maps.*
import static extension de.dlr.aspect.graphml.util.GraphMLHelper.*
import static extension de.dlr.aspect.graphml.util.PremiseModelHelper.*
import static extension de.dlr.aspect.graphml.util.YGraphMLHelper.*

/**
 * Transforms a GraphML model to PREMISE.
 * 
 * This transform is meant to be symmetrical to the {@link PremiseToGraphMLTransform}. This means, all features of the 
 * GraphML that the this transform will bring over to PREMISE must also be reverse transformed.
 */
class GraphMLToPremiseTransform {
	val SystemFactory pf = SystemFactory::eINSTANCE
	val UseCaseFactory uf = UseCaseFactory::eINSTANCE
	val ElementFactory ef = ElementFactory::eINSTANCE
	
	val Graphml graphmlRoot
	val Graph graphmlGraph
	
	Map<String, List<AElement>> roots
	
	Key nodegraphicsKey
	Key edgegraphicsKey
	
	Key premiseTypeNodeKey
	Key premiseTypeEdgeKey
	Key premiseIdNodeKey
	Key premiseContainerNodeKey
	Key premiseContainerEdgeKey
	
	
	new(Graphml graphmlRoot) {
		this.graphmlRoot = graphmlRoot
		
		this.graphmlGraph = graphmlRoot.graph.get(0)
	}
	
	def getRoots() {
		roots
	}
	
	def run() {
		nodegraphicsKey = graphmlRoot.key.findFirst[^for == KeyFor.NODE && yfilesType == "nodegraphics"]
		edgegraphicsKey = graphmlRoot.key.findFirst[^for == KeyFor.EDGE && yfilesType == "edgegraphics"]
		premiseTypeNodeKey = graphmlRoot.key.findFirst[^for == KeyFor.NODE && attrName == "premiseType" && attrType == KeyType.STRING]
		premiseTypeEdgeKey = graphmlRoot.key.findFirst[^for == KeyFor.EDGE && attrName == "premiseType" && attrType == KeyType.STRING]
		premiseIdNodeKey = graphmlRoot.key.findFirst[^for == KeyFor.NODE && attrName == "premiseId" && attrType == KeyType.STRING]
		premiseContainerNodeKey = graphmlRoot.key.findFirst[^for == KeyFor.NODE && attrName == "premiseContainer" && attrType == KeyType.STRING]
		premiseContainerEdgeKey = graphmlRoot.key.findFirst[^for == KeyFor.EDGE && attrName == "premiseContainer" && attrType == KeyType.STRING]
		
		// First, we build up the containment trees for SystemComponent and UseCases. We start by transforming all 
		// the nodes, at first assuming they are all root nodes of the tree. Later we remove all that are not.
		// Note that child nodes defined by groups are already hooked up as children at this point.
		val rootNodes = new ArrayList(graphmlGraph.node)

		// rootElements is a view, so changes made to it are reflected in rootNodes
		val rootElements = rootNodes.map[transformNode]
		
		// Create a by id map of the root elements whose ids are unique, to reference them later on. We ignore duplicated 
		// ids at this stage, because we can't decide which of the elements is the "correct one"
		val byId = rootElements
			// also get children that are already contained due to groups in GraphML
			.closure[contained]
			.groupBy[id]
			.entrySet
			.filter[key != null && value.size == 1]
			.map[value.get(0)]
			.uniqueIndex[id]
		
		// We connect up the Elements by considering all containment edges.
		for (e : graphmlGraph.edge.filter[premiseType == "containment"]) {
			
			// Find source and target SystemComponents for the edge
			val source = e.source.transformNode
			val target = e.target.transformNode

			// Check if component has already been taken from root components. This implies a node being target of multiple edges.
			if (!rootElements.contains(target)) {
				throw new GraphMLToPremiseTransformException('''Node "«target.name»" has multiple parents, Input is not a tree''')
			}
			
			// A node can't reference itself in a tree.
			if (target == source) {
				throw new GraphMLToPremiseTransformException('''Node "«target.name»" references itself, Input is not a tree''')
			}

			createContainment(source, target)
			
			// If a node is target of a containment edge, it is no longer a root.
			// Also removes the corresponding node from rootNodes, because rootElements is a view
			rootElements.remove(target)
		}
		
		// TODO input cycles where no node is targeted by two edges is not handled!
	
		// create the root components
		roots = createRootRepositories(rootNodes)

		// Now that we have built a containment tree, we can hook up the other elements inside
				
		// Build the relations which are represented as edges in graphml
		for (e : graphmlGraph.edge.filter[premiseType == "Relation"]) {
			// Source and target are easy to get by referencing the transformed nodes ...
			val source = e.source.transformNode
			val target = e.target.transformNode
			
			// ... but we also need the container into which we will put the relation. PremiseToGraphMLTransform will 
			// save the container ID, so we use it if present.
			val containerId = e.getPremiseData(premiseContainerEdgeKey, edgegraphicsKey)
			var container = byId.get(containerId)
			
			// If we still have no container, we need to guess one (this will happen for new relation edges or if the 
			// former container was deleted in GraphML)
			if (container == null) {
				// At first, we try to find a common root to place the relation into ...
				val commonRoot = findCommonRoot(source, target)
				// ... if everything else fails, fall back to the source element
				container = if (commonRoot instanceof AElement) commonRoot else source
			}
			
			// Hook up the relation (dispatch method to create the correct type)
			createRelation(container, source, target) => [
				// type is first edge label
				name = e.data.findByKey(edgegraphicsKey)?.findYEdge?.edgeLabel.firstVisible?.text
			]
		}
	}
	
	private def createRootRepositories(List<Node> rootNodes) {
		rootNodes
			// group by identification of container (to handle multiple input containers)
			.groupBy[getPremiseData(premiseContainerNodeKey, nodegraphicsKey)]
			// convert the List<Node> to SystemComponent/UseCase
			.mapValues[map[transformNode]]
	}
	
	
	private def create n.createAElement transformNode(Node n) {
		id = n.getPremiseData(premiseIdNodeKey, nodegraphicsKey)
		
		// we prefer the yEd data and then fall back to standard graphml
		name = n.data.findByKey(nodegraphicsKey)?.findYNodes?.head?.nodeLabel?.firstVisible?.text ?: n.desc
		
		// group node handeling
		if (n.graph != null) {
			val containedElements = n.graph.node.map[transformNode]
			containedElements.forEach[el | createContainment(it, el)]
		}
	}
	
	private def AElement createAElement(Node n) {
		switch (n.getPremiseType) {
			case "UseCase": uf.createUseCase
			case "SystemComponent": pf.createSystemComponent
			default: throw new GraphMLToPremiseTransformException("Unknown node type, add premiseType data")
		}
	}
	
	private def getPremiseType(Node n) {
		// First, get the type that was saved by PremiseToGraphML ...
		var type = n.getPremiseData(premiseTypeNodeKey, nodegraphicsKey)
		
		// ... if there is none, we need to use heuristics and try and guess the type by node appearance.
		if (type == null) {
			val color = n.data.findByKey(nodegraphicsKey)?.findYNodes?.head?.fill?.color?.toLowerCase
			type = switch (color) {
				case "#fffffe": "UseCase"
				case "#ffffff": "SystemComponent"
			}
		}
		
		type
	}
	
	private def getPremiseType(Edge e) {
		var type = e.getPremiseData(premiseTypeEdgeKey, edgegraphicsKey)
		
		if (type == null) {
			val lineType = e.data.findByKey(edgegraphicsKey)?.findYEdge?.lineStyle?.type
			type = switch (lineType) {
				case LineType.LINE: "containment"
				case LineType.DASHED: "Relation"
				default: null
			}
		}
		
		type
	}
	
	private def dispatch createContainment(SystemComponent source, UseCase target) {
		throw new GraphMLToPremiseTransformException("SystemComponent can't contain UseCase")
	}
	
	private def dispatch createContainment(UseCase source, SystemComponent target) {
		throw new GraphMLToPremiseTransformException("UseCase can't contain SystemComponent")
	}
	
	private def dispatch createContainment(UseCase source, UseCase target) {
		source.children.add(target)
	}
	
	private def dispatch createContainment(SystemComponent source, SystemComponent target) {
		source.children.add(target)
	}
	
	private def dispatch createRelation(AElement container, UseCase source, SystemComponent target) {
		throw new GraphMLToPremiseTransformException("Can't create Relation from UseCase to SystemComponent")
	}
	
	private def dispatch createRelation(SystemComponent container, AElement source, AElement target) {
		ef.createRelation => [
			it.source = source 
			it.target = target
			container.relations.add(it)
		]
	}
	
	private def dispatch createRelation(UseCase container, UseCase source, UseCase target) {
		ef.createRelation => [
			it.source = source 
			it.target = target
			container.relations.add(it)
		]
	}
	
	private def findCommonRoot(EObject left, EObject right) {
		val Set<EObject> leftRoots = newHashSet
		
		var leftCurrent = left
		while(leftCurrent != null) {
			leftRoots.add(leftCurrent)
			leftCurrent = leftCurrent.eContainer
		}
		
		var rightCurrent = right
		while (rightCurrent != null) {
			if (leftRoots.contains(rightCurrent)) {
				return rightCurrent
			} 
			rightCurrent = rightCurrent.eContainer
		}
	}
	
	private def <T> Collection<T> closure(Collection<? extends T> base, (T) => Collection<? extends T> fn) {
		val workingSet = base.newArrayList
		
		for (var i = 0; i < workingSet.size; i++) {
			workingSet += fn.apply(workingSet.get(i)).filter[!workingSet.contains(it)]
		}
		
		workingSet
	}
}