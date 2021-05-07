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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;

import de.dlr.premise.system.ABalancing;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.view.parameterviewer.ParameterContentProvider;
import de.dlr.premise.view.parameterviewer.ParameterViewerPage;

public class MyDropTargetListener extends ViewerDropAdapter {

    /**
     * 
     */
    private final ParameterViewerPage parameterViewerPage;

    public MyDropTargetListener(ParameterViewerPage parameterViewerPage) {
        super(parameterViewerPage.getObservedTreeViewer());
        this.parameterViewerPage = parameterViewerPage;
    }

    @Override
    public void drop(DropTargetEvent event) {
        super.drop(event);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean performDrop(Object obj) {
        TreeSelection data = (TreeSelection) obj;
        for (Iterator<EObject> iterator = data.iterator(); iterator.hasNext();) {
            Object element = iterator.next();
            if (element instanceof EObject && ParameterContentProvider.instanceofRecognizedElement(element)) {

                // Position
                int index = parameterViewerPage.getObservedParameter().indexOf(getCurrentTarget());
                switch (getCurrentLocation()) {
                case ViewerDropAdapter.LOCATION_AFTER:
                    index++;
                    break;
                case ViewerDropAdapter.LOCATION_BEFORE:
                    break;
                case ViewerDropAdapter.LOCATION_NONE:
                    index = parameterViewerPage.getObservedParameter().size();
                    break;
                case ViewerDropAdapter.LOCATION_ON:
                    index++;
                    break;
                }

                List<EObject> list = new LinkedList<>();
                // Balancing
                if (element instanceof ABalancing<?>) {
                    list.add(((ABalancing<?>) element).getTarget());
                    list.addAll(((ABalancing<?>) element).getSources());
                } else if (element instanceof SystemComponent) {
                    for (EObject eObj : ((SystemComponent) element).eContents()) {
                        if (eObj instanceof Parameter) {
                            list.add(eObj);
                        }
                    }
                } else {
                    list.add((EObject) element);
                }
                for (EObject eObj : list) {
                    if (parameterViewerPage.getObservedParameter().contains(eObj)) {
                        if (index == parameterViewerPage.getObservedParameter().size())
                            index--;
                        parameterViewerPage.getObservedParameter().remove(eObj);
                        parameterViewerPage.getObservedParameter().add(index, (EObject) eObj);
                    } else {
                        parameterViewerPage.getObservedParameter().add(index, (EObject) eObj);
                    }
                    // For right order
                    index++;
                }
            }
        }
        parameterViewerPage.getObservedTreeViewer().setInput(parameterViewerPage.getObservedParameter().toArray());
        return true;
    }

    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        return true;
    }

}