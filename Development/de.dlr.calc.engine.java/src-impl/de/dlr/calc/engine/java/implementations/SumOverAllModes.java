/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.java.implementations;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Lists;

import de.dlr.calc.my.CalculatorParameter;
import de.dlr.premise.element.Mode;
import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.system.ModeValueRef;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.util.PremiseHelper;

public class SumOverAllModes {

    public static double calculate(List<CalculatorParameter> params) {
        List<Double> dblValues = Lists.newArrayList();
        EList<EObject> modes = null;
        
        for (CalculatorParameter param : params) {
            if (param.isDouble()) {
                dblValues.add(param.getDoubleValue());
            } else {
                AParameterDef p = param.getAParameterDefValue();
                
                if (modes == null) {
                    modes = PremiseHelper.getAll(PremiseHelper.getRoot(p), Mode.class); 
                }
                
                for (EObject mode : modes) {
                    dblValues.addAll(getDoubleValues((Mode) mode, p));
                }
            }
               
        }
        
        double accumulator = 0;
        for (double d : dblValues) {
            accumulator += d;
        }
        
        return accumulator;
    }

    private static List<Double> getDoubleValues(Mode mode, AParameterDef param) {
        List<Double> dblValues = Lists.newArrayList();
                
        // check mode dependency
        if (isModeDependent(param, mode)) {
            // on mode value: search and store converted value
            for (ModeValueRef modeValRef : ((Parameter) param).getModeValues()) {
                if (modeValRef.getModes().contains(mode)) {
                    dblValues.add(Double.valueOf(modeValRef.getValue()));
                }
            }
        } else {
            // on default mode: store converted value
            dblValues.add(Double.valueOf(param.getValue().getValue()));
        }
        
        return dblValues;
    }
    


    /**
     * Checks if value of given parameter is dependent on given mode
     * 
     * @param param
     * @param mode
     * @return true if param is dependent from mode
     */
    private static boolean isModeDependent(AParameterDef param, Mode mode) {
        boolean result = false;
        if (mode != null && param instanceof Parameter) {
            for (ModeValueRef modeValRef : ((Parameter) param).getModeValues()) {
                if (modeValRef.getModes().contains(mode)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
