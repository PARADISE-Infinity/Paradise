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
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;

import de.dlr.premise.element.Relation;
import de.dlr.premise.element.provider.RelationItemProvider;
import de.dlr.premise.graph.INode;
import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author hschum, berr_ae, steh_ti
 *
 */
public class RelationItemProviderMy extends RelationItemProvider {

    public RelationItemProviderMy(AdapterFactory adapterFactory) {
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
            typeName = getString("_UI_Relation_type");
            typeName = typeName.replace(" ", "") + " ";
        }
       
        // create label
        String label = "";
        AdapterFactory myAdFactory = ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory();
        IItemLabelProvider adapter;
        INode src = ((Relation) object).getSource();
        if (src != null) {
            adapter = (IItemLabelProvider) myAdFactory.adapt(src, IItemLabelProvider.class);
            label += adapter.getText(src);
        }
        INode dest = ((Relation) object).getTarget();
        if (dest != null) {
            adapter = (IItemLabelProvider) myAdFactory.adapt(dest, IItemLabelProvider.class);
            label += " > " + adapter.getText(dest);
        }

        // return name
        return typeName + label;
    }

    @Override
    public Object getImage(Object object) {
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/Relation"));
    }
}
