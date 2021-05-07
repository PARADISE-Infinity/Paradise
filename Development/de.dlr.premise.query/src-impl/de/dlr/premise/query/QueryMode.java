/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.query;

/**
 * Modes a query can be used for.
 * 
 * @author enge_do
 */
public enum QueryMode {
    /** Shows only found objects */
    FILTER,
    /** Shows all objects but highlights the found ones */
    HIGHLIGHT,
    /** Like FILTER but doesn't attempt to display a tree. Found items will be shown as a list. */
    LIST
}
