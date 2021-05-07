/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.application.rcp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	@Override
    public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		//layout.setFixed(true);
		layout.addStandaloneView("premise.rcp.navigator",  true /* show title */, IPageLayout.LEFT, 	0.2f, editorArea);
		
		IFolderLayout myFolder = layout.createFolder("premise.rcp.bottomTabs", IPageLayout.BOTTOM, 0.7f, editorArea);
		myFolder.addView(IPageLayout.ID_PROP_SHEET);
		myFolder.addView("de.dlr.application.rcp.ProblemsView");
		myFolder.addView("org.eclipse.ui.console.ConsoleView");
	    
        layout.addShowViewShortcut("premise.rcp.navigator");
        layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
	    layout.addShowViewShortcut("de.dlr.application.rcp.ProblemsView");
        layout.addShowViewShortcut("org.eclipse.ui.console.ConsoleView");
	   }
}
