/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.system.impl.my;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreEList;

import de.dlr.premise.system.IComponent;
import de.dlr.premise.system.SystemPackage;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.impl.SystemComponentImpl;


public class SystemComponentImplMy extends SystemComponentImpl {

    /**
     * Returns a list of child SystemComponents, resolving any ComponentReferences to their implementations. 
     * 
     * The returned list is guaranteed to always form part of a tree. Client can therefore safely enumerate 
     * SystemComponents recursively using this method.
     * 
     * If this SystemComponent is part of a cycle formed by component implementations containing themselves, 
     * this method will return an empty list so that the condition specified above can hold.
     */
    @Override
    public EList<SystemComponent> getReferencedChildren() {
        List<SystemComponent> referencedChildren;
        if (hasCyclicComponentReference()) {
            referencedChildren = new ArrayList<>();
        } else {
            referencedChildren = doGetReferenceChildren();
        }

        return new EcoreEList.UnmodifiableEList<SystemComponent>(this, SystemPackage.Literals.SYSTEM_COMPONENT__REFERENCED_CHILDREN,
                                                                 referencedChildren.size(), referencedChildren.toArray());
    }

    private List<SystemComponent> doGetReferenceChildren() {
        List<SystemComponent> referencedChildren = new ArrayList<>();

        for (IComponent child : getChildren()) {
            SystemComponent component = child.getComponent();
            if (component != null) {
                referencedChildren.add(component);
            }
        }
        return referencedChildren;
    }
    
    @Override
    public SystemComponent basicGetComponent() {
        return this;
    }
}
