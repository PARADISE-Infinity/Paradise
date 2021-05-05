/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.aspect.graphml.transform.p2g

import com.yworks.yed.YedFactory
import com.yworks.yed.YedPackage
import com.yworks.ygraphml.AutoSizePolicy
import com.yworks.ygraphml.GroupNode
import com.yworks.ygraphml.LineType
import com.yworks.ygraphml.NodeLabelPosition
import com.yworks.ygraphml.ProxyShapeNode
import com.yworks.ygraphml.YGraphMLFactory
import com.yworks.ygraphml.YGraphMLPackage
import de.dlr.aspect.graphml.transform.g2p.GraphMLToPremiseTransform
import de.dlr.premise.element.AElement
import de.dlr.premise.element.Relation
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.functions.UseCase
import java.math.BigInteger
import java.util.List
import java.util.Map
import org.graphdrawing.graphml.Graph
import org.graphdrawing.graphml.GraphMLFactory
import org.graphdrawing.graphml.Graphml
import org.graphdrawing.graphml.Key
import org.graphdrawing.graphml.KeyFor
import org.graphdrawing.graphml.KeyType
import org.graphdrawing.graphml.Node
import org.graphdrawing.graphml.YfilesFoldertype

import static extension de.dlr.aspect.graphml.util.GraphMLHelper.*
import static extension de.dlr.aspect.graphml.util.PremiseModelHelper.*
import static extension de.dlr.aspect.graphml.util.YGraphMLHelper.*
import static extension org.eclipse.emf.ecore.util.FeatureMapUtil.*

/**
 * Transforms a PREMISE model to a GraphML model. 
 * 
 * The GraphML generated is in a dialect that uses features specifically for yWork's yEd Graph Editor.
 * 
 * This transform is meant to be symmetrical to the {@link GraphMLToPremiseTransform}. This means, all features of the 
 * PREMISE that the this transform will bring over to GraphML must also be reverse transformed.
 */
class PremiseToGraphMLTransform {
	public enum Containment {
		GROUPS, EDGES
	}
	
	// create references to static field, just to simplify access
	val gf = GraphMLFactory.eINSTANCE
	val yf = YGraphMLFactory.eINSTANCE
	val yedf = YedFactory.eINSTANCE

	val Map<String, List<? extends AElement>> premiseRoots
	
	val Containment containment;

	Graphml graphmlRoot
	Graph graphmlGraph
	
	Key nodegraphicsKey
	Key edgegraphicsKey
	Key resourcesKey
	
	Key premiseTypeNodeKey
	Key premiseTypeEdgeKey
	Key premiseIdNodeKey
	Key premiseContainerNodeKey
	Key premiseContainerEdgeKey
	
		
	new(Map<String, List<? extends AElement>> roots) {
		this(roots, Containment.EDGES)
	}
	
	new(Map<String, List<? extends AElement>> roots, Containment containment) {
		this.containment = containment
		this.premiseRoots = roots
	}
	
	def getRoot() {
		graphmlRoot
	}

	def run() {
		graphmlRoot = gf.createGraphml
		graphmlGraph = gf.createGraph
		graphmlRoot.graph.add(graphmlGraph)
		
		createKeys()
		
		graphmlRoot.data.add(createResourcesData)

		// Transform AElement containment tree
		premiseRoots.forEach[rootIdentification, roots | roots.forEach[transformSubtree(rootIdentification)]]
		
		// Transform remaining children of the AElements
		premiseRoots.values.flatten.forEach[transformAElementContents]
	}
	
	private def void transformSubtree(AElement elem, String rootIdentification) {
		val node = elem.transformAElement
		
		// store a root identification. this allows us to rebuild the containment hierarchy, even for elements that don't occur in graphml (like ARepository)
		node.setPremiseData(premiseContainerNodeKey, rootIdentification);
		
		elem.contained.forEach[transformSubtree(rootIdentification)]
	}
		
	private def create gf.createNode transformAElement(AElement elem) {	
		id = elem.id
		
		// write name in standard location for graphml
		desc = elem.name
		
		switch (containment) {
			case GROUPS: {
				if (elem.contained.isEmpty) {
					createLeafYNode(it, elem)
				} else {
					createGroupYNode(it, elem)
				}
			}
			case EDGES: createLeafYNode(it, elem)
		}

		
		// store id
		setPremiseData(premiseIdNodeKey, elem.id)
		
		// store type specific data
		switch (elem) {
			SystemComponent: {
				data.findByKey(nodegraphicsKey).findYNodes.forEach[
					// id of SystemComponent icon
					nodeLabel.firstVisible.iconData = "1"
				]
				// save type
				setPremiseDataWithHiddenLabel(premiseTypeNodeKey, nodegraphicsKey, "SystemComponent")	
			}
			UseCase: {
				data.findByKey(nodegraphicsKey).findYNodes.forEach[
					// id of UseCase icon
					nodeLabel.firstVisible.iconData = "2"
					
					// hack: chose a slightly different color for UseCase so that they will appear as different elements 
					// in yEd's "currentElements"
					fill.color = "#fffffe"
				]
				// save type
				setPremiseDataWithHiddenLabel(premiseTypeNodeKey, nodegraphicsKey, "UseCase")
			}
		}
		
		switch (containment) {
			case GROUPS: createGroupContainment(it, elem)
			case EDGES: createEdgeContainment(it, elem)
	
		}
	}
	
	private def wasTransformed(AElement elem) {
		_createCache_transformAElement.containsKey(newArrayList(elem))
	}
	
	private def createGroupYNode(Node it, AElement elem) {
		yfilesFoldertype = YfilesFoldertype.GROUP
		
		data.createByKey(nodegraphicsKey).createType(YGraphMLPackage.Literals.PROXY_SHAPE_NODE) as ProxyShapeNode => [
			realizers = yf.createRealizers => [
				active = BigInteger.ZERO
				
				val openNode = yf.createGroupNode.init as GroupNode => [
					geometry => [
						width =  200
						height = 200
					]
					
					nodeLabel.firstVisible => [						
						// write name into ygraphml node for yEd
						text = elem.name
						
						modelPosition = NodeLabelPosition.TR
						autoSizePolicy = AutoSizePolicy.CONTENT
					]
					
					state = yf.createState => [
						closed = false
					]
				]
				
				val closedNode = yf.createGroupNode.init as GroupNode => [
					geometry => [
						width =  140
						height = 45
					]
					
					// write name into ygraphml node for yEd
					nodeLabel.firstVisible.text = elem.name
					
					state = yf.createState => [
						closed = true
					]
				]
				
				any.addOne(openNode)
				any.addOne(closedNode)
			]
		]
	}
	
	private def createLeafYNode(Node it, AElement elem) {
		data.createByKey(nodegraphicsKey).createYGenericNode => [
			geometry => [
				width =  140
				height = 45
			]
			// write name into ygraphml node for yEd
			nodeLabel.firstVisible.text = elem.name
		]
	}
	

	private def createEdgeContainment(Node it, AElement elem) {
		// create containment edge for no-root-containers
		val container = elem.eContainer
		if (container instanceof AElement) {
			val edge = createContainmentEdge
			edge.source = container.transformAElement
			edge.target = it
			graphmlGraph.edge.add(edge)
		}
		// add the newly created node to model
		graphmlGraph.node.add(it)
	}
	
	private def createGroupContainment(Node it, AElement elem) {
		// add the newly created node to model, depending on its container ...
		val container = elem.eContainer
		switch (container) {
			// ... if the container has a node, add as subgraph ...
			AElement: {
				val containerNode = container.transformAElement
				if (containerNode.graph == null) {
					containerNode.graph = gf.createGraph
				}
				containerNode.graph.node.add(it)	
			} 
			// ... else add in the root
			default: graphmlGraph.node.add(it)
		}
	}
	
	
	private def void transformAElementContents(AElement elem) {
		var relationEdges = elem.relations
			.map[relation | 
				val sourceElement = if (relation.source != null) relation.sourceAElement else elem
				val targetElement = relation.targetAElement
				
				if (!sourceElement.wasTransformed || !targetElement.wasTransformed) {
					return null
				} 
				
				createRelationEdge => [
					source = sourceElement.transformAElement
					target = targetElement.transformAElement
					data.findOrCreateByKey(edgegraphicsKey).findOrCreateYEdge => [
						edgeLabel.firstVisible.text = relation.name
					]
					setPremiseData(premiseContainerEdgeKey, (relation.eContainer as AElement).id)
				]
			]
			.filter[it != null]
		
		graphmlGraph.edge.addAll(relationEdges)
		
		elem.contained.forEach[transformAElementContents]
	}
	
	private def createRelationEdge() {
		gf.createEdge => [
			data.findOrCreateByKey(edgegraphicsKey).createYPolyLineEdge => [
				lineStyle.type = LineType.DASHED
			]
			setPremiseDataWithHiddenLabel(premiseTypeEdgeKey, edgegraphicsKey, "Relation");
		]
	}
	
	private def createContainmentEdge() {
		gf.createEdge => [
			data.findOrCreateByKey(edgegraphicsKey).createYPolyLineEdge
			setPremiseDataWithHiddenLabel(premiseTypeEdgeKey, edgegraphicsKey, "containment");
		]
	}
	
	private def getSourceAElement(Relation rel) {
		if (rel.source instanceof AElement) rel.source as AElement else null
	}
	
	private def getTargetAElement(Relation rel) {
		if (rel.target instanceof AElement) rel.target as AElement else null
	}
	
	private def createKeys() {
		nodegraphicsKey = gf.createKey => [
			id = "d0"
			^for = KeyFor.NODE
			yfilesType = "nodegraphics"
			graphmlRoot.key.add(it)
		]
		
		edgegraphicsKey = gf.createKey => [
			id = "d1"
			^for = KeyFor.EDGE
			yfilesType = "edgegraphics"
			graphmlRoot.key.add(it)
		]
		
		resourcesKey = gf.createKey => [
			id = "d2"
			^for = KeyFor.GRAPHML
			yfilesType = "resources"
			graphmlRoot.key.add(it)
		]
		
		premiseTypeNodeKey = gf.createKey => [
			id = "d3"
			^for = KeyFor.NODE
			attrName = "premiseType"
			attrType = KeyType.STRING
			graphmlRoot.key.add(it)
		]
		
		premiseTypeEdgeKey = gf.createKey => [
			id = "d4"
			^for = KeyFor.EDGE
			attrName = "premiseType"
			attrType = KeyType.STRING
			graphmlRoot.key.add(it)
		]
		
		premiseIdNodeKey = gf.createKey => [
			id = "d5"
			^for = KeyFor.NODE
			attrName = "premiseId"
			attrType = KeyType.STRING
			graphmlRoot.key.add(it)
		]
		
		premiseContainerNodeKey = gf.createKey => [
			id = "d6"
			^for = KeyFor.NODE
			attrName = "premiseContainer"
			attrType = KeyType.STRING
			graphmlRoot.key.add(it)
		]
		
		premiseContainerEdgeKey = gf.createKey => [
			id = "d7"
			^for = KeyFor.EDGE
			attrName = "premiseContainer"
			attrType = KeyType.STRING
			graphmlRoot.key.add(it)
		]
	}
	
	def createResourcesData() {
		gf.createData => [
			key = resourcesKey
			
			any.add(YGraphMLPackage.Literals.DOCUMENT_ROOT__RESOURCES, yf.createResourceBlock => [
				resource.add(yf.createResource => [
					id = "1"
					any.add(YedPackage.Literals.DOCUMENT_ROOT__SCALED_ICON, yedf.createScaledIcon => [
						XScale = 1
						YScale = 1
						any.add(YedPackage.Literals.DOCUMENT_ROOT__IMAGE_ICON, yedf.createImageIcon => [
							image = "3"
						]);
					])
				])
		
				resource.add(yf.createResource => [
					id = "2"
					any.add(YedPackage.Literals.DOCUMENT_ROOT__SCALED_ICON, yedf.createScaledIcon => [
						XScale = 1
						YScale = 1
						any.add(YedPackage.Literals.DOCUMENT_ROOT__IMAGE_ICON, yedf.createImageIcon => [
							image = "4"
						]);
					])
				])
				
				resource.add(yf.createResource => [
					id = "3"
					type = "java.awt.image.BufferedImage"
					any.addText('''
						iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAB8klEQVR42pWSz08TURDH+x9sMUKM
						QcQjJrgNr1tUuBjabNd1Q1ti1WgoiCwapMYC5UdM9ICJHoBAC0gRxAiLBeWHEoMED4SEYKKScIEb
						F/w7vu486JImbaQv+WRm3s77vpnZZ7OdYK10aph/wEDWlutabpXx96UH+y+u4aDHDYpzEvgakbET
						vYKdjqvcUpxzFcn7DHvdlSD73+S1VzqMepbGZ70cv546TYEya4/yMgpM1THsdlXgT6sLvyMSP7je
						ImH5oQubYafZxmVst5WD8jIKvA8xnrjQyCyWmiR80isxFHByyKe8jAITtQw/miUk60SLxSYX4n6n
						dWC2S8fP5OixgNEsgyD/7V2Gb49cMO5dxHjNBcSvn0HCV4S+G1luNMIaNsJmiY0SRu8wvKstw2RN
						MQbddgxUCZxhpQADmiOtgq2ZowqGbzN8MQf0MXSJlzoXKsWYLGDMm4dxNZ8zoZ5GQiviIsSItxAj
						wSPByccan/CM+WtitxjigRIYih1x9Tw6PA48qWKIukXElHOYVvIw5bVzhoLicUsJXQZBfn+QYdFX
						gKjHkdYziSyogkXspph5Jq/9DKv+U+bt6QkUf68WLHp9WQSem9NeDwiYVs8iFjl8bWQppv0UPVoW
						gTftOp4pImf1w+Gkyab2UiTaGvi3f6x7SEVhLZX0AAAAAElFTkSuQmCC
					''')
				])
				
				resource.add(yf.createResource => [
					id = "4"
					type = "java.awt.image.BufferedImage"
					any.addText('''
						iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAACgklEQVR42nWSa0+aQRCF/ZH9ZtI0
						Jo0fTI2JIbFgqMYrWFC59rVyE8pFrVpqqTG1VIWImiKIWLQiUgTLRRQRVBROdzeNlbx2k80mu2fO
						zDyzDQ0P1ojFCfuCByanG2PTi9A4XKB3j+2Gx9bMko/3cH1TQaFYQjJzingqi0jsGO71IGSm2Xqt
						VDeNBY+fXebOLnBWKOGiWMZl+RqlqxvcVG5RrVbZe+X2DqH9OGjMvUE/Z4fbF2Si43SeZDxDKpNH
						Jn+B0/Mi8oVLdlItZ/2M27sqbJ/c/wx61Gb4AhEUSUb3mh/GaRdkOgf6tRbY5r8iGInhd+4ctVoN
						alL+duQQNObeQDysR2gvhgW3F11yDhKNERqDDcpxM6RaE5QGB775AiTzHXoVE6D6OgYvpRwSJ1kM
						cyYi0MG/G0M4msRu9BgrG9tQ6O3olKhQKpfRPWrgT0HQp2TATFPzEA0qsLS2hZ39X4R6CuGDBAKk
						hW6ZljESDXF8g/aeUUba+z0M+ZgZ3a/VULydgNE+B4PDCY1pEiq9FXSsbV1yvkFHv4qRDf1MYHUz
						hDHLe4j6ZBC8GoRYqsSQdoIw2EIml0eLSMI3EA6oGOEfh0nW+87fsr3+XSxvhOAPHyKdK2AvGkez
						oKfeoKlNDPvcIpv1ymYEvuABgXjEqqEQ945OEE2kUSxdYd0fAtXXGTxtEUI0oISe/H2N2UlGNgv5
						+AykbyYxoLahV2mFxuJCPJmFa8kDqr8PbmwWQPdultcTbYd+2fJ1hW3Hx2WckN85+WERNKZOLCFz
						d33xYJVACoT3EY0nGSxaMp0MNaP90xY5o4MP8MmzVvxvNz5vR9MLIVo7etHZN8LuHsb+AWxAXxj6
						XT3yAAAAAElFTkSuQmCC
					''')
				])
			]);
		]
	}
}
