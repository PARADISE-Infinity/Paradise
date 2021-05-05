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

import org.eclipse.emf.ecore.resource.URIConverter

class URIHandlerHelper {
	static def registerInto(URIConverter converter) {
		 // register the LegacyPremiseURIHandler to prevent freezing on unknown namespace URIs
        LegacyPremiseURIHandler.registerInto(converter)
         // register the PremiseLibraryURIHandler for "premise:/" URIs
        PremiseLibraryURIHandler.registerInto(converter)
	}
}