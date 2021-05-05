/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my.query;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import de.dlr.premise.query.QueryMode;
import de.dlr.premise.query.persistent.PersistableQuery;
import de.dlr.premise.query.ui.OCLEditor;
import de.dlr.premise.system.SystemPackage;

public class QueryComposite extends Composite implements SelectionListener {

    private QueryUIController controller;
    private Button btnAdvancedMode;
    private OCLEditor editor;
    private Button btnApply;
    private Combo combo;
    private Map<String, PersistableQuery> availableQueries;
    private Button btnFilter;
    private Button btnHighlight;
    private Button btnList;
    private Button btnSaveAs;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public QueryComposite(Composite parent, int style, QueryUIController controller) {
        super(parent, style);
        this.controller = controller;

        setLayout(new FormLayout());

        btnAdvancedMode = new Button(this, SWT.CHECK);
        FormData fd_btnAdvancedMode = new FormData();
        fd_btnAdvancedMode.top = new FormAttachment(0, 10);
        fd_btnAdvancedMode.right = new FormAttachment(100, -10);
        btnAdvancedMode.setLayoutData(fd_btnAdvancedMode);
        btnAdvancedMode.setSelection(true);
        btnAdvancedMode.setText("Advanced Mode");
        btnAdvancedMode.addSelectionListener(this);

        Label lblCurrentQuery = new Label(this, SWT.NONE);
        FormData fd_lblCurrentQuery = new FormData();
        fd_lblCurrentQuery.left = new FormAttachment(0, 10);
        lblCurrentQuery.setLayoutData(fd_lblCurrentQuery);
        lblCurrentQuery.setText("Current Query:");

        combo = new Combo(this, SWT.READ_ONLY | SWT.BORDER);
        fd_lblCurrentQuery.top = new FormAttachment(combo, 3, SWT.TOP);
        FormData fd_combo = new FormData();
        fd_combo.top = new FormAttachment(0, 10);
        fd_combo.left = new FormAttachment(lblCurrentQuery, 6, SWT.RIGHT);
        fd_combo.right = new FormAttachment(btnAdvancedMode, -6, SWT.LEFT);
        combo.setLayoutData(fd_combo);
        combo.addSelectionListener(this);

        Group grpQuery = new Group(this, SWT.NONE);
        FormData fd_grpQuery = new FormData();
        fd_grpQuery.top = new FormAttachment(combo, 6, SWT.BOTTOM);
        fd_grpQuery.left = new FormAttachment(0, 10);
        fd_grpQuery.right = new FormAttachment(100, -10);
        grpQuery.setLayoutData(fd_grpQuery);
        grpQuery.setText("Query");
        grpQuery.setLayout(new GridLayout(2, false));

        Composite queryEditor = new Composite(grpQuery, SWT.NONE);
        GridData gd_queryEditor = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_queryEditor.heightHint = 400;
        queryEditor.setLayoutData(gd_queryEditor);
        queryEditor.setLayout(new GridLayout());
        editor = OCLEditor.createEditor(queryEditor, SystemPackage.Literals.SYSTEM_COMPONENT);

        Group grpMode = new Group(grpQuery, SWT.NONE);
        grpMode.setText("Mode");
        grpMode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        grpMode.setLayout(new RowLayout(SWT.VERTICAL));

        btnFilter = new Button(grpMode, SWT.RADIO);
        btnFilter.setSelection(true);
        btnFilter.setText("Filter");
        btnFilter.addSelectionListener(this);

        btnHighlight = new Button(grpMode, SWT.RADIO);
        btnHighlight.setText("Highlight");
        btnHighlight.addSelectionListener(this);

        btnList = new Button(grpMode, SWT.RADIO);
        btnList.setText("List");
        btnList.addSelectionListener(this);

        btnApply = new Button(this, SWT.NONE);
        fd_grpQuery.bottom = new FormAttachment(btnApply, -6);
        FormData fd_btnApply = new FormData();
        fd_btnApply.bottom = new FormAttachment(100, -10);
        fd_btnApply.right = new FormAttachment(100, -10);
        btnApply.setLayoutData(fd_btnApply);
        btnApply.setText("Apply");
        btnApply.addSelectionListener(this);

        btnSaveAs = new Button(this, SWT.NONE);
        FormData fd_btnSaveAs = new FormData();
        fd_btnSaveAs.bottom = new FormAttachment(100, -10);
        fd_btnSaveAs.left = new FormAttachment(0, 10);
        btnSaveAs.setLayoutData(fd_btnSaveAs);
        btnSaveAs.setText("Save As...");
        btnSaveAs.addSelectionListener(this);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnAdvancedMode) {
            handleAdvancedMode();
        } else if (e.getSource() == btnApply) {
            handleApply();
        } else if (e.getSource() == combo) {
            handleCombo();
        } else if (e.getSource() == btnSaveAs) {
            handleSave();
        } else if (e.getSource() instanceof Button) {
            handleMode(e);
        }
    }

    private void handleAdvancedMode() {
        controller.toggleAdvancedMode(createQuery());
        btnAdvancedMode.setSelection(true);
    }

    private void handleApply() {
        PersistableQuery newQuery = createQuery();
        controller.onSelectedQueryChanged(newQuery);
        controller.applyQuery(newQuery);
        controller.closeQueryDialog();
    }

    private void handleCombo() {
        controller.onSelectedQueryChanged(availableQueries.get(combo.getText()));
    }

    private void handleSave() {
        PersistableQuery newQuery = createQuery();
        controller.onSelectedQueryChanged(newQuery);
        controller.openSaveAsDialog(newQuery);
    }

    private void handleMode(SelectionEvent e) {
        Button selected = (Button) e.getSource();
        if (selected.getSelection()) {
            controller.onSelectedQueryChanged(createQuery());
        }
    }

    private PersistableQuery createQuery() {
        return new PersistableQuery(combo.getText(), editor.getText(), getCurrentMode());
    }

    private QueryMode getCurrentMode() {
        if (btnFilter.getSelection())
            return QueryMode.FILTER;
        if (btnHighlight.getSelection())
            return QueryMode.HIGHLIGHT;
        return QueryMode.LIST;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // Unused
    }

    public void setAvailableQueries(Map<String, PersistableQuery> queries) {
        this.availableQueries = queries;

        combo.setItems(queries.keySet().toArray(new String[0]));
    }

    public void notifyQueryChanged(PersistableQuery activeQuery) {
        editor.setText(activeQuery.getQuery());
        String queryName = availableQueries.entrySet().stream().filter(entry -> activeQuery.equals(entry.getValue())).map(Map.Entry::getKey)
                                           .findFirst().orElse(activeQuery.getName());
        combo.setText(queryName);
        btnFilter.setSelection(activeQuery.getMode() == QueryMode.FILTER);
        btnHighlight.setSelection(activeQuery.getMode() == QueryMode.HIGHLIGHT);
        btnList.setSelection(activeQuery.getMode() == QueryMode.LIST);
    }

}
