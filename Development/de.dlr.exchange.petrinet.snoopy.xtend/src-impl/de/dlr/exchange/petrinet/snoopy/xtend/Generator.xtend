/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.petrinet.snoopy.xtend

import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.petrinet.base.xtend.AGenerator
import de.dlr.premise.element.ARepository
import de.dlr.premise.element.Transition
import de.dlr.premise.element.Mode
import java.util.List
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.element.StateMachine

class Generator extends AGenerator {

	private List<String> places
	private List<String> transitions
	private List<String> edges


	override reset() {
		super.reset()
		places = newArrayList
		transitions = newArrayList
		edges = newArrayList
	}

	override getFileExtension() {
		".snoopy.spped"
	}

	override createFileHeader(ARepository repository, String charset) '''
		<?xml version="1.0" encoding="«charset»"?>
		<?xml-stylesheet type="text/xsl" href="/xsl/spped2svg.xsl"?>
		<Snoopy version="2" revision="1.21">
		  <netclass name="Petri Net"/>
	'''

	override createFileFooter() {
		var result = new StringBuilder()
		result.append('''
			<nodeclasses count="«(places.size + transitions.size)»">
			  <nodeclass count="«places.size»" name="Place">
		''')
		for (place : places) {
			result.append(place)
		}
		result.append('''
			  </nodeclass>
			  <nodeclass count="«transitions.size»" name="Transition">
		''')
		for (trans : transitions) {
			result.append(trans)
		}
		result.append('''
			  </nodeclass>
			  <nodeclass count="0" name="Coarse Place"/>
			  <nodeclass count="0" name="Coarse Transition"/>
			</nodeclasses>
		''')
		result.append('''
			<edgeclasses count="1">
			  <edgeclass count="«edges.size»" name="Edge">
		''')
		for (edge : edges) {
			result.append(edge)
		}
		result.append('''
			  </edgeclass>
			</edgeclasses>
		''')
		result.append('''
			  <metadataclasses count="2">
			    <metadataclass count="1" name="General">
			      <metadata id="30000" net="1">
			        <attribute name="Name" id="31000" net="1">
			          <![CDATA[]]>
			          <graphics count="1">
			            <graphic xoff="3.00" x="20.00" id="32000" net="1" show="1" grparent="50000" state="1"/>
			          </graphics>
			        </attribute>
			        <attribute name="Created" id="33000" net="1">
			          <![CDATA[2013-02-21 09:39:49]]>
			          <graphics count="1">
			            <graphic xoff="25.00" yoff="20.00" id="34000" net="1" show="0" grparent="50000" state="1"/>
			          </graphics>
			        </attribute>
			        <attribute name="Authors" id="35000" net="1">
			          <![CDATA[]]>
			          <graphics count="1">
			            <graphic xoff="25.00" yoff="40.00" id="36000" net="1" show="1" grparent="50000" state="1"/>
			          </graphics>
			        </attribute>
			        <attribute name="Keywords" id="37000" net="1">
			          <![CDATA[]]>
			          <graphics count="1">
			            <graphic xoff="40.00" yoff="25.00" id="38000" net="1" show="1" grparent="50000" state="1"/>
			          </graphics>
			        </attribute>
			        <attribute name="Description" id="39000" net="1">
			          <![CDATA[]]>
			          <graphics count="1">
			            <graphic xoff="25.00" yoff="40.00" id="40000" net="1" show="1" grparent="50000" state="1"/>
			          </graphics>
			        </attribute>
			        <attribute name="References" id="41000" net="1">
			          <![CDATA[]]>
			          <graphics count="1">
			            <graphic xoff="25.00" yoff="40.00" id="42000" net="1" show="1" grparent="50000" state="1"/>
			          </graphics>
			        </attribute>
			        <graphics count="1">
			          <graphic x="17.00" y="20.00" id="50000" net="1" show="1" w="15.00" h="24.00" state="1" pen="255,255,255"/>
			        </graphics>
			      </metadata>
			    </metadataclass>
			    <metadataclass count="0" name="Comment"/>
			  </metadataclasses>
			</Snoopy>
		''')
	}
	
	override createPlace(Mode mode) {
		var result = new StringBuilder()
		val id = Integer::valueOf(GeneratorHelper::getRef(mode))
		var greyColorStr = ""
		if (mode.name.nullOrEmpty) {
			greyColorStr = ' pen="176,176,176"'
		}
		result.append('''
		    <node id="«id»" net="1">
		      <attribute name="Name" id="«(id+1000)»" net="1">
		        <![CDATA[«GeneratorHelper::encodeXML(mode.getLabel)»]]>
		        <graphics count="1">
		          <graphic xoff="-5.00" yoff="-23.00" id="«(id+2000)»" net="1" show="«IF greyColorStr.nullOrEmpty»1«ELSE»0«ENDIF»" grparent="«(id+10000)»" state="1"/>
		        </graphics>
		      </attribute>
		      <attribute name="ID" id="«(id+3000)»" net="1">
		        <![CDATA[0]]>
		        <graphics count="1">
		          <graphic xoff="25.00" yoff="20.00" id="«(id+4000)»" net="1" show="0" grparent="«(id+10000)»" state="1"/>
		        </graphics>
		      </attribute>
		      <attribute name="Marking" id="«(id+5000)»" net="1">
		        <![CDATA[«mode.getDefaultTokens»]]>
		        <graphics count="1">
		          <graphic id="«(id+6000)»" net="1" show="1" grparent="«(id+10000)»" state="1"/>
		        </graphics>
		      </attribute>
		      <attribute name="Logic" id="«(id+7000)»" net="1">
		        <![CDATA[0]]>
		        <graphics count="0"/>
		      </attribute>
		      <attribute name="Comment" id="«(id+8000)»" net="1">
		        <![CDATA[]]>
		        <graphics count="1">
		          <graphic yoff="40.00" id="«(id+9000)»" net="1" show="1" grparent="«(id+10000)»" state="1"/>
		        </graphics>
		      </attribute>
		      <graphics count="1">
		        <graphic x="«coordX».0" y="«coordY».0" id="«(id+10000)»" net="1" show="1" w="20.00" h="20.00" state="1"«greyColorStr»/>
		      </graphics>
		    </node>
		''')
		places.add(result.toString)
		return ""
	}

	override createTransition(Transition trans, int priority) {
		var result = new StringBuilder()
		val id = Integer::valueOf(GeneratorHelper::getRef(trans))
		var greyColorStr = ""
		if (priority == lowPrio) {
			greyColorStr = ' pen="176,176,176"'
		}
		result.append('''
		    <node id="«id»" net="1">
		      <attribute name="Name" id="«(id+1000)»" net="1">
		        <![CDATA[T«id»]]>
		        <graphics count="1">
		          <graphic xoff="3.0" yoff="0.0" id="«(id+2000)»" net="1" show="0" grparent="«(id+10000)»" state="1"/>
		        </graphics>
		      </attribute>
		      <attribute name="ID" id="«(id+3000)»" net="1">
		        <![CDATA[1]]>
		        <graphics count="1">
		          <graphic xoff="25.00" yoff="20.00" id="«(id+4000)»" net="1" show="0" grparent="«(id+10000)»" state="1"/>
		        </graphics>
		      </attribute>
		      <attribute name="Logic" id="«(id+5000)»" net="1">
		        <![CDATA[0]]>
		        <graphics count="0"/>
		      </attribute>
		      <attribute name="Comment" id="«(id+6000)»" net="1">
		        <![CDATA[]]>
		        <graphics count="1">
		          <graphic xoff="40.00" id="«(id+7000)»" net="1" show="1" grparent="«(id+10000)»" state="1"/>
		        </graphics>
		      </attribute>
		      <graphics count="1">
		        <graphic x="«coordX».0" y="«coordY».0" id="«(id+10000)»" net="1" show="1" w="20.00" h="20.00" state="1"«greyColorStr»/>
		      </graphics>
		    </node>
		''')
		if (trans.source != null) {
			transitions.add(result.toString)
			createEdge(trans.getSource, trans)
			createEdge(trans, trans.getTarget)	
		} else {
			for (Mode mode :  (trans.eContainer as StateMachine).modes) {
				if (mode != trans.target) {
					transitions.add(result.toString)
					createEdge(mode, trans)
					createEdge(trans, trans.getTarget)	
				}
			}
		}
		return ""
	}

	override createEdge(ANameItem source, ANameItem dest) {
		var result = new StringBuilder()
		val id = Integer::valueOf(GeneratorHelper::edgeID) + 20000
		val srcGraphId = Integer::valueOf(GeneratorHelper::getRef(source)) + 10000
		val destGraphId = Integer::valueOf(GeneratorHelper::getRef(dest)) + 10000
		var greyColorStr = ""
		if (source.eContainer==null || source.eContainer!=dest.eContainer) { 
			greyColorStr = ' pen="176,176,176" brush="176,176,176"'
		}
		result.append('''
		    <edge source="«GeneratorHelper::getRef(source)»" target="«GeneratorHelper::getRef(dest)»" id="«id»" net="1">
		      <attribute name="Multiplicity" id="«(id+1000)»" net="1">
		        <![CDATA[1]]>
		        <graphics count="1">
		          <graphic xoff="20.00" id="«(id+2000)»" net="1" show="1" grparent="«(id+10000)»" state="1"/>
		        </graphics>
		      </attribute>
		      <attribute name="Comment" id="«(id+3000)»" net="1">
		        <![CDATA[]]>
		        <graphics count="1">
		          <graphic xoff="40.00" id="«(id+4000)»" net="1" show="1" grparent="«(id+10000)»" state="1"/>
		        </graphics>
		      </attribute>
		      <graphics count="1">
		        <graphic id="«(id+10000)»" net="1" source="«srcGraphId»" target="«destGraphId»" state="1" show="1"«greyColorStr» edge_designtype="3">
		          <points count="2">
		            <point x="0.00" y="0.0"/>
		            <point x="0.00" y="0.0"/>
		          </points>
		        </graphic>
		      </graphics>
		    </edge>
		''')
		edges.add(result.toString)
		return ""
	}
}
