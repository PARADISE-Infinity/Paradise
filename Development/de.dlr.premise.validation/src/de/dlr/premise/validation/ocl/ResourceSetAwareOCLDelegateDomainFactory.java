/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.validation.ocl;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.ocl.common.delegate.DelegateResourceSetAdapter;
import org.eclipse.ocl.pivot.internal.delegate.DelegateDomain;
import org.eclipse.ocl.pivot.internal.delegate.OCLDelegateDomain;
import org.eclipse.ocl.pivot.internal.delegate.OCLDelegateDomainFactory;
import org.eclipse.ocl.pivot.utilities.PivotConstants;

import de.dlr.premise.system.SystemPackage;

/**
 * A {@link OCLDelegateDomainFactory} that uses the {@link ResourceSetAwarePivotModelManager} in the pivot OCL environment.
 * 
 * Sadly, there is no easy way to change the ModelManager used, since it is directly instantiated in
 * {@link PivotEvaluationEnvironment#createModelManager(Object)}. Therefore, we have to dig deep into the bowels of the OCL implementation
 * to get to it (see {@link #createDelegateDomain()});
 */
public class ResourceSetAwareOCLDelegateDomainFactory extends OCLDelegateDomainFactory {
   public static void register() {
        // For a given ecore package and a OCL delegate URI, a DelegateDomain.Factory can be registered, which will provide an appropriate
        // DelegateDomain
        DelegateDomain.Factory.Registry registry =
                DelegateResourceSetAdapter.getRegistry(SystemPackage.eINSTANCE, DelegateDomain.Factory.Registry.class,
                                                       DelegateDomain.Factory.Registry.INSTANCE);
        if (!(registry.get(PivotConstants.OCL_DELEGATE_URI_PIVOT) instanceof ResourceSetAwareOCLDelegateDomainFactory)) {
            registry.put(PivotConstants.OCL_DELEGATE_URI_PIVOT, new ResourceSetAwareOCLDelegateDomainFactory());
        }
    }
    
    @Override
    public OCLDelegateDomain createDelegateDomain(String delegateURI, EPackage ePackage) {
        return new ResourceSetAwareOCLDelegateDomain(delegateURI, ePackage);
    }
}