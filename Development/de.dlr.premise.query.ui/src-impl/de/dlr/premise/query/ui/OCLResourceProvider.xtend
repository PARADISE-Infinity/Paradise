/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.query.ui

import com.google.inject.Inject
import com.google.inject.Provider
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EClassifier
import org.eclipse.ocl.pivot.internal.context.EClassContext
import org.eclipse.ocl.xtext.base.utilities.BaseCSResource
import org.eclipse.xtext.resource.FileExtensionProvider
import org.eclipse.xtext.resource.IResourceFactory
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet

class OCLResourceProvider {
	@Inject FileExtensionProvider ext
	@Inject Provider<XtextResourceSet> resourceSetProvider
	@Inject IResourceFactory resourceFactory
	@Inject EClassifier context

	def XtextResource createResource() {
		val resSet = resourceSetProvider.get()
		val uri = URI.createURI('''synthetic:/query.«ext.getPrimaryFileExtension()»''')
		val resource = resourceFactory.createResource(uri) as XtextResource => [
			resSet.resources += it
		]
		val baseResource = resource as BaseCSResource
		baseResource.parserContext = new EClassContext(baseResource.environmentFactory, uri, context);
		return resource
	}

}
