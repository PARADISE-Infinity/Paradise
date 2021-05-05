/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.util.cyclecheck;

/**
 * This class converts the Balancings and Components to an AdjacencyList. On this list you can now apply the Tarjan algorithm to detect
 * cycles.
 * 
 * @author Sönke Escher
 *
 */
public interface IBalancingCycleChecker {

    /**
     * Applies the Tarjan algorithm to the AdjacencyList.  
     * @return true if the AdjacencyList has a cycle and false if not
     */
    public abstract boolean hasCycle();

}