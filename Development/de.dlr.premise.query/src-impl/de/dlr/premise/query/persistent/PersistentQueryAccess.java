/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.query.persistent;

import java.util.Set;

/**
 * Store and load queries from the model.
 * 
 * @author enge_do
 */
public interface PersistentQueryAccess {

    Set<PersistableQuery> readQueries();

    void addQuery(PersistableQuery query);

    void updateQuery(PersistableQuery query);

    void removeQuery(PersistableQuery query);
}
