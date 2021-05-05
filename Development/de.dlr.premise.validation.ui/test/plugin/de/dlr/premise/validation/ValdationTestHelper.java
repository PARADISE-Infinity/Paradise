/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.validation;

import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.ocl.pivot.internal.delegate.DelegateEPackageAdapter;
import org.eclipse.ocl.pivot.internal.delegate.OCLDelegateDomainFactory;
import org.eclipse.ocl.pivot.model.OCLstdlib;
import org.eclipse.ocl.xtext.oclinecore.OCLinEcoreStandaloneSetup;
import org.eclipse.ocl.xtext.oclinecore.validation.OCLinEcoreEObjectValidator;

import de.dlr.premise.element.ElementPackage;
import de.dlr.premise.functions.UseCasePackage;
import de.dlr.premise.system.SystemPackage;
import de.dlr.premise.validation.ocl.ResourceSetAwareOCLDelegateDomainFactory;
import de.dlr.premise.registry.RegistryPackage;

public class ValdationTestHelper {

    public static void registerOCL() {
        // // register Pivot globally (resourceSet == null)
        // org.eclipse.ocl.examples.pivot.OCL.initialize(null);
        //
        // String oclDelegateURI = OCLDelegateDomain.OCL_DELEGATE_URI_PIVOT;
        // EOperation.Internal.InvocationDelegate.Factory.Registry.INSTANCE.put(oclDelegateURI, new OCLInvocationDelegateFactory.Global());
        // EStructuralFeature.Internal.SettingDelegate.Factory.Registry.INSTANCE.put(oclDelegateURI, new
        // OCLSettingDelegateFactory.Global());

       if (!EMFPlugin.IS_ECLIPSE_RUNNING) {
           OCLinEcoreStandaloneSetup.doSetup();
       }
        // install the OCL standard library
        OCLstdlib.install();

        EValidator.Registry.INSTANCE.put(SystemPackage.eINSTANCE, new OCLinEcoreEObjectValidator());
        EValidator.Registry.INSTANCE.put(UseCasePackage.eINSTANCE, new OCLinEcoreEObjectValidator());
        EValidator.Registry.INSTANCE.put(RegistryPackage.eINSTANCE, new OCLinEcoreEObjectValidator());
        EValidator.Registry.INSTANCE.put(ElementPackage.eINSTANCE, new OCLinEcoreEObjectValidator());

        // clear out DelegateEPackageAdapters
        removeDelegateEPackageAdapter(SystemPackage.eINSTANCE);
        removeDelegateEPackageAdapter(UseCasePackage.eINSTANCE);
        removeDelegateEPackageAdapter(RegistryPackage.eINSTANCE);
        removeDelegateEPackageAdapter(ElementPackage.eINSTANCE);

        // register ResourceSetAwareDelegateDomain
        ResourceSetAwareOCLDelegateDomainFactory.register();
    }

    /**
     * The DelegateDomains for an EPackage are cached inside an {@link DelegateEPackageAdapter} on the global singleton instance of the
     * package. When running the application, we override the {@link OCLDelegateDomainFactory} before any DelegateDomain is instanciated
     * (see {@link ResourceSetAwareOCLDelegateDomainFactory#register()}).
     * 
     * But during tests the override happens to late, so the {@link DelegateEPackageAdapter} might contain a incorrect DelegateDomain.
     * 
     * @param ePackage
     */
    private static void removeDelegateEPackageAdapter(EPackage ePackage) {
        DelegateEPackageAdapter delegateEPackageAdapter = DelegateEPackageAdapter.findAdapter(ePackage);
        if (delegateEPackageAdapter != null) {
            ePackage.eAdapters().remove(delegateEPackageAdapter);
        }
    }
}
