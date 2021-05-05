/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my.filter.workarounds;

import org.eclipse.emf.edit.ui.provider.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;


/**
 * This ports the fix done in https://bugs.eclipse.org/bugs/attachment.cgi?id=173342
 * 
 * The fix has been part of the platform for over two years now, but we don't have it as our product is still based on indigo.
 * 
 * TODO remove this workaround once we have a product based on a somewhat current eclipse platform version.
 */
public class DelegatingStyledCellLabelProvider2 extends DelegatingStyledCellLabelProvider {

    public DelegatingStyledCellLabelProvider2(IStyledLabelProvider styledLabelProvider) {
        super(styledLabelProvider);
        // TODO Auto-generated constructor stub
    }
    

    @Override
    public Color getToolTipBackgroundColor(Object object) {
        if (this.getStyledStringProvider() instanceof CellLabelProvider) {
            return ((CellLabelProvider) this.getStyledStringProvider())
                    .getToolTipBackgroundColor(object);
        }
        return super.getToolTipBackgroundColor(object);
    }

    @Override
    public int getToolTipDisplayDelayTime(Object object) {
        if (this.getStyledStringProvider() instanceof CellLabelProvider) {
            return ((CellLabelProvider) this.getStyledStringProvider())
                    .getToolTipDisplayDelayTime(object);
        }
        return super.getToolTipDisplayDelayTime(object);
    }

    @Override
    public Font getToolTipFont(Object object) {
        if (this.getStyledStringProvider() instanceof CellLabelProvider) {
            return ((CellLabelProvider) this.getStyledStringProvider())
                    .getToolTipFont(object);
        }
        return super.getToolTipFont(object);
    }

    @Override
    public Color getToolTipForegroundColor(Object object) {
        if (this.getStyledStringProvider() instanceof CellLabelProvider) {
            return ((CellLabelProvider) this.getStyledStringProvider())
                    .getToolTipForegroundColor(object);
        }
        return super.getToolTipForegroundColor(object);
    }

    @Override
    public Image getToolTipImage(Object object) {
        if (this.getStyledStringProvider() instanceof CellLabelProvider) {
            return ((CellLabelProvider) this.getStyledStringProvider())
                    .getToolTipImage(object);
        }
        return super.getToolTipImage(object);
    }

    @Override
    public Point getToolTipShift(Object object) {
        if (this.getStyledStringProvider() instanceof CellLabelProvider) {
            return ((CellLabelProvider) this.getStyledStringProvider())
                    .getToolTipShift(object);
        }
        return super.getToolTipShift(object);
    }

    @Override
    public int getToolTipStyle(Object object) {
        if (this.getStyledStringProvider() instanceof CellLabelProvider) {
            return ((CellLabelProvider) this.getStyledStringProvider())
                    .getToolTipStyle(object);
        }
        return super.getToolTipStyle(object);
    }

    @Override
    public String getToolTipText(Object element) {
        if (this.getStyledStringProvider() instanceof CellLabelProvider) {
            return ((CellLabelProvider) this.getStyledStringProvider())
                    .getToolTipText(element);
        }
        return super.getToolTipText(element);
    }

    @Override
    public int getToolTipTimeDisplayed(Object object) {
        if (this.getStyledStringProvider() instanceof CellLabelProvider) {
            return ((CellLabelProvider) this.getStyledStringProvider())
                    .getToolTipTimeDisplayed(object);
        }
        return super.getToolTipTimeDisplayed(object);
    }

    @Override
    public boolean useNativeToolTip(Object object) {
        if (this.getStyledStringProvider() instanceof CellLabelProvider) {
            return ((CellLabelProvider) this.getStyledStringProvider())
                    .useNativeToolTip(object);
        }
        return super.useNativeToolTip(object);
    }

}
