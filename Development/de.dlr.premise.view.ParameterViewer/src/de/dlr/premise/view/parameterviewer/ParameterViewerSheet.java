/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.parameterviewer;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

import de.dlr.premise.system.presentation.my.SystemEditorMy;

public class ParameterViewerSheet extends PageBookView implements ISelectionListener {

    final static String EDITOR_NAME = "de.dlr.premise.editor";

    @Override
    protected IPage createDefaultPage(PageBook book) {
        MessagePage messagePage = new MessagePage();
        initPage(messagePage);
        messagePage.setMessage("Please select a .premise file!");
        messagePage.createControl(book);
        return messagePage;
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
    };

    @Override
    protected PageRec doCreatePage(IWorkbenchPart part) {
        if (part instanceof SystemEditorMy) {
            ParameterViewerPage page = new ParameterViewerPage(part);
            initPage(page);
            page.createControl(getPageBook());
            part.getSite().getPage().addPostSelectionListener(this);
            return new PageRec(part, page);
        } else
            return null;
    }

    @Override
    protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
        pageRecord.page.dispose();
        pageRecord.dispose();
    }

    @Override
    protected IWorkbenchPart getBootstrapPart() {
        IWorkbenchPage page = getSite().getPage();
        if (page != null) {
            return page.getActivePart();
        }
        return null;
    }

    @Override
    protected boolean isImportant(IWorkbenchPart part) {
        String pluginID = part.getSite().getPluginId();
        return EDITOR_NAME.equals(pluginID);
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        
        // get page
        ParameterViewerPage page = null;
        if (getCurrentPage() instanceof ParameterViewerPage) {
            page = (ParameterViewerPage) getCurrentPage();            
        }
        
        if (selection instanceof TreeSelection && page != null) {           
            page.setRootElementsIfVisible(selection);
        }
    }
}
