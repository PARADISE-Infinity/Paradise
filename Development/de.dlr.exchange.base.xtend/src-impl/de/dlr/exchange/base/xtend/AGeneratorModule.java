/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.base.xtend;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xtext.resource.generic.AbstractGenericResourceRuntimeModule;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.ui.resource.IStorage2UriMapper;
import org.eclipse.xtext.ui.resource.SimpleResourceSetProvider;
import org.eclipse.xtext.ui.shared.Access;
import org.eclipse.xtext.ui.shared.JdtHelper;
import org.eclipse.xtext.ui.shared.contribution.ISharedStateContributionRegistry;
import org.eclipse.xtext.ui.util.IJdtHelper;

import com.google.inject.Provider;

public abstract class AGeneratorModule extends AbstractGenericResourceRuntimeModule {

	@Override
	protected String getLanguageName() {
		return "sample.presentation.SampleEditorID";
	}

	@Override
	protected String getFileExtensions() {
		return "premise";
	}

	public Class<? extends ResourceSet> bindResourceSet() {
		return ResourceSetImpl.class;
	}
	
    public Class<? extends IResourceSetProvider> bindIResourceSetProvider() {
        return SimpleResourceSetProvider.class;
    }
 
    public Class<? extends IJdtHelper> bindIJdtHelper() {
        return JdtHelper.class;
    }

    public org.eclipse.core.resources.IWorkspaceRoot bindIWorkspaceRootToInstance() {
    	return org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot();
    }

    public org.eclipse.core.resources.IWorkspace bindIWorkspaceToInstance(){
    	return org.eclipse.core.resources.ResourcesPlugin.getWorkspace();
    }
    

    public Provider<ISharedStateContributionRegistry> provideSharedStateContributionRegistry() {
        return Access.getSharedStateContributionRegistry();
    }
    
    public Provider<IStorage2UriMapper> provideIStorage2UriMapper() {
        return Access.getIStorage2UriMapper();
    }   

}
