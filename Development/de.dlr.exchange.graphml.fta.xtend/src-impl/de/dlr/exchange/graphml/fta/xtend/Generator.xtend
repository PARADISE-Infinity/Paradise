/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.graphml.fta.xtend

import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.base.xtend.ICharsetProvidingFileSystemAccess
import de.dlr.exchange.base.xtend.IGeneratorMy
import de.dlr.premise.element.AElement
import de.dlr.premise.element.AGuardCondition
import de.dlr.premise.element.ARepository
import de.dlr.premise.element.Transition
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.element.GuardCombination
import de.dlr.premise.element.Mode
import de.dlr.premise.element.ModeGuard
import de.dlr.premise.functions.UseCaseRepository
import de.dlr.premise.util.PremiseHelper
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.ArrayList
import java.util.List
import java.util.Locale
import java.util.Set
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.generator.IFileSystemAccess
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.TransitionParameter
import de.dlr.premise.registry.ADataItem
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.registry.Junction
import de.dlr.premise.element.StateMachine
import de.dlr.premise.system.TransitionBalancing

import static extension de.dlr.premise.util.PremiseHelper.*

class Generator implements IGeneratorMy {

	public static String OPT_TECH = "GraphML/FTA"
	public static String OPT_PARAM_NAMES_NAME = "Parameter names containing one of the following are skipped in diagram"
	public static String OPT_PARAM_NAMES_VAL = "SkippedParamName, _, tmp"
	public static String OPT_DUMMY_COMPONENT_NAME = "Component names containing one of the following are skipped (parent name is taken instead)"
	public static String OPT_DUMMY_COMPONENT_VAL = "modes, _"

	private static ARepository repository
	private static Set<AGuardCondition> allRootGuardConditions
	
	// entry point for using editor popup
	override doGenerateFromAElements(ResourceSet resSet, List<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa) {
		
		// clear all previously stored references
		GeneratorHelper::clearRefs
			
		// check selected elements and compile
		if (!selectedElements.nullOrEmpty) {
			selectedElements.compile(fsa)
		} else {
			println("No elements were found to export.")
		}
	}

	// entry point for using navigator popup
	override doGenerateFromResources(ResourceSet resSet, List<Resource> selectedFiles,
		ICharsetProvidingFileSystemAccess fsa) {
		GeneratorHelper::clearRefs
		
		// create selectedElements list from ARepositories
		var selectedElements = selectedFiles.flatMap[contents].filter(ARepository).flatMap[elements].toList

		this.doGenerateFromAElements(resSet, selectedElements, fsa)
	}
	
	def dispatch void compile(EObject m, IFileSystemAccess fsa) {
		println("<!-- Should not be printed! -->")
	}

	def dispatch void compile(ArrayList<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa) {
		
		// get repository and file name
		repository = PremiseHelper::getRoot(selectedElements.get(0)) as ARepository
		val fileName = GeneratorHelper::encodeFileName(selectedElements.get(0).name)+".fta.graphml"
		
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
			«traverseRootGuardConditions(repository, selectedElements)»
			  </graph>
			  <data key="d0">
			    <y:Resources>
			      «createGatterSVGResources()»
			    </y:Resources>
			  </data>
			</graphml>
		''')
	}

	def CharSequence traverseRootGuardConditions(ARepository rep, ArrayList<AElement> selectedElements) {
		var result = new StringBuilder()
			// get all root GuardConditionss
			allRootGuardConditions = rep.eAllContents.filter(e|e instanceof Transition)
			      .map(mm|(mm as Transition).getCondition).filter(c|c instanceof GuardCombination).toSet()
			
			// traverse all GuardConditions
			for (rootTransCon : allRootGuardConditions) {
				val trans = (rootTransCon as AGuardCondition).eContainer as Transition
				if (trans != null && trans.getCondition != null) {
					result.append(trans.getTarget.createNode())
					result.append(traverseGuardConditions(rootTransCon as AGuardCondition, trans.getTarget, trans.getCondition))
				}
			}			
		return result
	}

	def CharSequence traverseGuardConditions(AGuardCondition rootGuardCon, Mode parentNode, AGuardCondition guardCon) {
		var result = new StringBuilder()
		
        var List<AParameterDef> params = new ArrayList<AParameterDef>()

		if (guardCon instanceof ModeGuard) {
			val triggerMode = (guardCon as ModeGuard).getMode
			// create node and leafCircle only if triggerMode is not targeted by another rootGuardCon (connected event trees)
			// and create them only if not yet done
            if (allRootGuardConditions.findFirst(
            		tc|((tc as AGuardCondition).eContainer as Transition).getTarget == triggerMode) == null
            		&& !GeneratorHelper::isRef(triggerMode)) {
				// create node and edge to parent
				result.append(triggerMode.createNode())
				result.append(createEdge(triggerMode, parentNode))
				// create leaf circle and edge to node
				val leafCircleMode = ElementFactory::eINSTANCE.createMode
				var List<TransitionParameter>leafTransParams = new ArrayList<TransitionParameter>();
				
				// get parameters of leaf nodes for displaying
				if (triggerMode.eContainer instanceof StateMachine) {
					val sm = triggerMode.eContainer as StateMachine
					leafTransParams =  sm.transitions.findFirst(t|t.getTarget==triggerMode)?.getParameters
				}

				if (leafTransParams != null) {
					params.addAll(leafTransParams)
					// remove parameters according to OPT_PARAM_NAMES_NAME
					params = removeSkippedParameters(params)
				}

			    result.append(leafCircleMode.createLeafCircle(params))
				result.append(createEdge(leafCircleMode, triggerMode))
			} else {
				// create edge only
				result.append(createEdge(triggerMode, parentNode))
			}

		} else if (guardCon instanceof GuardCombination) {
			val nestedTransCon = guardCon as GuardCombination
			val gatterNode = ElementFactory::eINSTANCE.createMode
			// if this transCon is root, get calculated transition parameters for displaying
			if (nestedTransCon == rootGuardCon && rootGuardCon.eContainer instanceof Transition) {
				val transBals = (rootGuardCon.eContainer as Transition).balancings.filterNull
				if (!transBals.nullOrEmpty) {
					params.addAll(transBals.map(tb|tb.getTarget))
				}
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

			// create gatter and edge to parent
			result.append(createGatter(gatterNode, nestedTransCon.getJunction, params))
			result.append(createEdge(gatterNode, parentNode))

			for (child : nestedTransCon.children) {
				result.append(traverseGuardConditions(rootGuardCon, gatterNode, child))
			}
		}
		return result
	}

	def createNode(Mode failureMode) '''
		
		  <node id="n«GeneratorHelper::getRef(failureMode)»">
		    <data key="d6">
		      <y:GenericNode configuration="com.yworks.bpmn.Activity.withShadow">
		        <y:Geometry height="55.0" width="125.0" x="0.0" y="0.0"/>
		        <y:Fill color="#FFFFFFE6" color2="#D4D4D4CC" transparent="false"/>
		        <y:BorderStyle color="#123EA2" type="line" width="1.0"/>
		  		<y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.701171875" modelName="internal" modelPosition="c" textColor="#000000" visible="true" width="105.40234375" x="0.0" y="0.0">«getNodeLabel(failureMode)»</y:NodeLabel>
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

	def createLeafCircle(Mode leafMode, List<AParameterDef> params) '''
		
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

	def createEdge(ADataItem source, ADataItem target) '''
		
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

	def createGatter(ADataItem item, Junction junction, List<AParameterDef> params) '''
		
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
		<!-- AND GATE -->
		<y:Resource id="0">&lt;?xml version="1.0" encoding="UTF-8" ?&gt;
			&lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
			&lt;svg width="60pt" height="62pt" viewBox="0 0 60 62" version="1.1" xmlns="http://www.w3.org/2000/svg"&gt;
			&lt;path fill="#ffffff" d=" M 0.00 0.00 L 60.00 0.00 L 60.00 62.00 L 0.00 62.00 L 0.00 0.00 Z" /&gt;
			&lt;path fill="#4b4b4b" d=" M 27.78 2.72 C 29.41 1.47 31.48 1.74 33.09 2.86 C 46.69 11.22 55.42 26.99 55.09 42.98 C 55.00 48.94 55.06 54.90 55.08 60.86 C 52.90 58.53 50.44 56.25 47.23 55.51 C 36.73 52.82 25.62 52.79 15.03 55.02 C 11.44 55.71 8.42 57.88 5.54 60.01 C 5.37 51.45 5.01 42.82 6.32 34.33 C 8.78 21.52 16.44 9.42 27.78 2.72 Z" /&gt;
			&lt;path fill="#ffffff" d=" M 7.25 37.21 C 8.97 23.12 17.27 9.38 30.10 2.82 C 41.69 7.98 49.36 19.59 52.38 31.62 C 54.73 39.83 53.86 48.44 53.94 56.87 C 42.85 51.56 30.10 51.08 18.14 53.04 C 14.24 53.63 10.62 55.25 7.01 56.78 C 7.03 50.26 6.76 43.72 7.25 37.21 Z" /&gt;
			&lt;/svg&gt;
		</y:Resource>
		<!-- OR GATE -->
		<y:Resource id="1">&lt;?xml version="1.0" encoding="UTF-8" ?&gt;
			&lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
			&lt;svg width="67pt" height="77pt" viewBox="0 0 67 77" version="1.1" xmlns="http://www.w3.org/2000/svg"&gt;
			&lt;path fill="#ffffff" d=" M 0.00 0.00 L 67.00 0.00 L 67.00 77.00 L 0.00 77.00 L 0.00 0.00 Z" /&gt;
			&lt;path fill="#515151" d=" M 13.70 9.68 C 22.13 1.14 36.35 -0.93 46.68 5.26 C 56.50 11.04 62.82 21.82 64.06 33.03 C 64.84 40.33 64.47 47.68 64.53 55.00 C 64.47 61.73 64.83 68.47 64.24 75.18 C 43.50 75.37 22.74 75.52 2.00 75.17 C 1.87 63.11 2.09 51.05 1.93 38.99 C 1.52 28.16 5.78 17.14 13.70 9.68 Z" /&gt;
			&lt;path fill="#ffffff" d=" M 11.58 13.52 C 19.07 3.87 33.32 -0.11 44.43 5.47 C 54.62 10.03 60.64 20.71 62.52 31.34 C 63.44 37.52 63.03 43.78 63.10 50.00 C 62.99 57.95 63.36 65.91 62.92 73.86 C 43.00 74.08 23.06 74.07 3.14 73.87 C 2.87 62.90 3.08 51.94 3.00 40.98 C 2.83 31.24 5.35 21.18 11.58 13.52 Z" /&gt;
			&lt;/svg&gt;
		</y:Resource>
		<!-- NOT GATE -->
		<y:Resource id="2">&lt;?xml version="1.0" encoding="UTF-8" ?&gt;
			&lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
			&lt;svg width="67pt" height="77pt" viewBox="0 0 67 77" version="1.1" xmlns="http://www.w3.org/2000/svg"&gt;
			&lt;path fill="#ffffff" d="M 0.00 0.00 L 67.00 0.00 L 67.00 77.00 L 0.00 77.00 L 0.00 0.00 Z" /&gt;
			&lt;path fill="#4b4b4b" d="M 2.01,74.92 33.11,2.71 64.25,75.15 Z" /&gt;
			&lt;path fill="#ffffff" d="M 4.17,73.42 33.17,5.61 62.27,73.64 Z" /&gt;
			&lt;/svg&gt;
		</y:Resource>
		<!-- XOR GATE -->
		<y:Resource id="3">&lt;?xml version="1.0" encoding="UTF-8" ?&gt;
			&lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
			&lt;svg width="60pt" height="62pt" viewBox="0 0 60 62" version="1.1" xmlns="http://www.w3.org/2000/svg"&gt;
		  	&lt;path fill="#ffffff" d=" M 0.00 0.00 L 60.00 0.00 L 60.00 62.00 L 0.00 62.00 L 0.00 0.00 Z" /&gt;
		  	&lt;path fill="#4b4b4b" d="M 6.10,38.74 C 7.91,23.39 16.68,8.43 30.24,1.28 42.48,6.90 50.58,19.54 53.77,32.65 56.26,41.56 55.34,50.96 55.42,60.15 43.71,54.36 30.24,53.84 17.60,55.97 c -4.12,0.64 -7.94,2.41 -11.76,4.07 0.021,-7.10 -0.26,-14.23 0.25,-21.31 z"/&gt;
		  	&lt;path fill="#ffffff" d="m 7.54,37.91 c 1.71,-14.09 9.94,-27.83 22.66,-34.39 11.49,5.16 19.10,16.77 22.10,28.80 2.33,8.21 1.47,16.82 1.55,25.25 -11.00,-5.31 -23.65,-5.79 -35.51,-3.83 -3.87,0.59 -7.46,2.21 -11.04,3.74 0.02,-6.52 -0.25,-13.06 0.24,-19.57 z" /&gt;
		  	&lt;path stroke="#4b4b4b" d="M 30.38,2.45 7.42,57.58 l 0,0" /&gt;
		  	&lt;path stroke="#4b4b4b" d="m 30.21,2.63 23.92,55.82 0,0" /&gt;
			&lt;/svg&gt;
		</y:Resource>
		<!-- VOTE GATE -->
		<y:Resource id="4">&lt;?xml version="1.0" encoding="UTF-8" ?&gt;
			&lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
			&lt;svg width="60pt" height="62pt" viewBox="0 0 60 62" version="1.1" xmlns="http://www.w3.org/2000/svg"&gt;
		  	&lt;path fill="#ffffff" d=" M 0.00 0.00 L 60.00 0.00 L 60.00 62.00 L 0.00 62.00 L 0.00 0.00 Z" /&gt;
		  	&lt;path fill="#4b4b4b" d=" M 6.09,38.74 C 7.91,23.39 16.68,8.43 30.24,1.28 42.48,6.90 50.58,19.55 53.77,32.65 56.26,41.59 55.34,50.96 55.42,60.15 43.71,54.36 30.24,53.84 17.60,55.97 c -4.12,0.64 -7.94,2.41 -11.76,4.07 0.02,-7.10 -0.26,-14.22 0.25,-21.31 z" /&gt;
		  	&lt;path fill="#ffffff" d="m 7.54,37.92 c 1.71,-14.09 9.94,-27.83 22.66,-34.39 11.49,5.16 19.10,16.77 22.10,28.80 2.33,8.21 1.47,16.82 1.55,25.25 -11.00,-5.31 -23.65,-5.79 -35.51,-3.83 -3.87,0.59 -7.46,2.21 -11.04,3.74 0.02,-6.52 -0.25,-13.06 0.24,-19.57 z" /&gt;
		  	&lt;text fill="#4b4b4b" x="14.42" y="41.57"&gt;
		    	&lt;tspan x="14.42" y="41.57"&gt;k/n&lt;/tspan&gt;
		   	&lt;/text&gt;
			&lt;/svg&gt;
		</y:Resource>
		<!-- PAND GATE -->
		<y:Resource id="5">&lt;?xml version="1.0" encoding="UTF-8" ?&gt;
			&lt;!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"&gt;
			&lt;svg width="67pt" height="77pt" viewBox="0 0 67 77" version="1.1"  xmlns="http://www.w3.org/2000/svg"&gt;
		  	&lt;path fill="#ffffff" d=" M 0.00 0.00 L 67.00 0.00 L 67.00 77.00 L 0.00 77.00 L 0.00 0.00 Z" /&gt;
		  	&lt;path fill="#4b4b4b" d=" M 13.70 9.68 C 22.13 1.14 36.35 -0.93 46.68 5.26 C 56.50 11.04 62.82 21.82 64.06 33.03 C 64.84 40.33 64.47 47.68 64.53 55.00 C 64.47 61.73 64.83 68.47 64.24 75.18 C 43.50 75.37 22.74 75.52 2.00 75.17 C 1.87 63.11 2.09 51.05 1.93 38.99 C 1.52 28.16 5.78 17.14 13.70 9.68 Z" /&gt;
			&lt;path fill="#ffffff" d=" M 11.58 13.52 C 19.07 3.87 33.32 -0.11 44.43 5.47 C 54.62 10.03 60.64 20.71 62.52 31.34 C 63.44 37.52 63.03 43.78 63.10 50.00 C 62.99 57.95 63.36 65.91 62.92 73.86 C 43.00 74.08 23.06 74.07 3.14 73.87 C 2.87 62.90 3.08 51.94 3.00 40.98 C 2.83 31.24 5.35 21.18 11.58 13.52 Z" /&gt;
			&lt;path stroke="#4b4b4b" d="M 33.057885,2.1453079 3.3296344,74.163554 l 0,0" /&gt;
			&lt;path stroke="#4b4b4b" d="m 33.309147,2.0198193 29.618819,72.4610417 0,0" /&gt;
			&lt;/svg&gt;
		</y:Resource>
	'''

	def private String createLabel(AParameterDef param) {
		var result = ""
		if (param!=null) {
			result = GeneratorHelper::encodeXML(param.name)
			if (param.getValue!=null) {
				result = result+"= "+getScaledValue(param.getValue.value)
			}
		}
		return result
	}
	
	def private String getScaledValue(String strValue) {
		var result = strValue
		val maxPrecision = 7
		val decimal = new BigDecimal(strValue)
		if (decimal.precision > maxPrecision) {
			// set US number format: decimalSeparator . and groupingSeparator ,
			val formatter = NumberFormat::getNumberInstance(Locale::US) as DecimalFormat
			formatter.applyPattern("0.000E0")
			result = formatter.format(decimal)
			// don't use scientific format for 'normal' numbers
			val leftDigits = decimal.precision - decimal.scale
			if (-3 <= leftDigits && leftDigits <= 5) {
				result = new BigDecimal(result).toPlainString
				//.setScale(maxPrecision, BigDecimal::ROUND_HALF_EVEN)
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
		val tokens = GeneratorHelper::getMetaDataValue(repository, optName).replace(' ', '').split(',')
		if (name != null) {
			for (token : tokens) {
				if (!token.empty && name.contains(token)) {
					return true
				}
			}
		}
		return false
	}
	
	def dispatch getElements(UseCaseRepository ur) {
		ur.usecases.map(u|u as AElement)
	}
	
	def dispatch getElements(ProjectRepository pr) {
		pr.projects.map(p|p as AElement)
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
}
