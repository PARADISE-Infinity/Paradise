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

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

/**
 * Implementation of a ShapeNodeRealizer that can automatically adjust it's bounds due to the size of the label.
 */
public class AutoSizeShapeNodeRealizer extends ShapeNodeRealizer {

    /**
     * 
     */
    public AutoSizeShapeNodeRealizer() {
    }

    /**
     * @param type
     */
    public AutoSizeShapeNodeRealizer(byte type) {
        super(type);
    }

    /**
     * @param argNodeRealizer
     */
    public AutoSizeShapeNodeRealizer(NodeRealizer argNodeRealizer) {
        super(argNodeRealizer);
    }

    /**
     * @param type
     * @param x
     * @param y
     * @param label
     */
    public AutoSizeShapeNodeRealizer(byte type, double x, double y, String label) {
        super(type, x, y, label);
        adjustBounds();
    }

    /*
     * (non-Javadoc)
     * 
     * @see y.view.NodeRealizer#setLabelText(java.lang.String)
     */
    @Override
    public void setLabelText(String label) {
        super.setLabelText(label);
        adjustBounds();

    }

    public void adjustBounds() {
        setWidth(Math.max(getLabel().getWidth() + 10, 80));
        setHeight(50);
    }

}
