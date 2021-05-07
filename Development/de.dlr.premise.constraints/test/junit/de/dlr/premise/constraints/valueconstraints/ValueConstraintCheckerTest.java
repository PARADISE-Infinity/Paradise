/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.constraints.valueconstraints;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.dlr.premise.constraints.util.ConstraintCheckTestHelper;

public class ValueConstraintCheckerTest {

    private ValueConstraintChecker checker;

    @Before
    public void setUp() {
        checker = new ValueConstraintChecker();
    }

    @Test
    public void checkValueInRange() {
        boolean inRange = checker.check(ConstraintCheckTestHelper.createValue(5), ConstraintCheckTestHelper.createRangeConstraint(0, 10));
        assertTrue(inRange);
    }

    @Test
    public void checkValueNotInRange() {
        boolean inRange = checker.check(ConstraintCheckTestHelper.createValue(15), ConstraintCheckTestHelper.createRangeConstraint(0, 10));
        assertFalse(inRange);
    }

    @Test
    public void checkNegativeInfinityOpenRange() {
        boolean inRange = checker.check(ConstraintCheckTestHelper.createValue(5), ConstraintCheckTestHelper.createRangeConstraint(null, "0"));
        assertFalse(inRange);
    }

    @Test
    public void checkPositiveInfinityOpenRange() {
        boolean inRange = checker.check(ConstraintCheckTestHelper.createValue(5), ConstraintCheckTestHelper.createRangeConstraint("0", null));
        assertTrue(inRange);
    }

    @Test
    public void checkInvalidRangeBounds() {
        boolean inRange = checker.check(ConstraintCheckTestHelper.createValue(5), ConstraintCheckTestHelper.createRangeConstraint("foo", "bar"));
        assertTrue(inRange);
    }

    @Test
    public void checkInvalidValue() {
        boolean inRange = checker.check(ConstraintCheckTestHelper.createValue("baz"), ConstraintCheckTestHelper.createRangeConstraint(null, null));
        assertFalse(inRange);
    }

    @Test
    public void checkNullValue() {
        boolean inRange = checker.check(null, ConstraintCheckTestHelper.createRangeConstraint(null, null));
        assertFalse(inRange);
    }

    @Test
    public void checkNullRange() {
        boolean inRange = checker.check(ConstraintCheckTestHelper.createValue(5), null);
        assertFalse(inRange);
    }

    @Test
    public void checkModeValueInRange() {
        boolean inRange = checker.check(ConstraintCheckTestHelper.createModeValueRef(5), ConstraintCheckTestHelper.createRangeConstraint(0, 10));
        assertTrue(inRange);
    }

    @Test
    public void checkModeValueNotInRange() {
        boolean inRange = checker.check(ConstraintCheckTestHelper.createModeValueRef(15), ConstraintCheckTestHelper.createRangeConstraint(0, 10));
        assertFalse(inRange);
    }
}
