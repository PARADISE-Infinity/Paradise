/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.states.xtend

import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.base.xtend.ICharsetProvidingFileSystemAccess
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.element.Mode
import de.dlr.premise.element.StateMachine
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.states.data.State
import java.util.ArrayList
import java.util.Comparator
import java.util.LinkedHashMap
import java.util.Set
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet

import static extension de.dlr.premise.util.PremiseHelper.*
import static extension de.dlr.premise.states.StateHelper.*
import de.dlr.premise.util.scope.ScopedEObjectFactory

class GeneratorTree {
	
	val static final MODE_COMPARATOR = Comparator
		// depth in model
		.<Mode>comparingInt[(it as EObject).closure[#[eContainer]].size]
		// name of containing state machine
		.<String>thenComparing[(eContainer as ANameItem).name]
	
	// A special LinkedHashMap that maps Mode to ModeMap and therefore can be used to build a tree of modes
	private static class ModeMap extends LinkedHashMap<Mode, ModeMap> {
		// No implementation needed, we just want to specify the type parameters for LinkedHashMap above
	}

	public static String OPT_TECH = "GraphML/StateTree"

	private ResourceSet root
	private Set<State> validStates
	
	def void compile(ArrayList<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa, IProgressMonitor monitor) {
		GeneratorHelper::clearRefs()
		
		root = selectedElements.get(0).eResource.resourceSet
		val sca = root.findOrCreateStateCheckerAdapter
		validStates = sca.getAllValidStates(monitor)
		if (validStates == null) {
			return
		}
		
		val sc1Name = selectedElements.get(0).name

		val fileName = GeneratorHelper::encodeFileName(sc1Name) + ".statestree.graphml"
		fsa.generateFile(fileName, '''
			<?xml version="1.0" encoding="«fsa.getFileCharset(fileName)»" standalone="no"?>
			  <graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:y="http://www.yworks.com/xml/graphml" xmlns:yed="http://www.yworks.com/xml/yed/3" xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd">
			  <!--Created by yFiles for Java 2.8-->
			  <key for="graphml" id="d0" yfiles.type="resources"/>
			  <key for="port" id="d1" yfiles.type="portgraphics"/>
			  <key for="port" id="d2" yfiles.type="portgeometry"/>
			  <key for="port" id="d3" yfiles.type="portuserdata"/>
			  <key attr.name="url" attr.type="string" for="node" id="d4"/>
			  <key attr.name="description" attr.type="string" for="node" id="d5"/>
			  <key for="node" id="d6" yfiles.type="nodegraphics"/>
			  <key attr.name="Beschreibung" attr.type="string" for="graph" id="d7"/>
			  <key attr.name="url" attr.type="string" for="edge" id="d8"/>
			  <key attr.name="description" attr.type="string" for="edge" id="d9"/>
			  <key for="edge" id="d10" yfiles.type="edgegraphics"/>
			  <graph edgedefault="directed" id="G">
			    <data key="d7"/>
			«selectedElements.createStatesTree()»
			«createNote()»
			  </graph>
			  <data key="d0">
			    <y:Resources/>
			  </data>
			</graphml>
		''')
	}

	def protected CharSequence createStatesTree(ArrayList<AElement> selectedElements) {
		var result = new StringBuilder()

		val tree = new ModeMap();
		val rootMode = ElementFactory::eINSTANCE.createMode()
		addNode(tree, rootMode)
		for (state : validStates) {
			// get map (children/subtree) of current rootMode
			var subtree = tree.get(rootMode)
			for (mode : state.modes.sortWith(MODE_COMPARATOR)) {
				addNode(subtree, mode)
				// set next depth level of tree
				subtree = subtree.get(mode)
			}
		}
		val rootId = GeneratorHelper::edgeID
		result.append(createMode(rootMode, rootId, true, false))
		result.append(createTreeRecursive(tree, rootMode, rootId))
		return result
	}
	
	def private addNode(ModeMap subtree, Mode mode) {
		if (!subtree.containsKey(mode)) {
			subtree.put(mode, new ModeMap())
		}
	}

	def protected CharSequence createTreeRecursive(ModeMap tree, Mode root, String rootId) {
		var result = new StringBuilder()
		val subtree = tree.get(root)
		for (mode : subtree.keySet) {
			val modeId = GeneratorHelper::edgeID
			val isLeaf = subtree.get(mode).isEmpty()
			// create mode
			result.append(createMode(mode, modeId, false, isLeaf))
			result.append(createEdge(rootId, modeId))
			// handle subtree
			result.append(createTreeRecursive(subtree, mode, modeId))
		}
		return result
	}

	def createMode(Mode mode, String id, boolean root, boolean leaf) '''
		<node id="n«id»">
		  <data key="d6">
		    <y:GenericNode configuration="com.yworks.bpmn.Event.withShadow">
		      <y:Geometry height="30.0" width="30.0" x="0.0" y="0.0"/>
		      <y:Fill color="#FFFFFFE6" color2="#D4D4D4CC" transparent="false"/>
		      <y:BorderStyle color="«IF root»#27AE27«ELSE»#DCBA00«ENDIF»" type="line" width="1.0"/>
		<y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="eight_pos" modelPosition="«IF leaf»s«ELSE»e«ENDIF»" textColor="#000000" visible="true" width="31.345703125" x="34.0" y="5.6494140625">«GeneratorHelper::encodeXML(mode.name)»
		«IF !root»«GeneratorHelper::encodeXML("(" + (mode.eContainer as StateMachine).name + ")")»«ENDIF»</y:NodeLabel>
		      <y:StyleProperties>
		        <y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="EVENT_TYPE_PLAIN"/>
		        <y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.line.color" value="#000000"/>
		        <y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill" value="#ffffff"/>
		        <y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill2" value="#d4d4d4"/>
		        <y:Property class="com.yworks.yfiles.bpmn.view.EventCharEnum" name="com.yworks.bpmn.characteristic" value="EVENT_CHARACTERISTIC_START"/>
		      </y:StyleProperties>
		    </y:GenericNode>
		  </data>
		</node>
	'''

	def createEdge(String sourceId, String targetId) '''
		<edge id="e«GeneratorHelper::getEdgeID»" source="n«sourceId»" target="n«targetId»">
		  <data key="d9"/>
		  <data key="d10">
		    <y:PolyLineEdge>
		      <y:Path sx="0.0" sy="0.0" tx="0.0" ty="0.0"/>
		      <y:LineStyle color="#000000" type="line" width="1.0"/>
		      <y:Arrows source="none" target="none"/>
		      <y:BendStyle smoothed="false"/>
		    </y:PolyLineEdge>
		  </data>
		</edge>
	'''

	def createNote() {
		val allStatesCount = ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(root, StateMachine).map[modes.size].filter[it > 0].reduce[s1, s2 | s1 * s2]
		val validStatesCount = validStates.size
	
		return '''
			<node id="n«GeneratorHelper::getEdgeID»">
			  <data key="d5"/>
			  <data key="d6">
			    <y:GenericNode configuration="com.yworks.bpmn.Artifact.withShadow">
			      <y:Geometry height="30.0" width="208.0" x="0.0" y="0.0"/>
			      <y:Fill color="#FFFFFFE6" transparent="false"/>
			      <y:BorderStyle color="#000000" type="line" width="1.0"/>
			      <y:NodeLabel alignment="left" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="33.40234375" modelName="internal" modelPosition="l" textColor="#000000" visible="true" width="177.408203125" x="4.0" y="-1.701171875">«validStatesCount» valid states
			(of max. «allStatesCount» without constraints)</y:NodeLabel>
			      <y:StyleProperties>
			        <y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="ARTIFACT_TYPE_ANNOTATION"/>
			        <y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.line.color" value="#000000"/>
			        <y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill" value="#ffffff"/>
			        <y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill2" value="#d4d4d4"/>
			      </y:StyleProperties>
			    </y:GenericNode>
			  </data>
			</node>
		'''
	}
	
}
