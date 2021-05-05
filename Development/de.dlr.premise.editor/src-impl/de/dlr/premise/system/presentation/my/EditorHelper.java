/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.ui.util.EditUIUtil;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.dlr.premise.registry.MetaData;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.registry.presentation.RegistryEditor;
import de.dlr.premise.representation.presentation.RepresentationEditor;
import de.dlr.premise.element.ARepository;
import de.dlr.premise.element.presentation.ElementEditor;
import de.dlr.premise.system.presentation.SystemEditor;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.functionpool.presentation.FunctionpoolEditor;
import de.dlr.premise.functions.presentation.UseCaseEditor;

/**
 * @author hschum
 *
 */
public class EditorHelper {

    /**
     * Initialize the default editor view options.
     * 
     * Options on how the editor should display certain model elements are saved in the model itself. They should be set when the model is
     * opened. This method set the options to their default values, if no other values were already set.
     */
	
    public static void initializeEditorViewOptions(ARepository repo) {
    	
    	// add default meta data to  a repository
        MetaData metaGroup = PremiseHelper.getMetaData(repo, SystemItemProviderAdapterFactoryMy.OPT_TYPE_VIEW);
        if (metaGroup == null) {
            metaGroup = RegistryFactory.eINSTANCE.createMetaData();
            repo.getMetaData().add(metaGroup);
            metaGroup.setName(SystemItemProviderAdapterFactoryMy.OPT_TYPE_VIEW);
        }
        
        // add data type names on
        if (PremiseHelper.getMetaData(repo, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES) == null) {
            MetaData meta;
            meta = de.dlr.premise.registry.RegistryFactory.eINSTANCE.createMetaData();
            metaGroup.getMetaData().add(meta);
            meta.setName(SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES);
            meta.setValue("on");
        }        
        
        // add default scientific number format
        if (PremiseHelper.getMetaData(repo, PremiseHelper.OPT_NUMBER_PATTERN) == null) {
            MetaData meta;
            meta = de.dlr.premise.registry.RegistryFactory.eINSTANCE.createMetaData();
            metaGroup.getMetaData().add(meta);
            meta.setName(PremiseHelper.OPT_NUMBER_PATTERN);
            meta.setValue(PremiseHelper.OPT_NUMBER_PATTERN_SCIENTIFIC_PATTERN);
        }
    }

    /**
     * Tries to enter into inline editing mode after creating a new element.
     * 
     * Simulates the user clicking the newly created element to enter into inline editing.
     * 
     * @param mostRecentCommand
     */
    public static void setFocus(final Viewer viewer, final IWorkbenchPartSite site, final Command mostRecentCommand) {
        if (viewer instanceof TreeViewer && (mostRecentCommand instanceof CreateChildCommand)) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Tree tree = ((TreeViewer) viewer).getTree();
                    TreeItem[] treeItems = tree.getSelection();
                    if (treeItems.length == 1) {
                        Event enableEditField = new Event();
                        enableEditField.button = 1;
                        enableEditField.x = treeItems[0].getBounds().x;
                        enableEditField.y = treeItems[0].getBounds().y;
                        tree.notifyListeners(SWT.Selection, enableEditField);
                        tree.notifyListeners(SWT.MouseDown, enableEditField);
                        tree.notifyListeners(SWT.MouseUp, enableEditField);
                    }
                }
            };
            site.getShell().getDisplay().asyncExec(runnable);
        }
    }

    /**
     * Gets the {@link Resource} of a given PARADISE editor, loaded via the editors {@link ResourceSet}
     * 
     * @param editor
     * @return
     */
    public static Resource getEMFResource(IEditorPart editor) {
        Resource res = null;

        ResourceSet resSet = getResourceSet(editor);
        if (resSet != null) {
            res = getEMFResource(resSet, editor);
        }

        return res;
    }

    /**
     * Gets the resource of a given editor, loaded via the supplied {@link ResourceSet}
     * 
     * @param resSet
     * @param editor
     * @return
     */
    public static Resource getEMFResource(ResourceSet resSet, IEditorPart editor) {
        return resSet.getResource(EditUIUtil.getURI(editor.getEditorInput()), true);
    }

    /**
     * Gets the {@link ResourceSet} of a given PARADISE editor
     * 
     * @param editor
     * @return
     */
    public static ResourceSet getResourceSet(IEditorPart editor) {
        ResourceSet resourceSet = null;
        if (editor instanceof SystemEditor) {
            resourceSet = ((SystemEditor) editor).getEditingDomain().getResourceSet();
        }
        if (editor instanceof RegistryEditor) {
            resourceSet = ((RegistryEditor) editor).getEditingDomain().getResourceSet();
        }
        if (editor instanceof FunctionpoolEditor) {
            resourceSet = ((FunctionpoolEditor) editor).getEditingDomain().getResourceSet();
        }
        if (editor instanceof UseCaseEditor) {
            resourceSet = ((UseCaseEditor) editor).getEditingDomain().getResourceSet();
        }
        if (editor instanceof ElementEditor) {
            resourceSet = ((ElementEditor) editor).getEditingDomain().getResourceSet();
        }
        if (editor instanceof RepresentationEditor) {
            resourceSet = ((RepresentationEditor) editor).getEditingDomain().getResourceSet();
        }
        return resourceSet;
    }

    public static IWorkbenchPage getPage() {
        IWorkbenchPage page = null;
        try {
            if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
                page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            }

            if (page == null) {
                IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
                for (int i = 0; i < windows.length && page == null; i++) {
                    if (windows[i] != null) {
                        page = windows[i].getActivePage();
                    }
                }
            }
        } catch (IllegalStateException e) {
            // Workbench is not available in tests
        }
        return page;
    }

}
