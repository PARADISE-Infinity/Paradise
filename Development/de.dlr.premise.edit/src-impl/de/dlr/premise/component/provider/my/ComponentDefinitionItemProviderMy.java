/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.component.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.component.provider.ComponentDefinitionItemProvider;
import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

public class ComponentDefinitionItemProviderMy extends ComponentDefinitionItemProvider {

    public ComponentDefinitionItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }
    
    @Override
    public String getText(Object object) {

        // check input
        if (object == null) {
            return null;
        }
        final EObject eObject = (EObject) object;
        
        // optional type
        String typeName = "";
        if (PremiseHelper.isSet(eObject, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            // show name of 1st metaType (if any defined) or of normal type
            IMetaTypable el = (IMetaTypable) eObject;
            if (!el.getMetaTypes().isEmpty()) {
                typeName = el.getMetaTypes().get(0).getName();
            } else {
                typeName = getString("_UI_ComponentDefinition_type");
            }
            typeName = typeName.replace(" ", "") + " ";
        } else {
            typeName = "";
        }

        // label
        String label = ((ANameItem) object).getName();
        if (label != null) {
            label = label.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
        } else {
            label = "";
        }

        // with qualified name
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
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/ComponentDefinition"));
    }
}
