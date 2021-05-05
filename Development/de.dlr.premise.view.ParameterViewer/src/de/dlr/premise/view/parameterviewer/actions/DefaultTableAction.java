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

public class DefaultTableAction extends Action implements IWorkbenchAction {

    /**
     * 
     */
    private final ParameterViewerPage parameterViewerPage;

    /**
     * @param parameterViewerPage
     */
    public DefaultTableAction(ParameterViewerPage parameterViewerPage) {
        this.parameterViewerPage = parameterViewerPage;
    }

    public void run() {

        parameterViewerPage.getObservedTreeViewer().getTree().setRedraw(false);
        parameterViewerPage.setDefaultColumns();

        // Default Order
        int[] order = new int[parameterViewerPage.getColumns().size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        parameterViewerPage.getObservedTreeViewer().getTree().setColumnOrder(order);

        // Default Size
        for (int i = 0; i < parameterViewerPage.getColumns().size(); i++) {
            parameterViewerPage.getObservedTreeViewer().getTree().getColumn(i).setWidth(parameterViewerPage.getColumns().get(i).width);
        }
        parameterViewerPage.saveTable();

        parameterViewerPage.getObservedTreeViewer().getTree().setRedraw(true);
    }

    @Override
    public void dispose() {
    }

}