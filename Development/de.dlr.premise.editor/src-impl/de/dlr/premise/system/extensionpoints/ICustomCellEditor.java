/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.extensionpoints;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;

public interface ICustomCellEditor {
    public boolean appliesForFeature(Object feature);
    public CellEditor createPropertyEditor(Object object, Composite composite, ILabelProvider propertyLabelProvider);
}
