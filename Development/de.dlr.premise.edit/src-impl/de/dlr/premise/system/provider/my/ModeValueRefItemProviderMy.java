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
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.element.AModeCombination;
import de.dlr.premise.registry.RegistryPackage;
import de.dlr.premise.system.ModeValueRef;
import de.dlr.premise.system.provider.ModeValueRefItemProvider;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author hschum
 *
 */
public class ModeValueRefItemProviderMy extends ModeValueRefItemProvider {

    public ModeValueRefItemProviderMy(AdapterFactory adapterFactory) {
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
            typeName = getString("_UI_Value_type");
            typeName = typeName.replace(" ", "") + " ";
        }

        // get label
        
        EObject repo = ((ModeValueRef) object).eContainer();
        while (repo != null && repo.eContainer() != null) {
            repo = repo.eContainer();
        }
        
        String label = ((ModeValueRef) object).getValue();
        if (label != null) {
            label = PremiseHelper.getStrValue(((ModeValueRef) object));
        } else {
            label = "";
        }

        // add mode dependent labels
        label += PremiseHelper.getModeCombinationString((AModeCombination) object);

        // return name
        return typeName + label;
    }

    @Override
    public EAttribute getPrimaryAttribute() {
        return RegistryPackage.Literals.AVALUE_DEF__VALUE;
    }
}
