/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.json.structure.json

import com.google.gson.GsonBuilder
import org.eclipse.emf.common.util.URI
import de.dlr.exchange.json.structure.model.StringOrDouble

class GsonFactory {
	def static createGson() {
		new GsonBuilder()
			.setPrettyPrinting()	
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ssz")
			.registerTypeAdapter(URI, new UriAdapter())
			.registerTypeAdapter(StringOrDouble, new StringOrDoubleAdapter())
			.create()
	}
}