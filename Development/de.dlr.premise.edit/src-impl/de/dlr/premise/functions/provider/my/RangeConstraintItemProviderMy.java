/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.functions.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.functions.ARange;
import de.dlr.premise.functions.provider.RangeConstraintItemProvider;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author steh_ti
 */
public class RangeConstraintItemProviderMy extends RangeConstraintItemProvider {

    public RangeConstraintItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    @Override
    public String getText(Object object) {
    	
    	// check output
    	if(object == null) {
    		return null;
    	}

        // type name
        String typeName = "";
        if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            typeName = getString("_UI_RangeConstraint_type");
            typeName = typeName.replace(" ", "") + " ";
        }

        // create label
        String label = PremiseHelper.getRangeString((ARange) object);

        // return name
        return typeName + label;
    }

}
