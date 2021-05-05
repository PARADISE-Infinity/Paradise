/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.constraints.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.dlr.premise.element.Mode;
import de.dlr.premise.functions.ModeRangeConstraint;
import de.dlr.premise.functions.RangeConstraint;
import de.dlr.premise.functions.UseCaseFactory;
import de.dlr.premise.graph.DirectPointer;
import de.dlr.premise.graph.GraphFactory;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.registry.Value;
import de.dlr.premise.system.ModeValueRef;
import de.dlr.premise.system.SystemFactory;

public class ConstraintCheckTestHelper {

    // for double values
    public static RangeConstraint createRangeConstraint(double lower, double upper) {
        return createRangeConstraint(Double.toString(lower), Double.toString(upper));
    }

    public static ModeRangeConstraint createModeRangeConstraint(double lower, double upper, Mode... modes) {
        return createModeRangeConstraint(Double.toString(lower), Double.toString(upper), modes);
    }

    public static Value createValue(double value) {
        return createValue(Double.toString(value));
    }

    public static ModeValueRef createModeValueRef(double value, Mode... modes) {
        return createModeValueRef(Double.toString(value), modes);
    }

    // for string values
    public static RangeConstraint createRangeConstraint(String lower, String upper) {
        RangeConstraint range = UseCaseFactory.eINSTANCE.createRangeConstraint();
        range.setLower(lower);
        range.setUpper(upper);
        return range;
    }

    public static ModeRangeConstraint createModeRangeConstraint(String lower, String upper, Mode... modes) {
        ModeRangeConstraint constr = UseCaseFactory.eINSTANCE.createModeRangeConstraint();
        constr.getModePointers().addAll(createDirectPointers(modes));
        constr.setLower(lower);
        constr.setUpper(upper);
        return constr;
    }

    public static Value createValue(String value) {
        Value val = RegistryFactory.eINSTANCE.createValue();
        val.setValue(value);
        return val;
    }

    public static ModeValueRef createModeValueRef(String value, Mode... modes) {
        ModeValueRef modeVal = SystemFactory.eINSTANCE.createModeValueRef();
        modeVal.setValue(value);
        modeVal.getModePointers().addAll(createDirectPointers(modes));
        return modeVal;
    }
    
    public static <T> List<DirectPointer<T>> createDirectPointers(T[] targets) {
        return Arrays.stream(targets).map(ConstraintCheckTestHelper::createDirectPointer).collect(Collectors.toList());
    }
    
    public static <T> DirectPointer<T> createDirectPointer(T target) {
        DirectPointer<T> pointer = GraphFactory.eINSTANCE.createDirectPointer();
        pointer.setTarget(target);
        return pointer;
    }

}
