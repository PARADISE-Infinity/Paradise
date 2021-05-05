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

import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;

import de.dlr.premise.component.provider.my.ComponentItemProviderAdapterFactoryMy;
import de.dlr.premise.element.provider.my.ElementItemProviderAdapterFactoryMy;
import de.dlr.premise.functionpool.provider.my.FunctionpoolItemProviderAdapterFactoryMy;
import de.dlr.premise.functions.provider.my.UseCaseItemProviderAdapterFactoryMy;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;
import de.dlr.premise.representation.provider.my.RepresentationItemProviderAdapterFactoryMy;
import de.dlr.premise.safety.provider.my.SafetyItemProviderAdapterFactoryMy;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;

public class PremiseAdapterFactoryHelper {

    public static ComposedAdapterFactory createComposedAdapterFactory() {
        ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
    	adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
    	// CHANGED REGISTRATION !!!
        adapterFactory.addAdapterFactory(new UseCaseItemProviderAdapterFactoryMy());
    	adapterFactory.addAdapterFactory(new RepresentationItemProviderAdapterFactoryMy());
        adapterFactory.addAdapterFactory(new SystemItemProviderAdapterFactoryMy());
    	adapterFactory.addAdapterFactory(new RegistryItemProviderAdapterFactoryMy());
    	adapterFactory.addAdapterFactory(new FunctionpoolItemProviderAdapterFactoryMy());
        adapterFactory.addAdapterFactory(new ElementItemProviderAdapterFactoryMy());
        adapterFactory.addAdapterFactory(new ComponentItemProviderAdapterFactoryMy());
    	adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
    	adapterFactory.addAdapterFactory(new SafetyItemProviderAdapterFactoryMy());
    	return adapterFactory;
    }
}
