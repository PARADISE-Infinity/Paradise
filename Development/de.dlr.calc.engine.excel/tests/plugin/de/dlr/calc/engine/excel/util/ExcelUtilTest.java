/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.excel.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ExcelUtilTest {

    @Test
    public void testExcelUtil() {
        ExcelUtil excelutil = new ExcelUtil();
        assertNotNull(excelutil);
    }
    
    @Test
    public void testConversion() {
        
        // test invalid inputs
        assertNull(ExcelUtil.convert(""));        
        assertNull(ExcelUtil.convert("asjsadcasödjkviajdfv"));
        
        int[] cellCoordinate = null;
        cellCoordinate = ExcelUtil.convert("A4");
        assertEquals(0,cellCoordinate[0]);
        assertEquals(3,cellCoordinate[1]);
        
        cellCoordinate = ExcelUtil.convert("AZ14");
        assertEquals(51,cellCoordinate[0]);
        assertEquals(13,cellCoordinate[1]);
    }
}
