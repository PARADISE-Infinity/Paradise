/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.component.impl.my.util

import de.dlr.premise.component.ISatisfieable
import de.dlr.premise.component.ISatisfying
import de.dlr.premise.graph.INode
import org.eclipse.emf.common.util.EList

import static extension de.dlr.premise.util.PremiseHelper.toUnmodifieableEList

class ISatisfyingHelper {
	def static <S extends INode, T extends ISatisfieable> EList<T> getSatisfiedSatisfieables(ISatisfying<S, T> it) {
		satisfies.map[target].toUnmodifieableEList
	}
}