/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.representation.impl.my;

import de.dlr.premise.representation.Representation;
import de.dlr.premise.representation.impl.RepresentationFactoryImpl;
import de.dlr.premise.util.PremiseHelper;

public class RepresentationFactoryImplMy extends RepresentationFactoryImpl {

	@Override
	public Representation createRepresentation() {
		Representation dataItem = super.createRepresentation();
        // EMF does not serialize/save default values of attributes, so, get and set it explicitly to ensure serialization:
		dataItem.setMetaModel(dataItem.getMetaModel());
		dataItem.setName("set a name");	
		dataItem.setId(PremiseHelper.createId());
		return dataItem;
	}
}
