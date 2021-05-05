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

public class LayoutOrientationAction extends Action implements IWorkbenchAction {

    private static final String ID = "de.dlr.premise.view.GraphicalEditorView.layoutOrientationAction";
    private static LayoutOrientationAction instance = null;
    private boolean topToBottom = true;

    public static synchronized LayoutOrientationAction getInstance() {
        if (instance == null) {
            instance = new LayoutOrientationAction();
        }
        return instance;
    }

    private LayoutOrientationAction() {
        setId(ID);
        setText("Toggle layout orientation");
        setToolTipText("Toggle layout orientation");
        setImageDescriptor(Activator.getImageDescriptor("icons/layoutLTR"+topToBottom+".png"));
    }

    @Override
    public void run() {
        topToBottom = !topToBottom;
        GraphRegistry.setLayoutTopToBottom(topToBottom);
        GraphRegistry.getLayoutExecutor().scheduleLayout(false);
        setImageDescriptor(Activator.getImageDescriptor("icons/layoutLTR"+topToBottom+".png"));
    }

    @Override
    public void dispose() {
    }

}
