/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.aspect.graphml.ui

import java.util.List
import org.eclipse.core.expressions.PropertyTester
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ResourcesPlugin 

import static extension de.dlr.aspect.graphml.ui.GraphMLHandlerHelper.*

class GraphMLPropertyTester extends PropertyTester {
	public static final String PROPERTY_EXPORTED_FILE_EXISTS = "exportedFileExists";
	
	override test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (property == PROPERTY_EXPORTED_FILE_EXISTS) {
			val expected = Boolean.valueOf(expectedValue.toString)
			
			expected == exportedFileExists(receiver as List<IResource>)
		} else {
			false
		}
	}
	
	private def exportedFileExists(List<IResource> elements)  {
		val graphMLFileURI = elements.filter(IFile).getGraphMLFileURI
		val file = ResourcesPlugin.getWorkspace().getRoot().findMember(graphMLFileURI.toPlatformString(false))
		
		file !== null
	}
}