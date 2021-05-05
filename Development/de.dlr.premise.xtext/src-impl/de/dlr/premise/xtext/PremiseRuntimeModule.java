/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.xtext;

import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.resource.IDefaultResourceDescriptionStrategy;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.generic.AbstractGenericResourceRuntimeModule;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;

import de.dlr.premise.xtext.naming.PremiseQualifiedNameProvider;
import de.dlr.premise.xtext.resource.PremiseResourceServiceProvider;


public class PremiseRuntimeModule extends AbstractGenericResourceRuntimeModule {

    @Override
    protected String getLanguageName() {
        return "PREMISE";
    }

    @Override
    protected String getFileExtensions() {
        return "premise,registry,functionpool,usecase";
    }
    
    public Class<? extends IDefaultResourceDescriptionStrategy> bindIDefaultResourceDescriptionStrategy() {
        return DefaultResourceDescriptionStrategy.class;
    }
 
    @Override
    public Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
        return PremiseQualifiedNameProvider.class;
    }
    
    public Class<? extends IResourceServiceProvider> bindIResourceServiceProvider() {
        return PremiseResourceServiceProvider.class;
    }
}
