/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.system.provider.my;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ViewerNotification;

import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.registry.provider.my.AParameterDefItemProviderHelper;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.SystemPackage;
import de.dlr.premise.system.provider.ParameterItemProvider;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author hschum
 * 
 */
public class ParameterItemProviderMy extends ParameterItemProvider {

    private class LabelChangeAdapter extends AdapterImpl {

        private EObject toNotify;

        public LabelChangeAdapter(EObject toNotify) {
            this.toNotify = toNotify;
        }

        @Override
        public void notifyChanged(Notification notification) {
            super.notifyChanged(notification);
            triggerLabelUpdate(notification, toNotify);
        }
    };

    public ParameterItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    @Override
    public String getText(Object object) {
        
        // check input
        if (object == null) {
            return null;
        }
        
        Parameter parameter = (Parameter) object;
        
        // update label in case of change of value or change of parameter's unit
        addLabelChangeAdapter(parameter, parameter.getValue());
        addLabelChangeAdapter(parameter, parameter);

        // optional type
        String typeName = "";
        if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            typeName = getString("_UI_Parameter_type");
            typeName = typeName.replace(" ", "") + " ";
        }

        // label
        String label = parameter.getName();
        if (label != null) {
            label = label.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
        } else {
            label = "";
        }
        EObject repo = ((Parameter) object).eContainer();
        while (repo != null && repo.eContainer() != null) {
            repo = repo.eContainer();
        }
        // with value & unit, if no mode values available additionally
        String strVal = "";
        if (parameter.getValue() != null)
            strVal = PremiseHelper.getStrValue(parameter.getValue());

        if (strVal != "")
            strVal = " = " + strVal;

        if (parameter.getUnit() != null && parameter.getUnit().getSymbol() != null) {
            strVal += " " + SystemItemProviderAdapterFactoryMy.UNIT_PRE + parameter.getUnit().getSymbol()
                    + SystemItemProviderAdapterFactoryMy.UNIT_POST;
        }

        // with qualified name
        String postfix = PremiseHelper.getQualifyingNamePrefix(parameter);
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
        if (notification.getNotifier() instanceof Parameter
                && notification.getFeatureID(Parameter.class) == SystemPackage.PARAMETER__VALUE) {
            Parameter toNotify = (Parameter) notification.getNotifier();

            updateChildren(notification);
            triggerLabelUpdate(notification, toNotify);
        } else {
            super.notifyChanged(notification);
        }
    }

    private void addLabelChangeAdapter(EObject toNotify, EObject toCheck) {
        if (toCheck != null) {
            Boolean adapterExists = false;
            for (Adapter a : toCheck.eAdapters()) {
                if (a instanceof LabelChangeAdapter) {
                    adapterExists = true;
                    break;
                }
            }
            if (!adapterExists) {
                toCheck.eAdapters().add(new LabelChangeAdapter(toNotify));
            }
        }
    }

    private void triggerLabelUpdate(Notification notification, EObject toNotify) {
        fireNotifyChanged(new ViewerNotification(notification, toNotify, true, true));
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
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/Parameter"));
    }
}
