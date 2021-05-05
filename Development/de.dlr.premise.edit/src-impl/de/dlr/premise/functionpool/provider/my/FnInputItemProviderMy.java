/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.functionpool.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;

import de.dlr.premise.functionpool.FnInput;
import de.dlr.premise.functionpool.provider.FnInputItemProvider;

public class FnInputItemProviderMy extends FnInputItemProvider {

	public FnInputItemProviderMy(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}
	
	@Override
	public String getText(Object object) {
		
		// default name
		String label = getString("_UI_FnInput_type");
		
		// unit name
		FnInput input = (FnInput) object;
		String name = input.getName();
		if ( name != null && name.length() > 0) {
			label = name;
		}
		
		// append unit string
		String symbol = LabelHelper.getUnitSymbol(input.getUnit());
		if (symbol != null) {
			label +=  " [" +  symbol + "]";
		}

		return label;
	}
}
