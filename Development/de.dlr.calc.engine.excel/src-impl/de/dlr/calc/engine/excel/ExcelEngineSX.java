/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.calc.engine.excel;

import com.smartxls.WorkBook;

public class ExcelEngineSX {

	private WorkBook workBook;
	
	private String filePath;	
	private String[] sheets;
	private int currentSheet;
	
	public ExcelEngineSX() {
		workBook = new WorkBook();
		// set 1st sheet to default
		currentSheet = workBook.getSheet();
	}
	
	public String getFile() {
		return filePath;
	}

	public boolean setFile(String filePath) {
		boolean result = false;

		// open work sheet
		try {
			workBook.read(filePath);

			// get all sheet names
			int numSheets = workBook.getNumSheets();
			sheets = new String[numSheets];
			for (int i = 0; i < numSheets; i++) {
				sheets[i] = workBook.getSheetName(i);
			}
			this.filePath = filePath;
			result = true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return result;
	}

	public String getSheet() {
	    String name = "";
		try {
            name = workBook.getSheetName(currentSheet);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return name;
	}

	public boolean setSheet(String name) {
		boolean result = false;

		// get sheet index
		int index = 0;
		for (int i = 0; i < sheets.length; i++) {
			if (sheets[i].equals(name)) {
				index = i;
				break;
			}
		}

		// load sheet
		try {		
			workBook.setSheet(index);
			currentSheet = index;
			result = true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return result;
	}

	public void setCell(int col, int row, double value) {

		try {
			workBook.setNumber(row, col, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getCell(int col, int row) {
		String result = null;
		try {
			// warning: we can't use getText() to get the number because it will 
			// return the string-value formatted by excel which can include roundings.
			// getText() is used to check if the value is a number. we can't use 
			// getNumber() here because if there is a string inside the cell getNumber()
			// will simply return 0
			Double.valueOf(workBook.getText(row, col).replace(",","."));
			
			result = String.valueOf(workBook.getNumber(row, col));
		} catch (NumberFormatException e) {
			try {
				// if the cell is not a number, simply return the text
				result = workBook.getText(row, col);
			} catch (Exception e1) {
				throw new RuntimeException(e);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	public void save() {
		try {
			workBook.write(filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clean() {
	    workBook = new WorkBook();
	    setFile(filePath);
        try {
            workBook.setSheet(currentSheet);
        } catch (Exception e) {
        }
	}
}
