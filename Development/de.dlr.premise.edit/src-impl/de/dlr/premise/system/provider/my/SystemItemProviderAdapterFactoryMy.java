/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.system.provider.my;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.edit.provider.IUpdateableItemText;

import de.dlr.premise.system.provider.SystemItemProviderAdapterFactory;

public class SystemItemProviderAdapterFactoryMy extends SystemItemProviderAdapterFactory {

	// Considered meta data settings of a repository
	public static final String OPT_TYPE_VIEW = "View";
	public static final String OPT_DATA_TYPE_NAMES = "dataTypeNames";
	public static final String NEWLINE = "\\n";
	public static final String QNAME_PRE = " {";
	public static final String QNAME_POST = "}";
	public static final String UNIT_PRE = "[";
	public static final String UNIT_POST = "]";

    public SystemItemProviderAdapterFactoryMy() {
        super();

        // enable support for inline editing of attributes in tree editor
        supportedTypes.add(IUpdateableItemText.class);
    }

	@Override
	public Adapter createSystemComponentAdapter() {
		if (systemComponentItemProvider == null) {
			systemComponentItemProvider = new SystemComponentItemProviderMy(this);
		}

		return systemComponentItemProvider;
	}

	@Override
	public Adapter createTransitionParameterAdapter() {
        if (transitionParameterItemProvider == null) {
            transitionParameterItemProvider = new TransitionParameterItemProviderMy(this);
        }

        return transitionParameterItemProvider;
    }

    @Override
    public Adapter createTransitionBalancingAdapter() {
        if (transitionBalancingItemProvider == null) {
            transitionBalancingItemProvider = new TransitionBalancingItemProviderMy(this);
        }
        return transitionBalancingItemProvider;
    }

    @Override
    public Adapter createBalancingAdapter() {
        if (balancingItemProvider == null) {
            balancingItemProvider = new BalancingItemProviderMy(this);
        }
        return balancingItemProvider;
    }

    @Override
    public Adapter createModeValueRefAdapter() {
        if (modeValueRefItemProvider == null) {
            modeValueRefItemProvider = new ModeValueRefItemProviderMy(this);
        }
        return modeValueRefItemProvider;
    }

    @Override
    public Adapter createParameterAdapter() {
        if (parameterItemProvider == null) {
            parameterItemProvider = new ParameterItemProviderMy(this);
        }
        return parameterItemProvider;
    }


    


    @Override
    public Adapter createComponentReferenceAdapter() {
        if (componentReferenceItemProvider == null) {
            componentReferenceItemProvider = new ComponentReferenceItemProviderMy(this);
        }

        return componentReferenceItemProvider;
    }
}
