/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.prefs.Preferences;

import de.dlr.premise.query.QueryMode;
import de.dlr.premise.query.persistent.PersistableQuery;
import de.dlr.premise.query.persistent.PersistentQueryAccess;
import de.dlr.premise.query.persistent.PersistentQueryAccessFactory;

public class QueryUIController {

    public static final String SELECT_EVERYTHING_QUERY = "registry::IPremiseObject.allInstances()";
    public static final String FILTER_QUERY_NAME = "Simple filter";

    private static final String selectQueryFragment = "->reject(oclIsKindOf(";
    private static final String filterPrefKey = "filter";
    private static final String filterDescriptionPrefKey = "filterDescription";
    private static final Preferences prefs = InstanceScope.INSTANCE.getNode("de.dlr.premise.editor");

    private final PersistentQueryAccess queryAccess;
    private final QueryableTree view;
    private QueryDialog queryDialog;
    private boolean advanced = false;
    private Map<String, PersistableQuery> availableQueries = new HashMap<>();

    public QueryUIController(ResourceSet resourceSet, QueryableTree view) {
        queryAccess = PersistentQueryAccessFactory.createPersistentQueryAccess(resourceSet);
        this.view = view;
    }

    public void onSelectedQueryChanged(PersistableQuery query) {
        queryDialog.notifyQueryChanged(query);
    }

    public void toggleAdvancedMode(PersistableQuery activeQuery) {
        advanced = !advanced;
        queryDialog.setActiveDialog(advanced);
        if (!advanced && !activeQuery.equals(availableQueries.get(FILTER_QUERY_NAME))) {
            onSelectedQueryChanged(getDefaultFilter());
            applyQuery(getDefaultFilter());
        }
    }

    public void onFilterBoxChanged(List<Class<? extends EObject>> unselectedTypes, List<String> unselectedNames) {
        String queryText = SELECT_EVERYTHING_QUERY;
        if (!unselectedTypes.isEmpty()) {
            queryText += unselectedTypes.stream().map(this::toOclType)
                                        .collect(Collectors.joining("))" + selectQueryFragment, selectQueryFragment, "))"));
        }

        PersistableQuery newQuery = new PersistableQuery(namesToDescription(unselectedNames), queryText, QueryMode.FILTER);
        availableQueries.put(FILTER_QUERY_NAME, newQuery);
        prefs.put(filterPrefKey, newQuery.getQuery());
        prefs.put(filterDescriptionPrefKey, newQuery.getName());
        onSelectedQueryChanged(newQuery);

        applyQuery(newQuery);
    }

    private String toOclType(Class<? extends EObject> clazz) {
        String packagePrefix = clazz.getPackage().getName().replaceFirst(".*\\.", "");
        return packagePrefix + "::" + clazz.getSimpleName();
    }

    private String namesToDescription(List<String> unselectedNames) {
        if (unselectedNames.isEmpty()) {
            return "Unfiltered";
        }
        return unselectedNames.stream().collect(Collectors.joining(", ", "Hiding ", ""));
    }

    public void applyQuery(PersistableQuery activeQuery) {
        String prefix = advanced ? "Query: " : "";
        String description = prefix + activeQuery.getName();
        view.applyQuery(activeQuery, description);
        view.setReapplyVisibility(advanced);
    }

    public void openQueryDialog() {
        availableQueries.putAll(queryAccess.readQueries().stream()
                                           .collect(Collectors.toMap(PersistableQuery::getName, Function.identity())));
        availableQueries.putIfAbsent(FILTER_QUERY_NAME, getDefaultFilter());

        Display display = Display.getDefault();
        queryDialog = new QueryDialog(display, this);
        queryDialog.open();
        queryDialog.setAvailableQueries(availableQueries);
        queryDialog.notifyQueryChanged(view.getActiveQuery());
        queryDialog.setActiveDialog(advanced);
    }

    public void closeQueryDialog() {
        queryDialog.close();
    }

    public void openSaveAsDialog(PersistableQuery activeQuery) {
        InputDialog saveDialog = new InputDialog(Display.getCurrent().getActiveShell(), "Save query as", "Name:", activeQuery.getName(),
                                                 s -> s.isEmpty() ? "The name can't be empty" : null);
        saveDialog.open();
        if (saveDialog.getReturnCode() == InputDialog.OK) {
            String queryName = saveDialog.getValue();
            PersistableQuery toBeSaved = activeQuery.assocName(queryName);
            if (queryAccess.readQueries().stream().map(PersistableQuery::getName).anyMatch(name -> name.equals(queryName))) {
                queryAccess.updateQuery(toBeSaved);
            } else {
                queryAccess.addQuery(toBeSaved);
            }
        }
    }

    public PersistableQuery getDefaultFilter() {
        String query = prefs.get(filterPrefKey, SELECT_EVERYTHING_QUERY);
        String description = prefs.get(filterDescriptionPrefKey, "Unfiltered");
        return new PersistableQuery(description, query, QueryMode.FILTER);
    }

}
