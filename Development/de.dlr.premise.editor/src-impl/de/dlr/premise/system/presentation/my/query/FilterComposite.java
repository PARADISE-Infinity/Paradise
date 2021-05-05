/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.google.common.collect.ImmutableMultimap;

import de.dlr.premise.component.Satisfies;
import de.dlr.premise.element.AGuardCondition;
import de.dlr.premise.element.Connection;
import de.dlr.premise.element.Mode;
import de.dlr.premise.element.Relation;
import de.dlr.premise.element.Transition;
import de.dlr.premise.functions.RequiredParameter;
import de.dlr.premise.query.persistent.PersistableQuery;
import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.registry.MetaData;
import de.dlr.premise.system.Balancing;

public class FilterComposite extends Composite implements SelectionListener {

    private QueryUIController controller;

    private Button btnAdvancedMode;

    private Set<Button> buttons = new HashSet<>();

    private Button btnClose;

    private static final Map<String, Collection<Class<? extends EObject>>> filterCheckboxes = ImmutableMultimap.<String, Class<? extends EObject>>builder()
            .putAll("Parameter", AParameterDef.class, RequiredParameter.class)
            .putAll("Balancing", Balancing.class)
            .putAll("Relation", Relation.class)
            .putAll("Connection", Connection.class)
            .putAll("Satisfies", Satisfies.class)
            .putAll("Mode", Mode.class)
            .putAll("Transition", Transition.class)
            .putAll("MetaData", MetaData.class)
            .putAll("GuardCondition", AGuardCondition.class)
            .build().asMap();

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public FilterComposite(Composite parent, int style, QueryUIController controller) {
        super(parent, style);
        this.controller = controller;

        setLayout(new FormLayout());

        Group grpFilter = new Group(this, SWT.NONE);
        grpFilter.setLayout(new RowLayout(SWT.HORIZONTAL));
        FormData fd_grpFilter = new FormData();
        fd_grpFilter.height = 70;
        fd_grpFilter.left = new FormAttachment(0, 10);
        grpFilter.setLayoutData(fd_grpFilter);
        grpFilter.setText("Filter");

        btnClose = new Button(this, SWT.CENTER);
        fd_grpFilter.bottom = new FormAttachment(btnClose, -6);
        fd_grpFilter.right = new FormAttachment(btnClose, 0, SWT.RIGHT);
        FormData fd_btnClose = new FormData();
        fd_btnClose.right = new FormAttachment(100, -10);
        fd_btnClose.bottom = new FormAttachment(100, -10);
        btnClose.setLayoutData(fd_btnClose);
        btnClose.setText("Close");
        btnClose.addSelectionListener(this);

        btnAdvancedMode = new Button(this, SWT.CHECK);
        FormData fd_btnAdvancedMode = new FormData();
        fd_btnAdvancedMode.top = new FormAttachment(0, 10);
        fd_btnAdvancedMode.right = new FormAttachment(100, -10);
        btnAdvancedMode.setLayoutData(fd_btnAdvancedMode);
        btnAdvancedMode.setText("Advanced Mode");
        fd_grpFilter.top = new FormAttachment(btnAdvancedMode, 6);
        btnAdvancedMode.addSelectionListener(this);

        filterCheckboxes.keySet().stream().forEach(name -> createCheckButton(grpFilter, name));
    }

    private void createCheckButton(Group grp, String name) {
        Button btn = new Button(grp, SWT.CHECK);
        buttons.add(btn);
        btn.setSelection(true);
        btn.setText(name);
        btn.addSelectionListener(this);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == btnAdvancedMode) {
            controller.toggleAdvancedMode(null);
            btnAdvancedMode.setSelection(false);
        } else if (e.getSource() == btnClose) {
            controller.closeQueryDialog();
        } else {
            controller.onFilterBoxChanged(getUnselectedClasses(), getUnselectedNames());
        }
    }

    private List<Class<? extends EObject>> getUnselectedClasses() {
        return getUnselectedNames().stream().flatMap(text -> filterCheckboxes.get(text).stream()).collect(Collectors.toList());
    }

    private List<String> getUnselectedNames() {
        return buttons.stream().filter(btn -> !btn.getSelection()).map(Button::getText).collect(Collectors.toList());
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // Unused
    }

    public void notifyQueryChanged(PersistableQuery activeQuery) {
        buttons.stream().forEach(btn -> btn.setSelection(!activeQuery.getQuery().contains(btn.getText())));
    }

}
