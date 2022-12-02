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

import de.dlr.premise.element.Connection
import de.dlr.premise.functions.UseCase

import org.eclipse.emf.ecore.resource.ResourceSet

class UseCaseHelper {

	// filters all connections of UseCases classes
	static def getConncections(ResourceSet input) {
		return input.allContents.filter(Connection as Class<?> as Class<Connection<?>>).filter [
			target instanceof UseCase
		].toList
	}
}