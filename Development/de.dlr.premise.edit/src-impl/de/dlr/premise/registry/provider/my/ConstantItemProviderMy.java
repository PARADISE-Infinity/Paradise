/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.registry.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Strings;

import de.dlr.premise.registry.Constant;
import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.registry.Value;
import de.dlr.premise.registry.provider.ConstantItemProvider;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author hschum
 *
 */
public class ConstantItemProviderMy extends ConstantItemProvider {

    public ConstantItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    @Override
    public String getText(Object object) {
        Constant constant = (Constant) object;

        // type
        String typeName = "";
        if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            typeName = getString("_UI_Constant_type");
            typeName = typeName.replace(" ", "") + " ";
        }

        // label
        String label = constant.getName();
        if (!Strings.isNullOrEmpty(label)) {
            typeName = ""; // remove type name if name of constant is given
            label = label.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
        } else {
            label = "";
        }

        // with value & unit
        String strVal = "";
        Value value = constant.getValue();
        if (value != null && value.getValue() != null && !value.getValue().isEmpty()) {
            strVal = " = " + PremiseHelper.getStrValue(value);
        }
        if (constant.getUnit() != null && constant.getUnit().getSymbol() != null) {
            strVal += " " + SystemItemProviderAdapterFactoryMy.UNIT_PRE + constant.getUnit().getSymbol()
                    + SystemItemProviderAdapterFactoryMy.UNIT_POST;
        }

        return typeName + label + strVal;
    }

    @Override
    public String getUpdateableText(Object object) {
        return AParameterDefItemProviderHelper.getUpdateableText(object);
    }

    @Override
    public void setText(Object object, String string) {
        AParameterDefItemProviderHelper.setText(object, string);
    }

    @Override
    public Object getImage(Object object) {
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/Constant"));
    }
}
