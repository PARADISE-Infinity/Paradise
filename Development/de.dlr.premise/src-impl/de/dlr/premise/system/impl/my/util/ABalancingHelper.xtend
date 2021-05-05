/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.system.impl.my.util

import com.google.common.cache.CacheBuilder
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.system.ABalancing
import de.dlr.premise.util.PremiseHelper
import de.dlr.premise.util.cyclecheck.BalancingCycleChecker
import de.dlr.premise.util.scope.ScopedEObjectFactory
import java.util.concurrent.TimeUnit
import org.eclipse.emf.ecore.EObject

import static extension de.dlr.premise.util.PremiseHelper.flatMap
import static extension de.dlr.premise.util.PremiseHelper.getRootNotifier

class ABalancingHelper {
	val private static cycleCheckerCache = CacheBuilder.newBuilder()
		.expireAfterWrite(1000, TimeUnit.MILLISECONDS)
		.build[ABalancing<?> bal | new BalancingCycleChecker(bal.rootNotifier, bal)]
	
	def static boolean isNotCyclicBalancing(ABalancing<?> bal) {
		!ScopedEObjectFactory.INSTANCE.createAllScopedEObjects(bal)
			.map[cycleCheckerCache.get(it)]
			.exists[hasCycle]
	}
	
	def static getErrorMessageForNotCyclicBalancing(ABalancing<?> bal) {
		ScopedEObjectFactory.INSTANCE.createAllScopedEObjects(bal)
			.map[cycleCheckerCache.get(it)]
			.flatMap[cycles]
			.map[
				"Balancing is part of cycle: " + map['"' + nameForErrorMessageForNotCyclicBalancing + '"'].join(" -> ")
			]
			.join("\n\n")
	}
	
	private def static getNameForErrorMessageForNotCyclicBalancing(EObject it) {
		eClass.name + " " + PremiseHelper.getMeaningfulName(it as ANameItem)
	}
}