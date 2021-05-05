/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.system.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.system.provider.BalancingItemProvider;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author hschum
 *
 */
public class BalancingItemProviderMy extends BalancingItemProvider {

    public BalancingItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    @Override
    public String getText(Object object) {

        // check input
        if (object == null) {
            return null;
        }

        // get type name
        String typeName = "";
        if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            typeName = getString("_UI_Balancing_type");
            typeName = typeName.replace(" ", "") + " ";
        }

        // create label
        String label = PremiseHelper.getMeaningfulName((ANameItem) object);
        label = label.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");

        // return name
        return typeName + label;
    }

    @Override
    public Object getImage(Object object) {
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/Balancing"));
    }
}
