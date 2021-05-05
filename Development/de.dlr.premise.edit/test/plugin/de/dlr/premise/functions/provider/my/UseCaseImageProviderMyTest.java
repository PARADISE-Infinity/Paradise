/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.functions.provider.my;

import static org.junit.Assert.assertNotNull;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.junit.Test;

import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.element.provider.my.RelationItemProviderMy;
import de.dlr.premise.functions.UseCaseFactory;
import de.dlr.premise.functions.provider.RequiredParameterItemProvider;
import de.dlr.premise.functions.provider.my.ModeRangeConstraintItemProviderMy;
import de.dlr.premise.functions.provider.my.RangeConstraintItemProviderMy;
import de.dlr.premise.functions.provider.my.UseCaseItemProviderMy;
import de.dlr.premise.system.provider.my.SystemImageProviderMyTest;


public class UseCaseImageProviderMyTest {

    @Test
    public void testUseCaseRepository() {
        EObject item = UseCaseFactory.eINSTANCE.createUseCaseRepository();
        ItemProviderAdapter itemAdapter = new ModeRangeConstraintItemProviderMy(null);
        String menuIconPathHead = null;
        long fileSize = 646;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testModeRangeConstraint() {
        EObject item = UseCaseFactory.eINSTANCE.createModeRangeConstraint();
        ItemProviderAdapter itemAdapter = new ModeRangeConstraintItemProviderMy(null);
        String menuIconPathHead = "CreateRequiredParameter_modeValueConstraints_";
        long fileSize = 329;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testRangeConstraint() {
        EObject item = UseCaseFactory.eINSTANCE.createRangeConstraint();
        ItemProviderAdapter itemAdapter = new RangeConstraintItemProviderMy(null);
        String menuIconPathHead = "CreateRequiredParameter_ValueConstraint_";
        long fileSize = 329;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testUseCase() {
        EObject item = UseCaseFactory.eINSTANCE.createUseCase();
        ItemProviderAdapter itemAdapter = new UseCaseItemProviderMy(null);
        String menuIconPathHead = "CreateUseCase_children_";
        long fileSize = 1045;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);

        // test again with child of Repository
        menuIconPathHead = "CreateUseCaseRepository_usecases_";
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testParameterConstraint() {
        EObject item = UseCaseFactory.eINSTANCE.createRequiredParameter();
        // Notice: not ParameterConstraintItemProvider-MY
        ItemProviderAdapter itemAdapter = new RequiredParameterItemProvider(null);
        String menuIconPathHead = "CreateUseCase_requiredParameters_";
        long fileSize = 736;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testRelation() {
        EObject item = ElementFactory.eINSTANCE.createRelation();
        ItemProviderAdapter itemAdapter = new RelationItemProviderMy(null);
        String menuIconPathHead = "CreateUseCase_relations_";
        long fileSize = 138;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }
}
