/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.component.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.component.ParameterDefinition;
import de.dlr.premise.component.provider.ParameterDefinitionItemProvider;
import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

public class ParameterDefinitionItemProviderMy extends ParameterDefinitionItemProvider {

    public ParameterDefinitionItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }
    
    @Override
    public String getText(Object object) {
        
        // set type input
        ParameterDefinition parameterDefinition = null;
        if (object instanceof ParameterDefinition) {
            parameterDefinition = (ParameterDefinition) object;
        } else {
            return null;
        }

        // optional type
        String typeName = "";
        if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            typeName = getString("_UI_ParameterDefinition_type");
            typeName = typeName.replace(" ", "") + " ";
        }

        // label
        String label = parameterDefinition.getName();
        if (label != null) {
            label = label.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
        } else {
            label = "";
        }

        // get unit
        String strVal = "";
        if (parameterDefinition.getUnit() != null && parameterDefinition.getUnit().getSymbol() != null) {
            strVal += " " + SystemItemProviderAdapterFactoryMy.UNIT_PRE + parameterDefinition.getUnit().getSymbol()
                    + SystemItemProviderAdapterFactoryMy.UNIT_POST;
        }

        // with qualified name
        String postfix = PremiseHelper.getQualifyingNamePrefix(parameterDefinition);
        if (!postfix.isEmpty()) {
            postfix = SystemItemProviderAdapterFactoryMy.QNAME_PRE + postfix.substring(0, postfix.length() - 1)
                    + SystemItemProviderAdapterFactoryMy.QNAME_POST;
            postfix = postfix.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
        }

        // return name
        return typeName + label + strVal + postfix;
    }

    @Override
    public Object getImage(Object object) {
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/ParameterDefinition"));
    }    
}
