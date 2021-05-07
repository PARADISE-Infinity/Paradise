/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.json.structure

import de.dlr.exchange.json.structure.model.JsonElement
import de.dlr.exchange.json.structure.model.JsonExport
import de.dlr.exchange.json.structure.model.JsonParameter
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import de.dlr.premise.system.Parameter
import de.dlr.premise.registry.RegistryFactory

import static extension de.dlr.premise.util.PremiseHelper.*
import org.eclipse.xtend.lib.annotations.Accessors

@FinalFieldsConstructor class JsonIntoPremiseMerge {
	val ResourceSet resSet
	val JsonExport jsonRoot
	
	val Map<JsonParameter, Parameter> parameterMap = newHashMap
	
	var prepared = false
	@Accessors(PUBLIC_GETTER) var changed = false
	
	def prepare() throws MergeException {
		for (jsonRootElement : jsonRoot.children) {
			val modelRoot = resSet.getEObject(jsonRootElement.uri, true)
			
			if (modelRoot == null) {
				throw new MergeException('''Can't find root element "«jsonRootElement.name»" (URI: «jsonRootElement.uri»)''')
			}
			
			prepareRootElement(modelRoot, jsonRootElement)
		}
		
		prepared = true
	}
	
	def prepareRootElement(EObject modelRoot, JsonElement jsonRootElement) throws MergeException {
		val parameters = modelRoot.eAllContents.filter(Parameter).toSet
		val jsonParameters = jsonRootElement.closure[children].filterNull.map[it.parameters].filterNull.flatten.toSet
		
		for (jsonParameter : jsonParameters) {
			val parameter = parameters.findFirst[id == jsonParameter.id]
			
			if (parameter == null) {
				throw new MergeException('''Can't find parameter "«jsonParameter.name»" (ID: «jsonParameter.id»)''')
			}
			
			parameterMap.put(jsonParameter, parameter)
		}
	}
	
	def run() {
		if (!prepared) {
			throw new IllegalStateException()
		}
		
		parameterMap.forEach[jsonParameter, parameter |
			val stringValue = jsonParameter.value?.string
			if (parameter.stringValue != stringValue) {
				parameter.stringValue = stringValue
				changed = true
			}
		]
	}
	
	def getStringValue(Parameter param) {
		param.value?.value
	}
	
	def setStringValue(Parameter param, String stringValue) {
		if (param.value == null) {
			param.value = RegistryFactory.eINSTANCE.createValue
		}
		param.value.value = stringValue
	}
	
	static class MergeException extends Exception {
		new(String message) {
			super(message)
		}
	}
}