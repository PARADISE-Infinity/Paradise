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

import de.dlr.premise.element.StateMachine;
import de.dlr.premise.element.provider.StateMachineItemProvider;
import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author bitt_ti
 *
 */
public class StateMachineItemProviderMy extends StateMachineItemProvider {

    public StateMachineItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    @Override
    public String getText(Object object) {

        // check input
        String label = "";
        if (object == null) {
            return "";
        }

        // optional type
        String typeName = "";
        if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            // show name of 1st metaType (if any defined) or of normal type
            StateMachine sm = (StateMachine) object;
            if (!sm.getMetaTypes().isEmpty()) {
                typeName = sm.getMetaTypes().get(0).getName();
            } else {
                typeName = getString("_UI_StateMachine_type");
            }
            typeName = typeName.replace(" ", "") + " ";
        }

        // get label
        label = ((ANameItem) object).getName();
        if (label != null) {
            label = label.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
        } else {
            label = "State Machine";
        }

        // create qualified name
        String postfix = PremiseHelper.getQualifyingNamePrefix((ANameItem) object);
        if (!postfix.isEmpty()) {
            postfix = SystemItemProviderAdapterFactoryMy.QNAME_PRE + postfix.substring(0, postfix.length() - 1)
                    + SystemItemProviderAdapterFactoryMy.QNAME_POST;
            postfix = postfix.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
        }

        // return name
        return typeName + label + postfix;
    }
    
    @Override
    public Object getImage(Object object) {
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/StateMachine"));
    }
}
