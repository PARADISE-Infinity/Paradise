/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.registry.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;

import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.registry.Unit;
import de.dlr.premise.registry.provider.UnitItemProvider;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

public class UnitItemProviderMy extends UnitItemProvider {

	public UnitItemProviderMy(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}
	
	@Override
	public String getText(Object object) {		
		// default name
		String label = getString("_UI_Unit_type");
		
		// unit name
		Unit unit = (Unit) object;
		String name = unit.getName();
		if ( name == null || name.length() == 0) {
			return label;
		} else {
			label = name;
		}

		// add unit symbol
		String symbol = unit.getSymbol();
		if (symbol != null ) {
			label = label + " " + "["+ symbol +"]"; 
		}
		
        // with qualified name
        String postfix = PremiseHelper.getQualifyingNamePrefix((ANameItem) object);
        if (!postfix.isEmpty()) {
            postfix = SystemItemProviderAdapterFactoryMy.QNAME_PRE + postfix.substring(0, postfix.length() - 1)
                    + SystemItemProviderAdapterFactoryMy.QNAME_POST;
            postfix = postfix.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
        }

		
		return label + postfix;
	}
}
