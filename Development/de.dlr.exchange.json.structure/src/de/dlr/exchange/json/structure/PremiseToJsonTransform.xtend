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
import de.dlr.exchange.json.structure.model.JsonRangeConstraint
import de.dlr.exchange.json.structure.model.StringOrDouble
import de.dlr.premise.element.AElement
import de.dlr.premise.functions.AValueConstraint
import de.dlr.premise.functions.RangeConstraint
import de.dlr.premise.functions.RequiredParameter
import de.dlr.premise.functions.UseCase
import de.dlr.premise.system.Parameter
import de.dlr.premise.system.SystemComponent
import java.util.Date
import java.util.List
import java.util.function.Predicate
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static extension org.eclipse.emf.ecore.util.EcoreUtil.getURI

@FinalFieldsConstructor class PremiseToJsonTransform {
	
	val List<? extends AElement> roots
	val Predicate<Object> predicate
	
	@Accessors(PUBLIC_GETTER) var JsonExport jsonRoot
	
	def run() {
		val jsonRootElements = roots
			.map[
				root | root.transformAElement => [
					uri = root.URI
				]
			]
			
		jsonRoot = new JsonExport
		jsonRoot.children = jsonRootElements
		jsonRoot.exportDate = new Date()
	}
	
	def private JsonElement transformAElement(AElement elem) {
		new JsonElement => [
			id = elem.id
			name = elem.name
			
			if (!elem.contained.empty) {
				children = elem.contained.map[transformAElement]
			}
			if (elem instanceof SystemComponent) {
				if (!elem.parameters.empty) {
					parameters = elem.parameters.filter(predicate).map[transformParameter].toList
				}
			}
		]
	}
	
	def private transformParameter(Parameter param) {
		val valueStr = param.value?.value
		new JsonParameter => [
			id = param.id
			name = param.name
			
			if (valueStr != null) {
				value = new StringOrDouble(valueStr)
			}
			
			unit = param.unit?.symbol
			if (!param.constraints.empty) {
				constraints = param.constraints.map[transformValueConstraint]
			}
		]
	}
	
	def private dispatch transformValueConstraint(AValueConstraint constr) {
		throw new IllegalArgumentException
	}	
	
	def private dispatch transformValueConstraint(RangeConstraint constr) {
		new JsonRangeConstraint => [
			if (constr.lower != null) {
				lower = new StringOrDouble(constr.lower)
			}
			if (constr.upper != null) {
				upper = new StringOrDouble(constr.upper)
			}
		]
	}
	
	def getConstraints(Parameter param) {
		param.satisfiedSatisfieables.filter(RequiredParameter).map[valueConstraint].filterNull.toList
	}
	
	def dispatch List<? extends AElement> getContained(UseCase uc) {
		uc.children.filter(predicate).toList
	}

	def dispatch List<? extends AElement> getContained(SystemComponent sc) {
		sc.referencedChildren.filter(predicate).toList
	}
}