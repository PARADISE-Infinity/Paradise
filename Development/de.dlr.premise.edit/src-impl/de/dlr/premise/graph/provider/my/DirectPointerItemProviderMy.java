/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.graph.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;

import de.dlr.premise.graph.APointer;
import de.dlr.premise.graph.DirectPointer;
import de.dlr.premise.graph.provider.DirectPointerItemProvider;

public class DirectPointerItemProviderMy extends DirectPointerItemProvider {


    private final AdapterFactory rootAdapterFactory;

    public DirectPointerItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
        
        if (adapterFactory instanceof ComposeableAdapterFactory) {
            this.rootAdapterFactory = ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory();
        } else {
            this.rootAdapterFactory = adapterFactory;
        }
        
    }

    @Override
    public String getText(Object object) {
        if (object instanceof DirectPointer) {
            @SuppressWarnings("unchecked")
            DirectPointer<Object> directPointer = (DirectPointer<Object>) object;
            IItemLabelProvider labelProvider = getTargetLabelProvider(directPointer);
            if (labelProvider != null) {
                return labelProvider.getText(directPointer.getTarget());
            }
        }
        return "<invalid>";
    }
    
    @Override
    public Object getImage(Object object) {
        if (object instanceof DirectPointer) {
            @SuppressWarnings("unchecked")
            DirectPointer<Object> directPointer = (DirectPointer<Object>) object;
            IItemLabelProvider labelProvider = getTargetLabelProvider(directPointer);
            if (labelProvider != null) {
                return labelProvider.getImage(directPointer.getTarget());
            }
        }
        return null;
    }

    private IItemLabelProvider getTargetLabelProvider(APointer<?> pointer) {
        return (IItemLabelProvider) rootAdapterFactory.adapt(pointer.getTarget(), IItemLabelProvider.class);
    }
}
