/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.migration.strategies

import de.dlr.premise.migration.MigrationModel
import de.dlr.premise.migration.ModelVersion
import org.w3c.dom.Element

import static extension de.dlr.premise.migration.util.MigrationHelper.*

class MigrateV120ToV121 extends AMigration {
	
	boolean needsCompNamespace
	boolean needsXsiNamespace
	
	override protected migrateRoot(MigrationModel model, Element modelRoot) {
		needsXsiNamespace = needsCompNamespace = false
		
		val connectionsAndRelations = (modelRoot.getElementsByTagName("connections").listFromNodeList + modelRoot.getElementsByTagName("relations").listFromNodeList).filter(Element)
		connectionsAndRelations.forEach[migrateConnectionOrRelation(model, it)]
		
		val balancings = modelRoot.getElementsByTagName("balancings").listFromNodeList.filter(Element)
		balancings.forEach[migrateBalancing(model, it)]
		
		if (needsXsiNamespace) {
			modelRoot.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
		}
		
		if (needsCompNamespace) {
			modelRoot.setAttribute("xmlns:comp", "http://www.dlr.de/premise/component/2016/")
		}
	}
	
	def private migrateConnectionOrRelation(MigrationModel model, Element edge) {
		model.transformToPointer(edge, "source")
		model.transformToPointer(edge, "target")
	}
	
	def private migrateBalancing(MigrationModel model, Element balancing) {
		val sources = balancing.getDirectChildElementsByTagName("actualSources")
		for (source : sources) {
			switch (source.getAttribute("xsi:type")) {
				case "prem:ParameterNameToParameterMapping": model.transformParameterNameToParameterMapping(source)
				case "prem:ParameterNameToParameterDefinitionReferenceMapping": model.transformParameterNameToParameterDefinitionReferenceMapping(source)
			}
			source.removeAttribute("xsi:type")
			model.setChange()
		}
		
		for (target : balancing.getDirectChildElementsByTagName("actualTarget")) {
			model.transformParameterNameToParameterMapping(target)
			target.removeAttribute("xsi:type")
			model.setChange()
		}
	}
	
	def transformParameterNameToParameterMapping(MigrationModel model, Element pnm) {
		model.transformToPointer(pnm, "actualValue", "value")
	}
	
	def transformParameterNameToParameterDefinitionReferenceMapping(MigrationModel model, Element pnm) {
		val actualValue = pnm.getDirectChildElementsByTagName("actualValue").head
		val compRef = actualValue.getAttribute("componentReference")
		
		val paramDefEl = actualValue.getDirectChildElementsByTagName("parameterDefinition").head
		
		if (actualValue.hasAttribute("parameterDefinition")) {
			val paramDef = actualValue.getAttribute("parameterDefinition")
			
			pnm.appendChild(pnm.ownerDocument.parse('''
				<value xsi:type="comp:ComponentReferencePointer" componentReference="«compRef»" definition="«paramDef»"/>
			'''))
			
			pnm.removeChild(actualValue)			
			needsXsiNamespace = true
			needsCompNamespace = true
			model.setChange()
		} else if (paramDefEl != null) {
			val paramDefHref = paramDefEl.getAttribute("href")
			
			pnm.appendChild(pnm.ownerDocument.parse('''
				<value xsi:type="comp:ComponentReferencePointer" componentReference="«compRef»">
					<definition xsi:type="comp:ParameterDefinition" href="«paramDefHref»"/>
				</value>
			'''))
			
			pnm.removeChild(actualValue)			
			needsXsiNamespace = true
			needsCompNamespace = true
			model.setChange()
		}
	}
	
	def transformToPointer(MigrationModel model, Element el, String name) {
		model.transformToPointer(el, name, name + "Pointer")
	}
	
	def transformToPointer(MigrationModel model, Element el, String oldName, String newName) {
		val attributeElement  = el.getDirectChildElementsByTagName(oldName).head
		
		if (el.hasAttribute(oldName)) {
			val value = el.getAttribute(oldName)
			el.removeAttribute(oldName)
			el.appendChild(el.ownerDocument.parse('''
				<«newName» xsi:type="comp:DirectPointer" target="«value»"/>
			'''))
			
			needsXsiNamespace = true
			needsCompNamespace = true
			model.setChange()
		} else if (attributeElement != null) {
			val href = attributeElement.getAttribute("href")
			val xsiType = attributeElement.getAttribute("xsi:type")
			
			el.removeChild(attributeElement)
			
			el.appendChild(el.ownerDocument.parse('''
				<«newName» xsi:type="comp:DirectPointer">
					<target xsi:type="«xsiType»" href="«href»"/>
				</«newName»>
			'''))
			
			needsXsiNamespace = true
			needsCompNamespace = true
			model.setChange()
		}
	}
	
	def private getDirectChildElementsByTagName(Element el, String name) {
		el.childNodes.listFromNodeList.filter(Element).filter[tagName == name]
	}
	
	override getTargetVersion() {
		ModelVersion.V121.toString
	}
	
}