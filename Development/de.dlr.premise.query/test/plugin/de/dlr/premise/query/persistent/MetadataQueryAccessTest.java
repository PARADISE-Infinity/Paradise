/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.query.persistent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;

import java.util.Set;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.query.QueryMode;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.util.TestHelper;

public class MetadataQueryAccessTest {

    private static String PATH_INPUT_FILE = TestHelper.locateFile("de.dlr.premise.query", "test/data/PersistedQueries.premise").getPath();

    private static ResourceSet resSet;
    private static PersistentQueryAccess queryAccess;

    private static CommandStack commandStack;

    @BeforeClass
    public static void setUpBeforeClass() {
        AdapterFactory adapterFactory = new AdapterFactoryImpl();
        commandStack = new BasicCommandStack();
        EditingDomain editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack);
        resSet = editingDomain.getResourceSet();
        queryAccess = PersistentQueryAccessFactory.createPersistentQueryAccess(resSet);
    }

    @Before
    public void setUpBefore() {
        TestHelper.loadResource(resSet, PATH_INPUT_FILE);
    }

    @After
    public void cleanUpAfter() {
        resSet.getResources().clear();
    }

    @Test
    public void shouldReadQueries() {
        Set<PersistableQuery> queries = queryAccess.readQueries();

        assertThat(queries.size(), is(3));
        assertThat(queries, hasItem(new PersistableQuery("filtering", "filteringQuery", QueryMode.FILTER)));
        assertThat(queries, hasItem(new PersistableQuery("highlighting", "highlightingQuery", QueryMode.HIGHLIGHT)));
        assertThat(queries, hasItem(new PersistableQuery("listing", "listingQuery", QueryMode.LIST)));
    }

    @Test
    public void shouldCreateAQuery() {
        ((ProjectRepository) resSet.getResources().get(0).getContents().get(0)).getMetaData().clear();
        PersistableQuery query = new PersistableQuery("AddTest", "test.query()", QueryMode.LIST);
        queryAccess.addQuery(query);

        assertThat(queryAccess.readQueries(), hasItem(query));

        commandStack.undo();
        assertThat(queryAccess.readQueries(), not(hasItem(query)));
    }

    @Test
    public void shouldUpdateAQuery() {
        PersistableQuery newQuery = new PersistableQuery("filtering", "newQuery", QueryMode.HIGHLIGHT);
        PersistableQuery oldQuery = new PersistableQuery("filtering", "filteringQuery", QueryMode.FILTER);
        queryAccess.updateQuery(newQuery);

        assertThat(queryAccess.readQueries(), hasItem(newQuery));
        assertThat(queryAccess.readQueries(), not(hasItem(oldQuery)));

        commandStack.undo();
        assertThat(queryAccess.readQueries(), not(hasItem(newQuery)));
        assertThat(queryAccess.readQueries(), hasItem(oldQuery));
    }

    @Test
    public void shouldRemoveAQuery() {
        PersistableQuery query = new PersistableQuery("listing", "listingQuery", QueryMode.LIST);
        assertThat(queryAccess.readQueries(), hasItem(query));

        queryAccess.removeQuery(query);
        assertThat(queryAccess.readQueries(), not(hasItem(query)));

        commandStack.undo();
        assertThat(queryAccess.readQueries(), hasItem(query));
    }
}
