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
import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.functionpool.FnDefSpreadSheet;
import de.dlr.premise.functionpool.FnInputSpreadSheet;
import de.dlr.premise.functionpool.provider.FnInputSpreadSheetItemProvider;

public class FnInputSpreadSheetItemProviderMy extends
		FnInputSpreadSheetItemProvider {

	public FnInputSpreadSheetItemProviderMy(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	@Override
	public String getText(Object object) {
		
		// check input
		if (object == null) {
			return null;
		}
		
		// default name
		String label = getString("_UI_FnInputSpreadSheet_type");
		
		// unit name
		FnInputSpreadSheet input = (FnInputSpreadSheet) object;
		String name = input.getName();
		if (name != null && name.length() > 0) {
			label = name;
		}
		
		// add cell
		String cell = input.getCell();
		if (cell != null && cell.length() > 0) {
			
			String defLabel = getString("_UI_FnInputSpreadSheet_type");
			if (!label.equals(defLabel)) {
				label += " > " + cell;
			} else {
				label = cell;
			}
		}
		
		// append unit string
		String symbol = LabelHelper.getUnitSymbol(input.getUnit());
		if (symbol != null) {
			label +=  " [" +  symbol + "]";
		}

		// add ... for multiple input cells
		if(isMultipleInput(input)) {
			label += " ...";
		}
		
		return label;
	}
	
	/**
	 * check if the imput is poart of an input cell
	 */
	public boolean isMultipleInput(final FnInputSpreadSheet input) {
		EObject object = input.eContainer();
		if (object instanceof FnDefSpreadSheet) {
			return ((FnDefSpreadSheet) object).isLastInputMultiple();
		}
				
		return false;
	}
}
