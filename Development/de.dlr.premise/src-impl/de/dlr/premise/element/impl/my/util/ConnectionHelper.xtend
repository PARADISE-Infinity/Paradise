/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.element.impl.my.util

import de.dlr.premise.component.ComponentReferencePointer
import de.dlr.premise.graph.DirectPointer
import de.dlr.premise.element.AElement
import de.dlr.premise.element.Connection
import org.eclipse.emf.common.util.ECollections
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.IComponent
import de.dlr.premise.system.SystemComponent

import static extension de.dlr.premise.util.PremiseHelper.closure
import static extension de.dlr.premise.util.PremiseHelper.toUnmodifieableEList

class ConnectionHelper {
	static def queryValidSourcePointerParentComponentReferenceList(Connection<?> con) {
		doQueryValidSourceOrTargetPointerParentComponentReferenceList(con)
	}	
	
	static def queryValidTargetPointerParentComponentReferenceList(Connection<?> con) {
		doQueryValidSourceOrTargetPointerParentComponentReferenceList(con)
	}
	
	static def <T extends AElement> isSourcePointerValid(Connection<T> con) {
		val sourcePointer = con.sourcePointer
		switch (sourcePointer) {
			case null: false
			DirectPointer<T>: con.queryValidSourcePointerList.contains(sourcePointer.target)
			ComponentReferencePointer<T>: con.queryValidSourcePointerParentComponentReferenceList.contains(sourcePointer.componentReference)
			default: throw new IllegalArgumentException
		}
	}
	
	static def <T extends AElement> isTargetPointerValid(Connection<T> con) {
		val targetPointer = con.targetPointer
		switch (targetPointer) {
			case null: false
			DirectPointer<T>: con.queryValidTargetPointerList.contains(targetPointer.target)
			ComponentReferencePointer<T>: con.queryValidTargetPointerParentComponentReferenceList.contains(targetPointer.componentReference)
			default: throw new IllegalArgumentException
		}
	}
	
	private static def doQueryValidSourceOrTargetPointerParentComponentReferenceList(Connection<?> con) {
		val eContainer = con.eContainer()
		
		switch(eContainer) {
			SystemComponent: eContainer
				.closure[IComponent comp | if (comp instanceof SystemComponent) comp.children else #[]]
				.filter(ComponentReference)
				.toUnmodifieableEList
			default: ECollections.<ComponentReference>emptyEList()
		}
	}
}