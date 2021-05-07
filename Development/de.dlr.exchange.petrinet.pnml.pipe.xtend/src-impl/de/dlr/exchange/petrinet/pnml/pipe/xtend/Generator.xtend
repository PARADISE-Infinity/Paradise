/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.petrinet.pnml.pipe.xtend

import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.petrinet.base.xtend.AGenerator
import de.dlr.premise.element.ARepository
import de.dlr.premise.element.Transition
import de.dlr.premise.element.Mode
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.element.StateMachine

class Generator extends AGenerator {

	override getFileExtension() {
		".pipe.pnml.xml"
	}

	override createFileHeader(ARepository repository, String charset) '''
		<?xml version="1.0" encoding="«charset»"?>
		<pnml>
		  <net id="«GeneratorHelper::encodeXML((repository as ProjectRepository).projects.get(0).name)»" type="P/T net">
		    <token id="Default" enabled="true" red="0" green="0" blue="0"/>
	'''

	override createFileFooter() '''
		  </net>
		</pnml>
	'''

	override createPlace(Mode mode) '''
		«var modeLabel = GeneratorHelper::encodeXML(mode.getLabel)»
		<place id="«modeLabel»">
		  <graphics>
		    <position x="«coordX».0" y="«coordY».0"/>
		  </graphics>
		  <name>
		    <value>«modeLabel»</value>
		    <graphics>
		      <offset x="«modeLabel.length * 4».0" y="-10.0"/>
		    </graphics>
		  </name>
		  <initialMarking>
		    <value>Default,«mode.defaultTokens»</value>
		    <graphics>
		      <offset x="0.0" y="0.0"/>
		    </graphics>
		  </initialMarking>
		  <capacity>
		    <value>1</value>
		  </capacity>
		</place>
	'''

	override createTransition(Transition trans, int priority) {
		val result = new StringBuilder()
		val transLabel = GeneratorHelper::encodeXML(trans.getLabel)
		result.append('''
			<transition id="«transLabel»">
			  <graphics>
			    <position x="«coordX».0" y="«coordY».0"/>
			  </graphics>
			  <name>
			    <value>«transLabel»</value>
			    <graphics>
			      <offset x="-5.0" y="35.0"/>
			    </graphics>
			  </name>
			  <orientation>
			    <value>0</value>
			  </orientation>
			  <rate>
			    <value>1.0</value>
			  </rate>
			  <timed>
			    <value>false</value>
			  </timed>
			  <infiniteServer>
			    <value>false</value>
			  </infiniteServer>
			  <priority>
			    <value>«priority»</value>
			  </priority>
			</transition>
		''')
		if (trans.source != null) {
			result.append(createEdge(trans.getSource, trans))
			result.append(createEdge(trans, trans.getTarget))	
		} else {
			for (Mode mode :  (trans.eContainer as StateMachine).modes) {
				if (mode != trans.target) {
					result.append(createEdge(mode, trans))
					result.append(createEdge(trans, trans.getTarget))	
				}
			}
		}
		return result
	}

	override createEdge(ANameItem source, ANameItem dest) '''
		«val srcLabel = GeneratorHelper::encodeXML(source.getLabel)»
		«val destLabel = GeneratorHelper::encodeXML(dest.getLabel)»
		<arc id="«srcLabel» to «destLabel»" source="«srcLabel»" target="«destLabel»">
		  <graphics/>
		  <inscription>
		    <value>Default,1</value>
		    <graphics/>
		  </inscription>
		  <tagged>
		    <value>false</value>
		  </tagged>
		  <arcpath id="000" x="0.0" y="0.0" curvePoint="false"/>
		  <arcpath id="001" x="0.0" y="0.0" curvePoint="false"/>
		  <type value="normal"/>
		</arc>
	'''
}
