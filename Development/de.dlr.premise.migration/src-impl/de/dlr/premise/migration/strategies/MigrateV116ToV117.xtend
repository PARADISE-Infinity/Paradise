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

import com.google.common.base.CharMatcher
import com.google.common.cache.CacheBuilder
import com.google.common.cache.LoadingCache
import com.google.common.collect.ImmutableMultimap
import de.dlr.premise.migration.IPremiseMigration
import de.dlr.premise.migration.MigrationModel
import de.dlr.premise.migration.ModelVersion
import java.io.File
import java.util.Collections
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Set
import javax.xml.parsers.DocumentBuilderFactory
import org.eclipse.xtext.util.StringInputStream
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList

import static extension com.google.common.collect.Multimaps.index
import static extension de.dlr.premise.migration.util.MigrationHelper.listFromNodeList

class MigrateV116ToV117 implements IPremiseMigration {
	val wellKnownFunctions = #["Sum", "Subtract", "Multiply", "Divide", "Max", "Min", "Mean", "DivideFirstThenMultiply"]

	val Map<String, String> functionNameByURI = newHashMap
	val Map<String, String> aParameterDefNameById = newHashMap
	val List<String> wellKnownFunctionURIs = newArrayList
	
	var String sumFunctionURI = null
	var boolean needsSumOverAllModes = false
	var boolean hasSumOverAllModes = false

	override getTargetVersion() {
		ModelVersion.V117.toString
	}

	override void migrate(MigrationModel model) {
		val Map<String, Document> refFiles = new HashMap(model.referencedFiles)
		refFiles.put(model.path, model.modelDocument)

		refFiles.forEach [ path, document |
			createIndexes(path, document)
		]

		refFiles.forEach [ path, document |
			migrateRoot(model, path, document)
		]

		if (needsSumOverAllModes && !hasSumOverAllModes) {
			val functionpool = refFiles.entrySet.findFirst[key.endsWith(".functionpool")]?.value
			if (functionpool != null) {
				val calcEngineNode = functionpool.parse('''
					<calcEngines className="de.dlr.calc.engine.java.implementations.SumOverAllModes" description="" id="SumOverAllModes" name="SumOverAllModes" xsi:type="fnp:CalcEngineJava"/>
				''')
				val functionNode = functionpool.parse('''
					<functions calcEngine="SumOverAllModes" description="" methodName="calculate" name="SumOverAllModes" xsi:type="fnp:FnDefJava">
						<output name="SumOfAllModeValues"/>
						<inputs description="" name="Parameter"/>
					</functions>
				''')
				functionpool.documentElement.appendChild(calcEngineNode)
				functionpool.documentElement.appendChild(functionNode)
			}
		}

	}

	def private createIndexes(String path, Document document) {
		val allElements = document.getElementsByTagName("*").elements

		val functions = document.getElementsByTagName("functions").elements
		for (i : 0 ..< functions.size) {
			val function = functions.get(i)
			val name = function.getAttribute("name")
			if (name != "") {
				val uri = '''«path»#//@functions.«i»'''
				functionNameByURI.put(uri, name)

				if (name == "Sum" && path.endsWith("GlobalFuncDefs.premise.functionpool")) {
					sumFunctionURI = uri
				}
				
				if (wellKnownFunctions.contains(name) && path.endsWith("GlobalFuncDefs.premise.functionpool")) {
					wellKnownFunctionURIs.add(uri)
				}
				
				if (name == "SumOverAllModes" && function.getAttribute("xsi:type") == "fnp:FnDefJava") {
					hasSumOverAllModes = true
				}
			}
		}

		val elementsByName = allElements.filter[nameAsValidID != null].index[nameAsValidID]
		val nameIndexCache = CacheBuilder.newBuilder.build[String name|1]

		val parameters = document.getElementsByTagName("parameters").elements
		for (parameter : parameters) {
			val name = parameter.getName(elementsByName, nameIndexCache)
			aParameterDefNameById.put(parameter.getAttribute("id"), name)
		}

		val constants = document.getElementsByTagName("constants").elements
		for (constant : constants) {
			val name = constant.getName(elementsByName, nameIndexCache)
			aParameterDefNameById.put(constant.getAttribute("id"), name)
		}
	}

	def private migrateRoot(MigrationModel model, String path, Document document) {
		val Set<String> referencedFunctionpools = newHashSet

		val balancings = document.getElementsByTagName("balancings").elements
		for (balancing : balancings) {
			val parent = balancing.parentNode
			if (parent instanceof Element) {
				// don't migrate transition balancings
				if (parent.tagName != "transitions") {
					model.setChange()
					migrateBalancing(path, balancing, referencedFunctionpools)
				}
			}

		}

		for (referencedFunctionpool : referencedFunctionpools) {
			document.documentElement.appendChild(
				document.parse(
					'''<functionPools href="«referencedFunctionpool»#/" />'''
				))
		}
	}

	def private migrateBalancing(String path, Element balancing, Set<String> referencedFunctionpools) {
		val List<String> inputNames = newArrayList
		var String outputName

		if (balancing.hasAttribute("sources")) {
			for (sourceId : balancing.sourceIds) {
				val name = aParameterDefNameById.get(sourceId)

				val node = balancing.ownerDocument.parse('''<actualSources key="«name»" value="«sourceId»" />''')
				balancing.appendChild(node)

				inputNames += name
			}
		} else {
			for (source : balancing.sourceURIs) {
				val sourceURI = source.key
				val sourceId = sourceURI.split("#").get(1)
				val name = aParameterDefNameById.get(sourceId)

				val node = balancing.ownerDocument.parse(
					'''
						<actualSources key="«name»">
							 <value xsi:type="«source.value»" href="«sourceURI»"/>
						</actualSources>
					''')
				balancing.appendChild(node)

				inputNames += name
			}
		}

		val targetId = balancing.getAttribute("target")
		if (targetId != "") {
			outputName = aParameterDefNameById.get(targetId)

			val node = balancing.ownerDocument.parse('''<actualTarget key="«outputName»" value="«targetId»" />''')
			balancing.appendChild(node)
		}

		val functionCall = createFunctionCall(balancing, path, referencedFunctionpools, inputNames)

		val function = '''«outputName» = «functionCall»'''
		balancing.setAttribute("function", function)

		balancing.removeAttribute("target")
		balancing.removeAttribute("sources")
		balancing.childNodes.elements.filter[tagName == "sources"].forEach[balancing.removeChild(it)]
		balancing.childNodes.elements.filter[tagName == "functionDef"].forEach[balancing.removeChild(it)]
		balancing.removeAttribute("balancingMode")
	}
	
	private def createFunctionCall(Element balancing, String path, Set<String> referencedFunctionpools, List<String> inputNames) {
		val relativeFunctionURI = balancing.childNodes.elements.filter[tagName == "functionDef"].head?.
			getAttribute("href")
		
		if (relativeFunctionURI != null) {
			val absoluteFunctionURI = relativeFunctionURI.resolveAgainst(path)
			
			if (wellKnownFunctionURIs.contains(absoluteFunctionURI) && inputNames.size > 0 && balancing.getAttribute("balancingMode") == "") {
				// For "copyValue"
				if (inputNames.size == 1) {
					return inputNames.get(0)
				}
				
				val functionName = functionNameByURI.get(absoluteFunctionURI)
				
				switch (functionName) {
					case "Sum": return inputNames.join(" + ")
					case "Subtract": return inputNames.join(" - ")
					case "Multiply": return inputNames.join(" * ")
					case "Divide": return inputNames.join(" / ")
					case "DivideFirstThenMultiply": return inputNames.get(0) + " / " + inputNames.subList(1, inputNames.size).join(" * ")
				}
			}
			
			var functionName = functionNameByURI.get(absoluteFunctionURI).toValidID
			if (functionName != null) {
				if (balancing.getAttribute("balancingMode") == "OVER_ALL_MODES") {
					if (absoluteFunctionURI == sumFunctionURI) {
						needsSumOverAllModes = true
					}
					functionName += "OverAllModes"
				}
			
				referencedFunctionpools += relativeFunctionURI.split("#").get(0)
				
				return '''«functionName»(«inputNames.join(", ")»)'''
			}		
		}
		
		return '''<unknown function>(«inputNames.join(", ")»)'''
	}

	def private getSourceURIs(Element balancing) {
		balancing.childNodes.elements.filter[tagName == "sources"].map[
			(getAttribute("href") -> getAttribute("xsi:type"))]
	}

	def private List<String> getSourceIds(Element balancing) {
		val attributeSources = balancing.getAttribute("sources")

		if (attributeSources == "") {
			return Collections.emptyList
		}

		return attributeSources.split(" ")
	}

	def private String getName(Element element, ImmutableMultimap<String, Element> elementsByName,
		LoadingCache<String, Integer> nameIndexCache) {
		val qName = element.getQualifiedName(elementsByName)

		if (qName != null) {
			return qName
		}

		var baseName = element.nameAsValidID
		if (baseName == null) {
			baseName = switch (element.tagName) {
				case "constants": "Constant"
				default: "Parameter"
			}
		}

		var index = nameIndexCache.get(baseName)
		nameIndexCache.put(baseName, index + 1)

		return baseName + index
	}

	def private String getQualifiedName(Element element, ImmutableMultimap<String, Element> elementsByName) {
		val name = element.nameAsValidID
		if (name == null) {
			return null
		}
		
		val List<String> qualifiedNameSegments = newArrayList
		qualifiedNameSegments.add(name)

		val allDuplicates = elementsByName.get(name).filter[it != element]
		var Iterable<? extends Node> duplicates
		if (element.tagName == "parameters") {
			duplicates = allDuplicates.filter[tagName == "parameters"]
		} else {
			duplicates = allDuplicates
		}
		
		var currentElement = element as Node
		
		while (currentElement != null && !duplicates.isEmpty) {
			val parentName = currentElement.parentNode.nameAsValidID
			qualifiedNameSegments += parentName ?: "_"
			
			duplicates = duplicates.filter[parentNode.nameAsValidID == parentName].map[parentNode]
			currentElement = currentElement.parentNode
		}
		
		if (!duplicates.isEmpty) {
			qualifiedNameSegments.add(0, element.idSuffix)
		}

		return qualifiedNameSegments.reverseView.dropWhile[it == "_"].join(".")
	}

	def private getNameAsValidID(Node node) {
		if (node instanceof Element)  {
			val name = node.getAttribute("name")
			if (name == "") {
				return null
			}
			return toValidID(name)
		}
		return null
	}
	
	private def toValidID(String str) {
		if (str == null) {
			return "_"
		} 
		
		val trimmedStr = str.trim()	
		if (trimmedStr.length == 0) {
			return "_"
		}
		
		val resultStr = trimmedStr.substring(0, 1).replaceAll("[^a-zA-Z_]", "_") + trimmedStr.substring(1).replaceAll("[^a-zA-Z0-9_]+", "_")
		// trim leading and trailing _
		return CharMatcher.is("_").trimFrom(resultStr)}
	
	def private getIdSuffix(Element element) {
		val id = element.getAttribute("id")
		val shortenedId = id.substring(0, Math.min(id.length, 5))
		("id_" + shortenedId).toValidID
	}

	def private getElements(NodeList nodeList) {
		nodeList.listFromNodeList.filter(Element)
	}

	def private parse(Document document, String str) {
		val node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new StringInputStream(str)).
			documentElement

		document.importNode(node, true)
	}

	def private resolveAgainst(String relativeURI, String basePath) {
		val splitRelativeURI = relativeURI.split("#")
		val absolutePath = new File(
			new File(basePath).getCanonicalFile().getParent() + File.separator + splitRelativeURI.get(0)).canonicalPath
		val absoluteURI = absolutePath + (if(splitRelativeURI.size > 1) ("#" + splitRelativeURI.get(1)) else "")

		return absoluteURI
	}
}
