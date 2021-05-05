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

import com.google.inject.Inject
import java.io.InputStream
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.resource.FileExtensionProvider
import org.eclipse.xtext.resource.IResourceFactory
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.util.LazyStringInputStream
import org.eclipse.xtext.resource.XtextResourceSet
import com.google.inject.Provider

class ResourceProviderMy {
	@Inject FileExtensionProvider ext
	@Inject Provider<XtextResourceSet> resourceSetProvider
	@Inject IResourceFactory resourceFactory
	
	def XtextResource createResource() {
		val resSet = resourceSetProvider.get()
		resourceFactory.createResource(URI.createURI('''synthetic:/calc.«ext.getPrimaryFileExtension()»''')) as XtextResource => [
			resSet.resources += it
		]
	}
	
	def XtextResource createResource(CharSequence content) {
		createResource() => [
			load(content.asStream, null)
		]
	}
	
	def private InputStream asStream(CharSequence text) {
		return new LazyStringInputStream(if (text == null) "" else text.toString)
	}
}