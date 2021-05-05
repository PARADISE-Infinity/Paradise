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

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.SelectionListener;
import de.dlr.premise.view.graphicaleditorview.view.GraphRegistry;

public class SpinnerAction extends ControlContribution {

    private Spinner spinner;

    public SpinnerAction(String id) {
        super(id);
    }

    @Override
    protected Control createControl(Composite parent) {
        spinner = new Spinner(parent, SWT.BORDER);
        spinner.setMinimum(0);
        spinner.setMaximum(20);
        spinner.setSelection(GraphRegistry.getDepth());
        spinner.setToolTipText("Set a maximum depth. 0 means no restriction.");

        spinner.addModifyListener(e -> {
            GraphRegistry.setDepth(spinner.getSelection());
            SelectionListener.getInstance().scheduleGraphing(300, false);
        });

        return spinner;
    }

}
