/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.block

import com.google.common.collect.ImmutableList
import de.dlr.premise.element.AElement
import de.dlr.premise.registry.IPremiseObject
import de.dlr.premise.registry.MetaData
import java.util.List
import java.util.Map

import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.getWrappedElement

class MetaDataHelper {	
	def dispatch getMetaDataMap(AElement ele) {
		val metaData = ele.metaDataMapForMetaTypable
		
		if (!ele.description.nullOrEmpty) {
			metaData.put("description", ele.description)
		}
		
		if (!ele.id.nullOrEmpty) {
			metaData.put("ID", ele.wrappedElement.id)
		}
		
		metaData
	}
	
	def dispatch getMetaDataMap(IPremiseObject mt) {
		mt.metaDataMapForMetaTypable
	}
	
	def private getMetaDataMapForMetaTypable(IPremiseObject mt) {
		val map = mt.metaData.getMetaDataMap(ImmutableList.of())
		
		val urlKeys = map.keySet.filter[equalsIgnoreCase("url")].toSet
		if (!urlKeys.empty) {
			val urlValue = map.get(urlKeys.last)
			map.keySet.removeAll(urlKeys)
			map.put("url", urlValue)
		}
		
		return map
	}
	
	def private Map<String, String> getMetaDataMap(List<MetaData> metaDatas, ImmutableList<String> parentPath) {
		val map = newLinkedHashMap
		
		for (metaData : metaDatas) {
			val name = metaData.name
			val path = ImmutableList.<String>builder().addAll(parentPath).add(name).build()
			map.put(path.join("/"), metaData.value)
			map.putAll(metaData.metaData.getMetaDataMap(path))
		}
		
		return map
	}
}