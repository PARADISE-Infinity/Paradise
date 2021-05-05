/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.system.impl.my;

import de.dlr.premise.system.ComponentReference;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.TransitionParameter;
import de.dlr.premise.system.impl.ComponentReferenceImpl;
import de.dlr.premise.system.impl.SystemFactoryImpl;
import de.dlr.premise.util.PremiseHelper;

public class SystemFactoryImplMy extends SystemFactoryImpl {

    @Override
    public ProjectRepository createProjectRepository() {
        ProjectRepository repository = super.createProjectRepository();
        // EMF does not serialize/save default values of attributes, so, get and set it explicitly to ensure serialization:
        repository.setMetaModel(repository.getMetaModel());
        return repository;
    }

    @Override
    public SystemComponent createSystemComponent() {
        SystemComponent dataItem = new SystemComponentImplMy(); // use specific implementation
        dataItem.setId(PremiseHelper.createId());
        return dataItem;
    }

    @Override
    public TransitionParameter createTransitionParameter() {
        TransitionParameter dataItem = super.createTransitionParameter();
        dataItem.setId(PremiseHelper.createId());
        return dataItem;
    }

    @Override
    public Parameter createParameter() {
        Parameter dataItem = super.createParameter();
        dataItem.setId(PremiseHelper.createId());
        return dataItem;
    }
        
    @Override
    public ComponentReference createComponentReference() {
        ComponentReferenceImpl componentReference = new ComponentReferenceImplMy();
        componentReference.setId(PremiseHelper.createId());
        return componentReference;
    }
}
