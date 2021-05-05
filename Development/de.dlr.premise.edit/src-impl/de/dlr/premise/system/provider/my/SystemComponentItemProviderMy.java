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

import java.util.Collection;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.provider.ViewerNotification;

import de.dlr.premise.element.AElement;
import de.dlr.premise.element.ElementPackage;
import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.SystemPackage;
import de.dlr.premise.system.provider.SystemComponentItemProvider;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author hschum
 *
 */
public class SystemComponentItemProviderMy extends SystemComponentItemProvider {

    public SystemComponentItemProviderMy(AdapterFactory adapterFactory) {
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
            // show name of 1st metaType (if any defined) or of normal type
            AElement el = (AElement) object;
            if (!el.getMetaTypes().isEmpty()) {
                typeName = el.getMetaTypes().get(0).getName();
            } else {
                typeName = getString("_UI_SystemComponent_type");
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

        // return name
        return typeName + label + postfix;
    }

    @Override
    public Object getImage(Object object) {
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/SystemComponent"));
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

    /**
     * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
     * that can be created under this object.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated NOT
     */
    @Override
    protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
        super.collectNewChildDescriptors(newChildDescriptors, object);

        // always add value to parameter
        for (Object ncd : newChildDescriptors) {
            if (ncd instanceof CommandParameter) {
                CommandParameter commandParameter = (CommandParameter) ncd;
                if (commandParameter.feature == SystemPackage.Literals.SYSTEM_COMPONENT__PARAMETERS) {
                    Parameter param = (Parameter) commandParameter.value;
                    param.setValue(RegistryFactory.eINSTANCE.createValue());
                }
                
            }
        }
    }
    
//    @Override
//    protected EAttribute getPrimaryAttribute() {
//        return RegistryPackage.Literals.ANAME_ITEM__NAME;
//    }
}
