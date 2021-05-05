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
import org.w3c.dom.Element

import static extension de.dlr.premise.migration.util.MigrationHelper.*

class MigrateV121ToV122 extends AMigration {
	
	override protected migrateRoot(MigrationModel model, Element modelRoot) {
		var needsNamespaces = false
		
		
		val satisfiesElement = modelRoot.getElementsByTagName("satisfies").listFromNodeList.filter(Element)
		for (satisfies : satisfiesElement) {
			val newSatisfies = satisfies.ownerDocument.parse('''
				<satisfies>
					<targetPointer xsi:type="comp:DirectPointer">
						<target xsi:type="«satisfies.getAttribute("xsi:type")»" href="«satisfies.getAttribute("href")»"/>
					</targetPointer>
				</satisfies>
			''')
			satisfies.parentNode.replaceChild(newSatisfies, satisfies)
			
			model.setChange()
			needsNamespaces = true
		}
		
		
		val haveSatisfiesAttribute = modelRoot.getElementsByTagName("*").listFromNodeList.filter(Element).filter[getAttribute("satisfies") != ""]
		for (hasSatisfiesAttribute : haveSatisfiesAttribute) {
			val satisfiedIds = hasSatisfiesAttribute.getAttribute("satisfies").split(" ")
			
			
			
			for (satisfiedId : satisfiedIds) {
				val newSatisfies = hasSatisfiesAttribute.ownerDocument.parse('''
					<satisfies>
						<targetPointer xsi:type="comp:DirectPointer" target="«satisfiedId»"/>
					</satisfies>
				''')
				hasSatisfiesAttribute.appendChild(newSatisfies)
			}
			
			hasSatisfiesAttribute.removeAttribute("satisfies")
			model.setChange()
			needsNamespaces = true
		}
		
		
		val transitionElement = modelRoot.getElementsByTagName("transitions").listFromNodeList.filter(Element)
		for (transition : transitionElement) {
			val sourceId = transition.getAttribute("source")
			val targetId = transition.getAttribute("target")
			
			if (sourceId != "") {
				transition.appendChild(transition.ownerDocument.parse('''
					<sourcePointer xsi:type="comp:DirectPointer" target="«sourceId»"/>
				'''))
				transition.removeAttribute("source")
			}
			if (targetId != "") {
				transition.appendChild(transition.ownerDocument.parse('''
					<targetPointer xsi:type="comp:DirectPointer" target="«targetId»"/>
				'''))
				transition.removeAttribute("target")
			}
			
			model.setChange()
			needsNamespaces = true
		}
		
		if (needsNamespaces) {
			modelRoot.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
			modelRoot.setAttribute("xmlns:comp", "http://www.dlr.de/premise/component/2016/")
		}
	}
	
	override getTargetVersion() {
		ModelVersion.V122.toString
	}
	
}