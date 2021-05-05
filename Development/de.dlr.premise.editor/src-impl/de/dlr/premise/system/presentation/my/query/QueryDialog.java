/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my.query;

import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dlr.premise.query.persistent.PersistableQuery;
import de.dlr.premise.safety.presentation.PREMISE_SafetyEditorPlugin;

public class QueryDialog extends Dialog {

    private StackLayout layout;
    private FilterComposite filterComposite;
    private QueryComposite queryComposite;
    private Composite content;
    private QueryUIController controller;

    /**
     * Create the shell.
     * 
     * @param display
     */
    public QueryDialog(Display display, QueryUIController controller) {
        super(display.getActiveShell());
        this.controller = controller;
        setBlockOnOpen(false);
    }

    @Override
    protected Control createContents(Composite parent) {
        content = new Composite(parent, SWT.NONE);
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        layout = new StackLayout();
        content.setLayout(layout);

        filterComposite = new FilterComposite(content, SWT.NONE, controller);
        queryComposite = new QueryComposite(content, SWT.NONE, controller);

        return content;
    }

    public void setActiveDialog(boolean advanced) {
        layout.topControl = advanced ? queryComposite : filterComposite;
        content.layout();
        getShell().setSize(layout.topControl.computeSize(700, SWT.DEFAULT));
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Filter");
    }

    public void setAvailableQueries(Map<String, PersistableQuery> queries) {
        queryComposite.setAvailableQueries(queries);
    }

    public void notifyQueryChanged(PersistableQuery activeQuery) {
        queryComposite.notifyQueryChanged(activeQuery);
        filterComposite.notifyQueryChanged(activeQuery);
    }

    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        String sectionName = getClass().getName() + "_dialogBounds";
        IDialogSettings settings = PREMISE_SafetyEditorPlugin.getPlugin().getDialogSettings();
        IDialogSettings section = settings.getSection(sectionName);
        if (section == null)
            section = settings.addNewSection(sectionName);
        return section;
    }

    @Override
    protected int getDialogBoundsStrategy() {
        return DIALOG_PERSISTLOCATION;
    }
}
