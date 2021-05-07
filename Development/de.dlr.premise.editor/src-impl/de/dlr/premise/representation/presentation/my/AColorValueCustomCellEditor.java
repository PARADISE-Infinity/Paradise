/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.representation.presentation.my;

import org.eclipse.emf.common.ui.celleditor.ExtendedDialogCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.dlr.premise.representation.Color;
import de.dlr.premise.representation.RepresentationPackage;
import de.dlr.premise.system.extensionpoints.ICustomCellEditor;

public class AColorValueCustomCellEditor implements ICustomCellEditor {
    
    @Override
    public boolean appliesForFeature(Object feature) {
        return feature == RepresentationPackage.Literals.COLOR__VALUE;
    }

    public CellEditor createPropertyEditor(Object object, Composite composite, ILabelProvider propertyLabelProvider) {
        final Color color = (Color) object;
        final RGB rgb = fromHexString(color.getValue());

        return new ExtendedDialogCellEditor(composite, propertyLabelProvider) {

            @Override
            protected Object openDialogBox(Control cellEditorWindow) {
                ColorDialog dlg = new ColorDialog(cellEditorWindow.getShell());
                if (rgb != null)  {
                    dlg.setRGB(rgb);
                }
                
                RGB result = dlg.open();
                return toHexString(result); 
            }
        };
    }

    private RGB fromHexString(final String str) {
        java.awt.Color theColor;
        try {
            theColor = java.awt.Color.decode(str);
        } catch (NumberFormatException|NullPointerException e) {
            return null;
        }
        return new RGB(theColor.getRed(), theColor.getGreen(), theColor.getBlue());
    }
    
    private String toHexString(RGB rgb) {
        return "#" + String.format("%02X", rgb.red) + String.format("%02X", rgb.green) + String.format("%02X", rgb.blue);
    }
}
