/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my.query;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.emf.ecore.resource.Resource;

import com.google.common.base.Predicate;

import de.dlr.premise.element.ARepository;
import de.dlr.premise.registry.IPremiseObject;
import de.dlr.premise.system.provider.my.ComponentReferenceChildrenDelegatingWrapperItemProvider;

public class CollectionBasedPredicate implements Predicate<Object> {

    private Collection<?> shown = Collections.emptyList();
    private Collection<?> hidden = Collections.emptyList();
    private final Runnable unknownElementCallback;

    public CollectionBasedPredicate(Runnable unknownElementCallback) {
        this.unknownElementCallback = unknownElementCallback;
    }

    public void setCollections(Collection<?> shown, Collection<?> hidden) {
        this.shown = shown;
        this.hidden = hidden;
    }

    @Override
    public boolean apply(Object element) {
        if (element instanceof Resource) {
            return !"ecore".equals(((Resource) element).getURI().fileExtension());
        }
        if (element instanceof ARepository || element instanceof Number || element instanceof String) {
            return true;
        }
        if (element instanceof ComponentReferenceChildrenDelegatingWrapperItemProvider) {
            element = ((ComponentReferenceChildrenDelegatingWrapperItemProvider) element).getDelegateValue();
        }
        if (!shown.isEmpty() && !shown.contains(element) && !hidden.contains(element)) {
            unknownElementCallback.run();
        }
        return element instanceof IPremiseObject && (shown.contains(element) || !hidden.contains(element));
    }

}
