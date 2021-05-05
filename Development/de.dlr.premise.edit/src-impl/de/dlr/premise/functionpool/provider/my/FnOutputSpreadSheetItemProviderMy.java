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

import de.dlr.premise.functionpool.FnOutputSpreadSheet;
import de.dlr.premise.functionpool.provider.FnOutputSpreadSheetItemProvider;

public class FnOutputSpreadSheetItemProviderMy extends
		FnOutputSpreadSheetItemProvider {

	public FnOutputSpreadSheetItemProviderMy(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}
	
	@Override
	public String getText(Object object) {
		
		// check input
		if (object == null) {
			return null;
		}
		
		// default name
		String label = getString("_UI_FnOutputSpreadSheet_type");
		
		// unit name
		FnOutputSpreadSheet output = (FnOutputSpreadSheet) object;
		String name = output.getName();
		if ( name != null && name.length() > 0) {
			label = name;
		}
		
		// add cell
		String cell = output.getCell();
		if (cell != null && cell.length() > 0) {
			
			String defLabel = getString("_UI_FnOutputSpreadSheet_type");
			if (!label.equals(defLabel)) {
				label += " > " + cell;
			} else {
				label = cell;
			}
		}

		// append unit string
		String symbol = LabelHelper.getUnitSymbol(output.getUnit());
		if (symbol != null) {
			label +=  " [" +  symbol + "]";
		}
		
		return label;
	}
}
