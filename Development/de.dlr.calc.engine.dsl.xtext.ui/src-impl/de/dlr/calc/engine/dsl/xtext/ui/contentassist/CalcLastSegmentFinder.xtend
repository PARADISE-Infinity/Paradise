/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.calc.engine.dsl.xtext.ui.contentassist

import org.eclipse.xtext.ui.editor.contentassist.FQNPrefixMatcher.LastSegmentFinder

class CalcLastSegmentFinder implements LastSegmentFinder {
	
	override getLastSegment(String fqn, char delimiter) {
		val lastIndex = fqn.lastIndexOf(delimiter)
		if (lastIndex == -1) {
			return null
		}
		return fqn.substring(lastIndex + 1)
	}
	
}