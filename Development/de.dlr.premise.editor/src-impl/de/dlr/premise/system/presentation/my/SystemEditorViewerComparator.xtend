/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my

import de.dlr.premise.component.ComponentPackage
import de.dlr.premise.element.ElementPackage
import de.dlr.premise.util.urihandlers.PremiseLibraryURIHandler
import java.util.Arrays
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.jface.viewers.Viewer
import org.eclipse.jface.viewers.ViewerComparator

/**
 * Sort elements in the SystemEditor TreeViewer.
 * 
 * Most list are returned as given, with the following changes being made:
 * 
 * <ul>
 * 	<li>Resources with "premise:/" schema (like Base.registry) are sorted to the bottom
 * 	<li>Some references are sorted to the top, where they would naturally be expected
 * </li>
 * 
 * Sorts , everything else is returned as given
 */
class SystemEditorViewerComparator extends ViewerComparator {
	override sort(Viewer viewer, Object[] elements) {
		if (elements.exists[indexOffset != 0]) {
			Arrays.sort(elements, [a, b | Integer.compare(a.getIndex(elements), b.getIndex(elements))]);
		}
	}
	
	private def getIndex(Object el, Object[] elements) {
		elements.indexOf(el) + el.indexOffset
	}
	
	private def getIndexOffset(Object obj) {	
		switch (obj) {
			EObject case obj.eContainingFeature == ComponentPackage.Literals.ISATISFYING__SATISFIES,
			EObject case obj.eContainingFeature == ElementPackage.Literals.ICONDITIONAL__CONDITION: -1000
			Resource case obj.URI.scheme == PremiseLibraryURIHandler.PREMISE_LIBRARY_SCHEME: 1000
			default: 0
		}
	}
}