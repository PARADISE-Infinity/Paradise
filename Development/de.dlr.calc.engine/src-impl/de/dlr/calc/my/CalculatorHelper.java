/**
* Copyright (C) 2011-2016 systemsdesign.de, Germany
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Holger Schumann
*
*/
 
package de.dlr.calc.my;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;

import de.dlr.premise.functionpool.ACalculationEngine;
import de.dlr.premise.functionpool.AFnDef;
import de.dlr.premise.states.StateHelper;
import de.dlr.premise.states.data.State;
import de.dlr.premise.util.PremiseHelper;


public class CalculatorHelper {

    public static List<Double> getDoubleValues(List<CalculatorParameter> params, State state) {
        List<Double> doubleParams = new ArrayList<Double>();
        for (CalculatorParameter param : params) {
            doubleParams.add(getDoubleValue(param, state));
        }
        return doubleParams;
    }

    public static double getDoubleValue(CalculatorParameter param, State state) {
        double value;
        if (param.isDouble()) {
            value = param.getDoubleValue();
        } else {
            String strVal = StateHelper.getValue(param.getAParameterDefValue(), state);
            try {
                value = Double.parseDouble(strVal);
            } catch (NullPointerException|NumberFormatException e) {
                value = Double.NaN;
            }
        }
        return value;
    }
    
    
    public static Path resolveCalculationFilePath(ACalculationEngine calcEngine, String filePathStr) {
        
        // get the stored file path
        Path filePath = FileSystems.getDefault().getPath(filePathStr);

        // the file is relative stored in the project workspace
        if (filePath.isAbsolute() == false) {
            if (calcEngine.eResource() != null) {
                Path basePath = FileSystems.getDefault().getPath(PremiseHelper.getResourceAbsPath(calcEngine));
                return basePath.resolve(filePath);
            }
        }
        
        return filePath;
    }
    
    public static Path resolveCalculationFilePath(AFnDef funDef, final String filePathString) throws Exception {

        // get the stored file path
        Path filePath = FileSystems.getDefault().getPath(filePathString);
        if (filePath.isAbsolute()) {
            return filePath;
        }
        
        // get the resource of the fnDef object
        URI path = funDef.eResource().getURI();
        String common = CommonPlugin.resolve(path).toFileString();
        File file = new File(common);
        
        // remove function pool file and return the base path
        String pathStr = file.getPath().replace(path.lastSegment(), "");
        Path basePath = FileSystems.getDefault().getPath(pathStr);
        
        // return the full path to the calculation file
        return basePath.resolve(filePath);
    }
}
