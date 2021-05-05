/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.element.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.element.GuardCombination;
import de.dlr.premise.element.provider.GuardCombinationItemProvider;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.registry.ANameItem;

/**
 * @author hschum
 *
 */
public class GuardCombinationItemProviderMy extends GuardCombinationItemProvider {

	public GuardCombinationItemProviderMy(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	@Override
	public String getText(Object object) {

		// check input
		String label = "";
		if (object == null) {
			return label;
		}
		
		// type name
		String typeName = "";
		if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
			typeName = getString("_UI_GuardCombination_type");
			typeName = typeName.replace(" ", "") + " ";
		}
		
		// create label
		label = ((GuardCombination) object).getJunction().toString();
		
		// get label
        String name = ((ANameItem) object).getName();
        if (name != null) {
            name = name.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
            name = " " + name;
        } else {
            name = "";
        }
		
		// return name
		return typeName + label + name;
	}
}
