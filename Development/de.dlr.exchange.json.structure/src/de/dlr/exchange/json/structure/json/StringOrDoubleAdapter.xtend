/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.json.structure.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import de.dlr.exchange.json.structure.model.StringOrDouble
import java.lang.reflect.Type

class StringOrDoubleAdapter implements JsonSerializer<StringOrDouble>, JsonDeserializer<StringOrDouble> {
	
	override serialize(StringOrDouble src, Type typeOfSrc, JsonSerializationContext context) {
		try {
			return new JsonPrimitive(Double.parseDouble(src.string))
		} catch (NumberFormatException e) {
			return new JsonPrimitive(src.string)
		}
	}
	
	override deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		try {
			return new StringOrDouble(json.asString)
		} catch (Throwable e) {
			throw new JsonParseException("Couldn't convert to json", e)
		}
	}
	
}