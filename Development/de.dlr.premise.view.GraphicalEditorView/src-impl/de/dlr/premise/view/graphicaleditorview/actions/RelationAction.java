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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import de.dlr.premise.view.graphicaleditorview.Activator;
import de.dlr.premise.view.graphicaleditorview.GraphicalEditorView;
import de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry;
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.relation.RelationToModelMapper;

public class RelationAction extends Action implements IWorkbenchAction {

    private static final String ID = "de.dlr.premise.view.GraphicalEditorView.relationAction";
    private static RelationAction instance = null;

    public static synchronized RelationAction getInstance() {
        if (instance == null) {
            instance = new RelationAction();
        }
        return instance;
    }

    private RelationAction() {
        setId(ID);
        setText("Relations");
        setToolTipText("Draw Relations");
        setChecked(false);
        setImageDescriptor(Activator.getImageDescriptor("icons/Relation.gif"));
    }

    @Override
    public void run() {
        GraphicalEditorView.setActiveButton(this);
        MapperRegistry.setMapper(RelationToModelMapper.class);
    }

    @Override
    public void dispose() {
    }

}
