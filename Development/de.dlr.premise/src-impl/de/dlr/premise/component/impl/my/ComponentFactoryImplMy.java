/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.component.impl.my;

import de.dlr.premise.component.ChildComponentDefinition;
import de.dlr.premise.component.ComponentDefinition;
import de.dlr.premise.component.ComponentReferencePointer;
import de.dlr.premise.component.ComponentRepository;
import de.dlr.premise.component.ModeDefinition;
import de.dlr.premise.component.ParameterDefinition;
import de.dlr.premise.component.impl.ComponentFactoryImpl;
import de.dlr.premise.component.impl.ComponentReferencePointerImpl;
import de.dlr.premise.graph.INode;
import de.dlr.premise.util.PremiseHelper;

public class ComponentFactoryImplMy extends ComponentFactoryImpl {
    
    @Override
    public ComponentRepository createComponentRepository() {
        ComponentRepository componentRepository = super.createComponentRepository();
        // EMF does not serialize/save default values of attributes, so, get and set it explicitly to ensure serialization:
        componentRepository.setMetaModel(componentRepository.getMetaModel());
        return componentRepository;
    }

    @Override
    public ComponentDefinition createComponentDefinition() {
        ComponentDefinition componentDefinition = super.createComponentDefinition();
        componentDefinition.setId(PremiseHelper.createId());
        return componentDefinition;
    }

    @Override
    public ParameterDefinition createParameterDefinition() {
        ParameterDefinition parameterDefinition = super.createParameterDefinition();
        parameterDefinition.setId(PremiseHelper.createId());
        return parameterDefinition;
    }
    
    @Override
    public ChildComponentDefinition createChildComponentDefinition() {
        ChildComponentDefinition childComponentDefinition = super.createChildComponentDefinition();
        childComponentDefinition.setId(PremiseHelper.createId());
        return childComponentDefinition;
    }

    @Override
    public ModeDefinition createModeDefinition() {
        ModeDefinition modeDefinition = super.createModeDefinition();
        modeDefinition.setId(PremiseHelper.createId());
        return modeDefinition;
    }

    
    @Override
    public <T extends INode> ComponentReferencePointer<T> createComponentReferencePointer() {
        ComponentReferencePointerImpl<T> componentReferencePointer = new ComponentReferencePointerImplMy<T>();
        return componentReferencePointer;
    }
}
