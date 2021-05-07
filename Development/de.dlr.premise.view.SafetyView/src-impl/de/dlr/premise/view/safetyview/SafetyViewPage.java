/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.safetyview;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.Page;

import de.dlr.premise.element.AElement;
import de.dlr.premise.element.Mode;
import de.dlr.premise.element.StateMachine;
import de.dlr.premise.element.Transition;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.registry.Value;
import de.dlr.premise.safety.impl.my.FailureHelper;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.system.TransitionParameter;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.SafetyModeContainer;

import de.dlr.premise.view.safetyview.generic.AddNewHazard;
import de.dlr.premise.view.safetyview.generic.AddPreliminaryHazards;
import de.dlr.premise.view.safetyview.generic.GenericFaultGeneration;
import de.dlr.premise.view.safetyview.helper.SafetyChangedContentAdapter;
import de.dlr.premise.view.safetyview.helper.SafetyViewHelper;

class ColumnValue implements Serializable {

    private static final long serialVersionUID = 1L;

    public String capture = "";
    public int width = 75;
    public final ID id;

    ColumnValue(String captuer, int width, ID id) {
        this.capture = captuer;
        this.width = width;
        this.id = id;
    }

    public enum ID {
        NAME, STATEMACHINE, TARGET_MODE, FAILURE_RATE, TRANSITION, COMPONENT
    }
}

public class SafetyViewPage extends Page {

    // selection lock
    private boolean lockObservedElement = false;
    // color setting
    private boolean highlightBasicEvents = false;

    // observed StateMachines
    private LinkedList<EObject> observedElements = new LinkedList<EObject>();
    private LinkedList<EObject> selectedElements = new LinkedList<EObject>();
    private EObject observedObject;
    // Contains the view
    private Composite composite;

    TableViewer observedTableViewer;
    private int[] columnOrder = null;
    private SafetyChangedContentAdapter changeAdapter;

    // Stores columns (order represents order in the Table)
    LinkedList<ColumnValue> columns = new LinkedList<ColumnValue>();

    /**
     * Constructor
     */
    public SafetyViewPage() {
        setDefaultColumns();
    }

    // page layout and control

    @Override
    public void createControl(Composite parent) {

        composite = new Composite(parent, SWT.NONE);

        FillLayout layout = new FillLayout(SWT.VERTICAL);
        layout.spacing = 5;
        composite.setLayout(layout);
        TabelLabelProvider labelProvider = new TabelLabelProvider(this);
        composite.setVisible(true);


        // Transfer is needed for Drag&Drop
        Transfer[] transfer = new Transfer[] { LocalTransfer.getInstance()};

        // --------ObservationTable--------
        SashForm observedSash = new SashForm(composite, SWT.HORIZONTAL);
        observedSash.setVisible(true);
        observedSash.setLayout(layout);
        final Table observedTable = new Table(observedSash, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        observedTable.setHeaderVisible(true);
        observedTable.setLinesVisible(true);
        observedTableViewer = new TableViewer(observedTable);
        observedTableViewer.getTable().setRedraw(false);
        observedTable.addKeyListener(new MyKeyListener(observedTableViewer));

        observedTable.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
            }

            @Override
            public void focusGained(FocusEvent e) {
                refreshObservedList();
            }
        });
        createColumns(observedTableViewer);

        // CellEditors for EditValue
        CellEditor[] editors = new CellEditor[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            editors[i] = new TextCellEditor(observedTable);
        }
        observedTableViewer.setCellEditors(editors);
        observedTableViewer.setCellModifier(new MyCellModifier(observedTableViewer));
        observedTableViewer.setColumnProperties(getColumnProperty());
        observedTableViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, transfer, new MyDragSourceListener(observedTableViewer));
        observedTableViewer.setContentProvider(new SafetyContentProvider(false));
        observedTableViewer.setLabelProvider(labelProvider);
        observedTable.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                dispose();
            }
        });

        // TABLE FOCUS CELL MANAGER
        TableViewerFocusCellManager focusCellManager =
                new TableViewerFocusCellManager(observedTableViewer, new FocusCellOwnerDrawHighlighter(observedTableViewer));

        ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(observedTableViewer) {

            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                // Enable editor only with mouse double click
                if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
                        || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.SPACE)) {
                    EventObject source = event.sourceEvent;
                    if (source instanceof MouseEvent && ((MouseEvent) source).button == 3)
                        return false;

                    return true;
                }

                return false;
            }
        };

        TableViewerEditor.create(observedTableViewer, focusCellManager, activationSupport,
                                 ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
                                         | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);


        // ----------Menu---------------
        IToolBarManager tbm = getSite().getActionBars().getToolBarManager();

        // Function Generation
        Action addSSF = new AddSafetySignificantFunction();
        tbm.add(addSSF);

        getSite().getActionBars().getToolBarManager().add(new Separator());

        // Hazard Generation in an ordered way
        // Add default hazard, add

        Action newStateMachine = new AddNewHazardAction();
        tbm.add(newStateMachine);

        Action defaultFaults = new AddPreliminaryHazardsAction();
        tbm.add(defaultFaults);

        Action genericFaultTree = new GenericFaultGenerationAction();
        tbm.add(genericFaultTree);


        getSite().getActionBars().getToolBarManager().add(new Separator());

        // Editor Settings
        Action toggleColor = new ToggleColorHighlights();
        tbm.add(toggleColor);

        Action toggleLock = new LockObservedObject();
        tbm.add(toggleLock);
        toggleLock.setChecked(true);


        if (!observedElements.isEmpty()) {
            observedTableViewer.setInput(observedElements);
        }

        observedTableViewer.getTable().setRedraw(true);

    }

    private String[] getColumnProperty() {
        String[] property = new String[columns.size()];
        for (ColumnValue col : columns) {
            property[columnOrder[columns.indexOf(col)]] = col.capture;
        }
        return property;
    }

    private void setDefaultColumns() {
        columns.clear();
        columnOrder = null;
        // Add new Columns at the end and change position after id is set
        columns.add(new ColumnValue("Container", 220, ColumnValue.ID.COMPONENT));
        columns.add(new ColumnValue("Name", 180, ColumnValue.ID.NAME));
        columns.add(new ColumnValue("Hazard", 180, ColumnValue.ID.STATEMACHINE));
        columns.add(new ColumnValue("Failure Mode", 180, ColumnValue.ID.TARGET_MODE));
//        columns.add(new ColumnValue("Event", 180, ColumnValue.ID.TRANSITION));
        columns.add(new ColumnValue("Assessment", 180, ColumnValue.ID.FAILURE_RATE));
    }

    @Override
    public void dispose() {
        if (observedObject != null && observedObject.eAdapters().contains(changeAdapter)) {
            changeAdapter.setDisposing();
            observedObject.eAdapters().remove(changeAdapter);
        }

        super.dispose();
    }

    private void createColumns(TableViewer observedTableViewer2) {
        TableViewerColumn temp;
        for (ColumnValue colVal : columns) {
            temp = new TableViewerColumn(observedTableViewer2, SWT.LEFT);
            temp.getColumn().setMoveable(true);
            temp.getColumn().setText(colVal.capture);
            temp.getColumn().setWidth(colVal.width);
        }
        observedTableViewer2.getTable().setToolTipText("");
        columnOrder = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            columnOrder[i] = i;
        }
    }


    // logic ----------------------------------------------------

    public void setSelectedElements(EObject selectedObject) {
        selectedElements.clear();
        selectedElements.add(selectedObject);
    }

    public void setObservedObjects(EObject observedObject){

        // check if the object should be updated, if not abort
        if (lockObservedElement) {
            return;
        }

        observedElements = new LinkedList<EObject>();
        if (observedObject instanceof AElement) {
            for (EObject child : observedObject.eContents()) {
                for (EObject childTrans : child.eContents()) {
                    if (childTrans instanceof Transition && FailureHelper.validFailureStateMachine((StateMachine)childTrans.eContainer())) {
                        addTransitionsToObvserved((Transition)childTrans);
                    }
                }
            }
        } else {
            for (EObject child : PremiseHelper.getAll(observedObject, Transition.class)) {
                if (child instanceof Transition && FailureHelper.validFailureStateMachine((StateMachine)child.eContainer())) {
                    addTransitionsToObvserved((Transition)child);
                }
            }
        }

        this.observedObject = observedObject;
        changeAdapter = new SafetyChangedContentAdapter(this);
        observedObject.eAdapters().add(changeAdapter);


    }

    public void refreshObservedList() {
        // this refreshes the view by resetting the observed parameter
        // it is using the initial selected object, not a new selection

        observedElements = new LinkedList<EObject>();

        if (observedObject != null) {
            if (observedObject instanceof AElement) {
                for (EObject child : observedObject.eContents()) {
                    for (EObject childTrans : child.eContents()) {
                        if (childTrans instanceof Transition && FailureHelper.validFailureStateMachine((StateMachine)childTrans.eContainer())) {
                            addTransitionsToObvserved((Transition)childTrans);
                        }
                    }
                }
            } else {
                // get all in the repository
                for (EObject child : PremiseHelper.getAll(observedObject, Transition.class)) {
                    if (child instanceof Transition && FailureHelper.validFailureStateMachine((StateMachine)child.eContainer())) {
                        addTransitionsToObvserved((Transition)child);
                    }
                }
            }
        }

        observedTableViewer.setInput(observedElements);
        observedTableViewer.refresh();
    }



    @Override
    public Control getControl() {
        return composite;
    }

    @Override
    public void setFocus() {
        composite.setFocus();
    }



    private void addTransitionsToObvserved(Transition trans) {
            observedElements.add(trans);
            return;
    }

    public boolean getHighlightBasicEvents(){
        return highlightBasicEvents;
    }




    // --------------------------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------Inner_Classes--------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------------------------------------

    private class ToggleColorHighlights extends Action implements IWorkbenchAction {


        public ToggleColorHighlights() {
            super("Highlights", SWT.TOGGLE);
            try {
                ImageDescriptor im = ImageDescriptor.createFromURL(new URL("platform:/plugin/de.dlr.premise.view.SafetyView/icons/highlight.gif"));
                this.setImageDescriptor(im);
            } catch (MalformedURLException e1) {}
            this.setToolTipText("Highlight Basic-Events");
        }

        @Override
        public void run() {
            highlightBasicEvents = !highlightBasicEvents;
            observedTableViewer.refresh();
        }

        @Override
        public void dispose() {

        }

    }

    private class AddPreliminaryHazardsAction extends Action implements IWorkbenchAction {

        public AddPreliminaryHazardsAction() {
            super();
            try {
                ImageDescriptor im = ImageDescriptor.createFromURL(new URL("platform:/plugin/de.dlr.premise.view.SafetyView/icons/DefaultHazard.png"));
                this.setImageDescriptor(im);
            } catch (MalformedURLException e1) {
                this.setText("Adds preliminary hazards");
            }
            this.setToolTipText("Add preliminary hazards");
        }

        public void run(){
            if (observedObject instanceof AElement) {
                AddPreliminaryHazards generator = new AddPreliminaryHazards((AElement) observedObject);
                generator.start();
            }
        }

        @Override
        public void dispose() {}
    }

    private class GenericFaultGenerationAction extends Action implements IWorkbenchAction {

        public GenericFaultGenerationAction() {
            super();
            try {
                ImageDescriptor im = ImageDescriptor.createFromURL(new URL("platform:/plugin/de.dlr.premise.view.SafetyView/icons/addfaulttree.png"));
                this.setImageDescriptor(im);
            } catch (MalformedURLException e1) {}
            this.setToolTipText("Add generic Fault Tree");
        }

        public void run(){
            GenericFaultGeneration generator = new GenericFaultGeneration((AElement) observedObject);
            generator.start();
        }

        @Override
        public void dispose() {
        }
    }

    private class LockObservedObject extends Action implements IWorkbenchAction {


            public LockObservedObject() {
                super("Lock", SWT.TOGGLE);
                try {
                    ImageDescriptor im = ImageDescriptor.createFromURL(new URL("platform:/plugin/de.dlr.premise.view.SafetyView/icons/synced.gif"));
                    this.setImageDescriptor(im);
                } catch (MalformedURLException e1) {}
                this.setToolTipText("Link with Editor");
            }

            @Override
            public void run() {
                lockObservedElement = !lockObservedElement;

                // adjust the safety editor to the selection once unlocked
                if (lockObservedElement == false && selectedElements.size() > 0) {
                    setObservedObjects(selectedElements.getFirst());
                    refreshObservedList();
                }
            }

            @Override
            public void dispose() {

            }

        }

    private class AddNewHazardAction extends Action implements IWorkbenchAction {

        public AddNewHazardAction(){
            super();
            try {
                ImageDescriptor im = ImageDescriptor.createFromURL(new URL("platform:/plugin/de.dlr.premise.view.SafetyView/icons/addfault.png"));
                this.setImageDescriptor(im);
            } catch (MalformedURLException e1) {}
            this.setToolTipText("Add a hazard");
        }

        public void run() {

            // do not allow generation if the user selected anything but AElements
            if(!(observedObject instanceof AElement)) {
                AddNewHazard.displayWarning();
                return;
            }

            AddNewHazard hazard = new AddNewHazard((AElement) observedObject);
            hazard.start();
        }

        @Override
        public void dispose() {

        }
    }

    private class AddSafetySignificantFunction extends Action implements IWorkbenchAction {

        public AddSafetySignificantFunction(){
            super();
            try {
                ImageDescriptor im = ImageDescriptor.createFromURL(new URL("platform:/plugin/de.dlr.premise.view.SafetyView/icons/newSSF.png"));
                this.setImageDescriptor(im);
            } catch (MalformedURLException e1) {
                this.setText("Add SSF");
            }
            this.setToolTipText("Add Safety Significant Function");
        }

        public void run() {

            // do not allow generation if the user selected anything but UseCases or ARepositories
            if (!(observedObject instanceof UseCase)){
                System.out.println("!WARNING Safety Significant Functions can only be added to UseCases/Functions");
                return;
            }


            // build the StateMachine Command and execute it
            EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(observedObject);
            Command com = SafetyViewHelper.buildSSF(observedObject);
            if (com.canExecute()) {
                editingDomain.getCommandStack().execute(com);
            }

            refreshObservedList();

        }

        @Override
        public void dispose() {

        }
    }

    private class MyKeyListener extends KeyAdapter implements KeyListener {

        private TableViewer viewer;

        public MyKeyListener(TableViewer observedTableViewer) {
            this.viewer = observedTableViewer;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if ((e.stateMask & SWT.CONTROL) == SWT.CONTROL && e.keyCode == 'z') {
                if (observedObject == null) {
                    return;
                }
                EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(observedObject);
                editingDomain.getCommandStack().undo();
                refreshObservedList();
                observedTableViewer.refresh();
            }

        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.keyCode == SWT.DEL && !viewer.getSelection().isEmpty()) {// DELETE removes selection
                for (Iterator<?> iterator = ((IStructuredSelection) observedTableViewer.getSelection()).iterator(); iterator.hasNext();) {
                    Object obj = iterator.next();
                    observedElements.remove(obj);
                    SafetyViewHelper.deleteStateMachine((StateMachine)((Transition)obj).eContainer());
                }
                viewer.refresh();
            }
            if (e.keyCode == SWT.DEL && !viewer.getSelection().isEmpty()) {//
                observedTableViewer.getSelection();

                viewer.refresh();
            }
        }
    }

    private class MyDragSourceListener extends ViewerDragAdapter {

        TableViewer viewer;

        public MyDragSourceListener(TableViewer observedTableViewer) {
            super(observedTableViewer);
            this.viewer = observedTableViewer;
        }


        @Override
        public void dragSetData(DragSourceEvent event) {
            if (!viewer.getSelection().isEmpty() && viewer.getSelection() instanceof IStructuredSelection){
                EObject objectSelected = (EObject)((IStructuredSelection)viewer.getSelection()).getFirstElement();
                if (objectSelected != null && objectSelected instanceof Transition) {
                    Mode targetMode = ((Transition) objectSelected).getTarget();
                    SafetyModeContainer modeCon = new SafetyModeContainer(targetMode);
                    event.data = modeCon;
                }
            } else {
                event.data = viewer.getSelection();
            }

        }

    }

    private class MyCellModifier implements ICellModifier {

        TableViewer viewer = null;

        public MyCellModifier(TableViewer viewer) {
            this.viewer = viewer;
        }

        @Override
        public boolean canModify(Object element, String property) {
            for (ColumnValue col : columns) {
                if (col.capture.equals(property)){
                    if (col.id == ColumnValue.ID.TARGET_MODE || col.id == ColumnValue.ID.FAILURE_RATE){
                        return true;
                    }
                    // make multiple failure state machines editable
                    if (col.id == ColumnValue.ID.STATEMACHINE) {
                        StateMachine obsSM = (StateMachine)((Transition)element).eContainer();
                        if (obsSM.getTransitions().size() > 1){
                            return true;
                        }
                    }
                }

            }
            return false;
        }

        // set the text Label here when modifying cells
        @Override
        public Object getValue(Object element, String property) {
            String returnString = "";
            for (ColumnValue col : columns) {
                if (col.id == ColumnValue.ID.STATEMACHINE && col.capture.equals(property)) {
                   // state machine name
                    returnString = ((ANameItem)((Transition) element).eContainer()).getName();
                } else if (col.id == ColumnValue.ID.TARGET_MODE && col.capture.equals(property)){
                    // target mode hazard name
                    try {
                        Mode targetMode = ((Transition)element).getTarget();
                        returnString = targetMode.getName();
                    } catch (Exception e) {System.out.println("Could not fetch Hazard");}
                } else if (col.id == ColumnValue.ID.FAILURE_RATE && col.capture.equals(property)){
                    // Failure Rate Value
                    try {
                        Value val = ((Transition)element).getParameters().get(0).getValue();
                        returnString = val.getValue();
                    } catch (Exception e) {
                        System.out.println("Could not fetch Failure Rate");
                        returnString = "N.A.";
                    }
                }  else if (col.id == ColumnValue.ID.TRANSITION && col.capture.equals(property)){
                    // Event Name NO LONGER MODIFYABLE
                    try {
                        String val = ((Transition)element).getName();
                        returnString = val;
                    } catch (Exception e) {
                        System.out.println("Could not fetch Failure Rate");
                        returnString = "N.A.";
                    }
                }
                // Note: Component is not editable
            }

            return returnString;
        }

        // modify cells
        @Override
        public void modify(final Object tableitem, String property, final Object value) {

            ChangeCommand cchange;



            // for every column check
            for (final ColumnValue col : columns) {

                // wether the column title is the selected one
                if (col.capture.equals(property)) {

                    // gets the object to modify and setup change command
                    EObject element = (EObject) ((TableItem) tableitem).getData();
                    cchange = new ChangeCommand(element.eContainer()){
                        @Override
                        protected void doExecute() {
                            if (property.equals("Failure Mode")) {
                                try {
                                    // 1. Set Hazard Name
                                    // 2. Set Event Name > F_<Hazard>
                                    // 3. Set StateMachine Name > <Hazard> State
                                    String hazardStr = String.valueOf(value);

                                    Mode targetMode = ((Transition)element).getTarget();
                                    targetMode.setName(hazardStr);

                                    ((Transition)element).setName("F: "+hazardStr);

                                    // 3 but check if its a multi failure state machine
                                    StateMachine observedMachine = ((StateMachine)((Transition)element).eContainer());
                                    if (observedMachine.getTransitions().size() == 1){
                                        observedMachine.setName(hazardStr + " State");
                                    }
                                } catch (Exception e) {System.out.println("Could not fetch Hazard");}
                            } else if (property.equals("Failure Rate")) {
                                editFailureRate(value, element);

                            } else if (property.equals("State Machine")) {
                                ((StateMachine)((Transition)element).eContainer()).setName(String.valueOf(value));
                            }
                        }};

                    if (cchange.canExecute()){
                        AdapterFactoryEditingDomain.getEditingDomainFor(element).getCommandStack().execute(cchange);
                    }

                    viewer.refresh();
                    break;
                }
            }
        }

        private void editFailureRate(final Object value, EObject element) {
            try {

                if (((Transition)element).getParameters().size() == 0){
                    // create a new value to edit
                    TransitionParameter tParam = SystemFactory.eINSTANCE.createTransitionParameter();
                    tParam.setName("Failure Rate");
                    Value frVal = RegistryFactory.eINSTANCE.createValue();
                    frVal.setValue("0");
                    tParam.setValue(frVal);
                    ((Transition)element).getParameters().add(tParam);
                }

                Value frVal = ((Transition)element).getParameters().get(0).getValue();
                if (value.toString().equals("")){
                    frVal.setValue("");
                } else {
                    Double.parseDouble(value.toString());
                    frVal.setValue(value.toString());
                }
            } catch (Exception e) {System.out.println("Could not fetch Failure Rate or input is invalid");}
        }
    }
}