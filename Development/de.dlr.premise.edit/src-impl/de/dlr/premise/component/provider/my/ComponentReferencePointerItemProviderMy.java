/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.component.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;

import de.dlr.premise.component.ComponentReferencePointer;
import de.dlr.premise.component.provider.ComponentReferencePointerItemProvider;
import de.dlr.premise.graph.INode;
import de.dlr.premise.system.ComponentReference;


public class ComponentReferencePointerItemProviderMy extends ComponentReferencePointerItemProvider {

    private final AdapterFactory rootAdapterFactory;

    public ComponentReferencePointerItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);

        if (adapterFactory instanceof ComposeableAdapterFactory) {
            this.rootAdapterFactory = ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory();
        } else {
            this.rootAdapterFactory = adapterFactory;
        }
    }

    @Override
    public String getText(Object object) {
        if (object instanceof ComponentReferencePointer) {
            @SuppressWarnings("unchecked")
            ComponentReferencePointer<INode> pointer = (ComponentReferencePointer<INode>) object;
            
            String label = "";

            IItemLabelProvider defLabelProvider = getLabelProvider(pointer.getDefinition());
            if (defLabelProvider != null) {
                label += defLabelProvider.getText(pointer.getDefinition());
            }

            ComponentReference componentReference = pointer.getComponentReference();
            if (componentReference != null && componentReference.getName() != null && !"".equals(componentReference.getName())) {
                label += " (" + componentReference.getName() + ")";
            }

            return label;
        }
        return "<invalid>";
    }

    @Override
    public Object getImage(Object object) {
        if (object instanceof ComponentReferencePointer) {
            @SuppressWarnings("unchecked")
            ComponentReferencePointer<INode> pointer = (ComponentReferencePointer<INode>) object;
            
            IItemLabelProvider defLabelProvider = getLabelProvider(pointer.getDefinition());
            if (defLabelProvider != null) {
                return defLabelProvider.getImage(pointer.getDefinition());
            }
        }
        return super.getText(object);
    }

    private IItemLabelProvider getLabelProvider(Object object) {
        return (IItemLabelProvider) rootAdapterFactory.adapt(object, IItemLabelProvider.class);
    }

}
