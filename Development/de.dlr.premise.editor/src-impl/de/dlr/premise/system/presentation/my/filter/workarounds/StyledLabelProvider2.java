/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my.filter.workarounds;

import org.eclipse.emf.edit.ui.provider.DecoratingColumLabelProvider.StyledLabelProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;

/**
 * A DecolatingColumnLabelProvider.StyledLabelProvider that can delegate its CellLabelProvider function to its delegate not only to the
 * decorator.
 */
public class StyledLabelProvider2 extends StyledLabelProvider {

    public StyledLabelProvider2(ILabelProvider labelProvider, ILabelDecorator labelDecorator) {
        super(labelProvider, labelDecorator);

        if (this.cellLabelProvider == null && labelProvider instanceof CellLabelProvider) {
            this.cellLabelProvider = (CellLabelProvider) labelProvider;
        }
    }

}
