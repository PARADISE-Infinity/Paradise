/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.parameterviewer.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import de.dlr.premise.view.parameterviewer.ParameterViewerPage;

public class LoadTableAction extends Action implements IWorkbenchAction {

    /**
     * 
     */
    private final ParameterViewerPage parameterViewerPage;

    /**
     * @param parameterViewerPage
     */
    public LoadTableAction(ParameterViewerPage parameterViewerPage) {
        this.parameterViewerPage = parameterViewerPage;
    }

    @Override
    public void run() {
        parameterViewerPage.loadTableFromPreferences();

        for (int i = 0; i < parameterViewerPage.getColumns().size(); i++) {
            parameterViewerPage.getObservedTreeViewer().getTree().getColumn(i).setWidth(parameterViewerPage.getColumns().get(i).width);
        }

        parameterViewerPage.getObservedTreeViewer().getTree().setColumnOrder(parameterViewerPage.getColumnOrder());
        parameterViewerPage.getObservedTreeViewer().refresh();
    }

    @Override
    public void dispose() {

    }

}