/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my.query;

import static org.eclipse.xtext.xbase.lib.IterableExtensions.isNullOrEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.IntConsumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.edit.provider.AdapterFactoryTreeIterator;
import org.eclipse.emf.edit.provider.ItemProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.progress.WorkbenchJob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

import de.dlr.premise.element.AElement;
import de.dlr.premise.query.QueryInterpreter;
import de.dlr.premise.query.QueryInterpreterFactory;
import de.dlr.premise.query.QueryMode;
import de.dlr.premise.query.exceptions.ParserException;
import de.dlr.premise.query.persistent.PersistableQuery;
import de.dlr.premise.registry.IPremiseObject;
import de.dlr.premise.system.presentation.my.filter.PredicateBasedViewerFilter;
import de.dlr.premise.system.presentation.my.filter.SearchDecorator;
import de.dlr.premise.system.presentation.my.filter.SearchPredicate;
import de.dlr.premise.util.PremiseHelper;

public class QueryableTree extends Composite {

    private static final int EXPAND_ALL_THRESHOLD = 100;
    private static final int UPDATE_UI_DELAY = 10;
    private static final int UPDATE_MATCHES_DELAY = 250;

    protected Composite parent;
    private Composite treeComposite;
    private Composite queryComposite;
    private Label queryLabel;
    private Button btnReapplyQuery;
    private TreeViewer treeViewer;

    private AdapterFactory adapterFactory;
    private QueryUIController controller;
    private Object originalTreeInput;
    private ResourceSet resourceSet;
    private SearchJob searchJob;
    private QueryJob queryJob;
    private UpdateUIJob updateUIJob;

    private QueryInterpreter queryInterpreter;
    private SearchDecorator searchDecorator;

    private Collection<EObject> queryResults = new HashSet<>();
    private Collection<Notifier> explicitlyHidden = new HashSet<>();
    private Collection<Notifier> searchResults = new HashSet<>();
    private CollectionBasedPredicate collectionPredicate = new CollectionBasedPredicate(this::updateQueryResults);
    private SearchPredicate searchPredicate = new SearchPredicate(collectionPredicate);
    private PredicateBasedViewerFilter predicateFilter = new PredicateBasedViewerFilter(collectionPredicate);

    private PersistableQuery activeQuery;

    private volatile IStructuredSelection structuredSelection;

    public QueryableTree(Composite parent, int treeStyle, ResourceSet resourceSet, AdapterFactory adapterFactory) {
        super(parent, SWT.NONE);

        this.parent = parent;
        this.resourceSet = resourceSet;
        this.adapterFactory = adapterFactory;
        queryInterpreter = QueryInterpreterFactory.createQueryInterpreter(resourceSet);
        init(treeStyle);
    }

    public SearchDecorator getSearchDecorator() {
        return searchDecorator;
    }

    public TreeViewer getViewer() {
        return treeViewer;
    }

    public CollectionBasedPredicate getPredicate() {
        return collectionPredicate;
    }

    public PersistableQuery getActiveQuery() {
        return activeQuery;
    }

    public boolean setFocus() {
        return treeViewer.getControl().setFocus();
    }

    private void init(int treeStyle) {

        this.queryJob = new QueryJob();
        this.searchJob = new SearchJob();
        this.updateUIJob = new UpdateUIJob();
        this.searchDecorator = new SearchDecorator();

        createControl(parent, treeStyle);

        resourceSet.eAdapters().add(new EContentAdapter() {

            @Override
            public void notifyChanged(Notification notification) {
                super.notifyChanged(notification);

                if (notification.isTouch()) {
                    return;
                }

                updateAfterModelChange(notification);
            }
        });
    }

    protected void createControl(Composite parent, int treeStyle) {
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        setLayout(layout);
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        queryComposite = new Composite(this, SWT.NONE);
        createQueryControls(queryComposite);
        queryComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        treeComposite = new Composite(this, SWT.NONE);
        GridLayout treeCompositeLayout = new GridLayout();
        treeCompositeLayout.marginHeight = 0;
        treeCompositeLayout.marginWidth = 0;
        treeComposite.setLayout(treeCompositeLayout);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        treeComposite.setLayoutData(data);
        createTreeControl(treeComposite, treeStyle);

        treeViewer.addSelectionChangedListener(e -> {
            structuredSelection = treeViewer.getStructuredSelection();
        });
    }

    protected void createQueryControls(Composite parent) {
        controller = new QueryUIController(resourceSet, this);
        activeQuery = controller.getDefaultFilter();

        parent.setLayout(new FormLayout());
        parent.setFont(parent.getFont());

        Button btnOpenDialog = new Button(parent, SWT.NONE);
        btnOpenDialog.setText("...");
        btnOpenDialog.setToolTipText("Filter/Query");
        FormData fd_btnOpenDialog = new FormData();
        fd_btnOpenDialog.right = new FormAttachment(100, -10);
        btnOpenDialog.setLayoutData(fd_btnOpenDialog);
        btnOpenDialog.addListener(SWT.Selection, (e) -> controller.openQueryDialog());

        btnReapplyQuery = new Button(parent, SWT.NONE);
        btnReapplyQuery.setText("\u21BB");
        btnReapplyQuery.setToolTipText("Reapply Query");
        FormData fd_btnReapplyQuery = new FormData();
        fd_btnReapplyQuery.right = new FormAttachment(btnOpenDialog, -6);
        btnReapplyQuery.setLayoutData(fd_btnReapplyQuery);
        btnReapplyQuery.addListener(SWT.Selection, (e) -> updateQueryResults());
        btnReapplyQuery.setVisible(false);

        queryLabel = new Label(parent, SWT.NONE);
        FormData fd_queryLabel = new FormData();
        fd_queryLabel.top = new FormAttachment(0, 5);
        fd_queryLabel.left = new FormAttachment(0, 10);
        fd_queryLabel.right = new FormAttachment(btnReapplyQuery, -6);
        queryLabel.setLayoutData(fd_queryLabel);
        applyQuery(activeQuery, activeQuery.getName());
    }

    protected void createTreeControl(Composite parent, int style) {
        treeViewer = doCreateTreeViewer(parent, style);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        treeViewer.getControl().setLayoutData(data);

        treeViewer.addFilter(predicateFilter);
    }

    private TreeViewer doCreateTreeViewer(Composite parent, int style) {
        return new TreeViewer(parent, style);
    }

    public void applyQuery(PersistableQuery query, String description) {
        if (activeQuery.getMode() != query.getMode()) {
            queryModeChanged(activeQuery.getMode(), query.getMode());
        }
        activeQuery = query;
        FilterQueryHelper.notifyQueryChanged(query);
        updateQueryResults();
        queryLabel.setText(description);
    }

    public void setReapplyVisibility(boolean visible) {
        btnReapplyQuery.setVisible(visible);
    }

    private void queryModeChanged(QueryMode from, QueryMode to) {
        if ((from == QueryMode.FILTER || from == QueryMode.HIGHLIGHT) && to == QueryMode.LIST) {
            originalTreeInput = treeViewer.getInput();
        } else if (from == QueryMode.LIST && (to == QueryMode.FILTER || to == QueryMode.HIGHLIGHT)) {
            treeViewer.setInput(originalTreeInput);
        }
    }

    private void updateQueryResults() {
        queryJob.schedule();
    }

    private IStatus doUpdateQueryResults() {
        Collection<EObject> selection;
        if (structuredSelection == null || structuredSelection.isEmpty() || !(structuredSelection.getFirstElement() instanceof EObject)) {
            selection = findFirstModelElement();
        } else {
            selection = structuredSelection.toList();
        }

        try {
            queryResults = queryInterpreter.query(selection, activeQuery.getQuery());
        } catch (ParserException e) {
            // The user entered an invalid query. The editor will mark any mistakes.
            queryResults = new ArrayList<>();
        }
        explicitlyHidden = Sets.newHashSet(resourceSet.getAllContents());
        explicitlyHidden.removeAll(queryResults);

        updateUI(true);

        return Status.OK_STATUS;
    }

    public void updateSearchResults(String searchString, IntConsumer callback) {
        searchPredicate.setSearchString(searchString);
        searchJob.cancel();
        searchJob.setCallback(callback);
        searchJob.schedule(UPDATE_MATCHES_DELAY);
    }

    private IStatus doUpdateSearchResults(IProgressMonitor monitor, IntConsumer callback) {
        if (searchStringEmpty()) {
            searchResults = ImmutableList.of();
            treeViewer.collapseAll();
        } else {
            UnmodifiableIterator<Notifier> matchesIterator =
                    Iterators.filter(new AdapterFactoryTreeIterator<Notifier>(adapterFactory, resourceSet), searchPredicate);

            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }

            List<Notifier> newMatches = Lists.newArrayList(matchesIterator);

            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }

            searchResults = newMatches;
        }

        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }

        if (callback != null) {
            callback.accept(searchResults.size());
        }

        searchDecorator.updateMatches(searchResults, SWT.COLOR_LIST_SELECTION);

        return Status.OK_STATUS;
    }

    private boolean searchStringEmpty() {
        return searchPredicate.getSearchString().isEmpty();
    }

    private List<EObject> findFirstModelElement() {
        List<Resource> resources = resourceSet.getResources();
        if (isNullOrEmpty(resources)) {
            return Collections.emptyList();
        }

        EObject repository;
        try {
            repository = resources.get(0).getContents().get(0);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            return Collections.emptyList();
        }
        List<EObject> repositoryContents = repository.eContents();
        if (isNullOrEmpty(repositoryContents)) {
            return Collections.singletonList(repository);
        }

        EObject firstElement =
                repositoryContents.stream().filter(e -> (e instanceof AElement)).findFirst().orElse(repositoryContents.get(0));
        return Collections.singletonList(firstElement);
    }

    private void updateUI(boolean expandMatches) {
        if (activeQuery.getMode() == QueryMode.LIST) {
            expandMatches = false;
        }
        updateUIJob.cancel();
        updateUIJob.setExpandMatches(expandMatches);
        updateUIJob.schedule(UPDATE_UI_DELAY);
    }

    private IStatus doUpdateUI(boolean expandMatches, IProgressMonitor monitor) {
        if (treeViewer.getControl().isDisposed()) {
            return Status.CANCEL_STATUS;
        }

        try {
            setRedraw(false);
            QueryMode mode = activeQuery.getMode();

            if (mode != QueryMode.FILTER && mode != QueryMode.LIST) {
                collectionPredicate.setCollections(Collections.emptyList(), Collections.emptyList());
            }

            if (mode != QueryMode.HIGHLIGHT) {
                searchDecorator.updateMatches(Collections.emptyList());
            }

            switch (mode) {
            case FILTER:
                collectionPredicate.setCollections(queryResults, explicitlyHidden);
                break;
            case HIGHLIGHT:
                searchDecorator.updateMatches(Collections.unmodifiableCollection(queryResults));
                break;
            case LIST:
                treeViewer.setInput(new ItemProvider(queryResults));
                collectionPredicate.setCollections(queryResults, explicitlyHidden);
                break;
            }

            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }

            treeViewer.refresh();

            ISelection selection = treeViewer.getSelection();

            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }

            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            if (expandMatches && queryResults.size() > 0 && activeQuery.getMode() == QueryMode.HIGHLIGHT) {
                // For large number of items expanding every single one takes too long. Instead we just expand the whole tree all at once
                if (queryResults.size() < EXPAND_ALL_THRESHOLD) {
                    expandAll(Lists.newArrayList(queryResults));
                } else {
                    expandAll();
                }
            }
            treeViewer.setSelection(selection);

            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }

            return Status.OK_STATUS;
        } finally {
            setRedraw(true);
        }
    }

    private void updateAfterModelChange(Notification notification) {
        updateSearchResultsAfterModelChange(notification);
        updateQueryResultsAfterModelChange(notification);
    }

    private void updateSearchResultsAfterModelChange(Notification notification) {
        Object notifier = notification.getNotifier();
        switch (notification.getEventType()) {
        case Notification.ADD:
            if (searchPredicate.apply(notification.getNewValue()) && notifier instanceof Notifier) {
                searchResults.add((Notifier) notification.getNewValue());
            }
            break;
        case Notification.REMOVE:
            if (searchResults.contains(notification.getOldValue())) {
                searchResults.remove(notification.getOldValue());
            }
            break;
        case Notification.SET:
        case Notification.UNSET:
            if (searchResults.contains(notifier)) {
                if (!searchPredicate.apply(notifier)) {
                    searchResults.remove(notifier);
                    updateUI(false);
                }
            } else {
                if (searchPredicate.apply(notifier) && notifier instanceof Notifier) {
                    searchResults.add((Notifier) notifier);
                }
            }
        }
    }

    private void updateQueryResultsAfterModelChange(Notification notification) {
        switch (notification.getEventType()) {
        case Notification.REMOVE_MANY:
            boolean removeManyChangedQueryResults = false;
            for (Object removed : (List<?>) notification.getOldValue()) {
                removeManyChangedQueryResults |= removeFromQueryResultWithChildren((EObject) removed);
            }
            if (removeManyChangedQueryResults) {
                updateUI(false);
            }
            break;
        case Notification.REMOVE:
            boolean removeChangedQueryResults = removeFromQueryResultWithChildren((EObject) notification.getOldValue());
            if (removeChangedQueryResults) {
                updateUI(false);
            }
            break;
        case Notification.ADD_MANY:
            for (Object added : (List<?>) notification.getNewValue()) {
                if (added instanceof IPremiseObject) {
                    queryResults.add((IPremiseObject) added);
                }
            }
            if (activeQuery.getMode() == QueryMode.FILTER) {
                updateQueryResults();
            }
            break;
        case Notification.ADD:
        case Notification.SET:
            if (notification.getNewValue() instanceof IPremiseObject) {
                queryResults.add((EObject) notification.getNewValue());

            }
            if (notification.getNewValue() != null && activeQuery.getMode() == QueryMode.FILTER) {
                updateQueryResults();
            }
            break;
        case Notification.UNSET:
        case Notification.MOVE:
            if (activeQuery.getMode() == QueryMode.FILTER) {
                updateQueryResults();
            }
            break;
        }
    }

    private boolean removeFromQueryResultWithChildren(EObject toRemove) {
        boolean queryResultsChanged = false;
        for (Iterator<EObject> iter = PremiseHelper.eAllContentsIncludingRoot(toRemove); iter.hasNext();) {
            EObject element = iter.next();
            queryResultsChanged |= queryResults.remove(element);
        }
        return queryResultsChanged;
    }

    private void expandAll(List<Notifier> objects) {
        for (Object obj : objects) {
            treeViewer.expandToLevel(obj, 0);
        }
    }

    private void expandAll() {
        treeViewer.expandAll();
    }

    private int getCurrentSelectedIndex() {
        ITreeSelection selection = (ITreeSelection) treeViewer.getSelection();

        if (selection.size() == 1) {
            return Lists.newArrayList(searchResults).indexOf(selection.getFirstElement());
        }

        return -1;
    }

    public void selectFirst() {
        if (searchResults.isEmpty()) {
            return;
        }

        selectAtIndex(0);
    }

    public void selectNext() {
        if (searchResults.isEmpty()) {
            return;
        }

        int currentSelectedIndex = getCurrentSelectedIndex();
        int nextIndex = (currentSelectedIndex == -1 ? 1 : currentSelectedIndex + 1) % searchResults.size();

        selectAtIndex(nextIndex);
    }

    public void selectPrevious() {
        if (searchResults.isEmpty()) {
            return;
        }

        int currentSelectedIndex = getCurrentSelectedIndex();
        int previousIndex = currentSelectedIndex == -1 ? searchResults.size() - 1 : currentSelectedIndex - 1;
        if (previousIndex == -1) {
            previousIndex = searchResults.size() - 1;
        }

        selectAtIndex(previousIndex);
    }

    private void selectAtIndex(int index) {
        Notifier next = Lists.newArrayList(searchResults).get(index);
        treeViewer.setSelection(new StructuredSelection(next));
    }

    @Override
    public void dispose() {
        queryJob.interrupt();
        super.dispose();
    }

    private final class SearchJob extends WorkbenchJob {

        private IntConsumer callback;

        private SearchJob() {
            super("Searching model");
            setSystem(true);
        }

        public void setCallback(IntConsumer callback) {
            this.callback = callback;
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            return doUpdateSearchResults(monitor, callback);
        }
    }

    private final class QueryJob extends Thread {

        private BlockingQueue<Object> queue = new ArrayBlockingQueue<>(1);

        private QueryJob() {
            super("Querying model");
            start();
        }

        public void schedule() {
            queue.offer(new Object());
        }

        @Override
        public void run() {
            while (true) {
                try {
                    queue.take();
                    doUpdateQueryResults();
                } catch (InterruptedException e) {
                    return;
                } catch (ConcurrentModificationException e) {
                    queue.offer(new Object()); // retry
                }
            }
        }
    }

    private final class UpdateUIJob extends WorkbenchJob {

        private boolean expandMatches;

        private UpdateUIJob() {
            super("Update query results");
            setSystem(true);
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            return doUpdateUI(expandMatches, monitor);
        }

        public void setExpandMatches(boolean expandMatches) {
            this.expandMatches = expandMatches;
        }
    }
}
