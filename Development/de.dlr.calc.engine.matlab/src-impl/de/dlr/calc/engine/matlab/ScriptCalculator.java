/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.matlab;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;

import de.dlr.calc.engine.combined.IFnDefCalculator;
import de.dlr.calc.my.CalculatorHelper;
import de.dlr.calc.my.CalculatorParameter;
import de.dlr.premise.functionpool.CalcEngineScript;
import de.dlr.premise.functionpool.FnDefScript;
import de.dlr.premise.registry.MetaData;
import de.dlr.premise.states.data.State;

public class ScriptCalculator implements IFnDefCalculator<FnDefScript> {
    @Override
    public Class<FnDefScript> getFndDefType() {
        return FnDefScript.class;
    }
    
    public double calculate(FnDefScript fnDef, List<CalculatorParameter> parameters, State state) {
        List<Double> params = CalculatorHelper.getDoubleValues(parameters, state);
        return calculateScript(fnDef, params);
    }

    private double calculateScript(FnDefScript funcDef, List<Double> params) {
        if (!(funcDef.getCalcEngine() instanceof CalcEngineScript)) {
            throw new IllegalArgumentException("A spreadsheet function must be backed by a spreadsheet calc engine");
        }

        // initialize calculation engine
        CalcEngineScript engine = (CalcEngineScript) funcDef.getCalcEngine();
        MatlabEngineJMBridge matlab = Activator.getCalculationScriptEngine();

        System.out.println("Matlab started and ready");

        // set properties
        for (MetaData prop : engine.getProperties()) {
            matlab.setProperty(prop.getName(), prop.getValue());
            System.out.println("Set " + prop.getName() + " > " + prop.getValue());
        }

        // get script name
        String scriptName = funcDef.getScriptName();
        if (scriptName == null || scriptName.isEmpty()) {
            throw new IllegalArgumentException("Calculation omitted: Calculation engine '" + " script '" + scriptName + "' not found!");
        }

        System.out.println("Use " + scriptName);

        // try to add libraries
        for (String curi : engine.getLibURIs()) {
//            Path absoulutePath = CalculatorHelper.resolveCalculationFilePath(engine, curi);
            
            // get absolute file name
            Path absolutePath = null;
            try {
                absolutePath = CalculatorHelper.resolveCalculationFilePath(funcDef, curi);                
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            
            if (!Files.exists(absolutePath)) {
                throw new IllegalArgumentException("Calculation engine '" + engine.getName() + "', library folder '" + curi
                        + "' doesn't exist (Full path: '" + absolutePath.toString() + "'");
            }

            if (!matlab.addLibrary(absolutePath.toString())) {
                throw new IllegalArgumentException("Calculation engine '" + engine.getName() + "', library folder '" + curi
                        + "' exists, but couldn't be added to matlab (Full path: '" + absolutePath.toString() + "'");
            }
        }

        // try to set working directory
        String wrkDir = engine.getWorkDirURI();

        try {
            if (wrkDir != null && !wrkDir.isEmpty()) {
                matlab.setWorkingDir(wrkDir);
                System.out.println("Set working directory to " + wrkDir);
            }
        } catch (Exception e) {
            throw new RuntimeException("Calculation omitted: Unable to set working directory to '" + wrkDir + "' not found!", e);
        }

        double val = matlab.callScript(scriptName, new BasicEList<Double>(params));

        matlab.close();

        return val;
    }
}
