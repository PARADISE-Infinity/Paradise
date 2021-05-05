/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.element.provider.my;

import static org.junit.Assert.assertNotNull;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.junit.Test;

import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.system.provider.my.SystemImageProviderMyTest;

public class ElementImageProviderMyTest {

    @Test
    public void testMode() {
        // TODO move to a Element model test
        EObject item = ElementFactory.eINSTANCE.createMode();
        ItemProviderAdapter itemAdapter = new ModeItemProviderMy(null);
        String menuIconPathHead = "CreateAElement_modes_";
        long fileSize = 609;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testTransition() {
        EObject item = ElementFactory.eINSTANCE.createTransition();
        ItemProviderAdapter itemAdapter = new TransitionItemProviderMy(null);
        String menuIconPathHead = "CreateSystemComponent_transitions_";
        long fileSize = 488;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testGuardCombination() {
        EObject item = ElementFactory.eINSTANCE.createGuardCombination();
        ItemProviderAdapter itemAdapter = new GuardCombinationItemProviderMy(null);
        String menuIconPathHead = "CreateTransition_condition_";
        long fileSize = 559;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testModeGuard() {
        EObject item = ElementFactory.eINSTANCE.createModeGuard();
        ItemProviderAdapter itemAdapter = new ModeGuardItemProviderMy(null);
        String menuIconPathHead = "CreateTransition_condition_";
        long fileSize = 331;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testRelation() {
        EObject item = ElementFactory.eINSTANCE.createRelation();
        ItemProviderAdapter itemAdapter = new RelationItemProviderMy(null);
        String menuIconPathHead = "CreateSystemComponent_relations_";
        long fileSize = 138;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }
}
