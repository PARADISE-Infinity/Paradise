/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.view;

import y.io.graphml.graph2d.ShapeNodeRealizerSerializer;

/**
 * Serializer needed for persisting the AutoSizeShapeNodeRealizer.
 */
public class AutoSizeShapeNodeRealizerSerializer extends ShapeNodeRealizerSerializer {

    @Override
    public Class<AutoSizeShapeNodeRealizer> getRealizerClass() {
        return AutoSizeShapeNodeRealizer.class;
    }

}
