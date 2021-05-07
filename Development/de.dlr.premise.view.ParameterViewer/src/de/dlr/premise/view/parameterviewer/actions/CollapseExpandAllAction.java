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
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import de.dlr.premise.view.parameterviewer.ParameterViewerPage;

public class CollapseExpandAllAction extends Action implements IWorkbenchAction {

    /**
     * 
     */
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
        parameterViewerPage.getSourceTreeViewer().getTree().setRedraw(false);
        parameterViewerPage.getSourceTreeViewer().getTree().setRedraw(false);
        if (isExpanded) {
            isExpanded = false;
            parameterViewerPage.getSourceTreeViewer().collapseAll();
            parameterViewerPage.getObservedTreeViewer().collapseAll();
        } else {
            isExpanded = true;
            parameterViewerPage.getSourceTreeViewer().expandAll();
            parameterViewerPage.getObservedTreeViewer().expandAll();
        }
        parameterViewerPage.getObservedTreeViewer().refresh();
        parameterViewerPage.getSourceTreeViewer().refresh();
        parameterViewerPage.getSourceTreeViewer().getTree().setRedraw(true);
        parameterViewerPage.getSourceTreeViewer().getTree().setRedraw(true);
    }

    @Override
    public void dispose() {
    }
}