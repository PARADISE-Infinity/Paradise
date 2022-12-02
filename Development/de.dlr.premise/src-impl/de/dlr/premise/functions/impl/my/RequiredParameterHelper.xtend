/**
* Copyright (C) 2022 Axel Berres
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres
*
*/

package de.dlr.premise.functions.impl.my

import org.eclipse.emf.ecore.resource.ResourceSet

import de.dlr.premise.element.Relation
import de.dlr.premise.functions.RequiredParameter

class RequiredParameterHelper {
	
	// returns the input relations of required Parameter		
	static def getInputRelations(ResourceSet input)	{	
		return input.allContents.filter(Relation).filter [
			target instanceof RequiredParameter
		].groupBy[target].mapValues[it.map[source]]
	}

	// returns the input relations of required Parameter
	static def getOutputRelations(ResourceSet input) {	
		return input.allContents.filter(Relation).filter [
			source instanceof RequiredParameter
		].groupBy[source].mapValues[it.map[target]]
	}	
}