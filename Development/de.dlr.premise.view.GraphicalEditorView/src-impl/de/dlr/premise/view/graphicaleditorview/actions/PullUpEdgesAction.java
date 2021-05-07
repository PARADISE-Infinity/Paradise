/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import de.dlr.premise.view.graphicaleditorview.Activator;
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.SelectionListener;
import de.dlr.premise.view.graphicaleditorview.view.GraphRegistry;

public class PullUpEdgesAction extends Action implements IWorkbenchAction {

    private static final String ID = "de.dlr.premise.view.GraphicalEditorView.pullUpEdgesAction";
    private static PullUpEdgesAction instance = null;
    private boolean pullUpEdges = true;

    public static synchronized PullUpEdgesAction getInstance() {
        if (instance == null) {
            instance = new PullUpEdgesAction();
        }
        return instance;
    }

    private PullUpEdgesAction() {
        setId(ID);
        setText("Move edges of hidden children to parents");
        setToolTipText("Move edges of hidden children to parents");
        setChecked(true);
        setImageDescriptor(Activator.getImageDescriptor("icons/pull_up_edges.png"));
    }

    @Override
    public void run() {
        pullUpEdges = !pullUpEdges;
        setChecked(pullUpEdges);
        GraphRegistry.setPullUpEdges(pullUpEdges);
        SelectionListener.getInstance().scheduleGraphing(300, false);
    }

    @Override
    public void dispose() {
    }

}
