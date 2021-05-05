/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.query.persistent

import de.dlr.premise.query.QueryMode
import org.eclipse.xtend.lib.annotations.Data

@Data
class PersistableQuery {
	String name
	String query
	QueryMode mode

	@Pure
	def assocName(String name) {
		new PersistableQuery(name, query, mode)
	}

	@Pure
	def assocQuery(String query) {
		new PersistableQuery(name, query, mode)
	}

	@Pure
	def assocMode(QueryMode mode) {
		new PersistableQuery(name, query, mode)
	}

	override equals(Object obj) {
		if (this === obj)
			return true;
		if (obj === null)
			return false;
		if (getClass() !== obj.getClass())
			return false;
		val other = obj as PersistableQuery;
		if (this.query === null) {
			if (other.query !== null)
				return false;
		} else if (!this.query.equals(other.query))
			return false;
		if (this.mode === null) {
			if (other.mode !== null)
				return false;
		} else if (!this.mode.equals(other.mode))
			return false;
		return true;
	}
}
