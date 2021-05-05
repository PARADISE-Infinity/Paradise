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

import de.dlr.premise.element.Mode;
import de.dlr.premise.element.ModeGuard;
import de.dlr.premise.element.provider.ModeGuardItemProvider;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author hschum
 *
 */
public class ModeGuardItemProviderMy extends ModeGuardItemProvider {

	public ModeGuardItemProviderMy(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	@Override
	public String getText(Object object) {
		
		// check input
		if (object == null) {
			return null;
		}

		// optional type
		String typeName = "";
		if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
			typeName = getString("_UI_ModeGuard_type");
			typeName = typeName.replace(" ", "") + " ";
		}

		// create label
		String label = "";
		Mode trigger = ((ModeGuard) object).getMode();
		if (trigger != null) {
            ModeItemProviderMy adapter =
                    (ModeItemProviderMy) new ElementItemProviderAdapterFactoryMy().createModeAdapter();
			label = adapter.getText(trigger);
		}
		
		// return name
		return typeName + label;
	}
}
