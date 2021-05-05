/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.google.common.base.Predicate;

public class PredicateBasedViewerFilter extends ViewerFilter {
    
    private final Predicate<Object> predicate;
    
    public PredicateBasedViewerFilter(Predicate<Object> predicate) {
        super();
        this.predicate = predicate;
    }



    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        return predicate.apply(element);
    }

}
