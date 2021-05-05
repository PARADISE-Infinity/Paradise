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

import de.dlr.premise.functionpool.FnOutput;
import de.dlr.premise.functionpool.provider.FnOutputItemProvider;

public class FnOutputItemProviderMy extends FnOutputItemProvider {

	public FnOutputItemProviderMy(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	@Override
	public String getText(Object object) {
		
		// check input
		if (object == null) {
			return null;
		}

		// default name
		String label = getString("_UI_FnOutput_type");
		
		// unit name
		FnOutput output = (FnOutput) object;
		String name = output.getName();
		if ( name != null && name.length() > 0) {
			label = name;
		}
		
		// append unit string
		String symbol = LabelHelper.getUnitSymbol(output.getUnit());
		if (symbol != null) {
			label +=  " [" +  symbol + "]";
		}
		
		return label;
	}
}
