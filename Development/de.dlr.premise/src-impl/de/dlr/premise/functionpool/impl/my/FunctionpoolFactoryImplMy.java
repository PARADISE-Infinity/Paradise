/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.functionpool.impl.my;

import de.dlr.premise.functionpool.CalcEngineJava;
import de.dlr.premise.functionpool.CalcEngineScript;
import de.dlr.premise.functionpool.CalcEngineSpreadSheet;
import de.dlr.premise.functionpool.FunctionPool;
import de.dlr.premise.functionpool.impl.FunctionpoolFactoryImpl;
import de.dlr.premise.util.PremiseHelper;

public class FunctionpoolFactoryImplMy extends FunctionpoolFactoryImpl {

    public FunctionPool createFunctionPool() {
        FunctionPool functionPool = super.createFunctionPool();
        // EMF does not serialize/save default values of attributes, so, get and set it explicitly to ensure serialization:
        functionPool.setMetaModel(functionPool.getMetaModel());
        return functionPool;
    }
    
    public CalcEngineScript createCalcEngineScript() {
        CalcEngineScript calcEngineScript = super.createCalcEngineScript();
        calcEngineScript.setId(PremiseHelper.createId());
        return calcEngineScript;
    }

    public CalcEngineSpreadSheet createCalcEngineSpreadSheet() {
        CalcEngineSpreadSheet calcEngineSpreadSheet = super.createCalcEngineSpreadSheet();
        calcEngineSpreadSheet.setId(PremiseHelper.createId());
        return calcEngineSpreadSheet;
    }

    public CalcEngineJava createCalcEngineJava() {
        CalcEngineJava calcEngineJava = super.createCalcEngineJava();
        calcEngineJava.setId(PremiseHelper.createId());
        return calcEngineJava;
    }
}
