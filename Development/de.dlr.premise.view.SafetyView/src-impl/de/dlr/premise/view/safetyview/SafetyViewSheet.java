/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.safetyview;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

import de.dlr.premise.element.AElement;
import de.dlr.premise.element.ARepository;
import de.dlr.premise.element.StateMachine;


public class SafetyViewSheet extends PageBookView implements ISelectionListener {

    private static final String DE_DLR_FT = "de.dlr.ft";
    private EObject selectedElement;
    private SafetyViewPage page;

    public SafetyViewSheet(){
    }

    @Override
    protected IPage createDefaultPage(PageBook book) {
            page = new SafetyViewPage();
            initPage(page);
            page.createControl(getPageBook());
            getSite().getPage().addPostSelectionListener(this);
            return page;
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
    }

    @Override
    protected PageRec doCreatePage(IWorkbenchPart part) {
        return null;
    }

    @Override
    protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
        getSite().getPage().removePostSelectionListener(this);
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
        String pluginId = part.getSite().getPluginId();
        return pluginId != null && pluginId.startsWith(DE_DLR_FT);
    }


    public EObject getSelectedElement() {
        return selectedElement;
    }


    public void setSelectedElement(EObject selectedElement) {
        this.selectedElement = selectedElement;
        if (page != null){
            page.setSelectedElements(selectedElement);
            page.setObservedObjects(selectedElement);
            page.refreshObservedList();
        }
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (selection instanceof TreeSelection && getCurrentPage() instanceof SafetyViewPage) {
            // only set the first object of the selection as input to provide proper functionality of "new failure"
            EObject firstSelectedElement = null;
            if (((IStructuredSelection) selection).getFirstElement() instanceof EObject) {
                firstSelectedElement = (EObject) ((IStructuredSelection) selection).getFirstElement();
            }
            // only set elements of correct type as input
            if (firstSelectedElement instanceof StateMachine || firstSelectedElement instanceof AElement || firstSelectedElement instanceof ARepository)
            {
                setSelectedElement(firstSelectedElement);
            }

        }
    }
}
