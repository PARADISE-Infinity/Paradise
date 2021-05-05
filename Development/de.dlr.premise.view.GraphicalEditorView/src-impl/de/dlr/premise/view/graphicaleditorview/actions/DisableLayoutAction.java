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

public class DisableLayoutAction extends Action implements IWorkbenchAction {

    private static final String ID = "de.dlr.premise.view.GraphicalEditorView.disableLayoutAction";
    private static DisableLayoutAction instance = null;
    private boolean layoutEnabled = true;

    public static synchronized DisableLayoutAction getInstance() {
        if (instance == null) {
            instance = new DisableLayoutAction();
        }
        return instance;
    }

    private DisableLayoutAction() {
        setId(ID);
        setText("Disable Layout");
        setToolTipText("Disable Layout");
        setImageDescriptor(Activator.getImageDescriptor("icons/layout_on.png"));
    }

    @Override
    public void run() {
        layoutEnabled = !layoutEnabled;
        GraphRegistry.setLayoutEnabled(layoutEnabled);
        if (layoutEnabled) {
            setImageDescriptor(Activator.getImageDescriptor("icons/layout_on.png"));
            setToolTipText("Disable Layout");
            GraphRegistry.getLayoutExecutor().scheduleLayout(true);
        } else {
            setImageDescriptor(Activator.getImageDescriptor("icons/layout_off.png"));
            setToolTipText("Enable Layout");
        }
    }

    @Override
    public void dispose() {
    }

}
