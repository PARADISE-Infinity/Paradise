/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.view;

import java.awt.Insets;
import java.awt.geom.Rectangle2D;

import y.base.Node;
import y.base.NodeList;
import y.layout.LayoutGraph;
import y.layout.grouping.InsetsGroupBoundsCalculator;

/**
 * Custom {@link InsetsGroupBoundsCalculator} that works - unlike the default one.
 */
public class InsetsGroupBoundsCalculatorMy extends InsetsGroupBoundsCalculator {

    private Insets defaultInsets;

    /**
     * @param insets
     */
    public InsetsGroupBoundsCalculatorMy(Insets insets) {
        super(insets);
        setDefaultInsets(insets);
    }

    /*
     * (non-Javadoc)
     * 
     * @see y.layout.grouping.InsetsGroupBoundsCalculator#calculateBounds(y.layout.LayoutGraph, y.base.Node, y.base.NodeList)
     */
    @Override
    public Rectangle2D calculateBounds(LayoutGraph graph, Node groupNode, NodeList children) {
        Rectangle2D bounds = super.calculateBounds(graph, groupNode, children);
        Rectangle2D correctBounds = (Rectangle2D) bounds.clone();
        double x = bounds.getX() - getDefaultInsets().left;
        double y = bounds.getY() - getDefaultInsets().top;
        double w = bounds.getWidth() + getDefaultInsets().left + getDefaultInsets().right;
        double h = bounds.getHeight() + getDefaultInsets().top + getDefaultInsets().bottom;
        correctBounds.setRect(x, y, w, h);
        return correctBounds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see y.layout.grouping.InsetsGroupBoundsCalculator#getDefaultInsets()
     */
    @Override
    public Insets getDefaultInsets() {
        return defaultInsets;
    }

    /*
     * (non-Javadoc)
     * 
     * @see y.layout.grouping.InsetsGroupBoundsCalculator#setDefaultInsets(java.awt.Insets)
     */
    @Override
    public void setDefaultInsets(Insets insets) {
        defaultInsets = insets;
    }

}
