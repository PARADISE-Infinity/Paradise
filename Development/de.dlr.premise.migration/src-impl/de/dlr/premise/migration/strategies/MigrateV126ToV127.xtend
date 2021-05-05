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
import java.util.List
import java.util.Map
import org.w3c.dom.Element
import org.w3c.dom.Node

import static extension de.dlr.premise.migration.util.MigrationHelper.*

class MigrateV126ToV127 extends AMigration {

	private val Map<String, String> typeMap = newHashMap("rep:Color" -> "rep:Color", "rep:Shape" -> "rep:NodeShape",
		"rep:EdgeColor" -> "rep:Color", "rep:Style" -> "rep:LineStyle", "rep:Coordinate" -> "rep:Coordinate")
	private val List<String> decoratorAttributes = newArrayList("name", "value", "width", "height", "type",
		"description", "x", "y")

	override protected migrateRoot(MigrationModel model, Element modelRoot) {
		val containers = modelRoot.childElements.filter[tagName.matches("^(node|edge)Decorators$")].toList
		val nodeDecorators = containers.filter[tagName == "nodeDecorators"].map[childNodes.listFromNodeList].flatten
		val edgeDecorators = containers.filter[tagName == "edgeDecorators"].map[childNodes.listFromNodeList].flatten

		nodeDecorators.migrate("Node").forEach[modelRoot.appendChild(it); model.setChange]
		edgeDecorators.migrate("Edge").forEach[modelRoot.appendChild(it); model.setChange]

		containers.forEach[modelRoot.removeChild(it)]
	}

	private def Iterable<Element> migrate(Iterable<Node> decorators, String prefix) {
		decorators.filter(Element).map [
			val selector = ownerDocument.createElement("selectors")
			selector.setAttribute("name", '''«prefix» «getAttribute("name")»''')
			if(hasAttribute("description")) selector.setAttribute("description", getAttribute("description"))
			selector.setAttribute("query", createQuery(prefix))
			createDecoratorChild(selector)
			selector
		]
	}

	private def String createQuery(Element decorator,
		String prefix) '''
		graph::I«prefix».allInstances()
		«IF decorator.childElements.exists[tagName=="sourceFilter"]»->select(getReferencedSources()->selectByKind(registry::ADataItem).id->intersection(Set{«decorator.getChildFilterIds("source")»})->notEmpty())«ENDIF»
		«IF decorator.hasAttribute("labelFilter")»->selectByKind(registry::ANameItem)->select(name.matches('«decorator.labelRegex»'))«ENDIF»
		«IF decorator.childElements.exists[tagName=="componentFilter"]»->selectByKind(registry::ADataItem)->select(Set{«decorator.getChildFilterIds("component")»}->includes(id))«ENDIF»
		«IF decorator.childElements.exists[tagName=="metaTypeFilter"]»->selectByKind(registry::IMetaTypable)->select(metaTypes.id->intersection(Set{«decorator.getChildFilterIds("metaType")»})->notEmpty())«ENDIF»
		«IF decorator.getAttribute("recursive") == "true"»->closure(oclContents)->selectByKind(graph::I«prefix»)«ENDIF»
	'''

	private def getChildElements(Element decorator) {
		decorator.childNodes.listFromNodeList.filter(Element)
	}

	private def String getLabelRegex(Element it) {
		"(?i)" + getAttribute("labelFilter").replaceAll("\\*", ".*")
	}

	private def String getChildFilterIds(Element it, String tag) {
		childElements.filter[tagName == tag + "Filter"].join("'", "','", "'", [getAttribute("href").split("#").last])
	}

	private def void createDecoratorChild(Element decorator, Element selector) {
		val child = selector.ownerDocument.createElement("decorators")
		child.setAttribute("xsi:type", typeMap.get(decorator.getAttribute("xsi:type")))
		child.setAttribute("name", decorator.getAttribute("name"))
		decoratorAttributes.forEach[if(decorator.hasAttribute(it)) child.setAttribute(it, decorator.getAttribute(it))]
		selector.appendChild(child)
	}

	override getTargetVersion() {
		return ModelVersion.V127.toString
	}
}
