/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.constraints.handlers;

import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.constraints.ConstraintViolationKind;

public interface IConstraintViolationHandler {

    public abstract void removeViolations();

    public abstract void removeViolations(EObject violator);

    public abstract void addViolation(EObject violator, ConstraintViolationKind kind, Object violatedConstraint);

}