/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.provider.util;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.DelegatingWrapperItemProvider;
import org.eclipse.emf.edit.provider.IUpdateableItemText;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;


public class DelegatingWrapperItemProviderMy extends DelegatingWrapperItemProvider implements IUpdateableItemText {

    public DelegatingWrapperItemProviderMy(Object value, Object owner, EStructuralFeature feature, int index, AdapterFactory adapterFactory) {
        super(value, owner, feature, index, adapterFactory);
    }

    // the superclass constructor is deprecated for public use but is still valid as protected method for subclasses
    @SuppressWarnings("deprecation")
    protected DelegatingWrapperItemProviderMy(Object value, Object owner, AdapterFactory adapterFactory) {
        super(value, owner, adapterFactory);
    }

    @Override
    public void setText(Object object, String text) {
        if (delegateItemProvider instanceof IUpdateableItemText) {
            ((IUpdateableItemText) delegateItemProvider).setText(object, text);
        }
    }

    @Override
    public String getUpdateableText(Object object) {
        return delegateItemProvider instanceof IUpdateableItemText ? ((IUpdateableItemText) delegateItemProvider).getUpdateableText(object)
                : null;
    }

    @Override
    protected IWrapperItemProvider createWrapper(Object value, Object owner, AdapterFactory adapterFactory) {
        return new DelegatingWrapperItemProviderMy(value, owner, adapterFactory);
    }
}
