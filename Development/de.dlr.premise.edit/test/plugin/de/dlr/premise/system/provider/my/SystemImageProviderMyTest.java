/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.system.provider.my;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.junit.Test;

import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.element.provider.my.ConnectionItemProviderMy;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.system.provider.ProjectRepositoryItemProvider;
import de.dlr.premise.util.TestHelper;

public class SystemImageProviderMyTest {

    private static String FILE_SUFFIX = ".gif";

    public static void testIcon(EObject item, ItemProviderAdapter itemAdapter, String menuIconPathHead, long fileSize) {

        String iconPath;
        if (menuIconPathHead != null) {
            iconPath =
                    TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_EDIT,
                                          "icons/full/ctool16/" + menuIconPathHead + item.eClass().getName() + FILE_SUFFIX).getPath();
            assertEquals(fileSize, new File(iconPath).length());
        }

        iconPath = TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_EDIT, "icons/full/obj16/" + item.eClass().getName() + FILE_SUFFIX).getPath();
        assertEquals(fileSize, new File(iconPath).length());
    }

    @Test
    public void testProjectRepository() {
        EObject item = SystemFactory.eINSTANCE.createProjectRepository();
        ItemProviderAdapter itemAdapter = new ProjectRepositoryItemProvider(null);
        String menuIconPathHead = null;
        long fileSize = 648;

        assertNotNull(itemAdapter);
        testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testSystemComponent() {
        EObject item = SystemFactory.eINSTANCE.createSystemComponent();
        ItemProviderAdapter itemAdapter = new SystemComponentItemProviderMy(null);
        String menuIconPathHead = "CreateSystemComponent_children_";
        long fileSize = 597;

        assertNotNull(itemAdapter);
        testIcon(item, itemAdapter, menuIconPathHead, fileSize);

        // test again with child of Repositories
        menuIconPathHead = "CreateProjectRepository_projects_";
        testIcon(item, itemAdapter, menuIconPathHead, fileSize);
        menuIconPathHead = "CreateComponentRepository_components_";
        testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testParameter() {
        EObject item = SystemFactory.eINSTANCE.createParameter();
        ItemProviderAdapter itemAdapter = new ParameterItemProviderMy(null);
        String menuIconPathHead = "CreateSystemComponent_parameters_";
        long fileSize = 587;

        assertNotNull(itemAdapter);
        testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testBalancing() {
        EObject item = SystemFactory.eINSTANCE.createBalancing();
        ItemProviderAdapter itemAdapter = new BalancingItemProviderMy(null);
        String menuIconPathHead = "CreateSystemComponent_balancings_";
        long fileSize = 368;

        assertNotNull(itemAdapter);
        testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testModeValueRef() {
        EObject item = SystemFactory.eINSTANCE.createModeValueRef();
        ItemProviderAdapter itemAdapter = new ModeValueRefItemProviderMy(null);
        String menuIconPathHead = "CreateParameter_modeValues_";
        long fileSize = 574;

        assertNotNull(itemAdapter);
        testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testConnection() {
        EObject item = ElementFactory.eINSTANCE.createConnection();
        ItemProviderAdapter itemAdapter = new ConnectionItemProviderMy(null);
        String menuIconPathHead = "CreateSystemComponent_connections_";
        long fileSize = 138;

        assertNotNull(itemAdapter);
        testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testTransitionParameter() {
        EObject item = SystemFactory.eINSTANCE.createTransitionParameter();
        ItemProviderAdapter itemAdapter = new TransitionParameterItemProviderMy(null);
        String menuIconPathHead = "CreateTransition_parameters_";
        long fileSize = 587;

        assertNotNull(itemAdapter);
        testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testTransitionBalancing() {
        EObject item = SystemFactory.eINSTANCE.createTransitionBalancing();
        ItemProviderAdapter itemAdapter = new TransitionBalancingItemProviderMy(null);
        String menuIconPathHead = "CreateTransition_balancings_";
        long fileSize = 368;

        assertNotNull(itemAdapter);
        testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

}
