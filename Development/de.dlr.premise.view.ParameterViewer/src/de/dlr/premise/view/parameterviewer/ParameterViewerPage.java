/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.parameterviewer;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.or;
import static de.dlr.premise.system.provider.my.ComponentReferenceChildrenDelegatingWrapperItemProvider.unwrap;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.dlr.premise.element.ARepository;
import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.registry.AValueDef;
import de.dlr.premise.registry.AVersionedModelRoot;
import de.dlr.premise.registry.Constant;
import de.dlr.premise.registry.Registry;
import de.dlr.premise.system.ABalancing;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.presentation.SystemEditor;
import de.dlr.premise.system.presentation.my.SystemEditorMy;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.view.parameterviewer.actions.ClearObsercedListAction;
import de.dlr.premise.view.parameterviewer.actions.ClearObservedListSelectionAction;
import de.dlr.premise.view.parameterviewer.actions.CollapseExpandAllAction;
import de.dlr.premise.view.parameterviewer.actions.DefaultTableAction;
import de.dlr.premise.view.parameterviewer.actions.LoadTableAction;
import de.dlr.premise.view.parameterviewer.actions.SaveTableAction;
import de.dlr.premise.view.parameterviewer.data.ColumnValue;
import de.dlr.premise.view.parameterviewer.data.ColumnValue.ID;
import de.dlr.premise.view.parameterviewer.data.ObjectSerializeDeserialize;
import de.dlr.premise.view.parameterviewer.data.QueuedMap;
import de.dlr.premise.view.parameterviewer.listener.MyDragSourceListener;
import de.dlr.premise.view.parameterviewer.listener.MyDropTargetListener;
import de.dlr.premise.view.parameterviewer.listener.UndoAndDeleteKeyListener;

public class ParameterViewerPage extends Page {

    private final String PREFERENCES_NODE = "de.dlr.premise.parameterviewer";
    private final String PREFERENCES_OBSERVEDKEY = "observedList";
    private final String PREFERENCES_TABLE = "tablePreferences";
    private final String PREFERENCES_TABLE_ORDER = "tablePreferencesOrder";
    private final String DEFAULT = "DEFAULT";
    private final int QUEUE_SIZE = 50; // For storing observed Lists
    private final Preferences prefs = InstanceScope.INSTANCE.getNode(PREFERENCES_NODE);

    private String resourcePath;
    // Editor - Needed to Change Data (and Set Editor Dirty)
    private SystemEditor currentEditor;
    // saves old Values
    private HashMap<AValueDef, String> oldValues;
    // "puffer" of old Values for Color"
    private HashMap<AValueDef, String> changedValues;
    private List<ABalancing<? extends AParameterDef>> allBalancings;
    // observed Parameters
    private List<EObject> observedParameter;
    // Contains both Trees
    private Composite composite;

    private TreeViewer sourceTreeViewer;
    private TreeViewer observedTreeViewer;
    private int[] columnOrder = null;

    // Stores columns (order represents order in the Table)
    private List<ColumnValue> columns = new LinkedList<ColumnValue>();

    public TreeViewer getSourceTreeViewer() {
        return sourceTreeViewer;
    }

    public void setSourceTreeViewer(TreeViewer sourceTreeViewer) {
        this.sourceTreeViewer = sourceTreeViewer;
    }

    public TreeViewer getObservedTreeViewer() {
        return observedTreeViewer;
    }

    public void setObservedTreeViewer(TreeViewer observedTreeViewer) {
        this.observedTreeViewer = observedTreeViewer;
    }

    public List<EObject> getObservedParameter() {
        return observedParameter;
    }

    public void setObservedParameter(List<EObject> observedParameter) {
        this.observedParameter = observedParameter;
    }

    public ParameterViewerPage(IWorkbenchPart part) {
        if (part instanceof SystemEditorMy) {
            setCurrentEditor((SystemEditorMy) part);
        }

        setOldValues(new HashMap<AValueDef, String>());
        setChangedValues(new HashMap<AValueDef, String>());
        setAllBalancings(new LinkedList<ABalancing<? extends AParameterDef>>());
        setDefaultColumns();
        loadObservedParameterFromPreferences();
    }

    @SuppressWarnings("unchecked")
    private void loadObservedParameterFromPreferences() {
        QueuedMap<String, String[]> queuedMap;
        setObservedParameter(new LinkedList<EObject>());
        String buffer = prefs.get(PREFERENCES_OBSERVEDKEY, DEFAULT);
        if (buffer != DEFAULT) {
            queuedMap = (QueuedMap<String, String[]>) ObjectSerializeDeserialize.deserialize(buffer);
        } else {
            queuedMap = new QueuedMap<String, String[]>(QUEUE_SIZE);
        }
        Resource res;
        if (((TreeSelection) getCurrentEditor().getSelection()).getFirstElement() instanceof Resource) {
            res = ((Resource) ((TreeSelection) getCurrentEditor().getSelection()).getFirstElement());
        } else {
            res = ((EObject) ((StructuredSelection) getCurrentEditor().getSelection()).getFirstElement()).eResource();
        }
        resourcePath = res.getURI().path();
        if (queuedMap.containsKey(resourcePath)) {
            for (String uri : queuedMap.getItem(resourcePath)) {
                try {
                    // no Constants
                    if (uri != null) {
                        EObject eObj = res.getEObject(uri);
                        // only add existing objects
                        if (eObj != null) {
                            getObservedParameter().add(eObj);
                        }
                    }
                } catch (WrappedException e) {
                    // DO nothing
                }
            }
        }
    }

    @Override
    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);

        FillLayout layout = new FillLayout(SWT.HORIZONTAL);
        layout.spacing = 5;
        composite.setLayout(layout);
        TabelLabelProvider labelProvider = new TabelLabelProvider(this);
        composite.setVisible(true);

        // Transfer is needed for Drag&Drop
        Transfer[] transfer = new Transfer[] { LocalTransfer.getInstance() };

        // ---------SourceTree-----------
        SashForm sourceSash = new SashForm(composite, SWT.HORIZONTAL);
        sourceSash.setVisible(true);
        sourceSash.setLayout(layout);
        Tree sourceTree = new Tree(sourceSash, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        sourceTree.setToolTipText("");
        setSourceTreeViewer(new TreeViewer(sourceTree));
        getSourceTreeViewer().getTree().setRedraw(false);
        TreeViewerColumn nameColumn = new TreeViewerColumn(getSourceTreeViewer(), SWT.LEFT);
        nameColumn.getColumn().setText("Name");
        nameColumn.getColumn().setWidth(1000);
        // CellEditors for EditValue
        getSourceTreeViewer().addDragSupport(DND.DROP_MOVE, transfer, new MyDragSourceListener(getSourceTreeViewer()));
        getSourceTreeViewer().setContentProvider(new ParameterContentProvider(true));
        getSourceTreeViewer().setLabelProvider(labelProvider);

        // --------ObservationTree--------
        SashForm observedSash = new SashForm(sourceSash, SWT.HORIZONTAL);
        observedSash.setVisible(true);
        observedSash.setLayout(layout);
        final Tree observedTree = new Tree(observedSash, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        observedTree.setHeaderVisible(true);
        observedTree.setLinesVisible(true);
        setObservedTreeViewer(new TreeViewer(observedTree));
        getObservedTreeViewer().getTree().setRedraw(false);
        observedTree.addKeyListener(new UndoAndDeleteKeyListener(this, getObservedTreeViewer()));

        observedTree.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
            }

            @Override
            public void focusGained(FocusEvent e) {
                refreshObservedList();
            }
        });
        createColumns(getObservedTreeViewer());

        // CellEditors for EditValue
        CellEditor[] editors = new CellEditor[getColumns().size()];
        for (int i = 0; i < getColumns().size(); i++) {
            editors[i] = new TextCellEditor(observedTree);
        }
        getObservedTreeViewer().setCellEditors(editors);
        getObservedTreeViewer().setCellModifier(new MyCellModifier(this));
        getObservedTreeViewer().setColumnProperties(getColumnProperty());
        getObservedTreeViewer().addDropSupport(DND.DROP_MOVE, transfer, new MyDropTargetListener(this));
        getObservedTreeViewer().addDragSupport(DND.DROP_MOVE, transfer, new MyDragSourceListener(getObservedTreeViewer()));
        getObservedTreeViewer().setContentProvider(new ParameterContentProvider(false));
        getObservedTreeViewer().setLabelProvider(labelProvider);
        observedTree.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                saveObservedList();
            }
        });

        sourceSash.setWeights(new int[] { 1, 4 });

        // ----------Menu---------------
        IToolBarManager tbm = getSite().getActionBars().getToolBarManager();

        Action saveTable = new SaveTableAction(this);
        saveTable.setText("Save Layout");
        tbm.add(saveTable);

        Action loadtable = new LoadTableAction(this);
        loadtable.setText("Load Layout");
        tbm.add(loadtable);

        Action restoreDefault = new DefaultTableAction(this);
        restoreDefault.setText("Default Layout");
        tbm.add(restoreDefault);

        getSite().getActionBars().getToolBarManager().add(new Separator());

        Action clearList = new ClearObsercedListAction(this);
        clearList.setText("Clear All");
        tbm.add(clearList);

        Action clearSelection = new ClearObservedListSelectionAction(this);
        clearSelection.setText("Clear Selection");
        tbm.add(clearSelection);

        getSite().getActionBars().getToolBarManager().add(new Separator());

        Action collapseExpandAll = new CollapseExpandAllAction(this);
        try {
            collapseExpandAll.setImageDescriptor(ImageDescriptor.createFromURL(new URL("platform:/plugin/de.dlr.premise.view.ParameterViewer/icons/expandall.gif")));
        } catch (IOException e1) {
            // ignore
        }
        // TODO ICON collapse Adden
        tbm.add(collapseExpandAll);

        if (!getObservedParameter().isEmpty()) {
            getObservedTreeViewer().setInput(getObservedParameter().toArray());
        }

        getSourceTreeViewer().getTree().setRedraw(true);
        getObservedTreeViewer().getTree().setRedraw(true);

    }

    private String[] getColumnProperty() {
        String[] property = new String[getColumns().size()];
        for (ColumnValue col : getColumns()) {
            property[getColumnOrder()[getColumns().indexOf(col)]] = col.capture;
        }
        return property;
    }

    public void setDefaultColumns() {
        getColumns().clear();
        setColumnOrder(null);
        // Add new Columns at the end and change position after id is set
        getColumns().add(new ColumnValue("Observed", 180, ColumnValue.ID.NAME));
        getColumns().add(new ColumnValue("Value", 50, ColumnValue.ID.VALUE));
        getColumns().add(new ColumnValue("Old Value", 70, ColumnValue.ID.OLD_VALUE));
        getColumns().add(new ColumnValue("Unit", 50, ColumnValue.ID.UNIT));
        getColumns().add(new ColumnValue("Absolute Change", 50, ColumnValue.ID.CHANGES_ABS));
        getColumns().add(new ColumnValue("% Change", 50, ColumnValue.ID.CHANGES_PERCENTAGE));
        getColumns().add(new ColumnValue("Uncertainty", 75, ColumnValue.ID.UNCERTAINTY));
        getColumns().add(new ColumnValue("Satisfies", 150, ColumnValue.ID.SATISFIES));
        getColumns().add(new ColumnValue("Target of", 150, ID.TARGET));
        getColumns().add(new ColumnValue("Source of", 150, ColumnValue.ID.SOURCE));
        getColumns().add(new ColumnValue("Full Qualified Name", 200, ColumnValue.ID.FULL_NAME));
    }

    public void saveTable() {
        try {
            prefs.node(PREFERENCES_TABLE).clear();

            for (TreeColumn col : getObservedTreeViewer().getTree().getColumns()) {
                for (ColumnValue colVal : getColumns()) {
                    if (col.getText().equals(colVal.capture)) {
                        colVal.width = col.getWidth();
                        break;
                    }
                }
            }
            prefs.put(PREFERENCES_TABLE, ObjectSerializeDeserialize.serialize(getColumns()));
            prefs.put(PREFERENCES_TABLE_ORDER, ObjectSerializeDeserialize.serialize(getObservedTreeViewer().getTree().getColumnOrder()));
            prefs.flush();
        } catch (BackingStoreException e) {
        }
    }

    @SuppressWarnings("unchecked")
    private void saveObservedList() {
        QueuedMap<String, String[]> queuedMap = null;

        // Load observedListMap
        String b = prefs.get(PREFERENCES_OBSERVEDKEY, DEFAULT);
        if (b != DEFAULT) {
            queuedMap = (QueuedMap<String, String[]>) ObjectSerializeDeserialize.deserialize(b);
        } else {
            queuedMap = new QueuedMap<String, String[]>(QUEUE_SIZE);
        }

        try {
            // Save observedList
            String[] temp = new String[getObservedParameter().size()];
            if (!getObservedParameter().isEmpty()) {
                for (EObject eObj : getObservedParameter()) {
                    if (!(eObj.eContainer() instanceof Constant))
                        temp[getObservedParameter().indexOf(eObj)] = EcoreUtil.getURI(eObj).fragment();
                }
                queuedMap.addItem(resourcePath, temp);
                prefs.node(PREFERENCES_OBSERVEDKEY).clear();
            } else if (queuedMap.containsKey(resourcePath)) {
                queuedMap.remove(resourcePath);
            }

            prefs.put(PREFERENCES_OBSERVEDKEY, ObjectSerializeDeserialize.serialize(queuedMap));
            prefs.flush();

        } catch (BackingStoreException e) {
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private void createColumns(TreeViewer viewer) {
        TreeViewerColumn temp;
        for (ColumnValue colVal : getColumns()) {
            temp = new TreeViewerColumn(viewer, SWT.LEFT);
            temp.getColumn().setMoveable(true);
            temp.getColumn().setText(colVal.capture);
            temp.getColumn().setWidth(colVal.width);
        }
        viewer.getTree().setToolTipText("");
        setColumnOrder(new int[getColumns().size()]);
        for (int i = 0; i < getColumns().size(); i++) {
            getColumnOrder()[i] = i;
        }

        if (loadTableFromPreferences()) {
            for (int i = 0; i < getColumns().size(); i++) {
                viewer.getTree().getColumn(i).setWidth(getColumns().get(i).width);
            }

            viewer.getTree().setColumnOrder(getColumnOrder());
            viewer.refresh();
        }
    }

    private void refreshObservedList() {

        for (Iterator<EObject> iterator = getObservedParameter().iterator(); iterator.hasNext();) {
            // EObject removed?
            EObject eObj = iterator.next();
            if (eObj.eResource() == null) {
                iterator.remove();
            }
        }
        getObservedTreeViewer().refresh();
    }

    @SuppressWarnings("unchecked")
    public boolean loadTableFromPreferences() {
        String buffer = prefs.get(PREFERENCES_TABLE, DEFAULT);
        // Columns
        if (buffer != DEFAULT) {
            setColumns(((List<ColumnValue>) ObjectSerializeDeserialize.deserialize(buffer)));
            String orderStr = prefs.get(PREFERENCES_TABLE_ORDER, DEFAULT);
            if (buffer != DEFAULT) {
                setColumnOrder((int[]) ObjectSerializeDeserialize.deserialize(orderStr));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Control getControl() {
        return composite;
    }

    @Override
    public void setFocus() {
        composite.setFocus();
    }

    public void setRootElementsIfVisible(ISelection sel) {
        // return if ParameterViewer isn't visible (Improve performance)
        if (!composite.isVisible()) {
            return;
        }
        setRootInputWithoutRedraw(sel);
    }

    private void setRootInputWithoutRedraw(ISelection sel) {
        getSourceTreeViewer().getTree().setRedraw(false);

        setRootInput(sel);

        getSourceTreeViewer().getTree().setRedraw(true);
    }

    @SuppressWarnings("unchecked")
    private void setRootInput(ISelection sel) {
        List<Object> selection = ((StructuredSelection) sel).toList();
        updateBalancingList(selection);
        setViewerInput(selection);
    }

    private void setViewerInput(List<Object> selection) {
        Object[] input = selection.stream().map(this::objectOrFirstElement).flatMap(this::required).toArray();
        getSourceTreeViewer().setInput(input);
        getSourceTreeViewer().expandAll();
    }

    private Optional<Object> objectOrFirstElement(Object obj) {
        obj = unwrap(obj);
        if (obj instanceof XMIResource) {
            return getRepository(obj).filter(or(instanceOf(ProjectRepository.class), instanceOf(Registry.class))).map(o -> (Object) o);
        } else if (ParameterContentProvider.instanceofRecognizedElement(obj) && !(obj instanceof AValueDef)) {
            return Optional.of(obj);
        }
        return Optional.empty();
    }

    private void updateBalancingList(List<Object> selection) {
        Set<AVersionedModelRoot> repositories = selection.stream().map(this::getRepository).flatMap(this::required)
                                                         .filter(instanceOf(ARepository.class)).collect(Collectors.toSet());
        getAllBalancings().clear();
        repositories.forEach(this::fillBalancingList);
    }

    private <T> Stream<T> required(Optional<T> opt) {
        return opt.map(Stream::of).orElseGet(Stream::empty);
    }

    private Optional<AVersionedModelRoot> getRepository(Object obj) {
        return getResource(obj).flatMap(r -> r.getContents().stream().filter(instanceOf(AVersionedModelRoot.class))
                                              .map(rep -> (AVersionedModelRoot) rep).findFirst());
    }

    private Optional<Resource> getResource(Object obj) {
        if (obj instanceof XMIResource) {
            return Optional.of((Resource) obj);
        } else if (obj instanceof EObject) {
            return Optional.ofNullable(((EObject) obj).eResource());
        } else if (obj instanceof IWrapperItemProvider) {
            return getResource(((IWrapperItemProvider) obj).getValue());
        } else {
            return Optional.empty();
        }
    }

    private void fillBalancingList(AVersionedModelRoot rep) {
        for (EObject bal : PremiseHelper.getAll(rep, ABalancing.class)) {
            getAllBalancings().add((ABalancing<?>) bal);
        }
    }

    public HashMap<AValueDef, String> getChangedValues() {
        return changedValues;
    }

    public void setChangedValues(HashMap<AValueDef, String> changedValues) {
        this.changedValues = changedValues;
    }

    public List<ABalancing<? extends AParameterDef>> getAllBalancings() {
        return allBalancings;
    }

    public void setAllBalancings(List<ABalancing<? extends AParameterDef>> allBalancings) {
        this.allBalancings = allBalancings;
    }

    public HashMap<AValueDef, String> getOldValues() {
        return oldValues;
    }

    public void setOldValues(HashMap<AValueDef, String> oldValues) {
        this.oldValues = oldValues;
    }

    public List<ColumnValue> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnValue> columns) {
        this.columns = columns;
    }

    public int[] getColumnOrder() {
        return columnOrder;
    }

    public void setColumnOrder(int[] columnOrder) {
        this.columnOrder = columnOrder;
    }

    public SystemEditor getCurrentEditor() {
        return currentEditor;
    }

    public void setCurrentEditor(SystemEditor currentEditor) {
        this.currentEditor = currentEditor;
    }
}