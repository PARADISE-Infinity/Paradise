/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.presentation.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public class DefaultSizeDialog extends Dialog {

    /** These are copied from Dialog class, where they are private. */
    public static final String DIALOG_FONT_DATA = "DIALOG_FONT_NAME"; //$NON-NLS-1$
    public static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$
    public static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$

    public DefaultSizeDialog(IShellProvider parentShell) {
        super(parentShell);
    }

    public DefaultSizeDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Mostly a copy of the same method in Dialog, but with a call to a separate method for providing a default size that is used if no
     * persisted dialog settings are available.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
     */
    @Override
    protected Point getInitialSize() {
        Point result = getDefaultSize();

        // Check the dialog settings for a stored size.
        if ((getDialogBoundsStrategy() & DIALOG_PERSISTSIZE) != 0) {
            IDialogSettings settings = getDialogBoundsSettings();

            if (settings != null) {
                // Check that the dialog font matches the font used
                // when the bounds was stored. If the font has changed,
                // we do not honor the stored settings.
                // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=132821
                boolean useStoredBounds = true;
                String previousDialogFontData = settings.get(DIALOG_FONT_DATA);

                // There is a previously stored font, so we will check it.
                // Note that if we haven't stored the font before, then we will
                // use the stored bounds. This allows restoring of dialog bounds
                // that were stored before we started storing the fontdata.
                if (previousDialogFontData != null && previousDialogFontData.length() > 0) {
                    FontData[] fontDatas = JFaceResources.getDialogFont().getFontData();

                    if (fontDatas.length > 0) {
                        String currentDialogFontData = fontDatas[0].toString();
                        useStoredBounds = currentDialogFontData.equalsIgnoreCase(previousDialogFontData);
                    }
                }

                if (useStoredBounds) {
                    try {
                        // Get the stored width and height.
                        int width = settings.getInt(DIALOG_WIDTH);

                        if (width != DIALOG_DEFAULT_BOUNDS) {
                            result.x = width;
                        }

                        int height = settings.getInt(DIALOG_HEIGHT);

                        if (height != DIALOG_DEFAULT_BOUNDS) {
                            result.y = height;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }

        // No attempt is made to constrain the bounds. The default
        // constraining behavior in Window will be used.
        return result;
    }

    /**
     * Provides the dialog's default size. Duplicates the behaviour of JFace's standard dialog. Subclasses may override.
     * 
     * @return Default size.
     */
    protected Point getDefaultSize() {
        return getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
    }
}
