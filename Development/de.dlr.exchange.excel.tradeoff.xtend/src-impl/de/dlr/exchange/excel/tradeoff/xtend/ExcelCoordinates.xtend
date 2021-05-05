/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.tradeoff.xtend

/**
 * This class is used to store the coordinates of a single cell in Excel
 * @author enge_do
 */
@org.eclipse.xtend.lib.annotations.Data
class ExcelCoordinates {
	int sheet
	int row
	int col
}
