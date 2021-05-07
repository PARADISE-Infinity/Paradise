/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.calc.engine.dsl.ui;

import org.eclipse.emf.common.ui.celleditor.ExtendedDialogCellEditor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.dlr.premise.system.Balancing;
import de.dlr.premise.system.SystemPackage;
import de.dlr.premise.system.extensionpoints.ICustomCellEditor;

public class CalcCellEditor implements ICustomCellEditor {

    @Override
    public boolean appliesForFeature(Object feature) {
        return feature == SystemPackage.Literals.BALANCING__FUNCTION || feature == SystemPackage.Literals.BALANCING__SOURCE_POINTERS  || feature == SystemPackage.Literals.BALANCING__TARGET_POINTER;
    }

    @Override
    public CellEditor createPropertyEditor(Object object, Composite composite, ILabelProvider propertyLabelProvider) {
        final Balancing balancing = (Balancing) object;

        return new ExtendedDialogCellEditor(composite, propertyLabelProvider) {

            @Override
            protected Object openDialogBox(Control cellEditorWindow) {
                CalcDialog xtextDialog = CalcDialog.getNewInstance(cellEditorWindow.getShell(), balancing);
                int returnCode = xtextDialog.open();
                if (returnCode == Dialog.CANCEL) {
                    return null;
                }
                return xtextDialog.getContent();
            }
        };
    }

}
