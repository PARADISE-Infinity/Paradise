/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.functionpool.provider.my;

import static org.junit.Assert.*;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.junit.Test;

import de.dlr.premise.system.provider.my.SystemImageProviderMyTest;
import de.dlr.premise.functionpool.FunctionpoolFactory;
import de.dlr.premise.functionpool.provider.my.CalcEngineScriptItemProviderMy;
import de.dlr.premise.functionpool.provider.my.CalcEngineSpreadSheetItemProviderMy;
import de.dlr.premise.functionpool.provider.my.FnDefScriptItemProviderMy;
import de.dlr.premise.functionpool.provider.my.FnDefSpreadSheetItemProviderMy;
import de.dlr.premise.functionpool.provider.my.FnInputItemProviderMy;
import de.dlr.premise.functionpool.provider.my.FnInputSpreadSheetItemProviderMy;
import de.dlr.premise.functionpool.provider.my.FnOutputItemProviderMy;
import de.dlr.premise.functionpool.provider.my.FnOutputSpreadSheetItemProviderMy;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.registry.provider.my.MetaDataItemProviderMy;


public class FunctionpoolImageProviderMyTest {

    @Test
    public void testMetaData() {
        EObject item = RegistryFactory.eINSTANCE.createMetaData();
        ItemProviderAdapter itemAdapter = new MetaDataItemProviderMy(null);
        String menuIconPathHead = "CreateACalculationEngine_properties_";
        long fileSize = 597;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testFnInput() {
        EObject item = FunctionpoolFactory.eINSTANCE.createFnInput();
        ItemProviderAdapter itemAdapter = new FnInputItemProviderMy(null);
        String menuIconPathHead = "CreateAFnDef_inputs_";
        long fileSize = 559;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testFnOutput() {
        EObject item = FunctionpoolFactory.eINSTANCE.createFnOutput();
        ItemProviderAdapter itemAdapter = new FnOutputItemProviderMy(null);
        String menuIconPathHead = "CreateAFnDef_output_";
        long fileSize = 556;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testFnInputSpreadSheet() {
        EObject item = FunctionpoolFactory.eINSTANCE.createFnInputSpreadSheet();
        ItemProviderAdapter itemAdapter = new FnInputSpreadSheetItemProviderMy(null);
        String menuIconPathHead = "CreateAFnDef_inputs_";
        long fileSize = 559;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testFnOutputSpreadSheet() {
        EObject item = FunctionpoolFactory.eINSTANCE.createFnOutputSpreadSheet();
        ItemProviderAdapter itemAdapter = new FnOutputSpreadSheetItemProviderMy(null);
        String menuIconPathHead = "CreateAFnDef_output_";
        long fileSize = 556;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testCalcEngineScript() {
        EObject item = FunctionpoolFactory.eINSTANCE.createCalcEngineScript();
        ItemProviderAdapter itemAdapter = new CalcEngineScriptItemProviderMy(null);
        String menuIconPathHead = "CreateFunctionPool_calcEngines_";
        long fileSize = 1048;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testCalcEngineSpreadSheet() {
        EObject item = FunctionpoolFactory.eINSTANCE.createCalcEngineSpreadSheet();
        ItemProviderAdapter itemAdapter = new CalcEngineSpreadSheetItemProviderMy(null);
        String menuIconPathHead = "CreateFunctionPool_calcEngines_";
        long fileSize = 1061;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testFnDefScript() {
        EObject item = FunctionpoolFactory.eINSTANCE.createFnDefScript();
        ItemProviderAdapter itemAdapter = new FnDefScriptItemProviderMy(null);
        String menuIconPathHead = "CreateFunctionPool_functions_";
        long fileSize = 357;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testFnDefSpreadSheet() {
        EObject item = FunctionpoolFactory.eINSTANCE.createFnDefSpreadSheet();
        ItemProviderAdapter itemAdapter = new FnDefSpreadSheetItemProviderMy(null);
        String menuIconPathHead = "CreateFunctionPool_functions_";
        long fileSize = 357;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }
}
