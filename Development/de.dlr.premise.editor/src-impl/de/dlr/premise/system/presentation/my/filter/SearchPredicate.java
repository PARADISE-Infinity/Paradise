/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my.filter;

import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.registry.AValueDef;
import de.dlr.premise.registry.MetaData;
import de.dlr.premise.registry.MetaTypeDef;
import de.dlr.premise.registry.Note;
import de.dlr.premise.registry.Unit;

import com.google.common.base.Predicate;

import de.dlr.premise.element.AElement;
import de.dlr.premise.system.Balancing;

public class SearchPredicate implements Predicate<Object> {

    private final Predicate<Object> filterPredicate;
    private String searchString = "";
    
    public SearchPredicate(Predicate<Object> filterPredicate) {
        super();
        this.filterPredicate = filterPredicate;
    }

    @Override
    public boolean apply(Object object) {
        // TODO steh_ti More useful predicate
        if(searchString.isEmpty()) {
            return false;
        }

        if (object instanceof EObject) {
            EObject eObject = (EObject) object;
            
            if (!matchesFilter(eObject)) {
                return false;
            }
                        
            String typeName = eObject.eClass().getName();
            if (wordMatches(typeName)) {
                return true;
            }
        }
        
        if (object instanceof AParameterDef) {
            AParameterDef param = (AParameterDef) object;
            if (param.getUnit() != null && unitMatches(param.getUnit())) {
                return true;
            }
        }
        
        if (object instanceof Unit) {
            Unit unit = (Unit) object;
            if (unitMatches(unit)) {
                return true;
            }
        }
        
        if (object instanceof MetaData) {
            String name = ((MetaData) object).getName();
            if (wordMatches(name))  {
                return true;
            }
        }
        
        if (object instanceof AElement) {
            AElement element = (AElement) object;
            for (MetaTypeDef metaType : element.getMetaTypes()) {
                if (nameItemMatches(metaType)) {
                    return true;
                }
            }
        }
        
        if (object instanceof Balancing) {
            Balancing balancing = (Balancing) object;
            if (wordMatches(balancing.getFunction())) {
                return true;
            }
        }
        
        if (object instanceof AValueDef) {
            AValueDef value = (AValueDef) object;
            if (wordMatches(value.getValue())) {
                return true;
            }
        }
        
        if (object instanceof Note) {
            Note note = (Note) object;
            if (wordMatches(note.getText()) || wordMatches(note.getAuthor()) || wordMatches(note.getDate())) {
                return true;
            }
        }        
        
        if (object instanceof ANameItem) {
            ANameItem nameItem = (ANameItem) object;
            
            if (nameItemMatches(nameItem)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean matchesFilter(EObject object) {
        while (object != null) {
            if (!filterPredicate.apply(object)) {
                return false;
            }
            object = object.eContainer();
        }
        return true;
    }
    
    private boolean nameItemMatches(ANameItem nameItem) {
        return wordMatches(nameItem.getName()) || wordMatches(nameItem.getDescription());
    }
    
    private boolean unitMatches(Unit unit) {
        return wordMatches(unit.getSymbol()) || nameItemMatches(unit);
    }
    
    public boolean wordMatches(String name) {
        if (name == null) {
            return false;
        }
        return name.toLowerCase().contains(searchString.toLowerCase());
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }
}
