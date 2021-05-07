/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.block.wizard

import org.eclipse.emf.ecore.EObject
import org.eclipse.jface.viewers.ILabelProvider
import org.eclipse.swt.graphics.Image
import org.eclipse.xtend.lib.annotations.Delegate
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.*

@FinalFieldsConstructor class UnwrappingDelegatingLabelProvider implements ILabelProvider {
	@Delegate val ILabelProvider delegate

	override String getText(Object element) {
		return delegate.getText(element.unwrap)
	}

	override Image getImage(Object element) {
		return delegate.getImage(element.unwrap)
	}

	override boolean isLabelProperty(Object element, String property) {
		return delegate.isLabelProperty(element.unwrap, property)
	}
	
	private def unwrap(Object obj) {
		switch(obj) {
			EObject case obj.isScopedEObject: obj.wrappedElement
			default: obj
		}
	}
}
