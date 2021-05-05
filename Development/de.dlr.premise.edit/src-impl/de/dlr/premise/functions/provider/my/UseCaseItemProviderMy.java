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
import org.eclipse.emf.edit.provider.ViewerNotification;

import de.dlr.premise.element.AElement;
import de.dlr.premise.element.ElementPackage;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.functions.provider.UseCaseItemProvider;
import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

public class UseCaseItemProviderMy extends UseCaseItemProvider {

    public UseCaseItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    @Override
    public String getText(Object object) {

        // type name
        String typeName = "";
        if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            // show name of 1st metaType (if any defined) or of normal type
            AElement el = (AElement) object;
            if (!el.getMetaTypes().isEmpty()) {
                typeName = el.getMetaTypes().get(0).getName();
            } else {
                typeName = getString("_UI_UseCase_type");
            }
            typeName = typeName.replace(" ", "") + " ";
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

        // add mode dependent labels
        String conditionString = PremiseHelper.getGuardConditionString(((UseCase) object).getCondition());
        if (conditionString != null) {
            label += " : " + conditionString;
        }

        // return name
        return typeName + label + postfix;
    }

    @Override
    public Object getImage(Object object) {
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/UseCase"));
    }

    @Override
    public void notifyChanged(Notification notification) {
        if (notification.getNotifier() instanceof AElement
                && notification.getFeatureID(AElement.class) == ElementPackage.AELEMENT__META_TYPES) {
            AElement element = (AElement) notification.getNotifier();

            updateChildren(notification);
            triggerLabelUpdate(notification, element);
        } else {
            super.notifyChanged(notification);
        }
    }

    private void triggerLabelUpdate(Notification notification, AElement toNotify) {
        fireNotifyChanged(new ViewerNotification(notification, toNotify, true, true));
    }
}
