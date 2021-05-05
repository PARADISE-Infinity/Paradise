/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.presentation.my.query;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.ui.PlatformUI;

import de.dlr.premise.query.persistent.PersistableQuery;
import de.dlr.premise.system.presentation.my.SystemEditorMy;

public abstract class FilterQueryHelper {

    private static volatile boolean listening = true;
    private static final Set<Consumer<PersistableQuery>> consumers = new HashSet<>();
    private static PersistableQuery previousQuery;

    /**
     * Temporarily overwrite the query in the editor. Can later be restored with {@link #restorePreviousQuery()}.
     * 
     * @param query
     * @param description
     */
    public static synchronized void setQuery(PersistableQuery query, String description) {
        QueryableTree tree = getTree();
        if (tree == null)
            return;
        if (previousQuery == null) {
            previousQuery = tree.getActiveQuery();
        }
        listening = false;
        tree.applyQuery(query, description);
        listening = true;
    }

    private static QueryableTree getTree() {
        Object editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editor instanceof SystemEditorMy) {
            QueryableTree tree = ((SystemEditorMy) editor).getTree();
            return tree;
        }
        return null;
    }

    /**
     * Restore the query or filter that was active before any queries were set with {@link #setQuery()}.
     */
    public static void restorePreviousQuery() {
        if (previousQuery != null) {
            setQuery(previousQuery, previousQuery.getName());
            previousQuery = null;
        }
    }

    /**
     * Add a consumer to be notified when the Query is changed externally.
     * 
     * @param queryConsumer
     */
    public static void registerQueryChangeHandler(Consumer<PersistableQuery> queryConsumer) {
        consumers.add(queryConsumer);
    }

    /**
     * Remove a previously added consumer.
     * 
     * @param queryConsumer
     */
    public static void unregisterQueryChangeHandler(Consumer<PersistableQuery> queryConsumer) {
        consumers.remove(queryConsumer);
    }

    static void notifyQueryChanged(PersistableQuery query) {
        if (listening) {
            previousQuery = null;
            consumers.stream().forEach(c -> c.accept(query));
        }
    }
}
