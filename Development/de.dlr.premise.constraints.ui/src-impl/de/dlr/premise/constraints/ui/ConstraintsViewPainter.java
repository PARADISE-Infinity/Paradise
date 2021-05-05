/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.constraints.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

public class ConstraintsViewPainter implements PaintListener {

    private static float HEIGHT = 1000;
    private static float ASPECT_RATIO = .4f; // width / height

    private ConstraintsStatusTracker statusTracker;

    public ConstraintsViewPainter(ConstraintsStatusTracker statusTracker) {
        this.statusTracker = statusTracker;
    }

    @Override
    public void paintControl(PaintEvent e) {

        GC gc = e.gc;
        Display d = e.display;

        Rectangle rect = ((Canvas) e.widget).getBounds();

        float width, height, scale;

        if (rect.height < rect.width / ASPECT_RATIO) {
            height = rect.height;
            width = height * ASPECT_RATIO;
        } else {
            width = rect.width;
            height = width / ASPECT_RATIO;
        }

        scale = height / HEIGHT;

        Transform trans = new Transform(d);
        gc.setTransform(trans);

        trans.translate((rect.width - width) / 2, 0);
        trans.scale(scale, scale);
        gc.setTransform(trans);

        draw(gc, d, trans);
    }

    private void draw(GC gc, Display d, Transform trans) {
        ConstraintsStatus status = statusTracker.getStatus();

        gc.setBackground(d.getSystemColor(SWT.COLOR_BLACK));
        gc.fillRectangle(0, 0, (int) (HEIGHT * ASPECT_RATIO), (int) HEIGHT);

        trans.translate(HEIGHT * ASPECT_RATIO / 2, 0);
        trans.translate(-HEIGHT / 8, 0);
        gc.setTransform(trans);

        if (status == ConstraintsStatus.HAS_VIOLATIONS) {
            gc.setBackground(d.getSystemColor(SWT.COLOR_RED));
        } else {
            gc.setBackground(d.getSystemColor(SWT.COLOR_GRAY));
        }
        gc.fillArc(0, (int) (1f / 16f * HEIGHT), (int) (HEIGHT / 4f), (int) (HEIGHT / 4f), 0, 360);

        if (status == ConstraintsStatus.HAS_NOT_SATISFIED) {
            gc.setBackground(d.getSystemColor(SWT.COLOR_YELLOW));
        } else {
            gc.setBackground(d.getSystemColor(SWT.COLOR_GRAY));
        }
        gc.fillArc(0, (int) (6f / 16f * HEIGHT), (int) (HEIGHT / 4f), (int) (HEIGHT / 4f), 0, 360);

        if (status == ConstraintsStatus.OK) {
            gc.setBackground(d.getSystemColor(SWT.COLOR_GREEN));
        } else {
            gc.setBackground(d.getSystemColor(SWT.COLOR_GRAY));
        }
        gc.fillArc(0, (int) (11f / 16f * HEIGHT), (int) (HEIGHT / 4f), (int) (HEIGHT / 4f), 0, 360);
    }
}
