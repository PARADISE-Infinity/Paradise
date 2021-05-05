/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.parameterviewer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TreeItem;

import de.dlr.premise.registry.AValueDef;
import de.dlr.premise.view.parameterviewer.data.ColumnValue;

class MyCellModifier implements ICellModifier {

    /**
     * 
     */
    private final ParameterViewerPage parameterViewerPage;

    /**
     * @param parameterViewerPage
     */
    MyCellModifier(ParameterViewerPage parameterViewerPage) {
        this.parameterViewerPage = parameterViewerPage;
    }

    @Override
    public boolean canModify(Object element, String property) {
        for (ColumnValue col : this.parameterViewerPage.getColumns()) {
            if (element instanceof AValueDef && (col.capture.equals(property))
                    && (col.id.equals(ColumnValue.ID.VALUE) || col.id.equals(ColumnValue.ID.UNCERTAINTY))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getValue(Object element, String property) {
        if (element instanceof AValueDef) {
            AValueDef val = ((AValueDef) element);
            for (ColumnValue col : this.parameterViewerPage.getColumns()) {
                if (col.capture == property) {
                    if (col.id == ColumnValue.ID.VALUE && val.getValue() != null) {
                        return val.getValue();
                    } else if (col.id == ColumnValue.ID.UNCERTAINTY && val.getUncertainty() != null) {
                        return val.getUncertainty();
                    }
                }
            }
        }
        return "";
    }

    @Override
    public void modify(final Object treeItem, String property, final Object value) {
        for (final ColumnValue col : this.parameterViewerPage.getColumns()) {
            if (col.capture.equals(property) && (col.id.equals(ColumnValue.ID.VALUE) || col.id.equals(ColumnValue.ID.UNCERTAINTY))) {
                Object[] expandedElements = this.parameterViewerPage.getObservedTreeViewer().getExpandedElements();
                EObject element = (EObject) ((TreeItem) treeItem).getData();

                if (element instanceof AValueDef) {
                    final AValueDef val = ((AValueDef) element);
                    if (value != null && (col.id == ColumnValue.ID.VALUE && !val.getValue().equals(value)
                            || col.id == ColumnValue.ID.UNCERTAINTY && !val.getUncertainty().equals(value))) {

                        ChangeValueCommand command = new ChangeValueCommand(this.parameterViewerPage, element, col, value);
                        this.parameterViewerPage.getCurrentEditor().getEditingDomain().getCommandStack().execute(command);
                    }
                }
                this.parameterViewerPage.getObservedTreeViewer().setExpandedElements(expandedElements);
                this.parameterViewerPage.getObservedTreeViewer().refresh();
                this.parameterViewerPage.getSourceTreeViewer().refresh();
                break;
            }
        }
    }
}