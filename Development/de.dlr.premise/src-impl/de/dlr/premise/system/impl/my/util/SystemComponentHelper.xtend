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

import com.google.common.collect.Sets
import de.dlr.premise.component.ISatisfieable
import de.dlr.premise.component.ISatisfying
import de.dlr.premise.graph.INode
import org.eclipse.emf.common.util.EList
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.registry.ANameItem

import static extension de.dlr.premise.util.PremiseHelper.closure
import static extension de.dlr.premise.util.PremiseHelper.eAllContentsIncludingRoot
import static extension de.dlr.premise.util.PremiseHelper.flatMap
import static extension de.dlr.premise.util.PremiseHelper.toUnmodifieableEList
import de.dlr.premise.system.ComponentReferenceSubstitutionMapping

class SystemComponentHelper {
	static def getUnsatisfiedSatisfieablesFor(SystemComponent sc, ISatisfieable componentSatisfieable) {
		val satisfiedSatisfieables = sc.childSatisfying.flatMap[satisfiedSatisfieables].toSet
		val neededSatisfieables = componentSatisfieable.childSatisfieables.toSet
		
		Sets.difference(neededSatisfieables, satisfiedSatisfieables).toUnmodifieableEList
	}
	
	static def satisfieableApplies(SystemComponent sc, ISatisfieable componentSatisfieable) {
		sc.getUnsatisfiedSatisfieablesFor(componentSatisfieable).empty
	}
	
	static def getInapplicableSatisfies(SystemComponent sc) {
		sc.satisfiedSatisfieables.filter[!sc.satisfieableApplies(it)].toUnmodifieableEList
	}
	
	static def isSatisfiesValid(SystemComponent sc) {
		sc.inapplicableSatisfies.empty
	} 
	
	static def getErrorMessageForInvalidSatisfies(SystemComponent sc) {
		sc.inapplicableSatisfies.map[componentSatisfieable | 
			val names = sc.getUnsatisfiedSatisfieablesFor(componentSatisfieable).map[childSatisfieable | '''«childSatisfieable.eClass.name» «(childSatisfieable as ANameItem).name»''']
			'''«componentSatisfieable.eClass.name» «(componentSatisfieable as ANameItem).name» can't be satisfied. The following children are not satisfied: «names.join(", ")»'''
		].join("\n\n")
	}
	
	static def hasCyclicComponentReference(SystemComponent sc) {
		// does not use SystemComponent#getReferencedChildren, since this method is used there to check for cycles
		sc.children.map[component].filterNull
			.closure[children.map[component].filterNull]
			.contains(sc)
	}
	
	private static def getChildSatisfieables(ISatisfieable satisfieable) {
		satisfieable.eAllContentsIncludingRoot.filter(ISatisfieable).toUnmodifieableEList as EList<?> as EList<ISatisfieable>
	}
	
	private static def getChildSatisfying(ISatisfying<?, ?> satisfying) {
		satisfying.eAllContentsIncludingRoot.filter(ISatisfying).filter[ISatisfying<?, ?> it | !(eContainer instanceof ComponentReferenceSubstitutionMapping)].toUnmodifieableEList as EList<?> as EList<ISatisfying<INode, ISatisfieable>>
	}
}