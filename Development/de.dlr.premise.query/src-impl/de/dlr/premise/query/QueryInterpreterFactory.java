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

import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * @author enge_do
 */
public abstract class QueryInterpreterFactory {

    /**
     * Create an instance of a {@link QueryInterpreter}
     * 
     * @param resourceSet the {@link ResourceSet} of the current editor
     * @return a new instance of {@link QueryInterpreter}
     */
    public static QueryInterpreter createQueryInterpreter(ResourceSet resourceSet) {
        return new PivotOCLQueryInterpreter(resourceSet);
    }
}
