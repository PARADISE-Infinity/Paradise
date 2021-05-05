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
import de.dlr.premise.util.urihandlers.PremiseLibraryURIHandler
import org.w3c.dom.Element

import static extension de.dlr.premise.migration.util.MigrationHelper.*

class MigrateV125ToV126 extends AMigration {
	val BASE_UNITS = newHashSet("%", "m", "km", "kg", "g", "s", "min", "h", "K", "A", "cd", "mol", "m/s", "km/h", "m/s^2", "m^2", "m^3", "Hz", "N", "J", "W", "deg", "kg/m3", "°C")
	val REPLACE_WITH_BASE = newHashMap(
		"m/s2" -> "m/s^2",
		"m2" -> "m^2",
		"m3" -> "m^3",
		"kg/m3" -> "kg/m^3"
	)
	val OLD_PI_ID = "1435a2aa-917e-4261-abcc-eab89aaedae0"
	val NEW_PI_ID = "a92e454b-e653-4b6e-8c7e-caf1b1175ca3"
	
	
	val BASE = PremiseLibraryURIHandler.PREMISE_GLOBAL_REGISTRY_URI
	
	override protected migrateRoot(MigrationModel model, Element modelRoot) {
		useBaseUnits(modelRoot, model)
		removeBaseUnitsFromRegistries(modelRoot, model)
		
		// deal with pi
		val hrefElements = modelRoot.getElementsByTagName("*")
			.listFromNodeList
			.filter(Element)
			.filter[hasAttribute("href")]
			
		for (hrefEl : hrefElements) {
			val id = hrefEl.getAttribute("href").split("#").get(1)
			if (id == OLD_PI_ID) {
				hrefEl.setAttribute("href", '''«BASE»#«NEW_PI_ID»''')
			}
		}
		
		val consts = modelRoot.getElementsByTagName("constants")
			.listFromNodeList
			.filter(Element)
			.filter[getAttribute("id") == OLD_PI_ID]
			.toList
		
		for (pi : consts) {
			pi.parentNode.removeChild(pi)
		}
	}
	
	protected def void useBaseUnits(Element modelRoot, MigrationModel model) {
		val unitRefs = modelRoot.getElementsByTagName("*")
			.listFromNodeList
			.filter(Element)
			.filter[childNodes.listFromNodeList.filter(Element).exists[tagName == "unit"] || hasAttribute("unit")]
		
		updateUnitReferences(model, unitRefs, "unit")
		
		
		val siUnitRefs = modelRoot.getElementsByTagName("siUnitRef")
			.listFromNodeList
			.filter(Element)
			
		updateUnitReferences(model, siUnitRefs, "target")
	}
	
	protected def void updateUnitReferences(MigrationModel model, Iterable<Element> siUnitRefs, String refName) {
		for (siUnitRef : siUnitRefs) {
			val el = siUnitRef.childNodes.listFromNodeList.filter(Element).findFirst[tagName == refName]
			
			val symbol = (if (el != null) el.getAttribute("href").split("#").reverseView.head else siUnitRef.getAttribute(refName))
			val newHref = symbol.newHref
			
			if (newHref != null) {
				if (el != null) {
					el.setAttribute("href", newHref)
				} else {
					val newEl = siUnitRef.ownerDocument.createElement(refName)
					siUnitRef.appendChild(newEl)
					newEl.setAttribute("href", newHref)
					siUnitRef.removeAttribute(refName)
				}
				model.setChange()
			}
		}
	}
	
	protected def void removeBaseUnitsFromRegistries(Element modelRoot, MigrationModel model) {
		for (unit : modelRoot.getElementsByTagName("units").listFromNodeList.filter(Element).toList) {
			val symbol = unit.getAttribute("symbol")
			if (BASE_UNITS.contains(symbol) || REPLACE_WITH_BASE.keySet.contains(symbol)) {
				unit.parentNode.removeChild(unit)
				model.setChange()
			}
		}
	}
	
	private def getNewHref(String symbol) {
		if (BASE_UNITS.contains(symbol)) {
			return '''«BASE»#«symbol»'''
		}
		if (REPLACE_WITH_BASE.keySet.contains(symbol)) {
			return '''«BASE»#«REPLACE_WITH_BASE.get(symbol)»'''
		}
		return null
	}
	
	override getTargetVersion() {
		return ModelVersion.V126.toString
	}
}