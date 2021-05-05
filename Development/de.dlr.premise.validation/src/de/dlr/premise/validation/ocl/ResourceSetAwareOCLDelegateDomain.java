/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.validation.ocl;

import java.util.List;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.ocl.pivot.internal.delegate.DelegateEClassifierAdapter;
import org.eclipse.ocl.pivot.internal.delegate.OCLDelegateDomain;
import org.eclipse.ocl.pivot.internal.utilities.GlobalEnvironmentFactory;
import org.eclipse.ocl.pivot.utilities.EnvironmentFactory;
import org.eclipse.ocl.pivot.utilities.OCL;

import de.dlr.premise.ocl.hacks.ResourceSetAwareDelegatingGlobalEnvironmentFactory;

final class ResourceSetAwareOCLDelegateDomain extends OCLDelegateDomain {

    ResourceSetAwareOCLDelegateDomain(@NonNull String delegateURI, @NonNull EPackage ePackage) {
        super(delegateURI, ePackage);
    }
    
    public @NonNull OCL getOCL() {
        OCL ocl2 = ocl;
        if (ocl2 == null) {
            // Delegates are an application-independent extension of EMF
            //  so we must use the neutral/global context see Bug 338501
//          EnvironmentFactory environmentFactory = getEnvironmentFactory();
            ResourceSetAwareDelegatingGlobalEnvironmentFactory environmentFactory =
                    new ResourceSetAwareDelegatingGlobalEnvironmentFactory(GlobalEnvironmentFactory.getInstance());
            ocl2 = ocl = environmentFactory.createOCL();
            environmentFactory.addListener(this);
        }
        return ocl2;
    }
    
    @Override
    public synchronized void reset() {
        OCL ocl2 = ocl;
        if (ocl2 != null) {
            ocl = null;
            for (EClassifier eClassifier : ePackage.getEClassifiers()) {
                List<Adapter> eClassifierAdapters = eClassifier.eAdapters();
                for (Adapter adapter : eClassifierAdapters) {
                    if (adapter instanceof DelegateEClassifierAdapter) {
                        eClassifierAdapters.remove(adapter);
                        break;
                    }
                }
                if (eClassifier instanceof EClass) {
                    EClass eClass = (EClass) eClassifier;
                    for (EOperation eOperation : eClass.getEOperations()) {
                        ((EOperation.Internal) eOperation).setInvocationDelegate(null);
                    }
                    for (EStructuralFeature eStructuralFeature : eClass.getEStructuralFeatures()) {
                        ((EStructuralFeature.Internal) eStructuralFeature).setSettingDelegate(null);
                    }
                }
            }
            @NonNull EnvironmentFactory environmentFactory = ocl2.getEnvironmentFactory();
            if (environmentFactory instanceof ResourceSetAwareDelegatingGlobalEnvironmentFactory) {
                ((ResourceSetAwareDelegatingGlobalEnvironmentFactory) environmentFactory).removeListener(this);
            } else {
                ((GlobalEnvironmentFactory) environmentFactory).removeListener(this);
            }
            ocl2.dispose();
        }
    }
}