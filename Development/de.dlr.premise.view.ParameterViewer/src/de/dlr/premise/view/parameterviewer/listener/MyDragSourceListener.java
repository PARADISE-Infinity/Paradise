/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.parameterviewer.listener;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

public class MyDragSourceListener implements DragSourceListener {

    TreeViewer viewer;

    public MyDragSourceListener(TreeViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void dragStart(DragSourceEvent event) {
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
        event.data = viewer.getSelection();
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
    }

}