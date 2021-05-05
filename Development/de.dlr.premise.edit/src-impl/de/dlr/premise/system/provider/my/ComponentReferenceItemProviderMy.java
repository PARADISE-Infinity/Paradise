/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.system.provider.my;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.google.common.collect.ImmutableSet;

import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.system.ComponentReference;
import de.dlr.premise.system.SystemPackage;
import de.dlr.premise.system.provider.ComponentReferenceItemProvider;
import de.dlr.premise.util.PremiseHelper;


public class ComponentReferenceItemProviderMy extends ComponentReferenceItemProvider {

    public ComponentReferenceItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }


    @Override
    public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
        Collection<? extends EStructuralFeature> childrenFeatures = super.getChildrenFeatures(object);
       
        if (object instanceof ComponentReference) {
            ComponentReference compRef = (ComponentReference) object;
            if (compRef.getActiveImplementation() != null && compRef.getActiveImplementation().hasCyclicComponentReference()) {
                childrenFeatures = new ArrayList<>(childrenFeatures);
                childrenFeatures.remove(SystemPackage.Literals.COMPONENT_REFERENCE__ACTIVE_IMPLEMENTATION);
            }
        }
       
        return childrenFeatures;
    }
    
    @Override
    public String getText(Object object) {

        // check input
        if (object == null) {
            return null;
        }
        final ComponentReference componentReference = (ComponentReference) object;
        
        // optional type
        String typeName = "";
        if (PremiseHelper.isSet(componentReference, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            typeName = getString("_UI_ComponentReference_type");
            typeName = typeName.replace(" ", "") + " ";
        }

        // label
        String label = componentReference.getName();
        if (label != null) {
            label = label.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
        } else {
            label = "";
        }
        
        // add active implementation name
        if (componentReference.getActiveImplementation() != null && componentReference.getActiveImplementation().getName() != null) {
            if (label.length() == 0) {
                label = componentReference.getActiveImplementation().getName();
            } else {
                label += ": " + componentReference.getActiveImplementation().getName();
            }
        }

        // with qualified name
        String postfix = PremiseHelper.getQualifyingNamePrefix((ANameItem) object);
        if (!postfix.isEmpty()) {
            postfix = SystemItemProviderAdapterFactoryMy.QNAME_PRE + postfix.substring(0, postfix.length() - 1)
                    + SystemItemProviderAdapterFactoryMy.QNAME_POST;
            postfix = postfix.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
        }

        // return name
        return typeName + label + postfix;
    }
    
    @Override
    protected boolean isWrappingNeeded(Object object) {
        return true;
    }

    @Override
    protected Object createWrapperForNonContainmentEReference(EObject object, EStructuralFeature feature, Object value, int index) {
        return new ComponentReferenceChildrenDelegatingWrapperItemProvider(value, object, feature, index,
                                                                           ImmutableSet.of((ComponentReference) object), adapterFactory);
    }
}
