/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.parameterviewer.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import de.dlr.premise.view.parameterviewer.ParameterViewerPage;

public class CollapseExpandAllAction extends Action implements IWorkbenchAction {

    // 
    private final ParameterViewerPage parameterViewerPage;

    /**
     * @param parameterViewerPage
     */
    public CollapseExpandAllAction(ParameterViewerPage parameterViewerPage) {
        this.parameterViewerPage = parameterViewerPage;
    }

    boolean isExpanded = false;

    @Override
    public void run() {
        
        TreeViewer treeViewer = parameterViewerPage.getSourceTreeViewer();
        
        treeViewer.getTree().setRedraw(false);
        treeViewer.getTree().setRedraw(false);
        
        if (isExpanded) {
            isExpanded = false;
            treeViewer.collapseAll();
            parameterViewerPage.getObservedTreeViewer().collapseAll();
        } else {
            isExpanded = true;
            treeViewer.expandAll();
            parameterViewerPage.getObservedTreeViewer().expandAll();
        }

        parameterViewerPage.getObservedTreeViewer().refresh();
        treeViewer.refresh();
        treeViewer.getTree().setRedraw(true);
        treeViewer.getTree().setRedraw(true);
    }

    @Override
    public void dispose() {
    }
}