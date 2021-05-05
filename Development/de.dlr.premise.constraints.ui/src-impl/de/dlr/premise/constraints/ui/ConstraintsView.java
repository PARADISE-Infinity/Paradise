/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.constraints.ui;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

public class ConstraintsView extends ViewPart {

    private class UpdateCanvasJob extends UIJob {

        public UpdateCanvasJob() {
            super("Updating constaint violation display");
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            if (canvas != null && !canvas.isDisposed()) {
                canvas.redraw();
            }
            return Status.OK_STATUS;
        }
    }

    private final ConstraintsStatusTracker statusTracker;

    private Canvas canvas;
    private UpdateCanvasJob updateJob;

    public ConstraintsView() {
        statusTracker = new ConstraintsStatusTracker(this);
    }

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());

        canvas = new Canvas(parent, SWT.NONE);
        canvas.addPaintListener(new ConstraintsViewPainter(statusTracker));
    }

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);


        getSite().getPage().addPostSelectionListener(statusTracker);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(statusTracker);


        statusTracker.selectionChanged(getSite().getPage()
                .getActivePart(), getSite().getPage().getSelection());
    }

    public void updateView() {
        if (updateJob != null) {
            updateJob.cancel();
        }
        updateJob = new UpdateCanvasJob();
        updateJob.schedule();
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub
    }
}
