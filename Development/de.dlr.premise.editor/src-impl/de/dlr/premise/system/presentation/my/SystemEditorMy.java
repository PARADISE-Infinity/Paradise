package de.dlr.premise.system.presentation.my;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.ui.viewer.ColumnViewerInformationControlToolTipSupport;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.ui.celleditor.AdapterFactoryTreeEditor;
import org.eclipse.emf.edit.ui.dnd.EditingDomainViewerDropAdapter;
import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.DiagnosticDecorator;
import org.eclipse.emf.edit.ui.provider.UnwrappingSelectionProvider;
import org.eclipse.emf.edit.ui.util.EditUIUtil;
import org.eclipse.emf.edit.ui.view.ExtendedPropertySheetPage;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import de.dlr.premise.common.command.AssociatedCommandsCommandStack;
import de.dlr.premise.element.AGuardCondition;
import de.dlr.premise.element.ARepository;
import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.element.GuardCombination;
import de.dlr.premise.element.IConditional;
import de.dlr.premise.element.Mode;
import de.dlr.premise.element.ModeGuard;
import de.dlr.premise.provider.util.PremiseAdapterFactoryHelper;
import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.registry.MetaData;
import de.dlr.premise.registry.MetaTypeDef;
import de.dlr.premise.registry.RegistryPackage;
import de.dlr.premise.system.ComponentReference;
import de.dlr.premise.system.ModeValueRef;
import de.dlr.premise.system.extensionpoints.IBeforeModelLoad;
import de.dlr.premise.system.presentation.PREMISEEditorPlugin;
import de.dlr.premise.system.presentation.SystemEditor;
import de.dlr.premise.system.presentation.my.filter.workarounds.DelegatingStyledCellLabelProvider2;
import de.dlr.premise.system.presentation.my.filter.workarounds.StyledLabelProvider2;
import de.dlr.premise.system.presentation.my.query.QueryableTree;
import de.dlr.premise.system.provider.my.AdapterFactoryContentProviderMy;
import de.dlr.premise.system.provider.my.ComponentReferenceChildrenDelegatingWrapperItemProvider;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.SafetyModeContainer;
import de.dlr.premise.util.urihandlers.PremiseLibraryURIHandler;
import de.dlr.premise.util.urihandlers.URIHandlerHelper;
import de.dlr.premise.xtext.ISelectionSettable;

public class SystemEditorMy extends SystemEditor implements ISelectionSettable {
    
    private QueryableTree tree;

    private static String getString(String key) {
        return PREMISEEditorPlugin.INSTANCE.getString(key);
    }

    private AdapterFactoryTreeEditor adapterFactoryTreeEditor;

    public SystemEditorMy() {
        super();
        this.resourceChangeListener = markerFilteredResourceChangeListener;
    }

    @Override
    public void createModel() {
        exectuteBeforeModelLoadExtensionPoint();

        // disable notification (& calculation) while loading resource
        editingDomain.getResourceSet().getLoadOptions().put(XMIResource.OPTION_DISABLE_NOTIFY, Boolean.TRUE);
        // register the LegacyPremiseURIHandler to prevent freezing on unknown namespace URIs
        URIHandlerHelper.registerInto(editingDomain.getResourceSet().getURIConverter());
        
        super.createModel();
        
        // get root model element
        EObject repo = editingDomain.getResourceSet().getResources().get(0).getContents().get(0);

        // when opening a ProjectRepository, initialize the view options and calculation
        if (repo instanceof ARepository) {
            EditorHelper.initializeEditorViewOptions((ARepository) repo);
        }
        
        // add global registry to res set
        editingDomain.getResourceSet().getResource(PremiseLibraryURIHandler.PREMISE_GLOBAL_REGISTRY_URI, true);
    }

    private void exectuteBeforeModelLoadExtensionPoint() {
        IEditorInput input = getEditorInput();
        final IFile premiseFile =
                ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(EditUIUtil.getURI(input).toPlatformString(true)));

        IConfigurationElement[] config =
                Platform.getExtensionRegistry().getConfigurationElementsFor("de.dlr.premise.editor.beforeModelLoad");
        try {
            for (IConfigurationElement e : config) {
                final Object o = e.createExecutableExtension("class");
                if (o instanceof IBeforeModelLoad) {
                    ISafeRunnable runnable = new ISafeRunnable() {

                        @Override
                        public void run() throws Exception {
                            ((IBeforeModelLoad) o).execute(premiseFile);
                        }

                        @Override
                        public void handleException(Throwable e) {
                            System.out.println(e.getMessage());
                        }
                    };
                    SafeRunner.run(runnable);
                }
            }
        } catch (CoreException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    protected void initializeEditingDomain() {
        // Create an adapter factory that yields item providers.
        //
        adapterFactory = PremiseAdapterFactoryHelper.createComposedAdapterFactory();

        // Create the command stack that will notify this editor as commands are executed.
        //
        BasicCommandStack commandStack = new AssociatedCommandsCommandStack();

        // Add a listener to set the most recent command's affected objects to be the selection of the viewer with focus.
        //
        commandStack.addCommandStackListener(new CommandStackListener() {

            @Override
            public void commandStackChanged(final EventObject event) {
                getContainer().getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        firePropertyChange(IEditorPart.PROP_DIRTY);

                        // Try to select the affected objects.
                        //
                        Command mostRecentCommand = ((CommandStack) event.getSource()).getMostRecentCommand();
                        if (mostRecentCommand != null) {
                            setSelectionToViewer(mostRecentCommand.getAffectedObjects());

                            // ADDED CALL!!! set the focus on the editable tree cell
                            EditorHelper.setFocus(getViewer(), getSite(), mostRecentCommand);
                        }
                        for (Iterator<PropertySheetPage> i = propertySheetPages.iterator(); i.hasNext();) {
                            PropertySheetPage propertySheetPage = i.next();
                            if (propertySheetPage.getControl().isDisposed()) {
                                i.remove();
                            } else {
                                propertySheetPage.refresh();
                            }
                        }
                    }
                });
            }
        });

        // Create the editing domain with a special command stack.
        //
        editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<Resource, Boolean>());
    }

    /**
     * This is the method used by the framework to install your own controls. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void createPages() {
        // Creates the model from the editor input
        //
        createModel();

        // Only creates the other pages if there is something that can be edited
        //
        if (!getEditingDomain().getResourceSet().getResources().isEmpty()) {
            // Create a page for the selection tree view.
            //
            tree = new QueryableTree(getContainer(), SWT.MULTI, editingDomain.getResourceSet(), adapterFactory);
            selectionViewer = tree.getViewer();
            selectionViewer.setUseHashlookup(true);
            setCurrentViewer(selectionViewer);
            //
            // Transfer[] transfer = new Transfer[] { LocalTransfer.getInstance() };
            // selectionViewer.addDragSupport(DND.DROP_MOVE, transfer, new MyDragSourceListener(selectionViewer));
            // selectionViewer.addDropSupport(DND.DROP_MOVE, transfer, new MyDropTargetListener(selectionViewer));

            selectionViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
            // CHANGED REGISTRATION !!!
            selectionViewer.setLabelProvider(new DelegatingStyledCellLabelProvider2(new StyledLabelProvider2(new StyledLabelProvider2(new AdapterFactoryLabelProviderMy(adapterFactory,
                                                                                                                                                                        selectionViewer),
                                                                                                                                      new DiagnosticDecorator.Styled(editingDomain.getResourceSet(),
                                                                                                                                                                     selectionViewer)),
                                                                                                             tree.getSearchDecorator())));
            selectionViewer.setInput(editingDomain.getResourceSet());
            selectionViewer.setSelection(new StructuredSelection(editingDomain.getResourceSet().getResources().get(0)), true);

            selectionViewer.setComparator(new SystemEditorViewerComparator());
            
            adapterFactoryTreeEditor = new AdapterFactoryTreeEditor(selectionViewer.getTree(), adapterFactory);
            adapterFactoryTreeEditor.grabHorizontal = true;
            
            new ColumnViewerInformationControlToolTipSupport(selectionViewer,
                                                             new DiagnosticDecorator.EditingDomainLocationListener(editingDomain,
                                                                                                                   selectionViewer));

            createContextMenuFor(selectionViewer);
            int pageIndex = addPage(tree);
            setPageText(pageIndex, getString("_UI_SelectionPage_label"));

            getSite().getShell().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    setActivePage(0);
                }
            });
            
            getSite().getService(IContextService.class).activateContext("de.dlr.premise.editor.context");
        }

        // Ensures that this editor will only display the page's tab
        // area if there are more than one page
        //
        getContainer().addControlListener(new ControlAdapter() {

            boolean guard = false;

            @Override
            public void controlResized(ControlEvent event) {
                if (!guard) {
                    guard = true;
                    hideTabs();
                    guard = false;
                }
            }
        });

        getSite().getShell().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                updateProblemIndication();
            }
        });

        // Add listen for changes in MetaTypes to update icons
        editingDomain.getResourceSet().eAdapters().add(new RefreshEditorAdapter());
    }

    protected void createContextMenuFor(StructuredViewer viewer) {
        MenuManager contextMenu = new MenuManager("#PopUp");
        contextMenu.add(new Separator("additions"));
        contextMenu.setRemoveAllWhenShown(true);
        contextMenu.addMenuListener(this);
        Menu menu = contextMenu.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(contextMenu, new UnwrappingSelectionProvider(viewer));

        int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
        Transfer[] transfers =
                new Transfer[] { LocalTransfer.getInstance(), LocalSelectionTransfer.getTransfer(), FileTransfer.getInstance() };
        viewer.addDragSupport(dndOperations, transfers, new ViewerDragAdapter(viewer));
        viewer.addDropSupport(dndOperations, transfers, new MyDropTargetListener(editingDomain, viewer));
    }

    @Override
    public void gotoMarker(IMarker marker) {
        // REMOVED Marker type check!
        String uriAttribute = marker.getAttribute(EValidator.URI_ATTRIBUTE, null);
        if (uriAttribute != null) {
            URI uri = URI.createURI(uriAttribute);
            EObject eObject = editingDomain.getResourceSet().getEObject(uri, true);
            if (eObject != null) {
                setSelectionToViewer(Collections.singleton(editingDomain.getWrapper(eObject)));
            }
        }
    }

    /**
     * This listens for workspace changes.
     * 
     * This is identical to the superclass implementation except for the handling of decoration for markers. The generated implementation
     * displays decoration for all Markers on files that are contained in the editor's ResourceSet. This behaviour is changed here to only
     * generated decoration for markers on the first resource in the ResourceSet.
     * 
     * We always attach markers to the file that the editor was opened on (which is always the first resource in its ResourceSet). But the
     * editor can display multiple files at once for interconnected models. If another file was also opened by itself, another editor can
     * add markers to it. If the editor showed decorations for all markers, those might appear in this editor.
     */
    protected IResourceChangeListener markerFilteredResourceChangeListener = new IResourceChangeListener() {

        public void resourceChanged(IResourceChangeEvent event) {
            IResourceDelta delta = event.getDelta();
            try {
                class ResourceDeltaVisitor implements IResourceDeltaVisitor {

                    protected ResourceSet resourceSet = editingDomain.getResourceSet();
                    protected Collection<Resource> changedResources = new ArrayList<Resource>();
                    protected Collection<Resource> removedResources = new ArrayList<Resource>();

                    public boolean visit(final IResourceDelta delta) {
                        if (delta.getResource().getType() == IResource.FILE) {
                            if (delta.getKind() == IResourceDelta.REMOVED || delta.getKind() == IResourceDelta.CHANGED) {
                                final Resource resource =
                                        resourceSet.getResource(URI.createPlatformResourceURI(delta.getFullPath().toString(), true), false);
                                if (resource != null) {
                                    if (delta.getKind() == IResourceDelta.REMOVED) {
                                        removedResources.add(resource);
                                    } else {
                                        if ((delta.getFlags() & IResourceDelta.MARKERS) != 0) {
                                            // ADDED INDEXOF CHECK
                                            if (resourceSet.getResources().indexOf(resource) == 0) {
                                                DiagnosticDecorator.DiagnosticAdapter.update(resource,
                                                                                             markerHelper.getMarkerDiagnostics(resource,
                                                                                                                               (IFile) delta.getResource(), false));
                                            }
                                        }
                                        if ((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
                                            if (!savedResources.remove(resource)) {
                                                changedResources.add(resource);
                                            }
                                        }
                                    }
                                }
                            }
                            return false;
                        }

                        return true;
                    }

                    public Collection<Resource> getChangedResources() {
                        return changedResources;
                    }

                    public Collection<Resource> getRemovedResources() {
                        return removedResources;
                    }
                }

                final ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
                delta.accept(visitor);

                if (!visitor.getRemovedResources().isEmpty()) {
                    getSite().getShell().getDisplay().asyncExec(new Runnable() {

                        public void run() {
                            removedResources.addAll(visitor.getRemovedResources());
                            if (!isDirty()) {
                                getSite().getPage().closeEditor(SystemEditorMy.this, false);
                            }
                        }
                    });
                }

                if (!visitor.getChangedResources().isEmpty()) {
                    getSite().getShell().getDisplay().asyncExec(new Runnable() {

                        public void run() {
                            changedResources.addAll(visitor.getChangedResources());
                            if (getSite().getPage().getActiveEditor() == SystemEditorMy.this) {
                                handleActivate();
                            }
                        }
                    });
                }
            } catch (CoreException exception) {
                PREMISEEditorPlugin.INSTANCE.log(exception);
            }
        }
    };

    public IPropertySheetPage getPropertySheetPage() {
        PropertySheetPage propertySheetPage = new ExtendedPropertySheetPage(editingDomain, ExtendedPropertySheetPage.Decoration.MANUAL) {

            @Override
            public void setSelectionToViewer(List<?> selection) {
                SystemEditorMy.this.setSelectionToViewer(selection);
                SystemEditorMy.this.setFocus();
            }

            @Override
            public void setActionBars(IActionBars actionBars) {
                super.setActionBars(actionBars);
                getActionBarContributor().shareGlobalActions(this, actionBars);
            }
        };
        // Changed registration
        propertySheetPage.setPropertySourceProvider(new AdapterFactoryContentProviderMy(adapterFactory));
        propertySheetPages.add(propertySheetPage);

        return propertySheetPage;
    }
    
    public AdapterFactoryTreeEditor getAdapterFactoryTreeEditor() {
        return adapterFactoryTreeEditor;
    }

    protected void handleChangedResources() {
        List<Object> oldExpanded = Lists.newArrayList(selectionViewer.getExpandedElements());
        List<Object> oldSelected = ((StructuredSelection) editorSelection).toList();

        // Not drawing during reload makes the reload look smoother. We need to make sure that it is reset after reloading the model
        selectionViewer.getControl().setRedraw(false);
        
        try {
            // START super-class code 
            if (!changedResources.isEmpty() && (!isDirty() || handleDirtyConflict())) {
                if (isDirty()) {
                    changedResources.addAll(editingDomain.getResourceSet().getResources());
                }
                editingDomain.getCommandStack().flush();

                updateProblemIndication = false;
                for (Resource resource : changedResources) {
                    if (resource.isLoaded()) {
                        resource.unload();
                        try {
                            // CHANGED load option
                            resource.load(editingDomain.getResourceSet().getLoadOptions());
                        } catch (IOException exception) {
                            if (!resourceToDiagnosticMap.containsKey(resource)) {
                                resourceToDiagnosticMap.put(resource, analyzeResourceProblems(resource, exception));
                            }
                        }
                    }
                }

                if (AdapterFactoryEditingDomain.isStale(editorSelection)) {
                    setSelection(StructuredSelection.EMPTY);
                }

                updateProblemIndication = true;
                updateProblemIndication();
            }   
            // END super-class code
            
            List<Object> newExpanded = mapAllToCurrentResSet(oldExpanded);
            List<Object> newSelected = mapAllToCurrentResSet(oldSelected);
            
            UIJob job = new UIJob("Restore selection") {
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    try {
                        selectionViewer.setExpandedElements(newExpanded.toArray());
                        selectionViewer.setSelection(new StructuredSelection(newSelected.toArray()), false);
                        return Status.OK_STATUS;
                    } finally {
                        // After refreshing, whatever happens, we want to set redraw back to true
                        selectionViewer.getControl().setRedraw(true);
                    }
                }
            };
            job.schedule(500);
        } catch (Throwable e) {
            // If anything fails, we want to set redraw back to true
            selectionViewer.getControl().setRedraw(true);
            throw new RuntimeException("Error while reloading editor", e);
        }
    }

    private List<Object> mapAllToCurrentResSet(List<Object> old) {
        return old.stream().map(this::mapToCurrentResSet).collect(Collectors.toList());
    }

    /**
     * Translate a given object to the equivalent in the current editors ResourceSet. This is needed because when reloading a file, new
     * EObject instances are created representing the same object in the file.
     */
    private Object mapToCurrentResSet(Object obj) {
        if (obj instanceof ResourceSet) {
            return editingDomain.getResourceSet();
        }
        if (obj instanceof Resource) {
            return editingDomain.getResourceSet().getResource(((Resource) obj).getURI(), false);
        }
        if (obj instanceof EObject) {
            return editingDomain.getResourceSet().getEObject(EcoreUtil.getURI((EObject) obj), true);
        }
        if (obj instanceof ComponentReferenceChildrenDelegatingWrapperItemProvider) {
            ComponentReferenceChildrenDelegatingWrapperItemProvider wrapper = (ComponentReferenceChildrenDelegatingWrapperItemProvider) obj;
            Object value = mapToCurrentResSet(wrapper.getValue());
            ImmutableSet<ComponentReference> scope =
                    wrapper.getScope().stream().map(this::mapToCurrentResSet).map(ComponentReference.class::cast)
                           .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));
            return new ComponentReferenceChildrenDelegatingWrapperItemProvider(value, wrapper.getOwner(), wrapper.getFeature(),
                                                                               wrapper.getIndex(), scope, adapterFactory);
        }
        throw new IllegalArgumentException();
    }
    
    private boolean handelingDirtyConflict = false;
    
    protected boolean handleDirtyConflict()
    {
        if (handelingDirtyConflict) {
            return false;
        }
        try {
            handelingDirtyConflict = true;
            return super.handleDirtyConflict();
        } finally {
            handelingDirtyConflict = false;
        }
    }

    @Override
    public void dispose() {
        tree.dispose();
        super.dispose();
    }

    public QueryableTree getTree() {
        return tree;
    }

    class RefreshEditorAdapter extends EContentAdapter {
        
        UIJob refreshJob = null;

        @Override
        public void notifyChanged(Notification notification) {
            super.notifyChanged(notification);

            boolean doRefresh = false;
            final Object notifier = notification.getNotifier();

            doRefresh = doRefresh || (notifier instanceof MetaTypeDef
                    && (notification.getFeatureID(MetaTypeDef.class) == RegistryPackage.META_TYPE_DEF__ICON
                            || notification.getFeatureID(MetaTypeDef.class) == RegistryPackage.META_TYPE_DEF__NAME));
            doRefresh = doRefresh || (notifier instanceof IMetaTypable
                    && notification.getFeatureID(IMetaTypable.class) == RegistryPackage.IMETA_TYPABLE__META_TYPES);
            doRefresh = doRefresh || (notifier instanceof MetaData
                    && SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES.equals(((MetaData) notifier).getName()));

            if (doRefresh) {
                if (refreshJob != null) {
                    refreshJob.cancel();
                }
                
                refreshJob = new UIJob("Refreshing editor"){
                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        SubMonitor submonitor = SubMonitor.convert(monitor, IProgressMonitor.UNKNOWN);
                        if (!selectionViewer.getControl().isDisposed()) {
                            selectionViewer.getControl().setRedraw(false);
                            submonitor.checkCanceled();
                            selectionViewer.refresh();
                            submonitor.checkCanceled();
                            selectionViewer.getControl().setRedraw(true);
                        }
                        return Status.OK_STATUS;
                    }
                };
                refreshJob.schedule(300);
            }
        }

    }

    private class MyDropTargetListener extends EditingDomainViewerDropAdapter {

        ChangeCommand cchange;

        protected MyDropTargetListener(EditingDomain domain, Viewer viewer) {
            super(domain, viewer);
        }

        @Override
        public void drop(DropTargetEvent event) {
            super.drop(event);
            if (cchange != null){
                if (cchange.canExecute()){
                    editingDomain.getCommandStack().execute(cchange);
                }
            }
        }
        
        protected Collection<?> extractDragSource(Object object) {
            if (object instanceof SafetyModeContainer) {
                return ImmutableList.of(object);
            } else {
                return super.extractDragSource(object);
            }
        }
        

        @Override
        public void helper(DropTargetEvent event) {
            super.helper(event);

            // cleanup change command in case the drop is standard but moved over a custom dropable object
            cchange = null;
            
            // if it is a forbidden drop
            if (event.detail == DND.DROP_NONE) {
                Object target = super.extractDropTarget(event.item);
                Collection<?> source = super.source;
                
                List<Mode> sourceModes = null;

                if (source.iterator().next() instanceof SafetyModeContainer) {
                    sourceModes = Arrays.asList(((SafetyModeContainer) source.iterator().next()).getContainedMode());
                } else if (source.stream().allMatch(Mode.class::isInstance)) {
                    sourceModes = source.stream().map(Mode.class::cast).collect(Collectors.toList());
                } else {
                    return;
                }


                // for GuardCombinations add
                if (target instanceof GuardCombination) {
                    addModesToGuardCombination(event, (GuardCombination) target, sourceModes);

                // for Transitions/UseCases add GuardCombination
                } else if (target instanceof IConditional) {
                    IConditional parentConditional = (IConditional) target;
                    // when the transition already has a Condition
                    AGuardCondition condition = parentConditional.getCondition();
                    if (condition != null) {
                        if (condition instanceof GuardCombination) {
                            addModesToGuardCombination(event, (GuardCombination) condition, sourceModes);
                        } else {
                            replaceAndAddModeGuards(event, (ModeGuard) condition, sourceModes);
                        }
                    } else {
                        addCondition(event, parentConditional, sourceModes);
                    }
                } else if (target instanceof ModeGuard) {
                    replaceAndAddModeGuards(event, (ModeGuard) target, sourceModes);
                } else if (target instanceof ModeValueRef) {
                    addModesToModeValueRef(event, (ModeValueRef) target, sourceModes);
                }
            }
        }

        private void addCondition(DropTargetEvent event, IConditional parentConditional, List<Mode> sourceModes) {
            // change the command and set DND.Dropable
            event.detail = DND.DROP_LINK;
            cchange = new ChangeCommand(parentConditional) {

                @Override
                protected void doExecute() {
                    AGuardCondition condition = null;
                    
                    if (sourceModes.size() == 1) {
                        condition = createModeGuard(sourceModes.get(0));
                    } else {
                        condition = createGuardCombination(sourceModes);
                    }
                    
                    parentConditional.setCondition(condition);
                }
            };
        }

        private void replaceAndAddModeGuards(DropTargetEvent event, ModeGuard originalMG, List<Mode> sourceModes) {
            //change the command and set DND.Dropable
            event.detail = DND.DROP_LINK;
            cchange = new ChangeCommand(originalMG){
                @Override
                protected void doExecute() {
                    EObject orinalContainer = originalMG.eContainer();
                    EReference originalContainmentFeature = originalMG.eContainmentFeature();
                    
                    EList<Object> containmentList = null;
                    int position = -1;
                    if (originalContainmentFeature.isMany()) {
                        containmentList = (EList<Object>) orinalContainer.eGet(originalContainmentFeature);
                        position = containmentList.indexOf(originalMG);
                    }
                    
                    GuardCombination newGC = createGuardCombination(sourceModes);
                    newGC.getChildren().add(0, originalMG);

                    if (originalContainmentFeature.isMany()) {
                        containmentList.add(position, newGC);
                    } else {
                        orinalContainer.eSet(originalContainmentFeature, newGC);
                    }
                }
            };
        }

        private void addModesToGuardCombination(DropTargetEvent event, GuardCombination parentGC, List<Mode> sourceModes) {
            //change the command and set DND.Dropable
            event.detail = DND.DROP_LINK;
            cchange = new ChangeCommand(parentGC){
                @Override
                protected void doExecute() {
                    addModesToGuardCombination(sourceModes, parentGC);
                }
            };
        }
        
        private void addModesToModeValueRef(DropTargetEvent event, ModeValueRef modeVal, List<Mode> sourceModes) {
            //change the command and set DND.Dropable
            event.detail = DND.DROP_LINK;
            cchange = new ChangeCommand(modeVal){
                @Override
                protected void doExecute() {
                    modeVal.getModes().addAll(sourceModes);
                }
            };
        }

        private GuardCombination createGuardCombination(List<Mode> sourceModes) {
            GuardCombination newGC = ElementFactory.eINSTANCE.createGuardCombination();
            addModesToGuardCombination(sourceModes, newGC);
            return newGC;
        }

        private void addModesToGuardCombination(List<Mode> sourceModes, GuardCombination gc) {
            for (Mode sm : sourceModes) {
                gc.getChildren().add(createModeGuard(sm));
            }
        }

        private ModeGuard createModeGuard(Mode mode) {
            ModeGuard newMG = ElementFactory.eINSTANCE.createModeGuard();
            newMG.setMode(mode);
            return newMG;
        }
    }

    

}
