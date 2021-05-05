/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my;

import org.eclipse.emf.edit.ui.action.CopyAction;
import org.eclipse.emf.edit.ui.action.CutAction;
import org.eclipse.emf.edit.ui.celleditor.AdapterFactoryTreeEditor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import de.dlr.premise.system.presentation.SystemActionBarContributor;

public class SystemActionBarContributorMy extends SystemActionBarContributor {

    @Override
    public void contributeToMenu(IMenuManager menuManager) {
        // Hide the menu because it was broken in older versions and we want it to stay broken
        
        // FIXME should be removed ?
    }
    
    @Override
    protected CopyAction createCopyAction() {
        return new CopyAction() {
            @Override
            public void runWithEvent(Event event) {
                Text text = getTextFromActiveEditorTreeEditor();
                if (text != null) {
                    text.copy();
                    return;
                }
                
                super.runWithEvent(event);
            }
        };
    }
    
    @Override
    protected CutAction createCutAction() {
        return new CutAction() {
            @Override
            public void runWithEvent(Event event) {
                Text text = getTextFromActiveEditorTreeEditor();
                if (text != null) {
                    text.cut();
                    return;
                }
                
                super.runWithEvent(event);
            }
        };
    }

    private Text getTextFromActiveEditorTreeEditor() {
        if (activeEditor instanceof SystemEditorMy) {
            AdapterFactoryTreeEditor treeEditor = ((SystemEditorMy) activeEditor).getAdapterFactoryTreeEditor();
            Control editor = treeEditor.getEditor();
            if (editor instanceof Text) {
                return (Text) editor;
            }
        }
        return null;
    }
}
