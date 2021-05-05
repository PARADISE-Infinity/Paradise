/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.handler

import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.system.provider.my.ComponentReferenceChildrenDelegatingWrapperItemProvider
import de.dlr.premise.util.scope.ScopedEObjectFactory
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.commands.ExecutionException
import org.eclipse.core.expressions.PropertyTester
import org.eclipse.emf.ecore.EObject
import org.eclipse.jface.viewers.StructuredSelection

import static extension org.eclipse.ui.handlers.HandlerUtil.*
import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.*

import static extension de.dlr.premise.util.SubstitutionMappingHelper.*

class CreateSubstitutionHandler extends AbstractHandler {
	
	static class CreateSubstitutionPropertyTester extends PropertyTester {
		public static final String PROPERTY_IS_SUBSTITUTABLE = "isSubstitutable";
		
		override test(Object receiver, String property, Object[] args, Object expectedValue) {
			if (property == PROPERTY_IS_SUBSTITUTABLE) {
				val expected = Boolean.valueOf(expectedValue.toString)
				
				expected == isSubstitutable(receiver as ComponentReferenceChildrenDelegatingWrapperItemProvider)
			} else {
				false
			}
		}
		
		private def isSubstitutable(ComponentReferenceChildrenDelegatingWrapperItemProvider provider)  {
			provider.delegateValue instanceof AParameterDef && !provider.isSubsituted
		}
	}
	
	override execute(ExecutionEvent event) throws ExecutionException {
		val selection = event.currentSelectionChecked as StructuredSelection
		
		val firstElement = selection.firstElement
		if (firstElement instanceof ComponentReferenceChildrenDelegatingWrapperItemProvider) {
			val parameter = firstElement.scoped
			if (parameter instanceof AParameterDef) {
				val mapping = parameter.scope.head.createSubstitution(parameter)
				mapping.pinned = true
			}
		}
		
		null
	}
	
	
	
	def private getScoped(ComponentReferenceChildrenDelegatingWrapperItemProvider prov) {
		ScopedEObjectFactory.INSTANCE.createScopedEObjectForScope(prov.delegateValue as EObject, prov.scope)
	}
}