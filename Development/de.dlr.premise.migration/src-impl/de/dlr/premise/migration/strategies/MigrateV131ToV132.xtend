/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.migration.strategies

import de.dlr.premise.migration.MigrationModel
import de.dlr.premise.migration.ModelVersion
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import org.w3c.dom.Element

import static extension de.dlr.premise.migration.util.MigrationHelper.*
import org.w3c.dom.NodeList

/**
 * Convert UseCase.condition from list of modes to ModeGuard
 */
class MigrateV131ToV132 extends AMigration{
	
	override protected migrateRoot(MigrationModel model, Element modelRoot) {
		var localChange = false
		
		val conditionElements = modelRoot.getElementsByTagName("condition").listFromNodeList.filter(Element).toSet
		val conditionElementUseCases = conditionElements.map[parentNode].filter(Element).filter[tagName == "usecases" || tagName == "children"].toSet
	
		for (uc : conditionElementUseCases) {
			val conditions = uc.childNodes.listFromNodeList.filter(Element).filter[tagName == "condition"]
			val newElement = if (conditions.size == 1) {
				'''
			      <condition xsi:type="elem:ModeGuard">
			        <modePointer xsi:type="graph:DirectPointer">
			          <target xsi:type="elem:Mode" href="«conditions.head.getAttribute("href")»"/>
			        </modePointer>
			      </condition>
				'''
			} else {
				'''
					<condition xsi:type="elem:GuardCombination" junction="AND">
					  «FOR mode : conditions»
					    <children xsi:type="elem:ModeGuard">
					      <modePointer xsi:type="graph:DirectPointer">
					        <target xsi:type="elem:Mode" href="«mode.getAttribute("href")»"/>
					      </modePointer>
					    </children>
					  «ENDFOR»
					</condition>
				'''
			}
			conditions.forEach[uc.removeChild(it)]
			uc.appendChild(uc.ownerDocument.parse(newElement))
			localChange = true
		}
		
		val conditionAttributeUseCases = (XPathFactory.newInstance().newXPath().evaluate('//*[@condition]', modelRoot, XPathConstants.NODESET) as NodeList).listFromNodeList.filter(Element)
		for (uc : conditionAttributeUseCases) {
			val conditions = uc.getAttribute("condition").split(" ").toList
			val newElement = if (conditions.size == 1) {
				'''
			      <condition xsi:type="elem:ModeGuard">
			        <modePointer xsi:type="graph:DirectPointer" target="«conditions.head»" />
			      </condition>
				'''
			} else {
				'''
					<condition xsi:type="elem:GuardCombination" junction="AND">
					  «FOR mode : conditions»
					    <children xsi:type="elem:ModeGuard">
					      <modePointer xsi:type="graph:DirectPointer" target="«mode»" />
					    </children>
					  «ENDFOR»
					</condition>
				'''
			}
			uc.removeAttribute("condition")
			uc.appendChild(uc.ownerDocument.parse(newElement))
			localChange = true
		}

		if (localChange) {
			model.setChange
			modelRoot.setAttribute("xmlns:elem", "http://www.dlr.de/premise/element/2014/")
		}
	}
		
	override getTargetVersion() {
		return ModelVersion.V132.toString
	}
	
}