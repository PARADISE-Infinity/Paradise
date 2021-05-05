/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import de.dlr.premise.view.graphicaleditorview.Activator;
import de.dlr.premise.view.graphicaleditorview.view.GraphRegistry;
import y.view.Graph2DView;
import y.view.LayoutMorpher;

public class ZoomToFitAction extends Action implements IWorkbenchAction {

    private static final String ID = "de.dlr.premise.view.GraphicalEditorView.zoomToFitAction";
    private static ZoomToFitAction instance = null;
    private LayoutMorpher morpher;

    public static synchronized ZoomToFitAction getInstance() {
        if (instance == null) {
            instance = new ZoomToFitAction();
        }
        return instance;
    }

    private ZoomToFitAction() {
        setId(ID);
        setText("Zoom to fit");
        setToolTipText("Zoom to fit");
        setImageDescriptor(Activator.getImageDescriptor("icons/zoom_fit.png"));

        morpher = new LayoutMorpher();
        morpher.setSmoothViewTransform(true);
    }

    @Override
    public void run() {
        morpher.execute((Graph2DView) GraphRegistry.getGraph().getCurrentView(), GraphRegistry.getGraph());
    }

    @Override
    public void dispose() {
    }

}
