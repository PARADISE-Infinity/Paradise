/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.util.urihandlers

import java.io.IOException
import java.util.Map
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.URIHandler
import org.eclipse.emf.ecore.resource.impl.URIHandlerImpl
import org.eclipse.emf.ecore.resource.URIConverter

/**
 * Handles URIs starting with "premise:/"
 * 
 * URIs are translated to "platform:/plugin/de.dlr.premise/library". This hides the internal plugin names and allows for renames if necessary.
 */
class PremiseLibraryURIHandler implements URIHandler {
	val public static PREMISE_LIBRARY_SCHEME = "premise"
	val public static PREMISE_GLOBAL_REGISTRY_URI = URI.createURI('''«PREMISE_LIBRARY_SCHEME»:/Base.registry''')
	
	def static registerInto(URIConverter converter) {
		converter.URIHandlers.removeIf[it instanceof PremiseLibraryURIHandler]
		converter.URIHandlers.add(0, new PremiseLibraryURIHandler())
	}
	
	val delegate = new URIHandlerImpl()
	
	override canHandle(URI uri) {
		try {
			val convertedUri = uri.translateURI
			delegate.canHandle(convertedUri)
		}	catch (Throwable e) {
			false
		}
	}
	
	override contentDescription(URI uri, Map<?, ?> options) throws IOException {
		delegate.contentDescription(uri.translateURI, options)
	}
	
	override createInputStream(URI uri, Map<?, ?> options) throws IOException {
		delegate.createInputStream(uri.translateURI, options)
	}
	
	override createOutputStream(URI uri, Map<?, ?> options) throws IOException {
		delegate.createOutputStream(uri.translateURI, options)
	}
	
	override delete(URI uri, Map<?, ?> options) throws IOException {
		delegate.delete(uri.translateURI, options)
	}
	
	override exists(URI uri, Map<?, ?> options) {
		delegate.exists(uri.translateURI, options)
	}
	
	override getAttributes(URI uri, Map<?, ?> options) {
		delegate.getAttributes(uri.translateURI, options)
	}
	
	override setAttributes(URI uri, Map<String, ?> attributes, Map<?, ?> options) throws IOException {
		delegate.setAttributes(uri.translateURI, attributes, options)
	}
	
	private def translateURI(URI uri) {
		if (uri.scheme != PREMISE_LIBRARY_SCHEME) {
			throw new IllegalArgumentException()
		}
		URI.createPlatformPluginURI('''/de.dlr.premise/library/«uri.path»''', false)
	}
	
}