/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.dsl

import de.dlr.premise.element.Mode
import de.dlr.premise.graph.GraphFactory
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.ComponentReferenceSubstitutionMapping
import de.dlr.premise.util.PremiseHelper
import java.util.List
import org.eclipse.emf.common.notify.Notifier
import org.eclipse.emf.ecore.EObject
import org.junit.Assert
import de.dlr.premise.util.scope.ScopedEObjectFactory

import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.*

class XbaseEngineTestHelper {
	def static getBalancingByName(Notifier rep, String name) {
		rep.allContents.filter(Balancing).findFirst[it.name == name].notNull
	}
	
	def static getModeByName(Notifier rep, String name) {
		rep.allContents.filter(Mode).findFirst[it.name == name].notNull
	}
	
	def static getAParameterDefByName(Notifier rep, String name) {
		rep.allContents.filter(AParameterDef)
			.findFirst[
				!(eContainer instanceof ComponentReferenceSubstitutionMapping)
				&& it.name == name
			]
			.notNull
	}
	
	def static getComponentReferenceByName(Notifier rep, String name) {
		rep.allContents.filter(ComponentReference).findFirst[it.name == name].notNull
	}
	
	def static <T extends EObject> T getScopedWrapper(Notifier rep, Class<T> filterClass, List<String> path, String name) {
		ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(rep, filterClass)
			.findFirst[
				val crnl = scope.map[it.name].toList
				
				crnl == path 
				&& (wrappedElement as ANameItem).name == name
			]
			.notNull
	}
	
	def static <T extends EObject> T getScopedWrapper(Notifier rep, Class<T> filterClass, String... path) {
		rep.getScopedWrapper(filterClass, path.subList(0, path.length - 1), path.last)
	}
	
	def static <T extends EObject> createDirectPointer(T target) {
		val p = GraphFactory.eINSTANCE.<T>createDirectPointer
		p.target = target
		return p
	}
	
	private def static <T> notNull(T obj) {
		Assert.assertNotNull(obj)
		return obj
	}
	
	def static getAllContents(Notifier n) {
		PremiseHelper.getAllContents(n, true)
	}
}