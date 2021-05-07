/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.registry.provider.my;

import static org.junit.Assert.assertNotNull;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.junit.Test;

import de.dlr.premise.system.provider.my.SystemImageProviderMyTest;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.registry.provider.NoteItemProvider;
import de.dlr.premise.registry.provider.my.ConstantItemProviderMy;
import de.dlr.premise.registry.provider.my.MetaDataItemProviderMy;
import de.dlr.premise.registry.provider.my.MetaTypeDefItemProviderMy;
import de.dlr.premise.registry.provider.my.ValueItemProviderMy;

public class RegistryImageProviderMyTest {

    @Test
    public void testNote() {
        EObject item = RegistryFactory.eINSTANCE.createNote();
        ItemProviderAdapter itemAdapter = new NoteItemProvider(null);
        String menuIconPathHead = "CreateADataItem_notes_";
        long fileSize = 361;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testMetaData() {
        EObject item = RegistryFactory.eINSTANCE.createMetaData();
        ItemProviderAdapter itemAdapter = new MetaDataItemProviderMy(null);
        String menuIconPathHead = "CreateAElement_metaData_";
        long fileSize = 597;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);

        // test again with children of MetaData
        menuIconPathHead = "CreateMetaData_children_";
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

    @Test
    public void testMetaType() {
        EObject item = RegistryFactory.eINSTANCE.createMetaTypeDef();
        ItemProviderAdapter itemAdapter = new MetaTypeDefItemProviderMy(null);
        String menuIconPathHead = "CreateRegistry_metaTypes_";
        long fileSize = 597;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }

	@Test
	public void testConstant() {
	    EObject item = RegistryFactory.eINSTANCE.createConstant();
        ItemProviderAdapter itemAdapter = new ConstantItemProviderMy(null);
        String menuIconPathHead = "CreateRegistry_constants_";
        long fileSize = 387;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
	}

    @Test
    public void testValue() {
        EObject item = RegistryFactory.eINSTANCE.createValue();
        ItemProviderAdapter itemAdapter = new ValueItemProviderMy(null);
        String menuIconPathHead = "CreateAParameterDef_value_";
        long fileSize = 607;

        assertNotNull(itemAdapter);
        SystemImageProviderMyTest.testIcon(item, itemAdapter, menuIconPathHead, fileSize);
    }
}
