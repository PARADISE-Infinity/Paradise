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
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;

import de.dlr.premise.component.ISatisfieable;
import de.dlr.premise.component.Satisfies;
import de.dlr.premise.component.provider.SatisfiesItemProvider;
import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

public class SatisfiesItemProviderMy extends SatisfiesItemProvider {

    public SatisfiesItemProviderMy(AdapterFactory adapterFactory) {
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
            typeName = getString("_UI_Satisfies_type");
            typeName = typeName.replace(" ", "") + " ";
        }

        // create label
        AdapterFactory rootAdapterFactory = ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory();
        String label = "";
        ISatisfieable dest = ((Satisfies<?, ?>) object).getTarget();
        if (dest != null) {
            IItemLabelProvider adapter = (IItemLabelProvider) rootAdapterFactory.adapt(dest, IItemLabelProvider.class);
            label += " > " + adapter.getText(dest);
        }

        // return name
        return typeName + label;
    }

    @Override
    public Object getImage(Object object) {
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/Satisfies"));
    }
}
