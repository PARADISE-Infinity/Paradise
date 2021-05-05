/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.cfta.xtend

import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.base.xtend.ICharsetProvidingFileSystemAccess
import de.dlr.exchange.base.xtend.IGeneratorMy
import de.dlr.premise.element.AElement
import de.dlr.premise.element.AGuardCondition
import de.dlr.premise.element.ARepository
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.element.GuardCombination
import de.dlr.premise.element.Mode
import de.dlr.premise.element.ModeGuard
import de.dlr.premise.element.StateMachine
import de.dlr.premise.element.Transition
import de.dlr.premise.functions.UseCaseRepository
import de.dlr.premise.util.PremiseHelper
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Set
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.generator.IFileSystemAccess
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.TransitionBalancing
import de.dlr.premise.system.TransitionParameter
import de.dlr.premise.registry.ADataItem
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.registry.Junction

import static extension de.dlr.premise.util.PremiseHelper.*

class Generator implements IGeneratorMy {

	public static String OPT_TECH = "GraphML/CFTA"
	public static String OPT_PARAM_NAMES_NAME = "Parameter names containing one of the following are skipped in diagram"
	public static String OPT_PARAM_NAMES_VAL = "SkippedParamName, _, tmp"
	public static String OPT_DUMMY_COMPONENT_NAME = "Component names containing one of the following are skipped (parent name is taken instead)"
	public static String OPT_DUMMY_COMPONENT_VAL = "modes, _"

	private static AElement selectedElement
	private static Set<AGuardCondition> allRootGuardConditions
	private static boolean generateFullTree
	private HashMap<String,String> portmap
	private List<UndevelopedComponent> undevCompList
	
	

	// entry point for using editor popup
	override doGenerateFromAElements(ResourceSet resSet, List<AElement> selectedElements,
		ICharsetProvidingFileSystemAccess fsa) {

		// clear all previous stored references
		GeneratorHelper::clearRefs


		// check selected elements and compile
		if (!selectedElements.nullOrEmpty) {

			generateFullTree = false
			selectedElement = selectedElements.get(0)
			selectedElements.compile(fsa)

		} else {
			println("No elements were found to export.")
		}
	}

	// entry point for using navigator popup
	override doGenerateFromResources(ResourceSet resSet, List<Resource> selectedFiles,
		ICharsetProvidingFileSystemAccess fsa) {		

		// clear all previous stored references
		GeneratorHelper::clearRefs
			
		
		// create selectedElements list from ARepositories
		var selectedElements = selectedFiles.flatMap[contents].filter(ARepository).flatMap[elements].toList

			// check selected elements and compile
		if (!selectedElements.nullOrEmpty) {

			generateFullTree = true
			selectedElement = selectedElements.get(0)
			selectedElements.compile(fsa)

		} else {
			println("No elements were found to export.")
		}
	}

	def dispatch void compile(EObject m, IFileSystemAccess fsa) {
		println("<!-- Should not be printed! -->")
	}

	def dispatch void compile(ArrayList<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa) {

		portmap = new HashMap<String,String>()
		undevCompList = new ArrayList<UndevelopedComponent>()

		// get repository and file name
		val fileName = GeneratorHelper::encodeFileName(selectedElements.get(0).name) + ".cfta.graphml"

		// start file generation
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
			«checkGenerationMode(selectedElement, selectedElements)»
			  </graph>
			  <data key="d0">
			    <y:Resources>
			      «createGatterSVGResources()»
			    </y:Resources>
			  </data>
			</graphml>
		''')

		println("File generated")
	}

	// call for graphml generation depending on what size the tree should be
	def CharSequence checkGenerationMode(AElement elm, ArrayList<AElement> selectedElements) {

		var result = new StringBuilder()
		var Set<AElement> systemElements

		// generate system component list and filter selected elements if only subtrees are to be generated
		if (generateFullTree) {
			systemElements = PremiseHelper::getRoot(elm).eAllContents.filter(sc|sc instanceof AElement).map(mm|
				mm as AElement
			).toSet()
		} else {
			systemElements = PremiseHelper::getRoot(elm).eAllContents.filter(sc|sc instanceof AElement).filter(scon|
				selectedElements.contains(scon)
			).map(mm|mm as AElement).toSet()
		}

		// traverse bottom up through the list and generate the file
		for (var i = systemElements.length - 1; i >= 0; i--) {
		//for (var i = 0; i < systemElements.length; i++) {
			val child = systemElements.get(i)
			selectedElement = child
			allRootGuardConditions = child.eAllContents.filter(e|e instanceof Transition).map(mm|
				(mm as Transition).getCondition
			).filter(c|(c instanceof GuardCombination || c instanceof ModeGuard)).filter(ch|PremiseHelper::getRootElement(ch) == child).toSet()

			if (!allRootGuardConditions.empty)
				result.append(traverseOneComponent(selectedElements))
		}
		return result
	}

	def CharSequence traverseOneComponent(ArrayList<AElement> selectedElements) {
		var result = new StringBuilder()
		var sysElement = PremiseHelper::getRootElement(selectedElement)

		// open group node
		result.append(createGroupFolderStart(sysElement))

		for (rootTransCon : allRootGuardConditions) {
			val trans = (rootTransCon as AGuardCondition).eContainer as Transition
			if (trans != null && trans.getCondition != null) {

				if (!generateFullTree) {
					// create outports of components as they are not referenced when not generating the whole tree
					val scftOutport = ElementFactory::eINSTANCE.createMode
					scftOutport.name = trans.getTarget.name
					result.append(scftOutport.createScftOutportNode(trans.getTarget))
					result.append(createEdge(trans.getTarget, scftOutport))
				}

				// append the event node of the root guard condition
				result.append(trans.getTarget.createNode())
				result.append(
					traverseGuardConditions(rootTransCon as AGuardCondition, trans.getTarget, trans.getCondition))
			}
		}

		// close group
		result.append(createGroupFolderEnd())

		// check if there are any undev Components to add before going to the next component
		if (!undevCompList.nullOrEmpty){
			for (undevComp : undevCompList){
				if (undevComp.verfiy){
					result.append(createUndevelopedGroup(undevComp.triggerMode, undevComp.params, undevComp.parentNode, undevComp.rootGuardCon))
				}
			}
			undevCompList.clear
		}

		return result
	}

	def CharSequence traverseGuardConditions(AGuardCondition rootGuardCon, Mode parentNode, AGuardCondition guardCon) {

		// define result and parameter list
		var result = new StringBuilder()
		var List<AParameterDef> params = new ArrayList<AParameterDef>()

		if (guardCon instanceof ModeGuard) {
			// create node and leafCircle only if triggerMode is not targeted by another rootGuardCon (connected event trees)
			// and create them only if not yet done
			// and create them only if they refer to modes in the same Component 
			val triggerMode = (guardCon as ModeGuard).getMode
			if (allRootGuardConditions.findFirst(
            		tc|
				((tc as AGuardCondition).eContainer as Transition).getTarget == triggerMode
			) == null && !GeneratorHelper::isRef(triggerMode) &&
				PremiseHelper::getRootElement(triggerMode) == PremiseHelper::getRootElement(rootGuardCon)) {

				// create node and edge to parent
				result.append(triggerMode.createNode())
				result.append(createEdge(triggerMode, parentNode))
				// create leaf circle and edge to node
				val leafCircleMode = ElementFactory::eINSTANCE.createMode

				var List<TransitionParameter> leafTransParams = new ArrayList<TransitionParameter>();

				// get parameters of leaf nodes for displaying
				if (triggerMode.eContainer instanceof StateMachine) {
					val sm = triggerMode.eContainer as StateMachine
					leafTransParams = sm.transitions.findFirst(t|t.getTarget == triggerMode)?.getParameters
				}

				if (leafTransParams != null) {
					params.addAll(leafTransParams)
					// remove parameters according to OPT_PARAM_NAMES_NAME
					params = removeSkippedParameters(params)
				}

				// create leaf circle and connect to parent
				result.append(leafCircleMode.createLeafCircle(params))
				result.append(createEdge(leafCircleMode, triggerMode))

			} else {
				// create edge only, except for ports
				if (PremiseHelper::getRootElement(triggerMode) == PremiseHelper::getRootElement(rootGuardCon)) {

					// in component edge
					result.append(createEdge(triggerMode, parentNode))

				} else {

					// link components via ports
					// get failure rates to display at inports
					var List<TransitionParameter> leafTransParams = new ArrayList<TransitionParameter>();
					// get parameters of leaf nodes for displaying
					val ae = PremiseHelper.getRootElement(triggerMode)
					leafTransParams = PremiseHelper.getTransitions(ae).findFirst(t|t.getTarget == triggerMode)?.
						getParameters

					if (leafTransParams != null) {
						params.addAll(leafTransParams)
						// remove parameters according to OPT_PARAM_NAMES_NAME
						params = removeSkippedParameters(params)
					}

					// catch single component cfts and hide subcomponents outports
					if (generateFullTree) {

						// check for missing mode guards in a component
						val triggerComponent = PremiseHelper::getRootElement(triggerMode)
						val childGuardCondition = triggerComponent.eAllContents.findFirst(gc|
							gc instanceof AGuardCondition
						)

						// do not create inport node if event is undeveloped (no mode guards)
						if (childGuardCondition == null) {
							
							var undevComp = new UndevelopedComponent(triggerMode, params, parentNode, rootGuardCon)
							undevCompList.add(undevComp)
							// Add the component in port
							val inport = ElementFactory::eINSTANCE.createMode
							inport.name = triggerMode.name + " of " + triggerMode.getModeComponentName
							inport.id = inport.name.replace(' ', '-') + PremiseHelper.getRootElement(rootGuardCon);
							result.append(inport.createInportNode(params))
							result.append(createEdge(inport, parentNode))
							
							
						} else {
							// Add the component in port
							val inport = ElementFactory::eINSTANCE.createMode
							inport.name = triggerMode.name + " of " + triggerMode.getModeComponentName
							inport.id = inport.name.replace(' ', '-') + PremiseHelper.getRootElement(rootGuardCon);
							result.append(inport.createInportNode(params))
							result.append(createEdge(inport, parentNode))
							// if an edge is already there, do not create an additional edge
							if (!(portmap.containsKey(triggerMode.id) && portmap.containsValue(inport.id) && portmap.get(triggerMode.id) == inport.id)){
								result.append(createEdge(triggerMode, inport))
								portmap.put(triggerMode.id, inport.id)
							}
							
							result.append(triggerMode.createOutportNode())
						}
					} else {
						// add the inport as each component is viewed individually
						val inport = ElementFactory::eINSTANCE.createMode
						inport.name = triggerMode.name + " of
						" + triggerMode.getModeComponentName
						inport.name = inport.name.replace('	','')
						inport.id = inport.name.replace(' ', '-') + PremiseHelper.getRootElement(triggerMode).id;
						
						result.append(inport.createInportNode(params))
						result.append(createEdge(inport, parentNode))
					}

				}
			}

		} else if (guardCon instanceof GuardCombination) {

			// define guard condition and create gate node
			val nestedTransCon = guardCon as GuardCombination
			val gateNode = ElementFactory::eINSTANCE.createMode

			// if this transCon is root, get calculated transition parameters for displaying
			if (nestedTransCon == rootGuardCon && rootGuardCon.eContainer instanceof Transition) {

				// get calculated transition parameter
				val ae = PremiseHelper.getRootElement(rootGuardCon)

				
				var tParamBals = ae.transitionBalancings
				if (!tParamBals.nullOrEmpty) {
					var allParams = new ArrayList<AParameterDef>()
					allParams.addAll(tParamBals.map(tb|tb.target))
					params.clear
					// add only parameter out of the same transition the guardCon is contained in
					for (param : allParams){
						if (guardCon.eContainer instanceof Transition){
							if (param.eContainer == guardCon.eContainer){
								params.add(param)
							}
						}
					}
				}

				// remove parameters according to OPT_PARAM_NAMES_NAME
				params = removeSkippedParameters(params)
			}

			// add intermediate nodes between gates
			// check that there is no gate to gate edge
			if (!(guardCon.eContainer instanceof GuardCombination)) {

				// create gate and edge to parent
				result.append(createGatter(gateNode, nestedTransCon.getJunction, params))
				result.append(createEdge(gateNode, parentNode))
			} else {

				// create new node and draw connections
				val intermediateEvent = ElementFactory::eINSTANCE.createMode
				intermediateEvent.name = "Unspecified 
Intermediate Event"
				if(guardCon.name != "") intermediateEvent.name = guardCon.name
				result.append(intermediateEvent.createIntermediateNode())

				// create gate and edges around intermediate event
				result.append(createGatter(gateNode, nestedTransCon.getJunction, params))
				result.append(createEdge(gateNode, intermediateEvent))
				result.append(createEdge(intermediateEvent, parentNode))
			}

			// recursive call
			for (child : nestedTransCon.children) {
				result.append(traverseGuardConditions(rootGuardCon, gateNode, child))
			}
		}
		return result
	}

	def createNode(
		Mode failureMode) '''
		
		  <node id="n«GeneratorHelper::getRef(failureMode)»">
		    <data key="d6">
		      <y:GenericNode configuration="com.yworks.bpmn.Activity.withShadow">
		        <y:Geometry height="55.0" width="125.0" x="0.0" y="0.0"/>
		        <y:Fill color="#FFFFFFE6" color2="#D4D4D4CC" transparent="false"/>
		        <y:BorderStyle color="#123EA2" type="line" width="1.0"/>
		    <y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="internal" modelPosition="c" textColor="#000000" visible="true" width="105.40234375" x="0.0" y="0.0">«failureMode.name»</y:NodeLabel>
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

	def createIntermediateNode(
		Mode failureMode ) '''
		
		  <node id="n«GeneratorHelper::getRef(failureMode)»">
		    <data key="d6">
		      <y:GenericNode configuration="com.yworks.bpmn.Activity.withShadow">
		        <y:Geometry height="55.0" width="125.0" x="0.0" y="0.0"/>
		        <y:Fill color="#FFFFFFE6" color2="#D4D4D4cc" transparent="false"/>
		        <y:BorderStyle color="#123EA2" type="dashed" width="1.0"/>
		    <y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="internal" modelPosition="c" textColor="#000000" visible="true" width="105.40234375" x="0.0" y="0.0">«failureMode.name»</y:NodeLabel>
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

	def private getNodeLabel(Mode failureMode) '''
	«GeneratorHelper::encodeXML(failureMode.name)+" of"»
	«GeneratorHelper::encodeXML(getModeComponentName(failureMode))»'''

	def createLeafCircle(Mode leafMode,
		List<AParameterDef> params) '''
		
		  <node id="n«GeneratorHelper::getRef(leafMode)»">
		    <data key="d5"/>
		    <data key="d6">
		      <y:GenericNode configuration="com.yworks.bpmn.Event">
		        <y:Geometry height="30.0" width="30.0" x="0.0" y="0.0"/>
		        <y:Fill color="#FFFFFF" color2="#FFFFFF" transparent="false"/>
		        <y:BorderStyle color="#C0C0C0" type="line" width="2.0"/>
		        <y:NodeLabel alignment="left" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="eight_pos" modelPosition="s" textColor="#000000" visible="true" width="20.0078125" x="0.0" y="0.0">«FOR param : params»«createLabel(param)»«ENDFOR»</y:NodeLabel>
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

	def createEdge(ADataItem source,
		ADataItem target) '''
		
		  <edge id="n«GeneratorHelper::getEdgeID»" source="n«GeneratorHelper::getRef(source)»" target="n«GeneratorHelper::getRef(target)»">
		    <data key="d10">
		      <y:GenericEdge configuration="com.yworks.bpmn.Connection">
		        <y:Path sx="15.0" sy="-0.0" tx="-15.0" ty="-0.0"/>
		        <y:LineStyle color="#000000" type="line" width="1.0"/>
		        <y:Arrows source="none" target="short"/>
		        <y:StyleProperties>
		          <y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="CONNECTION_TYPE_SEQUENCE_FLOW"/>
		        </y:StyleProperties>
		      </y:GenericEdge>
		    </data>
		  </edge>
	'''

	def createGatter(ADataItem item, Junction junction,
		List<AParameterDef> params) '''
		
		  <!-- «junction.getName()» GATTER -->
		  <node id="n«GeneratorHelper::getRef(item)»">
		    <data key="d6">
		      <y:SVGNode>
		        <y:Geometry height="36.76666031837427" width="35.58063671913899" x="0.0" y="0.0"/>
		        <y:Fill color="#CCCCFF" transparent="false"/>
		        <y:BorderStyle color="#000000" type="line" width="1.0"/>
		        <y:NodeLabel alignment="left" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="eight_pos" modelPosition="e" textColor="#000000" visible="true" width="0.0" x="39.58063671913899" y="0.0">«FOR param : params»«createLabel(param)»
		«ENDFOR»</y:NodeLabel>
		        <y:SVGNodeProperties usingVisualBounds="true"/>
		        <y:SVGModel svgBoundsPolicy="0">
		  <y:SVGContent refid="«junction.ordinal»"/>
		        </y:SVGModel>
		      </y:SVGNode>
		    </data>
		  </node>
	'''

	def createGatterSVGResources() '''
		<!-- AND GATE-->
		<y:Resource id="1">&lt;?xml version="1.0" encoding="utf-8"?&gt;
		&lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
		&lt;svg version="1.1" id="Ebene_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
			 width="67px" height="77px" viewBox="0 0 67 77" enable-background="new 0 0 67 77" xml:space="preserve"&gt;
		&lt;path fill="#515151" d="M13.7,9.68C22.13,1.14,36.35-0.93,46.68,5.26C56.5,11.04,62.82,21.82,64.061,33.03
			c0.779,7.3,0.41,14.649,0.469,21.97c-0.059,6.73,0.301,13.47-0.289,20.18C43.5,75.37,22.74,75.52,2,75.17
			C1.87,63.109,2.09,51.05,1.93,38.99C1.52,28.16,5.78,17.14,13.7,9.68z"/&gt;
		&lt;path fill="#FFFFFF" d="M11.58,13.52c7.49-9.65,21.74-13.63,32.85-8.05c10.19,4.561,16.211,15.24,18.09,25.87
			c0.92,6.18,0.51,12.44,0.58,18.66c-0.109,7.95,0.26,15.91-0.18,23.859C43,74.08,23.06,74.07,3.14,73.87
			C2.87,62.9,3.08,51.939,3,40.98C2.83,31.24,5.35,21.18,11.58,13.52z"/&gt;
		&lt;/svg&gt;</y:Resource>
		<!-- OR GATE-->
		<y:Resource id="0">&lt;?xml version="1.0" encoding="utf-8"?&gt;
		&lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
		&lt;svg version="1.1" id="Ebene_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
			 width="60px" height="62px" viewBox="0 0 60 62" enable-background="new 0 0 60 62" xml:space="preserve"&gt;
		&lt;path fill="#4B4B4B" d="M6.09,38.74C7.91,23.39,16.68,8.43,30.24,1.28C42.48,6.9,50.58,19.55,53.77,32.65
			c2.49,8.939,1.57,18.31,1.65,27.5C43.71,54.359,30.24,53.84,17.6,55.97c-4.12,0.64-7.94,2.41-11.76,4.07
			c0.021-7.101-0.26-14.22,0.25-21.31V38.74z"/&gt;
		&lt;path fill="#FFFFFF" d="M7.54,37.92C9.25,23.83,17.48,10.09,30.2,3.53c11.489,5.16,19.1,16.77,22.1,28.8
			c2.33,8.21,1.47,16.82,1.55,25.25c-11-5.311-23.649-5.79-35.51-3.83c-3.87,0.59-7.46,2.21-11.04,3.74
			C7.32,50.97,7.05,44.43,7.54,37.92z"/&gt;
		&lt;/svg&gt;
		</y:Resource>
		<!-- NOT GATE-->
		      <y:Resource id="2">&lt;?xml version="1.0" encoding="utf-8"?&gt;
		&lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
		&lt;svg version="1.1" id="Ebene_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
			 width="67px" height="77px" viewBox="0 0 67 77" enable-background="new 0 0 67 77" xml:space="preserve"&gt;
		&lt;path fill="#4B4B4B" d="M2.01,74.92l31.1-72.21l31.14,72.44L2.01,74.92z"/&gt;
		&lt;path fill="#FFFFFF" d="M4.17,73.42l29-67.81l29.1,68.031L4.17,73.42z"/&gt;
		&lt;/svg&gt;
		</y:Resource>
		<!-- XOR GATE-->
		      <y:Resource id="3">&lt;?xml version="1.0" encoding="utf-8"?&gt;
		&lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
		&lt;svg version="1.1" id="Ebene_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
			 width="60px" height="62px" viewBox="0 0 60 62" enable-background="new 0 0 60 62" xml:space="preserve"&gt;
		&lt;path fill="#4B4B4B" d="M6.1,38.74C7.91,23.39,16.68,8.43,30.24,1.28C42.48,6.9,50.58,19.54,53.77,32.65
			c2.49,8.91,1.57,18.31,1.65,27.5C43.71,54.359,30.24,53.84,17.6,55.97c-4.12,0.64-7.94,2.41-11.76,4.07
			c0.021-7.101-0.26-14.229,0.25-21.31L6.1,38.74z"/&gt;
		&lt;path fill="#FFFFFF" d="M7.54,37.91C9.25,23.82,17.48,10.08,30.2,3.52c11.489,5.16,19.1,16.77,22.1,28.8
			c2.33,8.21,1.47,16.82,1.55,25.25c-11-5.311-23.649-5.79-35.51-3.83c-3.87,0.59-7.46,2.21-11.04,3.74
			C7.32,50.96,7.05,44.42,7.54,37.91z"/&gt;
		&lt;path stroke="#4B4B4B" d="M30.38,2.45L7.42,57.58l0,0"/&gt;
		&lt;path stroke="#4B4B4B" d="M30.21,2.63l23.92,55.82l0,0"/&gt;
		&lt;/svg&gt;
		</y:Resource>
		<!-- VOTE GATE-->
		<y:Resource id="4">&lt;?xml version="1.0" encoding="utf-8"?&gt;
		&lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
		&lt;svg version="1.1" id="Ebene_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
			 width="60px" height="62px" viewBox="0 0 60 62" enable-background="new 0 0 60 62" xml:space="preserve"&gt;
		&lt;path fill="#4B4B4B" d="M6.09,38.74C7.91,23.39,16.68,8.43,30.24,1.28C42.48,6.9,50.58,19.55,53.77,32.65
			c2.49,8.939,1.57,18.31,1.65,27.5C43.71,54.359,30.24,53.84,17.6,55.97c-4.12,0.64-7.94,2.41-11.76,4.07
			c0.02-7.101-0.26-14.22,0.25-21.31V38.74z"/&gt;
		&lt;path fill="#FFFFFF" d="M7.54,37.92C9.25,23.83,17.48,10.09,30.2,3.53c11.489,5.16,19.1,16.77,22.1,28.8
			c2.33,8.21,1.47,16.82,1.55,25.25c-11-5.311-23.649-5.79-35.51-3.83c-3.87,0.59-7.46,2.21-11.04,3.74
			C7.32,50.97,7.05,44.43,7.54,37.92z"/&gt;
		&lt;text transform="matrix(1 0 0 1 14.4199 41.5703)" fill="#4B4B4B" font-family="'ArialMT'" font-size="12"&gt;k/n&lt;/text&gt;
		&lt;/svg&gt;
		</y:Resource>
		<!-- PAND GATE-->
		<y:Resource id="5">&lt;?xml version="1.0" encoding="utf-8"?&gt;
		&lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
		&lt;svg version="1.1" id="Ebene_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
			 width="67px" height="77px" viewBox="0 0 67 77" enable-background="new 0 0 67 77" xml:space="preserve"&gt;
		&lt;path fill="#4B4B4B" d="M13.7,9.68C22.13,1.14,36.35-0.93,46.68,5.26C56.5,11.04,62.82,21.82,64.061,33.03
			c0.779,7.3,0.41,14.649,0.469,21.97c-0.059,6.73,0.301,13.47-0.289,20.18C43.5,75.37,22.74,75.52,2,75.17
			C1.87,63.109,2.09,51.05,1.93,38.99C1.52,28.16,5.78,17.14,13.7,9.68z"/&gt;
		&lt;path fill="#FFFFFF" d="M11.58,13.52c7.49-9.65,21.74-13.63,32.85-8.05c10.19,4.561,16.211,15.24,18.09,25.87
			c0.92,6.18,0.51,12.44,0.58,18.66c-0.109,7.95,0.26,15.91-0.18,23.859C43,74.08,23.06,74.07,3.14,73.87
			C2.87,62.9,3.08,51.939,3,40.98C2.83,31.24,5.35,21.18,11.58,13.52z"/&gt;
		&lt;path stroke="#4B4B4B" d="M33.058,2.146L3.33,74.164l0,0"/&gt;
		&lt;path stroke="#4B4B4B" d="M33.309,2.02l29.619,72.46l0,0"/&gt;
		&lt;/svg&gt;
		</y:Resource>
	'''

	def createGroupFolderStart(
		AElement sysElem) '''
		<node id="n«GeneratorHelper::getRef(sysElem)»" yfiles.foldertype="group">
		      <data key="d4"/>
		      <data key="d6">
		        <y:ProxyAutoBoundsNode>
		          <y:Realizers active="0">
		            <y:GroupNode>
		              <y:Geometry height="100" width="100" x="0" y="0"/>
		              <y:Fill color="#F5F5F5" transparent="false"/>
		              <y:BorderStyle color="#000000" type="dashed" width="1.0"/>
		              <y:NodeLabel alignment="right" autoSizePolicy="node_width" backgroundColor="#EBEBEB" borderDistance="0.0" fontFamily="Dialog" fontSize="15" fontStyle="plain" hasLineColor="false" height="22.37646484375" horizontalTextPosition="center" iconTextGap="4" modelName="internal" modelPosition="t" textColor="#000000" verticalTextPosition="bottom" visible="true" width="100" x="0.0" y="0.0">«sysElem.name»</y:NodeLabel>
		              <y:Shape type="roundrectangle"/>
		              <y:State closed="false" closedHeight="50.0" closedWidth="50.0" innerGraphDisplayEnabled="false"/>
		              <y:Insets bottom="5" bottomF="5.0" left="5" leftF="5.0" right="5" rightF="5.0" top="5" topF="5.0"/>
		              <y:BorderInsets bottom="170" bottomF="170.0509315658362" left="113" leftF="113.08079455004565" right="121" rightF="121.41920544995435" top="110" topF="109.5167392338277"/>
		            </y:GroupNode>
		            <y:GroupNode>
		              <y:Geometry height="50.0" width="50.0" x="0" y="0"/>
		              <y:Fill color="#F5F5F5" transparent="false"/>
		              <y:BorderStyle color="#000000" type="dashed" width="1.0"/>
		              <y:NodeLabel alignment="right" autoSizePolicy="node_width" backgroundColor="#EBEBEB" borderDistance="0.0" fontFamily="Dialog" fontSize="15" fontStyle="plain" hasLineColor="false" height="22.37646484375" horizontalTextPosition="center" iconTextGap="4" modelName="internal" modelPosition="t" textColor="#000000" verticalTextPosition="bottom" visible="true" width="50.0" x="0.0" y="0.0">«sysElem.name»</y:NodeLabel>
		              <y:Shape type="roundrectangle"/>
		              <y:State closed="true" closedHeight="50.0" closedWidth="50.0" innerGraphDisplayEnabled="false"/>
		              <y:Insets bottom="5" bottomF="5.0" left="5" leftF="5.0" right="5" rightF="5.0" top="5" topF="5.0"/>
		              <y:BorderInsets bottom="0" bottomF="0.0" left="0" leftF="0.0" right="0" rightF="0.0" top="0" topF="0.0"/>
		            </y:GroupNode>
		          </y:Realizers>
		        </y:ProxyAutoBoundsNode>
		      </data>
		      <graph edgedefault="directed" id="n«GeneratorHelper::getRef(sysElem)»">
	'''

	def createGroupFolderEnd() '''</graph>
	    </node>'''

	def createOutportNode(
		Mode outport) '''
		<node id="n«GeneratorHelper::getRef(outport)»">
			<data key="d6">
				<y:ShapeNode>
				<y:Geometry height="30.0" width="30.0" x="0" y="0"/>
				<y:Fill color="#FFCC00" transparent="false"/>
				<y:BorderStyle color="#000000" raised="false" type="line" width="1.0"/>
				<y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="33.40234375" horizontalTextPosition="center" iconTextGap="4" modelName="eight_pos" modelPosition="w" textColor="#000000" verticalTextPosition="bottom" visible="true" width="54.021484375" x="-12.0107421875" y="34.0">Out: «getNodeLabel(outport)»</y:NodeLabel>
				<y:Shape type="triangle"/>
				</y:ShapeNode>
			</data>
		</node>
	'''

	def createScftOutportNode(Mode outport,
		Mode failureMode) '''
		<node id="n«GeneratorHelper::getRef(outport)»">
			<data key="d6">
				<y:ShapeNode>
				<y:Geometry height="30.0" width="30.0" x="0" y="0"/>
				<y:Fill color="#FFCC00" transparent="false"/>
				<y:BorderStyle color="#000000" raised="false" type="line" width="1.0"/>
				<y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="33.40234375" horizontalTextPosition="center" iconTextGap="4" modelName="eight_pos" modelPosition="n" textColor="#000000" verticalTextPosition="bottom" visible="true" width="54.021484375" x="-12.0107421875" y="34.0">Out: «getNodeLabel(failureMode)»</y:NodeLabel>
				<y:Shape type="triangle"/>
				</y:ShapeNode>
			</data>
		</node>
	'''

	def createInportNode(Mode inport,
		List<AParameterDef> params) '''
		<node id="n«GeneratorHelper::getRef(inport)»">
			<data key="d6">
				<y:ShapeNode>
				<y:Geometry height="30.0" width="30.0" x="0" y="0"/>
				<y:Fill color="#FFCC00" transparent="false"/>
				<y:BorderStyle color="#000000" raised="false" type="line" width="1.0"/>
				<y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="33.40234375" horizontalTextPosition="center" iconTextGap="4" modelName="eight_pos" modelPosition="s" textColor="#000000" verticalTextPosition="bottom" visible="true" width="54.021484375" x="-12.0107421875" y="34.0">In: «inport.name»
				«FOR param : params»«createLabel(param)»«ENDFOR»</y:NodeLabel>
				<y:Shape type="triangle"/>
				</y:ShapeNode>
			</data>
		</node>
	'''

	def createUndevelopedNode(Mode outport,
		List<AParameterDef> params) '''
		<node id="n«GeneratorHelper::getRef(outport)»">
			<data key="d6">
				<y:ShapeNode>
				<y:Geometry height="30.0" width="30.0" x="0" y="0"/>
				<y:Fill color="#008000" transparent="false"/>
				<y:BorderStyle color="#000000" raised="false" type="line" width="1.0"/>
				<y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="33.40234375" horizontalTextPosition="center" iconTextGap="4" modelName="eight_pos" modelPosition="s" textColor="#000000" verticalTextPosition="bottom" visible="true" width="54.021484375" x="-12.0107421875" y="34.0">Undeveloped «outport.name»
				«FOR param : params»«createLabel(param)»«ENDFOR»</y:NodeLabel>
				<y:Shape type="diamond"/>
				</y:ShapeNode>
			</data>
		</node>
	'''

	def private String createLabel(AParameterDef param) {
		var result = ""
		if (param != null) {
			result = GeneratorHelper::encodeXML(param.name)
			if (param.getValue != null) {
				result = result + " = " + PremiseHelper.getStrValue(param.getValue());
			}
		}
		return result
	}

	def private List<AParameterDef> removeSkippedParameters(List<AParameterDef> params) {
		var List<AParameterDef> result = new ArrayList<AParameterDef>()
		for (param : params) {
			if (!containsStringFromOption(param.name, OPT_PARAM_NAMES_NAME)) {
				result.add(param)
			}
		}
		return result
	}

	def private String getModeComponentName(Mode mode) {
		var result = "";
		try {
			val parent = mode.eContainer.eContainer as AElement
			result = parent.name;
			return result

		} catch (Exception e) {
			return "";
		}
	}

	def private boolean containsStringFromOption(String name, String optName) {
		val tokens = GeneratorHelper::getMetaDataValue(selectedElement, optName).replace(' ', '').split(',')
		if (name != null) {
			for (token : tokens) {
				if (!token.empty && name.contains(token)) {
					return true
				}
			}
		}
		return false
	}

	def getTransitionBalancings(AElement element) {
		var transitions = PremiseHelper::getTransitions(element)
		var balancings = new ArrayList<TransitionBalancing>()
		for (tran : transitions) {
			for (balancing : tran.balancings) {
				balancings.add(balancing)
			}
		}
		return balancings
	}
	
	def private CharSequence createUndevelopedGroup(Mode triggerMode, List<AParameterDef> params, Mode parentNode, AGuardCondition rootGuardCon) {

		var result = new StringBuilder()
		val aelement = PremiseHelper.getRootElement(triggerMode)

		// open component group
		result.append(createGroupFolderStart(aelement))
		// append undeveloped event
		var undevMode = ElementFactory.eINSTANCE.createMode
		undevMode.name = triggerMode.name
		undevMode.id = triggerMode.id + "undev"
		result.append(undevMode.createUndevelopedNode(params))
		// create outport
		result.append(triggerMode.createOutportNode())
		// link event and outport
		if (!(portmap.containsKey(undevMode.id) && portmap.containsValue(triggerMode.id) &&
			portmap.get(undevMode.id) == triggerMode.id)) {
			result.append(createEdge(undevMode, triggerMode))
			portmap.put(undevMode.id, triggerMode.id)
		}

		// close component group
		result.append(createGroupFolderEnd)
		// recreate inport and link outport
		// Add the component in port
		val inport = ElementFactory::eINSTANCE.createMode
		inport.name = triggerMode.name + " of " + triggerMode.getModeComponentName
		inport.id = inport.name.replace(' ', '-') + PremiseHelper.getRootElement(rootGuardCon);
		// if an edge is already there, do not create an additional edge
		if (!(portmap.containsKey(triggerMode.id) && portmap.containsValue(inport.id) &&
			portmap.get(triggerMode.id) == inport.id)) {
			result.append(createEdge(triggerMode, inport))
			portmap.put(triggerMode.id, inport.id)
		}
		return result
	}

	def dispatch getElements(UseCaseRepository ur) {
		ur.usecases.map(u|u as AElement)
	}

	def dispatch getElements(ProjectRepository pr) {
		pr.projects.map(p|p as AElement)
	}

}
