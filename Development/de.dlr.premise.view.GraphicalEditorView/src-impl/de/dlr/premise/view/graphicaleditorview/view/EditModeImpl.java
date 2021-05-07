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

import java.awt.event.MouseEvent;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry;
import y.base.Node;
import y.view.EditMode;

/**
 * Custom {@link EditMode} with our constraints.
 */
public class EditModeImpl extends EditMode {

    private Composite composite;

    public EditModeImpl(Composite composite) {
        super();
        this.composite = composite;

        // apply our settings
        allowNodeEditing(false);
        allowNodeCreation(false);
        allowEdgeCreation(false);
        allowMoveSelection(false);
        allowResizeNodes(false);
        allowBendCreation(false);

        setSelectionBoxMode(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see y.view.EditMode#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                composite.forceFocus();
                getGraph2D().getCurrentView().getFrame().requestFocusInWindow();
                getGraph2D().getCurrentView().getComponent().requestFocus();
            }
        });
        allowNodeCreation(isNodeCreationAllowed());
        allowEdgeCreation(isEdgeCreationAllowed());

    }

    private boolean isNodeCreationAllowed() {
        return MapperRegistry.isNodeCreationAllowed();
    }

    private boolean isEdgeCreationAllowed() {
        return MapperRegistry.isEdgeCreationAllowed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see y.view.EditMode#nodeClicked(y.base.Node)
     */
    @Override
    protected void nodeClicked(final Node v) {
        if (!isNodeCreationAllowed()) {
            // disable selecting nodes so the user cannot delete them
            new Thread() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                    GraphRegistry.getGraph().selectAllNodesAndBends(false);
                    getGraph2D().getCurrentView().updateView();
                }
            }.start();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see y.view.EditMode#mouseMoved(double, double)
     */
    @Override
    public void mouseMoved(double x, double y) {
        // Fix ConncurrentModificationException when moving the mouse when drawing - yFiles bug
        if (!GraphRegistry.getLayoutExecutor().isBusy()) {
            // super.mouseMoved(x, y);
        }
    }

}
