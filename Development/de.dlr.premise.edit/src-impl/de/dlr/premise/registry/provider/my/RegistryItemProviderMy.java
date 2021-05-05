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
import de.dlr.premise.registry.provider.RegistryItemProvider;

/**
 * @author Admin
 *
 */
public class RegistryItemProviderMy extends RegistryItemProvider {

	public RegistryItemProviderMy(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}
	
	@Override
	public String getText(Object object) {
		
		// default name
		String label = getString("_UI_Registry_type");
		
		// unit name
		ANameItem registry = (ANameItem) object;
		String name = registry.getName();
		if ( name == null || name.length() == 0) {
			return label;
		} else {
			label = name;
		}
		
		return label;
	}	

}
