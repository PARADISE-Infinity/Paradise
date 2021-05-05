/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.application.rcp;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    @Override
    protected void makeActions(IWorkbenchWindow window) {
    	IWorkbenchAction saveAction = ActionFactory.SAVE.create(window);
    	IWorkbenchAction delAction = ActionFactory.DELETE.create(window);
    	IWorkbenchAction copyAction = ActionFactory.COPY.create(window);
    	IWorkbenchAction pasteAction = ActionFactory.PASTE.create(window);
    	IWorkbenchAction undoAction = ActionFactory.UNDO.create(window);
    	IWorkbenchAction redoAction = ActionFactory.REDO.create(window);
    	IWorkbenchAction cutAction = ActionFactory.CUT.create(window);
    	register(saveAction);
    	register(delAction);
    	register(pasteAction);
    	register(copyAction);
    	register(undoAction);
    	register(redoAction);
    	register(cutAction);
    }

    @Override
    protected void fillMenuBar(IMenuManager menuBar) {
    }
    
}
