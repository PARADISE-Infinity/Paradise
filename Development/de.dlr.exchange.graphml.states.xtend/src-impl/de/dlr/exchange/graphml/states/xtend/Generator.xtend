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
import de.dlr.exchange.base.xtend.IGeneratorMyWithProgress
import de.dlr.premise.element.AElement
import de.dlr.premise.element.AGuardCondition
import de.dlr.premise.element.ARepository
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.element.GuardCombination
import de.dlr.premise.element.Mode
import de.dlr.premise.element.ModeGuard
import de.dlr.premise.element.StateMachine
import de.dlr.premise.element.Transition
import de.dlr.premise.registry.ADataItem
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.functions.UseCase
import de.dlr.premise.functions.UseCaseRepository
import de.dlr.premise.util.PremiseHelper
import de.dlr.premise.util.scope.ScopedEObjectFactory
import java.util.ArrayList
import java.util.LinkedHashSet
import java.util.List
import java.util.Set
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.generator.IFileSystemAccess

import static extension de.dlr.exchange.base.xtend.GeneratorHelper.getMetaDataValue
import static extension de.dlr.premise.util.PremiseHelper.*

class Generator implements IGeneratorMyWithProgress {

	public static final String OPT_TECH = "GraphML/StateMachines"
	public static final String OPT_COLOR_GREY_VAL = "#AAAAAA"
	public static final String OPT_COLOR_GREY_NAME = "Color for referenced foreign modes and edges (default grey: "+OPT_COLOR_GREY_VAL+")"
	public static final String OPT_CONSTRAINTS_NAME = "Transition constraints"
	public static final String OPT_CONSTRAINTS_VAL = "on"
	public static final String OPT_EDGE_REF_NAME = "Links between referenced foreign mode and their origin"
	public static final String OPT_EDGE_REF_VAL = "off"
	public static final String OPT_GROUP_REF_NAME = "Links between groups containing foreign modes"
	public static final String OPT_GROUP_REF_VAL = "on"
	public static final String OPT_CALC_STATES_NAME = "Calculation of states"	
	public static final String OPT_CALC_STATES_VAL = "on"
	public static final String OPT_CALC_TREE_NAME = "Calculation of state tree"	
	public static final String OPT_CALC_TREE_VAL = "on"

	private static ARepository repository
	private GeneratorTree genTree = new GeneratorTree();
	
	private Set<StateMachine> stateMachines
	
	override doGenerateFromAElements(ResourceSet resSet, List<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa, IProgressMonitor monitor) {
		if(!resSet.resources.nullOrEmpty && resSet.resources.get(0).contents.get(0) instanceof ARepository){
			if(!selectedElements.nullOrEmpty){
				selectedElements.compile(fsa, monitor)
			} else {
				println("No elements were found to export.")
			}
		}
	}
	
	override doGenerateFromResources(ResourceSet resSet, List<Resource> selectedFiles, ICharsetProvidingFileSystemAccess fsa, IProgressMonitor monitor) {
		// create selectedElements list from ARepositories
		var selectedElements = selectedFiles.flatMap[contents].filter(ARepository).flatMap[elements].toList

		this.doGenerateFromAElements(resSet, selectedElements, fsa, monitor)
	}

	def dispatch void compile(ArrayList<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa, IProgressMonitor monitor) {
		GeneratorHelper::clearRefs
		repository = PremiseHelper::getRoot(selectedElements.get(0)) as ARepository
		val sc1Name = selectedElements.get(0).name
		
		stateMachines = selectedElements.map[ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(it)].closure[children].flatMap[statemachines].toSet

		val fileName = GeneratorHelper::encodeFileName(sc1Name)+".statemachines.graphml"
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
			«FOR sm : stateMachines»
				«sm.createStateMachine()»
			«ENDFOR»
			  </graph>
			  <data key="d0">
			    <y:Resources/>
			  </data>
			</graphml>
		''')

		// create also the states tree file
		if (repository.isSet(OPT_CALC_TREE_NAME, OPT_CALC_TREE_VAL)) {
			genTree.compile(selectedElements, fsa, monitor)
		}		
	}

	def dispatch void compile(EObject m, IFileSystemAccess fsa, IProgressMonitor monitor) {
		println("<!-- Should not be printed! -->")
	}

	def CharSequence createStateMachine(StateMachine sm) '''
		«sm.createGroup()»
		«IF PremiseHelper::isSet(repository, OPT_CONSTRAINTS_NAME) && PremiseHelper::isSet(repository, OPT_EDGE_REF_NAME, OPT_EDGE_REF_VAL)»
			«sm.createReferenceEdges()»
		«ENDIF»
		«IF PremiseHelper::isSet(repository, OPT_GROUP_REF_NAME)»
			«sm.createGroupEdges()»
		«ENDIF»
	'''	
	
	def createGroup(StateMachine el) '''
	    <node id="n«GeneratorHelper::getRef(el)»" yfiles.foldertype="group">
	      <data key="d6">
	        <y:ProxyAutoBoundsNode>
	          <y:Realizers active="0">
	            <y:GenericGroupNode configuration="com.yworks.bpmn.Artifact.withShadow">
	              <y:Geometry height="0.0" width="0.0" x="0.0" y="0.0"/>
	              <y:Fill color="#FFFFFFE6" transparent="false"/>
	              <y:BorderStyle color="#000000" type="line" width="1.0"/>
	    <y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="0.0" modelName="internal" modelPosition="b" textColor="#000000" visible="true" width="0.0" x="0.0" y="0.0">«GeneratorHelper::encodeXML(el.name)»</y:NodeLabel>
	              <y:StyleProperties>
	                <y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="ARTIFACT_TYPE_GROUP"/>
	                <y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.line.color" value="#000000"/>
	                <y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill" value="#ffffff"/>
	                <y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill2" value="#d4d4d4"/>
	              </y:StyleProperties>
	              <y:State autoResize="true" closed="false" closedHeight="80.0" closedWidth="100.0"/>
	              <y:Insets bottom="15" bottomF="15.0" left="15" leftF="15.0" right="15" rightF="15.0" top="15" topF="15.0"/>
	              <y:BorderInsets bottom="1" bottomF="1.000037202380895" left="0" leftF="0.0" right="0" rightF="0.0" top="1" topF="1.0"/>
	            </y:GenericGroupNode>
	          </y:Realizers>
	        </y:ProxyAutoBoundsNode>
	      </data>
	      <graph edgedefault="directed" id="n0:">
	    «el.createGroupContent()»
	      </graph>
	    </node>
	'''

	def CharSequence createGroupContent(StateMachine el) {
		var result = new StringBuilder()
		for (mode : el.modes) {
			result.append(mode.createState())
		}
		for (trans : el.transitions) {
			// normal transition without constraint
			if (trans.getCondition==null || !PremiseHelper::isSet(repository, OPT_CONSTRAINTS_NAME)) {
				if (trans.getSource!=null) {
					result.append(el.createEdgeTransition(trans.getSource, trans.getTarget, true))
				} else {
					// transition.source may be null (transition from all local modes intended)
					for (Mode mode : (trans.eContainer as StateMachine).modes) {
						if (mode != trans.getTarget) {
							result.append(el.createEdgeTransition(mode, trans.getTarget, true))
						}
					}
				}
			} else {
				// transition with constraint
				if (trans.getSource!=null) {
					result.append(el.createConnector(trans, "#000000"))
					// create edge(s) with connector in the middle
					result.append(el.createEdgeTransition(trans.getSource, trans, false))
					result.append(el.createEdgeTransition(trans, trans.getTarget, true))
				} else {
					// transition.source is null (transition from all local modes intended)
					for (Mode mode : (trans.eContainer as StateMachine).modes) {
						if (mode != trans.getTarget) {
							// create edge(s) with connector in the middle
							result.append(el.createConnector(trans, "#000000"))
							result.append(el.createEdgeTransition(mode, trans, false))
							result.append(el.createEdgeTransition(trans, trans.getTarget, true))
						} else if ((trans.eContainer as StateMachine).modes.size == 1) {
							// only one mode defined in component, create edge from connector to mode
							result.append(el.createConnector(trans, "#000000"))
							result.append(el.createEdgeTransition(trans, trans.getTarget, true))
						}
					}
				}
				// create hierarchical edges for transition constraints
				result.append(el.traverseGuardConditions(trans, trans.getCondition, 0))
			}
		}
		return result
	}

	def CharSequence traverseGuardConditions(StateMachine el, Transition connector, AGuardCondition condition, double depth) {
		var result = new StringBuilder()
		if (condition instanceof ModeGuard) {
			val triggerMode = (condition as ModeGuard).getMode
			result.append(el.createStateRef(triggerMode))
			result.append(el.createEdgeCondition(triggerMode, connector))
		} else if (condition instanceof GuardCombination) {
			// copy connector to allow renewing id
			var nestedConnector = ElementFactory::eINSTANCE.createTransition // EcoreUtil::copy(connector) 
			nestedConnector.id = connector.id
			if (depth >= 1) {
				// create nested connector with id of incoming connector + depth of GuardCombination (to allow multiple connectors)
				nestedConnector.id = nestedConnector.id + depth
				nestedConnector.condition = EcoreUtil::copy(condition) // needed for creation of correct label
				result.append(el.createConnector(nestedConnector, repository.getMetaDataValue(OPT_COLOR_GREY_NAME)))
				result.append(el.createEdgeCondition(nestedConnector, connector))
			}
			// vary depth for every iteration to enable different connectors 
			var dep = depth + 1
			for (child : (condition as GuardCombination).children) {
				dep = dep + 0.1
				result.append(el.traverseGuardConditions(nestedConnector, child, dep))
			}
		}
		return result
	}

	def CharSequence createReferenceEdges(StateMachine el) {
		var result = new StringBuilder()
		val contents = el.eAllContents.filter(ModeGuard).toSet()

		// eliminate duplicates
		var handled = new LinkedHashSet<Mode>();
		for (element : contents) {
			val mode = element.mode
			if (!handled.contains(mode)) {
				result.append(el.createEdgeReference(mode, mode))
				handled.add(mode)
			}
		}
		return result
	}

	def CharSequence createGroupEdges(StateMachine el) {
		var result = new StringBuilder()
		val contents = el.eAllContents.filter(ModeGuard).toSet()

		// to allow elimination of duplicates
		var handled = new LinkedHashSet<StateMachine>()
		for (element : contents) {
			// get parent component of transition constraint
			var constraintParent = element.eContainer()
			while (constraintParent!=null && !(constraintParent instanceof StateMachine)) {
				constraintParent = constraintParent.eContainer()
			}
			if (constraintParent == el) {
				// get parent component of trigger
				var triggerParent = element.getMode.eContainer
				while (triggerParent!=null && !(triggerParent instanceof StateMachine)) {
					triggerParent = triggerParent.eContainer()
				}
				if (stateMachines.contains(triggerParent) && !handled.contains(triggerParent)) {
					val triggerSC = triggerParent as StateMachine
					result.append(el.createEdgeGroup(triggerSC, el))
					handled.add(triggerSC)
				}
			}
		}
		return result
	}

	def createState(Mode mode) '''
        <node id="n«GeneratorHelper::getRef(mode.eContainer)»::n«GeneratorHelper::getRef(mode)»">
          <data key="d6">
            <y:GenericNode configuration="com.yworks.bpmn.Event.withShadow">
              <y:Geometry height="30.0" width="30.0" x="0.0" y="0.0"/>
              <y:Fill color="#FFFFFFE6" color2="#D4D4D4CC" transparent="false"/>
              <y:BorderStyle color="#DCBA00" type="line" width="1.0"/>
        <y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="0.0" modelName="eight_pos" modelPosition="s" textColor="#000000" visible="true" width="0.0" x="0.0" y="0.0">«GeneratorHelper::encodeXML(mode.name)»</y:NodeLabel>
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

	def createStateRef(StateMachine el, Mode mode) '''
        <node id="n«GeneratorHelper::getRef(el)»::n«GeneratorHelper::getRef(mode)»">
          <data key="d6">
            <y:GenericNode configuration="com.yworks.bpmn.Event">
              <y:Geometry height="30.0" width="30.0" x="0.0" y="0.0"/>
              <y:Fill color="#FFFFFFE6" color2="#FFFFFF«/* #D4D4D4CC */»" transparent="false"/>
              <y:BorderStyle color="«repository.getMetaDataValue(OPT_COLOR_GREY_NAME) /* #DCBA00 */»" type="line" width="1.0"/>
        <y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="0.0" modelName="eight_pos" modelPosition="s" textColor="«repository.getMetaDataValue(OPT_COLOR_GREY_NAME)»" visible="true" width="0.0" x="0.0" y="0.0">«GeneratorHelper::encodeXML(mode.name)»</y:NodeLabel>
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

	def createConnector(StateMachine el, Transition connector, String color) {
		var label = null as String
		if (connector.getCondition instanceof GuardCombination) {
			label = (connector.getCondition as GuardCombination).getJunction.literal
		}
		'''
		<node id="n«GeneratorHelper::getRef(el)»::n«GeneratorHelper::getRef(connector)»">
		  <data key="d5"/>
		  <data key="d6">
		    <y:GenericNode configuration="com.yworks.bpmn.Gateway">
		      <y:Geometry height="7.0" width="7.0" x="0.0" y="0.0"/>
		      <y:Fill color="#FFFFFFE6" color2="#D4D4D4CC" transparent="false"/>
		      <y:BorderStyle color="«color /* #E38B00 */»" type="line" width="1.0"/>
		      <y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" «IF label==null»hasText="false" «ENDIF»height="0.0" modelName="eight_pos" modelPosition="s" textColor="«color»" visible="true" width="0.0" x="0.0" y="0.0"«IF label==null»/>«ELSE»>«label»</y:NodeLabel>«ENDIF»
		      <y:StyleProperties>
		        <y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="GATEWAY_TYPE_PLAIN"/>
		        <y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.line.color" value="#000000"/>
		        <y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill" value="#ffffff"/>
		        <y:Property class="java.awt.Color" name="com.yworks.bpmn.icon.fill2" value="#d4d4d4"/>
		      </y:StyleProperties>
		    </y:GenericNode>
		  </data>
		</node>
		'''
	}

	def createEdgeTransition(StateMachine el, ADataItem source, ADataItem target, boolean arrow) {
		val id = GeneratorHelper::getRef(el)
		// check if id's are available, that means if modes are valid children of sc
		if (source?.eContainer == el && target?.eContainer == el) {
			'''
			<edge id="n«id»::e«GeneratorHelper::getEdgeID»" source="n«id»::n«GeneratorHelper::getRef(source)»" target="n«id»::n«GeneratorHelper::getRef(target)»">
			  <data key="d10">
			    <y:GenericEdge configuration="com.yworks.bpmn.Connection">
			      <y:Path sx="15.0" sy="-0.0" tx="-15.0" ty="-0.0"/>
			      <y:LineStyle color="#000000" type="line" width="1.0"/>
			      <y:Arrows source="none" target="«IF arrow»short«ELSE»none«ENDIF»"/>
			      <y:StyleProperties>
			        <y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="CONNECTION_TYPE_SEQUENCE_FLOW"/>
			      </y:StyleProperties>
			    </y:GenericEdge>
			  </data>
			</edge>
			'''
		} else {
			""
		}
	}

	def createEdgeCondition(StateMachine el, ADataItem source, ADataItem target) {
		val id = GeneratorHelper::getRef(el)
		'''
		<edge id="e«GeneratorHelper::getEdgeID»" source="n«id»::n«GeneratorHelper::getRef(source)»" target="n«id»::n«GeneratorHelper::getRef(target)»">
		  <data key="d10">
		    <y:GenericEdge configuration="com.yworks.bpmn.Connection">
		      <y:Path sx="15.0" sy="-0.0" tx="-15.0" ty="-0.0"/>
		      <y:LineStyle color="«repository.getMetaDataValue(OPT_COLOR_GREY_NAME)»" type="dashed" width="1.0"/>«/* changed color and type */»
		      <y:Arrows source="none" target="short"/>
		      <y:StyleProperties>
		        <y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="CONNECTION_TYPE_SEQUENCE_FLOW"/>
		      </y:StyleProperties>
		    </y:GenericEdge>
		  </data>
		</edge>
		'''
	}

	def createEdgeReference(StateMachine el, Mode source, Mode targetRef) '''
		<edge id="e«GeneratorHelper::getEdgeID»" source="n«GeneratorHelper::getRef(source.eContainer)»::n«GeneratorHelper::getRef(source)»" target="n«GeneratorHelper::getRef(el)»::n«GeneratorHelper::getRef(targetRef)»">
		  <data key="d10">
		    <y:GenericEdge configuration="com.yworks.bpmn.Connection">
		      <y:Path sx="15.0" sy="-0.0" tx="-15.0" ty="-0.0"/>
		      <y:LineStyle color="«repository.getMetaDataValue(OPT_COLOR_GREY_NAME)»" type="dashed" width="1.0"/>«/* changed color and type */»
		      <y:Arrows source="none" target="none"/>«/* changed arrow */»
		      <y:StyleProperties>
		        <y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="CONNECTION_TYPE_SEQUENCE_FLOW"/>
		      </y:StyleProperties>
		    </y:GenericEdge>
		  </data>
		</edge>
	'''

	def createEdgeGroup(StateMachine el, ADataItem source, ADataItem targetRef) '''
		<edge id="e«GeneratorHelper::getEdgeID»" source="n«GeneratorHelper::getRef(source)»" target="n«GeneratorHelper::getRef(targetRef)»">
		  <data key="d10">
		    <y:GenericEdge configuration="com.yworks.bpmn.Connection">
		      <y:Path sx="15.0" sy="-0.0" tx="-15.0" ty="-0.0"/>
		      <y:LineStyle color="#000000" type="dashed" width="1.0"/>«/* changed type */»
		      <y:Arrows source="none" target="short"/>
		      <y:StyleProperties>
		        <y:Property class="com.yworks.yfiles.bpmn.view.BPMNTypeEnum" name="com.yworks.bpmn.type" value="CONNECTION_TYPE_SEQUENCE_FLOW"/>
		      </y:StyleProperties>
		    </y:GenericEdge>
		  </data>
		</edge>
	'''
	
	def dispatch getElements(ProjectRepository pr) {
		pr.projects.map(p|p as AElement)
	}
	
	def dispatch getElements(UseCaseRepository ur) {
		ur.usecases.map(u|u as AElement)
	}
	
	def EList<? extends AElement> getChildren(AElement el) {
		if (el instanceof SystemComponent) {
			return (el as SystemComponent).referencedChildren
		} else if (el instanceof UseCase) {
			return (el as UseCase).children
		}
	}
}
