/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.xtext.naming

import de.dlr.premise.component.ParameterDefinition
import de.dlr.premise.functionpool.AFnDef
import java.util.List
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider
import org.eclipse.xtext.naming.QualifiedName
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.ComponentReferenceSubstitutionMapping
import de.dlr.premise.system.Parameter
import de.dlr.premise.registry.ADataItem
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.registry.Constant

class PremiseQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider {
	
	// @Inject IDValueConverter idValueConverter
	
	def QualifiedName qualifiedName(ANameItem ele) {
		if (ele instanceof Balancing) {
			return QualifiedName.create(EcoreUtil.getURI(ele).toString.replaceAll("\\.", ":"))
		}
		
		if (ele.name != null && ele.name != "") {
			val result = switch (ele) {
				Constant: ele.getQualifiedNameFromSegments
				AFnDef: QualifiedName.create(ele.name.escapeSegment)
				AParameterDef: ele.getQualifiedNameFromSegments
				ParameterDefinition: ele.getQualifiedNameFromSegments
				ComponentReference: ele.getQualifiedNameFromSegments
			}
			
			if (result != null) {
				return result
			}
		}
		
		if (ele instanceof ADataItem) {
			return QualifiedName.create("id_" + ele.id.escapeSegment)
		}
		
		return null
    }
    
    def private getQualifiedNameFromSegments(ANameItem item) {
		val segments = item.qualifiedNameSegments
		if (segments == null) {
			return null
		}
		return QualifiedName.create(segments)
    }	
		
	def private getQualifiedNameSegments(ANameItem item) {
		val name = item.nameAsValidID
		if (name == null) {
			return null
		}
		
		val List<String> qualifiedNameSegments = newArrayList
		qualifiedNameSegments.add(name)

		val filterClass = if (item instanceof Parameter) Parameter else ANameItem

		var duplicates = item.eResource.resourceSet.allContents
			.filter(filterClass)
			.filter[!(eContainer instanceof ComponentReferenceSubstitutionMapping)]
			.filter[it != item]
			.filter[it.nameAsValidID == name]
			
		var currentItem = item
		
		while (currentItem != null && !duplicates.isEmpty) {
			val parentName = currentItem.parentItem?.nameAsValidID
			qualifiedNameSegments += parentName ?: "_"
			
			duplicates = duplicates.filter[parentItem?.nameAsValidID == parentName].map[parentItem]
			currentItem = currentItem.parentItem
		}
				
		if (!duplicates.isEmpty) {
			if (item instanceof ADataItem) {
				qualifiedNameSegments.add(0, item.idSuffix)
			} else {
				return null
			}
		}

		return qualifiedNameSegments.reverseView.dropWhile[it == "_"].toList
	}
	
	def private nameAsValidID(ANameItem element) {
		element.name?.escapeSegment
	}
	
	def private parentItem(ANameItem item) {
		val container = item.eContainer
		switch (container) {
			ANameItem: container
			default: null
		} 
	}
	
	def private getIdSuffix(ADataItem item) {
		val id = item.id
		val shortenedId = id.substring(0, Math.min(id.length, 5))
		("id_" + shortenedId).escapeSegment
	}
	
	def escapeSegment(String str) {
		val trimmedStr = str.trim()	
		if (trimmedStr == null || trimmedStr.length == 0) {
			return "_"
		} 
		
		val resultStr = trimmedStr.substring(0, 1).replaceAll("[^a-zA-Z_]", "_") + trimmedStr.substring(1).replaceAll("[^a-zA-Z0-9_]+", "_")
		// trim leading and trailing _
		return resultStr //CharMatcher.is("_").trimFrom(resultStr)
	}
}