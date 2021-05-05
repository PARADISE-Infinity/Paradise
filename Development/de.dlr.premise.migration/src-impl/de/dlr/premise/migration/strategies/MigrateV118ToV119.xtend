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

import com.google.common.collect.Lists
import de.dlr.premise.migration.MigrationModel
import de.dlr.premise.migration.ModelVersion
import java.util.Map
import org.eclipse.emf.common.util.URI
import org.w3c.dom.Document
import org.w3c.dom.Element

import static extension de.dlr.premise.migration.util.MigrationHelper.*

class MigrateV118ToV119 extends ATwoPhaseMigration {

	val Map<String, Element> elementsById = newHashMap
	boolean needsUcNamespace
	boolean needsXsiNamespace

	override getTargetVersion() {
		ModelVersion.V119.toString
	}

	override protected prepareRoot(MigrationModel model, String path, Document document) {
		val modelRoot = document.documentElement
		
		for (element : modelRoot.getElementsByTagName("*").listFromNodeList.filter(Element)) {
			val id = element.getAttribute("id")
			if (!id.isNullOrEmpty) {
				elementsById.put(id, element)
			}
		}
	}

	override protected migrateRoot(MigrationModel model, String path, Document document) {
		needsXsiNamespace = needsUcNamespace = false
		
		val modelRoot = document.documentElement

		// migrate ParameterMappings
		migrateParameterMappings(model, modelRoot.getElementsByTagName("actualSources").listFromNodeList.filter(Element))
		migrateParameterMappings(model, modelRoot.getElementsByTagName("actualTarget").listFromNodeList.filter(Element))

		// add xsi:type to Parameter::satisfies, due to the introduction of IParameterSatisfieable
		for (element : modelRoot.getElementsByTagName("satisfies").listFromNodeList.filter(Element)) {
			if (element.parentNode.nodeName == "parameters") {
				element.setAttribute("xsi:type", "uc:RequiredParameter")
				model.setChange()
				needsXsiNamespace = true
				needsUcNamespace = true
			}
		}

		// translate "satisfies" relations to SystemComponent::satisfies references
		// copy, because we modify the underlying list and NodeList is live
		val filter = Lists.newArrayList(modelRoot.getElementsByTagName("relations").listFromNodeList.filter(Element))
		for (element : filter) {
			if (element.getAttribute("type") == "satisfies" || element.getAttribute("type") == "satisfy") {
				val sourceId = element.getReferenceTargetURI(path, "source").URIFragment
				val sourceElement = elementsById.get(sourceId)
				val targetURI = element.getReferenceTargetURI(path, "target")

				if (sourceElement != null && targetURI != null) {
					val targetFileExtension = URI.createURI(targetURI).fileExtension
					if (targetFileExtension == "usecase") {
						element.parentNode.removeChild(element)
						sourceElement.appendChild(sourceElement.ownerDocument.parse('''
							<satisfies href="«targetURI»" xsi:type="uc:UseCase" />
						'''))
						model.setChange()
						needsXsiNamespace = true
						needsUcNamespace = true
					}
				}
			}
		}

		// add xsi:type to SystemComponent::children. We can't just select all elements named "children", since there is also
		// MetaData::children, and NestedTransitionConstraint::children. So we recurse into the model from the root element instead.
		if (modelRoot.tagName == "prem:ProjectRepository") {
			val projects = modelRoot.childNodes.listFromNodeList.filter(Element).filter[tagName == "projects"]
			for (sysComp : projects) {
				addChildrenType(model, sysComp)
			}
		}

		if (needsXsiNamespace) {
			modelRoot.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
		}
		
		if (needsUcNamespace) {
			modelRoot.setAttribute("xmlns:uc", "http://www.dlr.de/premise/usecase/2014/")
		}
	}

	/**
	 * Add xsi:type to ParameterNameToParameterMapping elements, since there is now an abstract superclass (ParameterMapping). 
	 * Change value to actualValue, since value is now a computed property.
	 */
	def private migrateParameterMappings(MigrationModel model, Iterable<Element> mappings) {
		for (element : mappings) {
			element.renameAttribute("value", "actualValue")
			element.setAttribute("xsi:type", "prem:ParameterNameToParameterMapping")
			for (value : element.childNodes.listFromNodeList.filter(Element).filter[tagName == "value"]) {
				value.ownerDocument.renameNode(value, value.namespaceURI, "actualValue")
			}
			model.setChange()
			needsXsiNamespace = true
		}
	}

	def private void addChildrenType(MigrationModel model, Element sysComp) {
		val children = sysComp.childNodes.listFromNodeList.filter(Element).filter[tagName == "children"]

		for (child : children) {
			child.setAttribute("xsi:type", "prem:SystemComponent")
			model.setChange()
			needsXsiNamespace = true
			addChildrenType(model, child)
		}
	}

}
