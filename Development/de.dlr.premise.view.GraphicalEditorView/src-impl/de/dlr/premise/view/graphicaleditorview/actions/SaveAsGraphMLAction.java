/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.actions;

import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import de.dlr.premise.view.graphicaleditorview.Activator;
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.SelectionHolder;
import de.dlr.premise.view.graphicaleditorview.view.AutoSizeShapeNodeRealizerSerializer;
import de.dlr.premise.view.graphicaleditorview.view.GraphRegistry;
import y.io.GraphMLIOHandler;
import yext.svg.io.SVGNodeRealizerSerializer;

public class SaveAsGraphMLAction extends Action implements IWorkbenchAction {

    private static final String ID = "de.dlr.premise.view.GraphicalEditorView.saveAsGraphMLAction";
    private static SaveAsGraphMLAction instance = null;

    public static synchronized SaveAsGraphMLAction getInstance() {
        if (instance == null) {
            instance = new SaveAsGraphMLAction();
        }
        return instance;
    }

    private SaveAsGraphMLAction() {
        setId(ID);
        setText("Save Graph as GraphML");
        setToolTipText("Save Graph as GraphML");
        setImageDescriptor(Activator.getImageDescriptor("icons/save_edit.gif"));
    }

    @Override
    public void run() {
        try {
            GraphMLIOHandler ioh = new GraphMLIOHandler();
            ioh.addNodeRealizerSerializer(new AutoSizeShapeNodeRealizerSerializer());
            ioh.addNodeRealizerSerializer(new SVGNodeRealizerSerializer());
            ioh.write(GraphRegistry.getGraph(), SelectionHolder.getInstance().getExportOutputStream());
        } catch (IOException e) {
        } catch (NullPointerException e) {
            System.out.println("Nothing selected.");
        }
    }

    @Override
    public void dispose() {
    }

}
