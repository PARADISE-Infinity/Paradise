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

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import de.dlr.premise.view.parameterviewer.ParameterViewerPage;

public class ClearObservedListSelectionAction extends Action implements IWorkbenchAction {

    /**
     * 
     */
    private final ParameterViewerPage parameterViewerPage;

    /**
     * @param parameterViewerPage
     */
    public ClearObservedListSelectionAction(ParameterViewerPage parameterViewerPage) {
        this.parameterViewerPage = parameterViewerPage;
    }

    public void run() {
        if (!this.parameterViewerPage.getObservedTreeViewer().getSelection().isEmpty()) {
            for (Iterator<?> iterator = ((TreeSelection) this.parameterViewerPage.getObservedTreeViewer().getSelection()).iterator(); iterator.hasNext();) {
                this.parameterViewerPage.getObservedParameter().remove(iterator.next());
            }
            this.parameterViewerPage.getObservedTreeViewer().refresh();
            parameterViewerPage.getObservedTreeViewer().setInput(parameterViewerPage.getObservedParameter().toArray());
        }
    }

    @Override
    public void dispose() {

    }

}