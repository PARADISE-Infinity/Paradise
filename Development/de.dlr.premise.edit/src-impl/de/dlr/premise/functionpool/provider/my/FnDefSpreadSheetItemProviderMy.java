/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.functionpool.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;

import de.dlr.premise.functionpool.FnDefSpreadSheet;
import de.dlr.premise.functionpool.provider.FnDefSpreadSheetItemProvider;

public class FnDefSpreadSheetItemProviderMy extends
		FnDefSpreadSheetItemProvider {

	public FnDefSpreadSheetItemProviderMy(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}
	
	@Override
	public String getText(Object object) {
		
		// check input
		if (object == null) {
			return null;
		}

		// default name
		String label = getString("_UI_FnDefSpreadSheet_type");
		
		// unit name
		FnDefSpreadSheet fun = (FnDefSpreadSheet) object;
		String name = fun.getName();
		if ( name != null && name.length() > 0) {
			label = name;
		}
		
		// add ... for multiple input cells
		if(fun.isLastInputMultiple()) {
			label += " ...";
		}
		
		return label;
	}
}
