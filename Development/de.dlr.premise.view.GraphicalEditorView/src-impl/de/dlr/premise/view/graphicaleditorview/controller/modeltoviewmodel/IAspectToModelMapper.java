/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel;

import java.util.Collection;
import java.util.Observer;

import org.eclipse.emf.ecore.EObject;

/**
 *
 */
public interface IAspectToModelMapper extends Observer {

    public EContext getContext();

    public void selectionChanged(Collection<EObject> selection);

    public void dispose();

    /**
     * @return if the selection contains something this mapper can display
     */
    public boolean isSelectionEmptyForMapper(Collection<?> selection);

    public boolean isNodeCreationAllowed(Collection<EObject> selection);

    default boolean isEdgeCreationAllowed() {
        return true;
    }
}
