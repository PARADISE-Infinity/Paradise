/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.component.impl.my;

import de.dlr.premise.component.impl.ComponentReferencePointerImpl;
import de.dlr.premise.graph.INode;
import de.dlr.premise.util.PointerHelper;

public class ComponentReferencePointerImplMy<T extends INode> extends ComponentReferencePointerImpl<T> {

    @Override
    public T getTarget() {
        return PointerHelper.getTarget(this);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((componentReference == null) ? 0 : componentReference.hashCode());
        result = prime * result + ((definition == null) ? 0 : definition.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ComponentReferencePointerImplMy<?> other = (ComponentReferencePointerImplMy<?>) obj;
        if (componentReference == null) {
            if (other.componentReference != null) {
                return false;
            }
        } else if (!componentReference.equals(other.componentReference)) {
            return false;
        }
        if (definition == null) {
            if (other.definition != null) {
                return false;
            }
        } else if (!definition.equals(other.definition)) {
            return false;
        }
        if (eContainer != null && other.eContainer != null && !eContainer.equals(other.eContainer)) {
            return false;
        }
        return true;
    }
}
