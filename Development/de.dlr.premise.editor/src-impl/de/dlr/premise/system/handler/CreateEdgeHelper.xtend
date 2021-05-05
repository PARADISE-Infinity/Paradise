/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.handler

import de.dlr.premise.component.ComponentFactory
import de.dlr.premise.component.ComponentReferencePointer
import de.dlr.premise.component.IDefinition
import de.dlr.premise.component.ISatisfying
import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.GraphFactory
import de.dlr.premise.graph.INode
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.provider.my.ComponentReferenceChildrenDelegatingWrapperItemProvider
import org.eclipse.emf.ecore.EObject

class CreateEdgeHelper {
	def static dispatch APointer<EObject> convertSelectedToPointer(EObject obj) {
		GraphFactory.eINSTANCE.createDirectPointer => [
			target = obj
		]
	}
	
	def static dispatch ComponentReferencePointer<INode> convertSelectedToPointer(ComponentReference compRef) {
		if (compRef.componentDefinition != null) {
			return ComponentFactory.eINSTANCE.<INode>createComponentReferencePointer => [
				componentReference = compRef
				definition = compRef.componentDefinition as IDefinition<?> as IDefinition<INode>
			]
		}
		
		return null
	}

	
	def static dispatch APointer<? extends EObject> convertSelectedToPointer(ComponentReferenceChildrenDelegatingWrapperItemProvider wrapper) {
		if (wrapper.value instanceof ComponentReference) {
			return convertSelectedToPointer(wrapper.value)
		}
		
		val definitionCandidate = wrapper.definition
		if (definitionCandidate != null) {
			return ComponentFactory.eINSTANCE.<INode>createComponentReferencePointer => [
				componentReference = wrapper.scope.last
				definition = definitionCandidate
			]
		} 
		
		return null
	}
	
	def private static getDefinition(ComponentReferenceChildrenDelegatingWrapperItemProvider wrapper) {
		val wrapped = wrapper.value
		
		switch(wrapped) {
			IDefinition<?>: wrapped
			ISatisfying<?, ?>: (wrapped as ISatisfying<INode, ?>).satisfiedSatisfieables
				.filter(IDefinition)
				.findFirst[IDefinition<?> it |
					val definition = wrapper.scope.last.componentDefinition
					it == definition || it.eContainer == definition
				]
		} as IDefinition<INode>
	}
}