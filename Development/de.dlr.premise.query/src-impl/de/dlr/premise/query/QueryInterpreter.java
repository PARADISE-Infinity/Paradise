/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.query;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.query.exceptions.ParserException;

/**
 * Provides an interface to query a premise model.
 * 
 * @author enge_do
 */
public interface QueryInterpreter {

    /**
     * Interpret the given query and return the results.
     * 
     * @param roots the elements from which to start the query
     * @param expression the String-representation of the query
     * @return the results of the query
     * @throws ParserException
     */
    Collection<EObject> query(Collection<EObject> roots, String expression) throws ParserException;
}
