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

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.edit.provider.IUpdateableItemText;

import de.dlr.premise.component.provider.ComponentItemProviderAdapterFactory;

public class ComponentItemProviderAdapterFactoryMy extends ComponentItemProviderAdapterFactory {

    public ComponentItemProviderAdapterFactoryMy() {
        super();

        // enable support for inline editing of attributes in tree editor
        supportedTypes.add(IUpdateableItemText.class);
    }   
    
    @Override
    public Adapter createSatisfiesAdapter() {
        if (satisfiesItemProvider == null) {
            satisfiesItemProvider = new SatisfiesItemProviderMy(this);
        }

        return satisfiesItemProvider;
    }

    @Override
    public Adapter createComponentDefinitionAdapter() {
        if (componentDefinitionItemProvider == null) {
            componentDefinitionItemProvider = new ComponentDefinitionItemProviderMy(this);
        }

        return componentDefinitionItemProvider;
    }

    @Override
    public Adapter createParameterDefinitionAdapter() {
        if (parameterDefinitionItemProvider == null) {
            parameterDefinitionItemProvider = new ParameterDefinitionItemProviderMy(this);
        }

        return parameterDefinitionItemProvider;
    }

    @Override
    public Adapter createComponentReferencePointerAdapter() {
        if (componentReferencePointerItemProvider == null) {
            componentReferencePointerItemProvider = new ComponentReferencePointerItemProviderMy(this);
        }

        return componentReferencePointerItemProvider;
    }
    
    @Override
    public Adapter createComponentReferenceDirectPointerAdapter() {
        if (componentReferenceDirectPointerItemProvider == null) {
            componentReferenceDirectPointerItemProvider = new ComponentReferenceDirectPointerItemProviderMy(this);
        }

        return componentReferenceDirectPointerItemProvider;
    }


    @Override
    public Adapter createChildComponentDefinitionAdapter() {
        if (childComponentDefinitionItemProvider == null) {
            childComponentDefinitionItemProvider = new ChildComponentDefinitionItemProviderMy(this);
        }

        return childComponentDefinitionItemProvider;
    }
    
    @Override
    public Adapter createModeDefinitionAdapter() {
        if (modeDefinitionItemProvider == null) {
            modeDefinitionItemProvider = new ModeDefinitionItemProviderMy(this);
        }

        return modeDefinitionItemProvider;
    }
}
