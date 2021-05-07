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

    // private ISelection bootstrapSelection;

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
            // bootstrapSelection = page.getSelection();
            return page.getActivePart();
        }
        return null;
    }

    @Override
    protected boolean isImportant(IWorkbenchPart part) {
        return "de.dlr.premise.editor".equals(part.getSite().getPluginId());
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (selection instanceof TreeSelection && getCurrentPage() instanceof ParameterViewerPage) {
            ((ParameterViewerPage) getCurrentPage()).setRootElementsIfVisible(selection);

        }
    }

}
