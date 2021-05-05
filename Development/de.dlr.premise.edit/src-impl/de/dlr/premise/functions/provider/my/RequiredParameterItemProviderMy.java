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
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.ViewerNotification;

import de.dlr.premise.functions.AValueConstraint;
import de.dlr.premise.functions.RequiredParameter;
import de.dlr.premise.functions.provider.RequiredParameterItemProvider;
import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

public class RequiredParameterItemProviderMy extends RequiredParameterItemProvider {

    public RequiredParameterItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    @Override
    public String getText(Object object) {

        RequiredParameter reqParam = (RequiredParameter) object;

        // optional type name
        String typeName = "";
        if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            typeName = getString("_UI_RequiredParameter_type");
            typeName = typeName.replace(" ", "") + " ";
        }

        // label
        String label = reqParam.getName();
        if (label != null) {
            label = label.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
        } else {
            label = "";
        }

        // with range
        String strVal = "";
        AValueConstraint valueConstraint = reqParam.getValueConstraint();
        if (valueConstraint != null) {
            IItemLabelProvider constraintLabeler = (IItemLabelProvider) adapterFactory.adapt(valueConstraint, IItemLabelProvider.class);
            strVal += " = " + constraintLabeler.getText(valueConstraint).replaceFirst(".*\\[", "[");
            if (!valueConstraint.eAdapters().contains(this)) {
                valueConstraint.eAdapters().add(this);
            }
        }

        // with unit
        if (reqParam.getUnit() != null && reqParam.getUnit().getSymbol() != null) {
            strVal += " " + SystemItemProviderAdapterFactoryMy.UNIT_PRE + reqParam.getUnit().getSymbol()
                    + SystemItemProviderAdapterFactoryMy.UNIT_POST;
        }

        // with qualified name
        String postfix = PremiseHelper.getQualifyingNamePrefix(reqParam);
        if (!postfix.isEmpty()) {
            postfix = SystemItemProviderAdapterFactoryMy.QNAME_PRE + postfix.substring(0, postfix.length() - 1)
                    + SystemItemProviderAdapterFactoryMy.QNAME_POST;
            postfix = postfix.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
        }

        // return name
        return typeName + label + strVal + postfix;
    }

    @Override
    public void notifyChanged(Notification notification) {
        if (notification.getNotifier() instanceof AValueConstraint) {
            RequiredParameter toNotify = (RequiredParameter) ((AValueConstraint) notification.getNotifier()).eContainer();
            updateChildren(notification);
            fireNotifyChanged(new ViewerNotification(notification, toNotify, true, true));
        } else {
            super.notifyChanged(notification);
        }
    }

    @Override
    public Object getImage(Object object) {
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/RequiredParameter"));
    }
}
