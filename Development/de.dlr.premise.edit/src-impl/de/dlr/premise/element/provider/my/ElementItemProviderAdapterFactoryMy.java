/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.element.provider.my;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.edit.provider.IUpdateableItemText;
import de.dlr.premise.element.provider.ElementItemProviderAdapterFactory;
import de.dlr.premise.element.provider.TransitionItemProvider;


public class ElementItemProviderAdapterFactoryMy extends ElementItemProviderAdapterFactory {

    private TransitionItemProvider transitionItemProvider;

    public ElementItemProviderAdapterFactoryMy() {
        super();

        // enable support for inline editing of attributes in tree editor
        supportedTypes.add(IUpdateableItemText.class);
    }

    @Override
    public Adapter createRelationAdapter() {
        if (relationItemProvider == null) {
            relationItemProvider = new RelationItemProviderMy(this);
        }

        return relationItemProvider;
    }
    
    @Override
    public Adapter createModeAdapter() {
        if (modeItemProvider == null) {
            modeItemProvider = new ModeItemProviderMy(this);
        }

        return modeItemProvider;
    }
    
    @Override
    public Adapter createTransitionAdapter() {
        if (transitionItemProvider == null) {
            transitionItemProvider = new TransitionItemProviderMy(this);
        }

        return transitionItemProvider;
    }

    @Override
    public Adapter createModeGuardAdapter() {
        if (modeGuardItemProvider == null) {
            modeGuardItemProvider = new ModeGuardItemProviderMy(this);
        }

        return modeGuardItemProvider;
    }

    @Override
    public Adapter createGuardCombinationAdapter() {
        if (guardCombinationItemProvider == null) {
            guardCombinationItemProvider = new GuardCombinationItemProviderMy(this);
        }

        return guardCombinationItemProvider;
    }
    
    @Override
    public Adapter createConnectionAdapter() {
        if (connectionItemProvider == null) {
            connectionItemProvider = new ConnectionItemProviderMy(this);
        }

        return connectionItemProvider;
    }
    
   
    @Override
    public Adapter createStateMachineAdapter() {
        if (stateMachineItemProvider == null) {
            stateMachineItemProvider = new StateMachineItemProviderMy(this);
        }

        return stateMachineItemProvider;
    }

}
