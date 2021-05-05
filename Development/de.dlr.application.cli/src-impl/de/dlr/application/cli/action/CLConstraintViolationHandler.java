/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.application.cli.action;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Pair;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.dlr.premise.constraints.ConstraintViolationKind;
import de.dlr.premise.constraints.handlers.IConstraintViolationHandler;

public class CLConstraintViolationHandler implements IConstraintViolationHandler {

    // violator -> (ViolationKind, violatedConstraint)
    private final Multimap<EObject, Pair<ConstraintViolationKind, Object>> violatorCache = HashMultimap.create();

    @Override
    public void removeViolations() {
        violatorCache.clear();
    }

    @Override
    public void removeViolations(EObject violator) {
        violatorCache.removeAll(violator);
    }

    @Override
    public void addViolation(EObject violator, ConstraintViolationKind kind, Object violatedConstraint) {
        violatorCache.put(violator, new Pair<ConstraintViolationKind, Object>(kind, violatedConstraint));
    }

    
    public Multimap<EObject, Pair<ConstraintViolationKind, Object>> getViolatorCache() {
        return violatorCache;
    }
}
