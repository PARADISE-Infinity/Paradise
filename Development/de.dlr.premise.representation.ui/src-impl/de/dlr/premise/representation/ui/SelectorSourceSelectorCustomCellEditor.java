/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.representation.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;

import de.dlr.exchange.base.xtend.RepresentationHelper;
import de.dlr.premise.registry.RegistryPackage;
import de.dlr.premise.representation.RepresentationPackage;
import de.dlr.premise.representation.Selector;

public class SelectorSourceSelectorCustomCellEditor extends ASelectorCustomCellEditor {

    @Override
    protected EAttribute getSupportedFeature() {
        return RepresentationPackage.Literals.SELECTOR__SOURCE_SELECTOR;
    }

    @Override
    protected EClassifier getContext(Selector selector) {
        Collection<EObject> results = getQueryResults(selector);
        return computeType(results);
    }

    private Collection<EObject> getQueryResults(Selector selector) {
        return RepresentationHelper.getInstance(selector.eResource().getResourceSet()).getQueryResults(selector);
    }

    private EClassifier computeType(Collection<EObject> col) {
        List<EClass> classes = col.stream().map(obj -> obj.eClass()).collect(Collectors.toList());
        return computeCommonSuperClass(classes);
    }

    private EClass computeCommonSuperClass(List<EClass> classes) {
        if (classes.isEmpty()) {
            return RegistryPackage.Literals.IPREMISE_OBJECT;
        }
        List<EClass> rollingIntersect = getSuperClasses(classes.get(0));
        for (int i = 1; i < classes.size(); i++) {
            rollingIntersect.retainAll(getSuperClasses(classes.get(i)));
        }
        return rollingIntersect.stream().sorted(Comparator.comparing(EClass::isInterface)).findFirst()
                               .orElse(RegistryPackage.Literals.IPREMISE_OBJECT);
    }

    private List<EClass> getSuperClasses(EClass clazz) {
        List<EClass> results = new ArrayList<>();
        results.add(clazz);
        results.addAll(clazz.getEAllSuperTypes());
        return results;
    }

}
