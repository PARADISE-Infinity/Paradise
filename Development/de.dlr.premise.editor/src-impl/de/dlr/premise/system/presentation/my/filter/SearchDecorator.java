/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.ui.viewer.IStyledLabelDecorator;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class SearchDecorator extends BaseLabelProvider implements IStyledLabelDecorator {

    private Map<Color, Set<Notifier>> matches = Maps.newHashMap();
    private int defaultColor;

    public SearchDecorator() {
        this(SWT.COLOR_YELLOW);
    }

    public SearchDecorator(int defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void updateMatches(Collection<Notifier> newMatches) {
        updateMatches(newMatches, defaultColor);
    }

    public void updateMatches(Collection<Notifier> newMatches, int color) {
        Color systemColor = Display.getDefault().getSystemColor(color);
        matches.put(systemColor, ImmutableSet.copyOf(newMatches));
    }

    @Override
    public Image decorateImage(Image image, Object element) {
        return image;
    }

    @Override
    public String decorateText(String text, Object element) {
        return text;
    }

    @Override
    public StyledString decorateStyledText(StyledString styledString, Object element) {
        matches.entrySet().stream().forEach(entry -> {
            if (entry.getValue().contains(element)) {
                styledString.setStyle(0, styledString.length(), new HighlightStyler(entry.getKey()));
            }
        });

        return styledString;
    }

    class HighlightStyler extends Styler {

        private Color color;

        public HighlightStyler(Color color) {
            this.color = color;
        }

        @Override
        public void applyStyles(final TextStyle textStyle) {
            textStyle.background = color;
            if (isDark(color)) {
                textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
                textStyle.font = getBoldFont();
            }
        }

        private Font getBoldFont() {
            FontData fontData[] = Display.getDefault().getSystemFont().getFontData();
            Arrays.stream(fontData).forEach(fd -> fd.setStyle(fd.getStyle() | SWT.BOLD));
            return new Font(Display.getDefault(), fontData);
        }

        private boolean isDark(Color color) {
            RGB rgb = color.getRGB();
            double r = rgb.red / 255.0;
            double g = rgb.green / 255.0;
            double b = rgb.blue / 255.0;
            return Math.sqrt(0.299 * r * r + 0.587 * g * g + 0.114 * b * b) < 0.6;
        }
    }

}
