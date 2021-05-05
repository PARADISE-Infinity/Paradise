/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.registry.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.registry.RegistryPackage;
import de.dlr.premise.registry.Value;
import de.dlr.premise.registry.provider.ValueItemProvider;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author hschum
 *
 */
public class ValueItemProviderMy extends ValueItemProvider {

    public ValueItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    @Override
    public String getText(Object object) {

        // type name
        String typeName = "";
        if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            typeName = getString("_UI_Value_type");
            typeName = typeName.replace(" ", "") + " ";
        }

        EObject repo = ((Value) object).eContainer();
        while (repo != null && repo.eContainer() != null) {
            repo = repo.eContainer();
        }

        return typeName + PremiseHelper.getStrValue((Value) object);
    }

    @Override
    public EAttribute getPrimaryAttribute() {
        return RegistryPackage.Literals.AVALUE_DEF__VALUE;
    }
}
