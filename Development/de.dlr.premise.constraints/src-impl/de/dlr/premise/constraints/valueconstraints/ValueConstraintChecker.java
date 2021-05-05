/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.constraints.valueconstraints;

import de.dlr.premise.functions.AConstraint;
import de.dlr.premise.functions.ARange;
import de.dlr.premise.registry.AValueDef;

/**
 * Checks {@link AConstraint} instances, for a given value.
 * 
 * Allows support for different kinds of constraints as subclasses of AConstraint. Currently, only one kind of constraint ({@link ARange})
 * is implemented.
 * 
 * @author steh_ti
 * 
 */
public class ValueConstraintChecker {

    /**
     * Checks a constraint for a given value.
     * 
     * @param val The value to check
     * @param valConstr The constraint to check
     * @return Does value satisfy constraint?
     */
    public boolean check(AValueDef val, AConstraint valConstr) {
        if (valConstr instanceof ARange) {
            return checkRangeCondition(val, (ARange) valConstr);
        }
        return false;
    }

    private boolean checkRangeCondition(AValueDef val, ARange valConstr) {
        Double lower, upper;
        boolean valid;

        try {
            lower = Double.parseDouble(valConstr.getLower());
        } catch (NumberFormatException e) {
            lower = Double.NEGATIVE_INFINITY;
        } catch (NullPointerException e) {
            lower = Double.NEGATIVE_INFINITY;
        }
        try {
            upper = Double.parseDouble(valConstr.getUpper());
        } catch (NumberFormatException e) {
            upper = Double.POSITIVE_INFINITY;
        } catch (NullPointerException e) {
            upper = Double.POSITIVE_INFINITY;
        }

        valid = checkInRange(val, lower, upper);

        return valid;
    }

    private boolean checkInRange(AValueDef val, Double lower, Double upper) {
        boolean inRange = true;
        Double numericalValue;

        try {
            numericalValue = Double.parseDouble(val.getValue());
            inRange = checkInRange(numericalValue, lower, upper);
        } catch (NumberFormatException e) {
            inRange = false;
        } catch (NullPointerException e) {
            inRange = false;
        }

        return inRange;
    }

    private boolean checkInRange(Double value, Double lower, Double upper) {
        return lower <= value && value <= upper;
    }
}
