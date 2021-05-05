/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.element.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.element.Transition;
import de.dlr.premise.element.provider.TransitionItemProvider;
import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

public class TransitionItemProviderMy extends TransitionItemProvider {

	public TransitionItemProviderMy(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	@Override
	public String getText(Object object) {
		
		// check input
		if (object == null) {
			return null;
		}

		// type name
		String typeName = "";
        if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            typeName = getString("_UI_Transition_type");
            typeName = typeName.replace(" ", "") + " ";
        }
		
		// create label
		String label = ((ANameItem) object).getName();
		if (label == null || label.isEmpty()) {
			label = PremiseHelper.getMeaningfulName((ANameItem) object);
		}
		label = label.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
		
		// with qualified name
		String postfix = PremiseHelper.getQualifyingNamePrefix((ANameItem) object);
		if (!postfix.isEmpty()) {
			postfix = SystemItemProviderAdapterFactoryMy.QNAME_PRE + postfix.substring(0, postfix.length()-1) + SystemItemProviderAdapterFactoryMy.QNAME_POST;
			postfix = postfix.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
		}
		
        // add mode dependent labels
        String conditionString = PremiseHelper.getGuardConditionString(((Transition) object).getCondition());
        if (conditionString != null) {
            label += " : " + conditionString;
        }
		
		// return name
		return typeName + label + postfix;
	}
	
	@Override
    public Object getImage(Object object) {
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/Transition"));
    }
}
