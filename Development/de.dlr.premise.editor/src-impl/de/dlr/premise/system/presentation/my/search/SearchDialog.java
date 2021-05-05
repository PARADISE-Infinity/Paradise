/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.dlr.premise.safety.presentation.PREMISE_SafetyEditorPlugin;
import de.dlr.premise.system.presentation.my.query.QueryableTree;
import de.dlr.premise.system.util.ConditionLock;
import de.dlr.premise.system.util.FunctionalSelectionListener;

public class SearchDialog extends Dialog {

    private static boolean forwards = true;
    private static List<String> history = new ArrayList<>();
    private QueryableTree tree;
    private Combo combo;
    private Label statusLabel;

    private ConditionLock searchResultsUpToDate = new ConditionLock(false);

    public SearchDialog(Display display, QueryableTree tree) {
        super(display.getActiveShell());
        this.tree = tree;
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite panel = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        panel.setLayout(layout);
        setGridData(panel, SWT.FILL, true, SWT.FILL, true);

        Composite inputPanel = createInputContents(panel);
        setGridData(inputPanel, SWT.FILL, true, SWT.TOP, false);

        Composite directionPanel = createDirectionGroup(panel);
        setGridData(directionPanel, SWT.FILL, true, SWT.TOP, true);
        ((GridData) directionPanel.getLayoutData()).horizontalSpan = 2;

        Composite buttonPanel = createButtonSection(panel);
        setGridData(buttonPanel, SWT.TRAIL, false, SWT.TOP, false);

        Composite statusAndClosePanel = createStatusAndCloseSection(panel);
        setGridData(statusAndClosePanel, SWT.FILL, true, SWT.BOTTOM, false);
        ((GridData) statusAndClosePanel.getLayoutData()).horizontalSpan = 2;

        if (!combo.getText().isEmpty()) {
            textChanged();
        }

        panel.addTraverseListener((e) -> {
            if (e.detail == SWT.TRAVERSE_RETURN) {
                find();
            }
        });

        return panel;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Search");
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    private Composite createInputContents(Composite parent) {
        Composite panel = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        panel.setLayout(layout);

        Label findLabel = new Label(panel, SWT.LEAD);
        findLabel.setText("Find:");
        setGridData(findLabel, SWT.LEFT, false, SWT.CENTER, false);

        combo = new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
        setGridData(combo, SWT.FILL, true, SWT.CENTER, false);
        ((GridData) combo.getLayoutData()).widthHint = 130;
        updateCombo();
        combo.select(0);
        combo.addModifyListener(e -> textChanged());

        return panel;
    }

    private Composite createDirectionGroup(Composite parent) {
        Composite panel = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        panel.setLayout(layout);

        Group group = new Group(panel, SWT.SHADOW_ETCHED_IN);
        group.setText("Direction");
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Button forwardButton = new Button(group, SWT.RADIO | SWT.LEAD);
        forwardButton.setText("Forward");
        setGridData(forwardButton, SWT.LEAD, false, SWT.CENTER, false);
        FunctionalSelectionListener directionListener = e -> setFindForwards(forwardButton.getSelection());
        forwardButton.addSelectionListener(directionListener);

        Button backwardButton = new Button(group, SWT.RADIO | SWT.LEAD);
        backwardButton.setText("Backward");
        setGridData(backwardButton, SWT.LEAD, false, SWT.CENTER, false);

        forwardButton.setSelection(forwards);
        backwardButton.setSelection(!forwards);

        return panel;
    }

    private Composite createButtonSection(Composite parent) {
        Composite panel = new Composite(parent, SWT.NULL);
        panel.setLayout(new GridLayout());

        Button findButton = new Button(panel, SWT.PUSH);
        findButton.setText("Find");
        setGridData(findButton, SWT.LEAD, false, SWT.CENTER, false);
        FunctionalSelectionListener findListener = e -> find();
        findButton.addSelectionListener(findListener);

        return panel;
    }

    private Composite createStatusAndCloseSection(Composite parent) {
        Composite panel = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        panel.setLayout(layout);

        statusLabel = new Label(panel, SWT.LEAD);
        statusLabel.setText("");
        setGridData(statusLabel, SWT.FILL, true, SWT.CENTER, false);

        Button closeButton = new Button(panel, SWT.PUSH);
        closeButton.setText("Close");
        setGridData(closeButton, SWT.TRAIL, false, SWT.CENTER, false);
        FunctionalSelectionListener closeListener = e -> close();
        closeButton.addSelectionListener(closeListener);

        return panel;
    }

    private void setGridData(Control component, int horizontalAlignment, boolean grabExcessHorizontalSpace, int verticalAlignment,
            boolean grabExcessVerticalSpace) {
        GridData gd = new GridData();
        component.setLayoutData(gd);
        gd.horizontalAlignment = horizontalAlignment;
        gd.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
        gd.verticalAlignment = verticalAlignment;
        gd.grabExcessVerticalSpace = grabExcessVerticalSpace;
        if (component instanceof Button && (component.getStyle() & SWT.PUSH) != 0) {
            gd.widthHint = 80;
        }
    }

    private void textChanged() {
        searchResultsUpToDate.setCondition(false);
        tree.updateSearchResults(combo.getText(), this::searchResultsUpdated);
        statusLabel.setText("");
        findFirst();
    }

    public void searchResultsUpdated(int numResults) {
        searchResultsUpToDate.setCondition(true);
        String statusText = numResults == 0 ? "String not found" : numResults + " matches";
        statusText = combo.getText().isEmpty() ? "" : statusText;
        statusLabel.setText(statusText);
    }

    private void find() {
        new Thread(() -> {
            searchResultsUpToDate.await();
            Display.getDefault().asyncExec(() -> {
                if (forwards) {
                    tree.selectNext();
                } else {
                    tree.selectPrevious();
                }
            });
        }).start();
    }

    private void findFirst() {
        new Thread(() -> {
            searchResultsUpToDate.await();
            Display.getDefault().asyncExec(() -> {
                tree.selectFirst();
                updateHistory();
            });
        }).start();
    }

    /**
     * Updates the combo with the history.
     */
    private void updateHistory() {
        String findString = combo.getText();
        int index = history.indexOf(findString);
        if (index != 0) {
            if (index != -1) {
                history.remove(index);
            }
            history.add(0, findString);
            Point selection = combo.getSelection();
            updateCombo();
            combo.setText(findString);
            combo.setSelection(selection);
        }
    }

    /**
     * Updates the given combo with the given content.
     */
    private void updateCombo() {
        combo.removeAll();
        history.forEach(combo::add);
    }

    private void setFindForwards(boolean findForwards) {
        forwards = findForwards;
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
        return DIALOG_PERSISTLOCATION | DIALOG_PERSISTSIZE;
    }

}
