/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.block

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.base.xtend.RepresentationHelper
import de.dlr.exchange.graphml.block.appearance.EdgeAppearance
import de.dlr.exchange.graphml.block.appearance.NodeAppearance
import de.dlr.exchange.graphml.block.settings.GraphMLBlockSettings
import de.dlr.premise.component.ComponentPackage
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ARepository
import de.dlr.premise.element.ElementPackage
import de.dlr.premise.element.Mode
import de.dlr.premise.element.StateMachine
import de.dlr.premise.graph.IEdge
import de.dlr.premise.graph.INode
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.registry.Constant
import de.dlr.premise.registry.IMetaTypable
import de.dlr.premise.registry.IPremiseObject
import de.dlr.premise.representation.FontStyle
import de.dlr.premise.representation.StyleTypes
import de.dlr.premise.system.Parameter
import de.dlr.premise.system.SystemPackage
import de.dlr.premise.functions.ARange
import de.dlr.premise.functions.RequiredParameter
import de.dlr.premise.functions.UseCase
import de.dlr.premise.util.PremiseHelper
import java.util.List
import java.util.Map
import java.util.Set
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject

import static extension com.google.common.collect.Maps.*
import static extension de.dlr.exchange.graphml.block.PremiseExtensions.*
import static extension de.dlr.premise.util.PremiseHelper.*
import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.getId

class GraphMLBlockGenerator {
		
	private static val WIDTH = 110.0
	private static val HEIGHT_NODE = 50.0
	private static val HEIGHT_GRP_CLOSED = 70.0
	
	private static val NODE_ID_PREFIX = "nodeid_"
	private static val GRAPH_ID_PREFIX = "grapid_"	
	private static val EDGE_ID_PREFIX = "edgeid_"
	
	private static val NODE_META_DATA_ID_PREFIX = "nodemetaid_"
	private static val EDGE_META_DATA_ID_PREFIX = "edgemetaid_"
	
	private static val DEFAULT_AELEMENT_APPEARANCE = new NodeAppearance("#D4D4D4CC", 1, StyleTypes.LINE, 0, 0, WIDTH, HEIGHT_NODE, true, FontStyle.PLAIN, false)
	private static val DEFAULT_AELEMENT_GROUP_APPEARANCE = new NodeAppearance("#D4D4D4", 1, StyleTypes.LINE, 0, 0, WIDTH, HEIGHT_GRP_CLOSED, true, FontStyle.PLAIN, false)
	private static val DEFAULT_SHEET_APPEARANCE = new NodeAppearance("#D4D4D4", 1, StyleTypes.LINE, 0, 0, 26, 36, true, FontStyle.PLAIN, false)
	private static val DEFAULT_CONSTANT_APPEARANCE = new NodeAppearance("#545454", 1, StyleTypes.LINE, 0, 0, 26, 36, true, FontStyle.PLAIN, false)
	private static val DEFAULT_MODE_APPEARANCE = new NodeAppearance("#D4D4D4CC", 1, StyleTypes.LINE, 0, 0, 30, 30, true, FontStyle.PLAIN, false)
	private static val DEFAULT_STATE_MACHINE_APPEARANCE = new NodeAppearance("#D4D4D4", 1, StyleTypes.LINE, 0, 0, WIDTH, HEIGHT_GRP_CLOSED, true, FontStyle.PLAIN, false)
	
	private static val DEFAULT_EDGE_APPEARANCE = #{
		ElementPackage.Literals.CONNECTION -> new EdgeAppearance("#AD2C2C", 1, StyleTypes.LINE, false, true, FontStyle.PLAIN, false),
		ElementPackage.Literals.RELATION -> new EdgeAppearance("#009966", 1, StyleTypes.LINE, false, true, FontStyle.PLAIN, false),
		ComponentPackage.Literals.SATISFIES -> new EdgeAppearance("#0000FF", 1, StyleTypes.LINE, false, true, FontStyle.PLAIN, false),
		SystemPackage.Literals.BALANCING -> new EdgeAppearance("#8ABDFF", 1, StyleTypes.LINE, false, true, FontStyle.PLAIN, false),
		SystemPackage.Literals.TRANSITION_BALANCING -> new EdgeAppearance("#8ABDFF", 1, StyleTypes.LINE, false, true, FontStyle.PLAIN, false),
		ElementPackage.Literals.TRANSITION -> new EdgeAppearance("#777777", 1, StyleTypes.LINE, false, true, FontStyle.PLAIN, false)
	}
	private static val DEFAULT_CONTAINMENT_APPEARANCE = new EdgeAppearance("#000000", 1, StyleTypes.LINE, false, true, FontStyle.PLAIN, false)
	
	val extension GraphMLBlockSettings settings
	var extension RepresentationHelper repHelper
	val extension MetaDataHelper = new MetaDataHelper
	
	var Set<EObject> externalDependencies = newLinkedHashSet
	
	var Map<String, String> nodeMetaDataIds
	var Map<String, String> edgeMetaDataIds
	
	// one GraphML edge will be drawn for each entry in this map
	var Map<
		Pair<EObject, EObject>, // source -> target
		Map<EdgeAppearance, List<IEdge<?,?>>>  // appearance -> applicable edges
	> groupedEdges
	
	new (GraphMLBlockSettings settings) {
		this.settings = settings
		val rootRepo = PremiseHelper::getRoot(selectedRoots.get(0)) as ARepository
		repHelper = RepresentationHelper.getInstance(rootRepo.eResource.resourceSet)
	}
	
	def generate() {
		prepare()
		doGenerate()
	}
	
	def dispose() {
		repHelper.dispose()
	}
	
	def private prepare() {
		prepareMetaData()
		prepareEdges()
		prepareContainmentEdges()
	}
	
	def private prepareMetaData() {
		val nodeMetaDataNames = selectedNodes.filter(IPremiseObject).map[metaDataMap.keySet].flatten
		val edgeMetaDataNames =  selectedEdges.filter(IPremiseObject).map[metaDataMap.keySet].flatten
		
		nodeMetaDataIds = nodeMetaDataNames.toMap[NODE_META_DATA_ID_PREFIX + it]
		edgeMetaDataIds = edgeMetaDataNames.toMap[EDGE_META_DATA_ID_PREFIX + it]
	}
	
	def private prepareEdges() {
		val Multimap<Pair<EObject, EObject>, IEdge<?,?>> edges = LinkedHashMultimap.create
		
		for (edge : selectedEdges.sortBy[id]) {
			for (source : edge.referencedSources.map[preparedForDraw].filterNull) {
				for (target : edge.referencedTargets.map[preparedForDraw].filterNull) {
					edges.put(source -> target, edge)
				}
			}
		}
		
		groupedEdges = edges.asMap.transformEntries [ sourceTarget, scopedEdges |
			scopedEdges.groupBy[getEdgeAppearance(sourceTarget.key)]
		]
	}
			
	def private getPreparedForDraw(EObject obj) {
		val selectedAncestor = obj.ancestorInSelectedNodes
		if (selectedAncestor != null) {
			return selectedAncestor
		}
		
		if (!allNodes.contains(obj)) {
			var el = obj
			while (el != null && !isSelectedNodeType(el)) {
				el = el.eContainer
			}
			if (el != null) {
				externalDependencies.add(el)
				return el
			}
		}
		
		return null
	}
	
	def private isSelectedNodeType(EObject obj) {
		ElementPackage.Literals.AELEMENT.isSuperTypeOf(obj.eClass) || selectedAdditionalNodeTypes.exists[isSuperTypeOf(obj.eClass)]
	}
	
	def private getEdgeAppearance(IEdge<?,?> edge, EObject source) {
		EdgeAppearance.create(repHelper, edge.eClass.defaultAppearance, edge, source)
	}
	
	def private getDefaultAppearance(EClass edgeClass) {
		val appearance = DEFAULT_EDGE_APPEARANCE.entrySet.findFirst[key.isSuperTypeOf(edgeClass)]?.value
		
		if (appearance == null) {
			throw new IllegalArgumentException('''Unhandled argument «edgeClass»''')
		}
		
		return appearance
	}
	
	def private getNodeAppearance(INode node, NodeAppearance defaultAppearance) {
		NodeAppearance.create(repHelper, defaultAppearance, node)
	}
	
	def private prepareContainmentEdges() {
		if (showHierarchyAsGroups) {
			return;
		}
		
		val edges = newLinkedHashMap(groupedEdges)
		selectedNodes
			.filter[!(it instanceof StateMachine)]
			.sortBy[id]
			.forEach[parent |
				parent
					.eContentsReferenced
					.filter[selectedNodes.contains(it)]
					.forEach[child |
						edges.put(parent -> child, ImmutableMap.of(DEFAULT_CONTAINMENT_APPEARANCE, ImmutableList.of()))
					]
			]
			
		groupedEdges = edges
	}
	
	def private String doGenerate() '''
		<?xml version="1.0" encoding="«charset»" standalone="no"?>
			<graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:y="http://www.yworks.com/xml/graphml" xmlns:yed="http://www.yworks.com/xml/yed/3" xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd">
			<!--Created by yFiles for Java 2.8-->
			<key for="graphml" id="d0" yfiles.type="resources"/>
			<key for="port" id="d1" yfiles.type="portgraphics"/>
			<key for="port" id="d2" yfiles.type="portgeometry"/>
			<key for="port" id="d3" yfiles.type="portuserdata"/>
			<key for="node" id="d6" yfiles.type="nodegraphics"/>
			<key for="edge" id="d7" yfiles.type="edgegraphics"/>
			«FOR entry : nodeMetaDataIds.entrySet.sortBy[key]»
				<key attr.name="«GeneratorHelper::encodeXML(entry.key)»" attr.type="string" for="node" id="«entry.value»"/>
			«ENDFOR»
			«FOR entry : edgeMetaDataIds.entrySet.sortBy[key]»
				<key attr.name="«GeneratorHelper::encodeXML(entry.key)»" attr.type="string" for="edge" id="«entry.value»"/>
			«ENDFOR»
			<graph edgedefault="directed" id="G">
			«FOR root : selectedRoots.sortBy[id]»
				«createNode(root)»
			«ENDFOR»
			«FOR element : externalDependencies»
				«createNode(element, true) /* TODO FQN */»
			«ENDFOR»
			«createEdges()»
			</graph>
			<data key="d0">
			<y:Resources/>
			</data>
		</graphml>
	'''
	
	def private createNode(EObject root) {
		createNode(root, false)
	}	
	
	def private dispatch String createNode(EObject root, boolean external) {
		throw new IllegalArgumentException
	}
	
	def private dispatch String createNode(AElement root, boolean external) {
		if (root.selectedChildren.empty || !showHierarchyAsGroups) '''
			«createASpecificLineNode(root, external)»
			«FOR child: root.selectedChildren.sortBy[id]»
				«createNode(child)»
			«ENDFOR»
		''' else {
			createGroupNode(root)
		}
	}
	
	def private dispatch String createNode(AParameterDef root, boolean external) {
		createASpecificLineSheet(root, external)
	}	
	
	def private dispatch String createNode(RequiredParameter root, boolean external) {
		createASpecificLineSheet(root, external)
	}
	
	def private dispatch String createNode(StateMachine root, boolean external) {
		createStateMachine(root)
	}
	
	def private dispatch String createNode(Mode root, boolean external) {
		createMode(root)
	}
		
	def private String createASpecificLineNode(AElement root, boolean external) {
		val defaultAppearance = if (external) DEFAULT_AELEMENT_APPEARANCE.externalVariation else DEFAULT_AELEMENT_APPEARANCE
		val extension appearance = root.getNodeAppearance(defaultAppearance)
		
		'''
			<node id="«NODE_ID_PREFIX»«root.id»">
				«root.createMetaData»
				<data key="d6">
					<y:GenericNode configuration="com.yworks.bpmn.Activity.withShadow">
						<y:Geometry 
							height="«height»" 
							width="«width»" 
							x="«x»" 
							y="«y»"
						/>
						<y:Fill 
							color="#FFFFFFE6" 
							color2="«color»" 
							transparent="false"
						/>
						<y:BorderStyle color="#123EA2" type="«styleTypeString»" width="«lineWidth»"/>
						<y:NodeLabel 
							alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="«fontStyleString»" hasBackgroundColor="false" 
							hasLineColor="false" height="18.701171875" modelName="internal" modelPosition="c" textColor="#000000" visible="«labelVisible»" 
							width="105.40234375" x="-10.201171875" y="18.1494140625" underlinedText="«labelUnderlined»"
						>«GeneratorHelper::encodeXML(root.nodeLabel)»</y:NodeLabel>
						<y:StyleProperties>
							<y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="ACTIVITY_TYPE_STATELESS_TASK_PLAIN"/>
							<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.line.color" value="#000000"/>
							<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill" value="#ffffff"/>
							<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill2" value="#d4d4d4"/>
						</y:StyleProperties>
					</y:GenericNode>
				</data>
			</node>
		'''
	}
	
	def private String createGroupNode(AElement root) {
		val appearance = root.getNodeAppearance(DEFAULT_AELEMENT_GROUP_APPEARANCE)
		val closedAppearance = root.getNodeAppearance(DEFAULT_AELEMENT_GROUP_APPEARANCE.closedGroupVariation)
				
		return '''
			<node id="«NODE_ID_PREFIX»«root.id»" yfiles.foldertype="group">
				«root.createMetaData»
				<data key="d6">
					<y:ProxyAutoBoundsNode>
						<y:Realizers active="0">
							<y:GenericGroupNode configuration="com.yworks.bpmn.Activity.withShadow">
								<y:Geometry 
									height="«closedAppearance.height»" 
									width="«closedAppearance.width»" 
									x="«closedAppearance.x»" 
									y="«closedAppearance.y»"/>
								<y:Fill 
									color="#FFFFFFE6" 
									color2="«closedAppearance.color»" 
									transparent="false"
								/>
								<y:BorderStyle color="#123EA2" type="«closedAppearance.styleTypeString»" width="«closedAppearance.lineWidth»"/>
								<y:NodeLabel 
									alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="«closedAppearance.fontStyleString»" underlinedText="«closedAppearance.labelUnderlined»" modelName="internal" modelPosition="tl" visible="«closedAppearance.labelVisible»"
								>«GeneratorHelper::encodeXML(root.nodeLabel.replace("\\n", ""))»</y:NodeLabel>
								<y:Insets bottom="25" bottomF="25" left="15" leftF="15.0" right="15" rightF="15.0" top="15" topF="15.0"/>
								<y:StyleProperties>
									<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.line.color" value="#000000"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.TaskTypeEnum" name="com.yworks.bpmn.taskType" value="TASK_TYPE_ABSTRACT"/>
									<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill2" value="#d4d4d4"/>
									<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill" value="#ffffff"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="ACTIVITY_TYPE"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.ActivityTypeEnum" name="com.yworks.bpmn.activityType" value="ACTIVITY_TYPE_TASK"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.MarkerTypeEnum" name="com.yworks.bpmn.marker1" value="BPMN_MARKER_OPEN"/>
								</y:StyleProperties>
								<y:State autoResize="true" closed="false" closedHeight="«HEIGHT_GRP_CLOSED»" closedWidth="«WIDTH»"/>
							</y:GenericGroupNode>
							<y:GenericGroupNode configuration="com.yworks.bpmn.Activity.withShadow">
								<y:Geometry 
									height="«appearance.height»" 
									width="«appearance.width»" 
									x="«appearance.x»" 
									y="«appearance.y»"
								/>
								<y:Fill 
									color="#FFFFFFE6" 
									color2="«appearance.color»" 
									transparent="false"
								/>
								<y:BorderStyle color="#123EA2" type="«appearance.styleTypeString»" width="«appearance.lineWidth»"/>								
								<y:NodeLabel 
									alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="«appearance.fontStyleString»" hasBackgroundColor="false" 
									hasLineColor="false" modelName="internal" modelPosition="t" textColor="#000000" visible="«appearance.labelVisible»" x="-10.201171875" y="18.1494140625" underlinedText="«appearance.labelUnderlined»"
								>«GeneratorHelper::encodeXML(root.nodeLabel)»</y:NodeLabel>
								<y:StyleProperties>
									<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.line.color" value="#000000"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.TaskTypeEnum" name="com.yworks.bpmn.taskType" value="TASK_TYPE_ABSTRACT"/>
									<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill2" value="#d4d4d4"/>
									<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill" value="#ffffff"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="ACTIVITY_TYPE"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.ActivityTypeEnum" name="com.yworks.bpmn.activityType" value="ACTIVITY_TYPE_TASK"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.MarkerTypeEnum" name="com.yworks.bpmn.marker1" value="BPMN_MARKER_CLOSED"/>
								</y:StyleProperties>
								<y:State autoResize="true" closed="true" closedHeight="«HEIGHT_GRP_CLOSED»" closedWidth="«WIDTH»"/>
							</y:GenericGroupNode>
						</y:Realizers>
					</y:ProxyAutoBoundsNode>
				</data>
				<graph edgedefault="directed" id="«GRAPH_ID_PREFIX»«root.id»">
					«FOR child: root.selectedChildren.sortBy[id]»
						«createNode(child)»
					«ENDFOR»
				</graph>
			</node>
		'''
	}
	
	def private String createASpecificLineSheet(ANameItem par, boolean external) {
		var defaultAppearance = if (par instanceof Constant) DEFAULT_CONSTANT_APPEARANCE else DEFAULT_SHEET_APPEARANCE
		if (external) {
			defaultAppearance = defaultAppearance.externalVariation
		}
		val extension appearance = (par as INode).getNodeAppearance(defaultAppearance)
	
		return '''
			<node id="«NODE_ID_PREFIX»«par.id»">
				«IF par instanceof IMetaTypable»
					«par.createMetaData»
				«ENDIF»
				<data key="d6">
					<y:GenericNode configuration="com.yworks.bpmn.Artifact.withShadow">
					<y:Geometry height="«height»" width="«width»" x="«x»" y="«y»"/>
					<y:Fill color="#FFFFFF" color2="«color»" transparent="false"/>
					<y:BorderStyle color="#000000" type="«styleTypeString»" width="«lineWidth»"/>
					<y:NodeLabel 
						alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="«fontStyleString»" hasBackgroundColor="true" 
						hasLineColor="false" height="18.701171875" modelName="eight_pos" modelPosition="n" textColor="#000000" visible="«labelVisible»" width="154.732421875" x="-59.8662109375" y="-22.701171875" underlinedText="«labelUnderlined»"
					>«GeneratorHelper::encodeXML(par.nodeLabel)»</y:NodeLabel>
					<y:StyleProperties>
						<y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="ARTIFACT_TYPE_DATA_OBJECT"/>
						«IF par instanceof RequiredParameter»
							<y:Property class="com.yworks.yfiles.bpmn.view.DataObjectTypeEnum" name="com.yworks.bpmn.dataObjectType" value="DATA_OBJECT_TYPE_INPUT"/>
						«ENDIF»
						<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.line.color" value="#000000"/>
						<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill" value="#ffffff"/>
						<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill2" value="#d4d4d4"/>
					</y:StyleProperties>
					</y:GenericNode>
				</data>
			</node>
		'''
	}
	
	def private String createMode(Mode mode) {
		val extension appearance = (mode as INode).getNodeAppearance(DEFAULT_MODE_APPEARANCE)
		
		return '''
			<node id="«NODE_ID_PREFIX»«mode.id»">
				<data key="d6">
					«mode.createMetaData»
					<y:GenericNode configuration="com.yworks.bpmn.Event.withShadow">
						<y:Geometry height="«height»" width="«width»" x="«x»" y="«y»"/>
						<y:Fill color="#FFFFFFE6" color2="«color»" transparent="false"/>
						<y:BorderStyle color="#DCBA00" type="«styleTypeString»" width="«lineWidth»"/>
						<y:NodeLabel 
							alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="«fontStyleString»" hasBackgroundColor="false" 
							hasLineColor="false" height="0.0" modelName="eight_pos" modelPosition="s" textColor="#000000" visible="«labelVisible»" width="0.0" x="0.0" y="0.0" underlinedText="«labelUnderlined»"
						>«GeneratorHelper::encodeXML(mode.nodeLabel)»</y:NodeLabel>
						<y:StyleProperties>
							<y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="EVENT_TYPE_PLAIN"/>
							<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.line.color" value="#000000"/>
							<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill" value="#ffffff"/>
							<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill2" value="#d4d4d4"/>
							<y:Property class="com.yworks.yfiles.bpmn.view.EventCharEnum" name="com.yworks.bpmn.characteristic" value="«IF mode.isEntryMode»EVENT_CHARACTERISTIC_INTERMEDIATE_CATCHING«ELSE»EVENT_CHARACTERISTIC_START«ENDIF»"/>
						</y:StyleProperties>
					</y:GenericNode>
				</data>
			</node>
		'''	
	} 

	
	def private String createStateMachine(StateMachine root) {		
		val appearance = root.getNodeAppearance(DEFAULT_STATE_MACHINE_APPEARANCE)
		val closedAppearance = root.getNodeAppearance(DEFAULT_STATE_MACHINE_APPEARANCE.closedGroupVariation)
		
		return '''
			<node id="«NODE_ID_PREFIX»«root.id»" yfiles.foldertype="group">
				<data key="d6">
					<y:ProxyAutoBoundsNode>
						<y:Realizers active="0">
							<y:GenericGroupNode configuration="com.yworks.bpmn.Activity.withShadow">
								<y:Geometry 
									height="«closedAppearance.height»" 
									width="«closedAppearance.width»" 
									x="«closedAppearance.x»" 
									y="«closedAppearance.y»"/>
								<y:Fill 
									color="#FFFFFFE6" 
									color2="«closedAppearance.color»" 
									transparent="false"
								/>
								<y:BorderStyle color="#dcba00" type="«closedAppearance.styleTypeString»" width="«closedAppearance.lineWidth»"/>
								<y:NodeLabel 
									alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="«closedAppearance.fontStyleString»" underlinedText="«closedAppearance.labelUnderlined»" modelName="internal" modelPosition="tl" visible="«closedAppearance.labelVisible»"
								>«GeneratorHelper::encodeXML(root.nodeLabel.replace("\\n", ""))»</y:NodeLabel>
								<y:Insets bottom="25" bottomF="25" left="15" leftF="15.0" right="15" rightF="15.0" top="15" topF="15.0"/>
								<y:StyleProperties>
									<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.line.color" value="#000000"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.TaskTypeEnum" name="com.yworks.bpmn.taskType" value="TASK_TYPE_ABSTRACT"/>
									<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill2" value="#d4d4d4"/>
									<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill" value="#ffffff"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="ACTIVITY_TYPE"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.ActivityTypeEnum" name="com.yworks.bpmn.activityType" value="ACTIVITY_TYPE_TASK"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.MarkerTypeEnum" name="com.yworks.bpmn.marker1" value="BPMN_MARKER_OPEN"/>
								</y:StyleProperties>
								<y:State autoResize="true" closed="false" closedHeight="«HEIGHT_GRP_CLOSED»" closedWidth="«WIDTH»"/>
							</y:GenericGroupNode>
							<y:GenericGroupNode configuration="com.yworks.bpmn.Activity.withShadow">
								<y:Geometry 
									height="«appearance.height»" 
									width="«appearance.width»" 
									x="«appearance.x»" 
									y="«appearance.y»"/>
								<y:Fill 
									color="#FFFFFFE6" 
									color2="«appearance.color»" 
									transparent="false"
								/>
								<y:BorderStyle color="#dcba00" type="«appearance.styleTypeString»" width="«appearance.lineWidth»"/>								
								<y:NodeLabel 
									alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="«appearance.fontStyleString»" hasBackgroundColor="false" 
									hasLineColor="false" modelName="internal" modelPosition="t" textColor="#000000" visible="«appearance.labelVisible»" x="-10.201171875" y="18.1494140625" underlinedText="«appearance.labelUnderlined»"
								>«GeneratorHelper::encodeXML(root.nodeLabel)»</y:NodeLabel>
								<y:StyleProperties>
									<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.line.color" value="#000000"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.TaskTypeEnum" name="com.yworks.bpmn.taskType" value="TASK_TYPE_ABSTRACT"/>
									<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill2" value="#d4d4d4"/>
									<y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill" value="#ffffff"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="ACTIVITY_TYPE"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.ActivityTypeEnum" name="com.yworks.bpmn.activityType" value="ACTIVITY_TYPE_TASK"/>
									<y:Property class="com.yworks.yfiles.bpmn.view.MarkerTypeEnum" name="com.yworks.bpmn.marker1" value="BPMN_MARKER_CLOSED"/>
								</y:StyleProperties>
								<y:State autoResize="true" closed="true" closedHeight="«HEIGHT_GRP_CLOSED»" closedWidth="«WIDTH»"/>
							</y:GenericGroupNode>
						</y:Realizers>
					</y:ProxyAutoBoundsNode>
				</data>
				<graph edgedefault="directed" id="«GRAPH_ID_PREFIX»«root.id»">
					«FOR child: root.selectedChildren.sortBy[id]»
						«createNode(child)»
					«ENDFOR»
				</graph>
			</node>
		'''
	}
	
	private def dispatch getNodeLabel(ANameItem nameIt) {
		nameIt.name
	}
	
	private def dispatch String getNodeLabel(RequiredParameter reqParam) {
		val unitStr = if (reqParam.unit?.symbol != null) ''' «reqParam.unit.symbol»''' else ""
		val lines = newArrayList
		lines += reqParam.name
		if (reqParam.valueConstraint != null) {
			lines += (reqParam.valueConstraint as ARange).rangeString + unitStr
		}
		for (mv : reqParam.modeValueConstraints) {
			lines += (reqParam.valueConstraint as ARange).rangeString + unitStr + mv.modeCombinationString
		}
		return lines.join("\n")
	}

	private def dispatch String getNodeLabel(Parameter param) {
		val unitStr = if (param.unit?.symbol != null) ''' «param.unit.symbol»''' else ""
		val lines = newArrayList
		lines += param.name
		if (param.value != null) {
			lines += param.value.strValue + unitStr
		}
		for (mv : param.modeValues) {
			lines += param.value.strValue + unitStr + mv.modeCombinationString
		}
		return lines.join("\n")
	}
	
	private def dispatch getNodeLabel(UseCase uc) {
		uc.name + if (uc.condition != null) ''' : «PremiseHelper.getGuardConditionString(uc.condition)»''' else ""
	}
	
	private def getExternalVariation(NodeAppearance appearance) {
		return new NodeAppearance(appearance.color, appearance.lineWidth, StyleTypes.DOTTED, appearance.x, appearance.y,
			appearance.width, appearance.height, appearance.labelVisible, appearance.fontStyle,
			appearance.labelUnderlined)
	}

	private def getClosedGroupVariation(NodeAppearance appearance) {
		return new NodeAppearance(appearance.color, appearance.lineWidth, appearance.styleType, appearance.x,
			appearance.y, appearance.width, appearance.height, appearance.labelVisible, FontStyle.BOLD, true)
	}
	
	def private createEdges() {
		val ret = new StringBuilder()
		
		groupedEdges.forEach[pair, groupedEdges | 
			val source = pair.key
			val target = pair.value
			
			var i = 0
			for (entry : groupedEdges.entrySet) {
				val appearance = entry.key
				val edges = entry.value
				
				ret.append(createEdge(source, target, i, appearance, edges))
				
				i++
			}
		]
		
		return ret.toString()
	}
	
	def private createEdge(EObject source, EObject target, int index, EdgeAppearance appearance, List<IEdge<?,?>> edges) '''
		<edge id="«EDGE_ID_PREFIX»«source.id»_«target.id»_«index»" source="«NODE_ID_PREFIX»«source.id»" target="«NODE_ID_PREFIX»«target.id»">
			«IF edges.size == 1»
				«(edges.head as IPremiseObject).createMetaData»
			«ENDIF»
			<data key="d7">
				<y:PolyLineEdge>
					<y:Path sx="0.0" sy="0.0" tx="0.0" ty="0"/>
					<y:LineStyle 
						color="«appearance.color»" 
						type="«appearance.styleTypeString»" 
						width="«appearance.width»"
					/>
					<y:Arrows 
						source="« IF appearance.bidirectional »standard« ELSE »none« ENDIF »"
						target="standard
					"/>
					<y:BendStyle smoothed="true"/>
					<y:EdgeLabel 
						alignment="center" distance="2.0" fontFamily="Dialog" fontSize="12" fontStyle="«appearance.fontStyleString»" hasBackgroundColor="false" 
						hasLineColor="false" height="18.701171875" modelName="six_pos" modelPosition="tail" preferredPlacement="anywhere" 
						ratio="0.5" textColor="#000000" visible="«appearance.labelVisible»" width="23.341796875" x="0.0" y="0.0"
						«IF appearance.labelUnderlined» underlinedText="true"«ENDIF»
					>«edges.edgeLabel»</y:EdgeLabel>
				</y:PolyLineEdge>
			</data>
		</edge>
	'''
	
	def private getEdgeLabel(List<IEdge<?,?>> edges) {
		if (edges.size == 1) {
			edges.head.label
		} else if (showEdgeCount && !edges.nullOrEmpty) {
			edges.size
		} else {
			""
		}
	}
	
	def private String escapeValueInput(String input){
		// description is inside a CDATA block, so we need to escape only the CDATA closing sequence
		if(!input.nullOrEmpty) input.replaceAll("]]>", "]]]]><![CDATA[>")
	}
	
	def private getSelectedChildren(EObject obj) {
		obj.eContentsReferenced.filter[selectedNodes.contains(it)]
	}
	
	def private createMetaData(IPremiseObject mt) {
		val nameToId = switch(mt) {
			AElement: nodeMetaDataIds
			default: edgeMetaDataIds
		}
		
		return '''
			«FOR entry : mt.metaDataMap.entrySet»
				<data key="«nameToId.get(entry.key)»"><![CDATA[«escapeValueInput(entry.value)»]]></data>
			«ENDFOR»
		'''
	}
}
