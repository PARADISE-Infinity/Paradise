/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.parameterviewer.listener;

import java.util.Iterator;

import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

import de.dlr.premise.view.parameterviewer.ParameterViewerPage;

public class UndoAndDeleteKeyListener extends KeyAdapter implements KeyListener {

    /**
     * 
     */
    private final ParameterViewerPage parameterViewerPage;
    private TreeViewer viewer;

    public UndoAndDeleteKeyListener(ParameterViewerPage parameterViewerPage, TreeViewer viewer) {
        this.parameterViewerPage = parameterViewerPage;
        this.viewer = viewer;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.stateMask & SWT.CONTROL) == SWT.CONTROL && e.keyCode == 'z') {
            parameterViewerPage.getCurrentEditor().getEditingDomain().getCommandStack().undo();
            parameterViewerPage.getObservedTreeViewer().refresh();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.keyCode == SWT.DEL && !viewer.getSelection().isEmpty()) {// DELETE removes selection
            for (Iterator<?> iterator = ((TreeSelection) parameterViewerPage.getObservedTreeViewer().getSelection()).iterator(); iterator.hasNext();) {
                parameterViewerPage.getObservedParameter().remove(iterator.next());
            }
            viewer.refresh();
        }
    }

}