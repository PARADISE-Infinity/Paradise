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

import de.dlr.premise.element.GuardCombination
import de.dlr.premise.element.Mode
import de.dlr.premise.element.ModeGuard
import de.dlr.premise.element.Transition
import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.GraphFactory
import de.dlr.premise.system.TransitionBalancing
import de.dlr.premise.system.TransitionParameter
import de.dlr.premise.util.scope.ScopedEObjectFactory
import org.eclipse.emf.common.util.ECollections
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.util.EcoreEList

import static extension de.dlr.premise.util.PremiseHelper.flatMap
import static extension de.dlr.premise.util.PremiseHelper.getRootNotifier
import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.*
import de.dlr.premise.system.SystemPackage
import org.eclipse.emf.ecore.InternalEObject

class TransitionBalancingHelper {
	def static EList<APointer<TransitionParameter>> getSourcePointers(TransitionBalancing transBalUnscoped) {
		val transBal = ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(transBalUnscoped)
		val transition = transBal.eContainer as Transition
		val modes = transition?.condition.conditionModes.toSet
		val transitions = ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(transBal.rootNotifier, Transition).toSet
		val parameters = transitions
			.filter[modes.contains(target)]
			.flatMap[parameters]
			.filter[name == transBal.target?.name]
			.toSet
			
		val parameterPointers = parameters.map[transBal.invocationHandler.createPointer(it)].toList
		
		return new EcoreEList.UnmodifiableEList<APointer<TransitionParameter>>(
			transBalUnscoped as InternalEObject, 
			SystemPackage.Literals.TRANSITION_BALANCING__SOURCES, 
			parameterPointers.size, 
			parameterPointers.toArray
		)
	}
	
	def private static dispatch Iterable<Mode> getConditionModes(Void it) {
		return #[]
	}
	
	def private static dispatch Iterable<Mode> getConditionModes(ModeGuard it) {
		return #[mode]
	}
	
	def private static dispatch Iterable<Mode> getConditionModes(GuardCombination it) {
		return children.flatMap[conditionModes]
	}
	
	def static EList<APointer<TransitionParameter>> getTargetPointers(TransitionBalancing it) {
		if (target != null) {
			ECollections.singletonEList(target.createPointer)
		} else {
			ECollections.emptyEList()
		}
	}
	
	private def static <T> APointer<T> createPointer(T transParam) {
		GraphFactory.eINSTANCE.createDirectPointer => [
			target = transParam
		]
	}
}