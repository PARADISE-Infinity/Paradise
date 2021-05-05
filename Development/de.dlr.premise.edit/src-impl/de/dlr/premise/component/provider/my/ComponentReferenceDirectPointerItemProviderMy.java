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

import java.util.stream.Collectors;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;

import de.dlr.premise.component.ComponentReferenceDirectPointer;
import de.dlr.premise.component.provider.ComponentReferenceDirectPointerItemProvider;

public class ComponentReferenceDirectPointerItemProviderMy extends ComponentReferenceDirectPointerItemProvider {


    private final AdapterFactory rootAdapterFactory;

    public ComponentReferenceDirectPointerItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
        
        if (adapterFactory instanceof ComposeableAdapterFactory) {
            this.rootAdapterFactory = ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory();
        } else {
            this.rootAdapterFactory = adapterFactory;
        }
        
    }

    @Override
    public String getText(Object object) {
        if (object instanceof ComponentReferenceDirectPointer) {
            @SuppressWarnings("unchecked")
            ComponentReferenceDirectPointer<Object> pointer = (ComponentReferenceDirectPointer<Object>) object;
            
            String label = "";

            IItemLabelProvider defLabelProvider = getLabelProvider(pointer.getTarget());
            if (defLabelProvider != null) {
                label += defLabelProvider.getText(pointer.getTarget());
            }

            
            if (!pointer.getRelativeScope().isEmpty()) {
                label += " (" ;
                label += pointer.getRelativeScope().stream().map(compRef -> compRef.getName()).collect(Collectors.joining("."));
                label += ")";
            }
            
            return label;
        }
        return "<invalid>";
    }
    
    @Override
    public Object getImage(Object object) {
        if (object instanceof ComponentReferenceDirectPointer) {
            @SuppressWarnings("unchecked")
            ComponentReferenceDirectPointer<Object> pointer = (ComponentReferenceDirectPointer<Object>) object;
            IItemLabelProvider labelProvider = getLabelProvider(pointer.getTarget());
            if (labelProvider != null) {
                return labelProvider.getImage(pointer.getTarget());
            }
        }
        return null;
    }


    private IItemLabelProvider getLabelProvider(Object object) {
        return (IItemLabelProvider) rootAdapterFactory.adapt(object, IItemLabelProvider.class);
    }
}
