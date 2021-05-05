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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

import org.junit.Before;
import org.junit.Test;

public class ExcelEngineSXTest {

    private ExcelEngineSX excelEngineSX;

    private static String EXCEL_FILE = de.dlr.premise.util.TestHelper.locateFile("de.dlr.calc.engine.excel", "tests/data/worksheet.xls").getPath().substring(1);
    
    @Before
    public void createExcelEngineSX() throws Exception {
        excelEngineSX = new ExcelEngineSX();
    }
    
    @Test
    public void testExcelEngineSX() {

        // check excel engine
        assumeNotNull(excelEngineSX);

        // test default settings
        assertEquals(null, excelEngineSX.getFile());
        assertEquals("Sheet1",excelEngineSX.getSheet());
    }
    
    @Test
    public void testSetFile() throws Exception {            

        // check excel engine
        assumeNotNull(excelEngineSX);
        
        // test set file
        assertTrue(excelEngineSX.setFile(EXCEL_FILE));
        assertEquals(EXCEL_FILE, excelEngineSX.getFile());
    }

    @Test
    public void testSetSheet() throws Exception {
        
        // check excel engine
        assumeNotNull(excelEngineSX);

        // test default settings
        assertTrue(excelEngineSX.setFile(EXCEL_FILE));
        assertEquals("Tabelle1",excelEngineSX.getSheet());
        
        // test function
        final String sheet = "Tabelle2";
        assertTrue(excelEngineSX.setSheet(sheet));
        assertEquals(sheet, excelEngineSX.getSheet());
    }

    @Test
    public void testSetAndGetCell() throws Exception {
        
        // check excel engine
        assumeNotNull(excelEngineSX);

        // test default settings
        assertTrue(excelEngineSX.setFile(EXCEL_FILE));

        final double value = 7.5;        
        excelEngineSX.setCell(0, 12, value);
        assertEquals("sum", excelEngineSX.getCell(0, 3));
        assertEquals(value, Double.valueOf(excelEngineSX.getCell(0, 12)), 0.001);
    }

    @Test
    public void testClean() throws Exception {
        
        // check excel engine
        assumeNotNull(excelEngineSX);

        // test default settings
        assertTrue(excelEngineSX.setFile(EXCEL_FILE));
        assertEquals("Tabelle1",excelEngineSX.getSheet());
        
        // test function
        final String sheet = "Tabelle2";
        assertTrue(excelEngineSX.setSheet(sheet));
        
        excelEngineSX.clean();
        assertEquals(sheet, excelEngineSX.getSheet());
    }
}
