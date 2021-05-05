/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.functions.provider.my;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.edit.provider.IUpdateableItemText;

import de.dlr.premise.functions.provider.UseCaseItemProviderAdapterFactory;

public class UseCaseItemProviderAdapterFactoryMy extends UseCaseItemProviderAdapterFactory {

    public UseCaseItemProviderAdapterFactoryMy() {
        super();

        // enable support for inline editing of attributes in tree editor
        supportedTypes.add(IUpdateableItemText.class);
    }

    @Override
    public Adapter createRangeConstraintAdapter() {
        if (rangeConstraintItemProvider == null) {
            rangeConstraintItemProvider = new RangeConstraintItemProviderMy(this);
        }
        return rangeConstraintItemProvider;
    }

    @Override
    public Adapter createModeRangeConstraintAdapter() {
        if (modeRangeConstraintItemProvider == null) {
            modeRangeConstraintItemProvider = new ModeRangeConstraintItemProviderMy(this);
        }
        return modeRangeConstraintItemProvider;
    }

    @Override
    public Adapter createUseCaseAdapter() {
        if (useCaseItemProvider == null) {
            useCaseItemProvider = new UseCaseItemProviderMy(this);
        }
        return useCaseItemProvider;
    }

    @Override
    public Adapter createRequiredParameterAdapter() {
        if (requiredParameterItemProvider == null) {
            requiredParameterItemProvider = new RequiredParameterItemProviderMy(this);
        }

        return requiredParameterItemProvider;
    }
}
