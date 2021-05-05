/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.excel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import de.dlr.calc.engine.combined.IFnDefCalculator;
import de.dlr.calc.engine.excel.util.ExcelUtil;
import de.dlr.calc.my.CalculatorHelper;
import de.dlr.calc.my.CalculatorParameter;
import de.dlr.premise.functionpool.AFnDef;
import de.dlr.premise.functionpool.CalcEngineSpreadSheet;
import de.dlr.premise.functionpool.FnDefSpreadSheet;
import de.dlr.premise.functionpool.FnInput;
import de.dlr.premise.functionpool.FnInputSpreadSheet;
import de.dlr.premise.functionpool.FnOutputSpreadSheet;
import de.dlr.premise.functionpool.MultipleInputsCellDirection;
import de.dlr.premise.states.data.State;

public class SpreadsheetCalculator implements IFnDefCalculator<FnDefSpreadSheet> {
    
    @Override
    public Class<FnDefSpreadSheet> getFndDefType() {
        return FnDefSpreadSheet.class;
    }

    @Override
    public double calculate(FnDefSpreadSheet fnDef, List<CalculatorParameter> parameters, State state) {

        if (!(fnDef.getCalcEngine() instanceof CalcEngineSpreadSheet)) {
            throw new IllegalArgumentException("A spreadsheet function must be backed by a spreadsheet calc engine");
        }

        // TODO steh_ti This could be cached
        ExcelEngineSX excel = createCalculationSpreadsheet(fnDef);
        if (excel == null) {
            throw new IllegalArgumentException("The calc engine for function \"" + fnDef.getName() + "\" could not be initialized");
        }
        
        if (!isInputCellsValid(fnDef, parameters.size())) {
            throw new IllegalArgumentException("The input cell defintition for \"" + fnDef.getName() + "\" is not valid");
        }
        
        if (!isOutputCellValid(fnDef)) {
            throw new IllegalArgumentException("The output cell definition for \"" + fnDef.getName() + "\" is not valid");
        }
        
        List<Double> doubleParams = CalculatorHelper.getDoubleValues(parameters, state);
        
        setInputCells(excel, fnDef, doubleParams);

       try {
           return Double.parseDouble(getOutput(excel, fnDef));
       } catch (NumberFormatException e) {
           return Double.NaN;
       }
    }
    
    protected boolean isOutputCellValid(AFnDef fnSig) {
        boolean result = false;
        FnOutputSpreadSheet outputCell = (FnOutputSpreadSheet) fnSig.getOutput();
        if (outputCell != null && outputCell.getCell() != null && !outputCell.getCell().isEmpty()) {
            result = true;
        }
        return result;
    }

    protected boolean isInputCellsValid(AFnDef fnSig, int paramCount) {
        boolean result = false;
        EList<FnInput> inputCells = fnSig.getInputs();
        boolean multipleInputs = 0 < inputCells.size() && inputCells.size() < paramCount && fnSig.isLastInputMultiple();

        if (inputCells.size() == paramCount || multipleInputs) {
            result = true;
            if (inputCells.size() > 0) {
                // check inputCell (string) cells
                for (FnInput cell : inputCells) {
                    String cellStr = ((FnInputSpreadSheet) cell).getCell();
                    if (cellStr == null || cellStr.isEmpty()) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    private void setInputCells(ExcelEngineSX engine, FnDefSpreadSheet fnDef, List<Double> dblValues) {
        EList<FnInput> inputCells = fnDef.getInputs();

        // iterate over all source parameter values
        int cellIdx = 0, xoffset = 0, yoffset = 0;
        for (int valueIdx = 0; valueIdx < dblValues.size(); valueIdx++) {
            // increase cellIdx while inputsCells available
            if (valueIdx < inputCells.size()) {
                cellIdx = valueIdx;
            } else {
                // last inputCell reached && lastInputMultiple==true,
                // increase related cell coordinate
                if (fnDef.getMultipleInputsCellDirection() == MultipleInputsCellDirection.HORIZONTAL){
                    xoffset++;
                } else {
                    yoffset++;
                }
            }
            // get cell coordinates
            int[] cellCoordinates = ExcelUtil.convert(
                    ((FnInputSpreadSheet) inputCells.get(cellIdx)).getCell());
            // set cell with value
            engine.setCell(cellCoordinates[0] + xoffset, cellCoordinates[1] + yoffset,
                    dblValues.get(valueIdx).doubleValue());
        }
    }
    
    private String getOutput(ExcelEngineSX calc, FnDefSpreadSheet fnDef) {
        FnOutputSpreadSheet outputCell = (FnOutputSpreadSheet) fnDef.getOutput();
        int[] cellCoordinate = ExcelUtil.convert(outputCell.getCell());
        return calc.getCell(cellCoordinate[0], cellCoordinate[1]);
    }
    
    private ExcelEngineSX createCalculationSpreadsheet(FnDefSpreadSheet funcDef) {

        boolean success;
        
        // create an 
        ExcelEngineSX excel = new ExcelEngineSX();
        CalcEngineSpreadSheet engine = (CalcEngineSpreadSheet) funcDef.getCalcEngine();

        // resolve path of excel file relative to the FunctionPool file containing the CalcEngine
        Path filePath = null;
        try {
            filePath = CalculatorHelper.resolveCalculationFilePath(funcDef, engine.getFileURI());
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Calculation engine '" + engine.getName() + "' couldn't be initialized, excel file '"
                    + engine.getFileURI() + "' doesn't exist (Full path: '" + filePath.toString() + "'");
        }

        success = excel.setFile(filePath.toString());
        if (!success) {
            return null;
        }
        
        String sheetName = funcDef.getSheetName() ;
        if (sheetName != null && !sheetName.isEmpty()) {
            success = excel.setSheet(sheetName);
            if (!success) {
                return null;
            }
        }
        
        return excel;
    }
}
