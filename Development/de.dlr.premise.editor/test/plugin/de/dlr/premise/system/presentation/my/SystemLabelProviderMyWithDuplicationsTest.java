/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.system.presentation.my;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.element.provider.my.ElementItemProviderAdapterFactoryMy;
import de.dlr.premise.provider.util.PremiseAdapterFactoryHelper;
import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.presentation.my.AdapterFactoryLabelProviderMy;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.TestHelper;

public class SystemLabelProviderMyWithDuplicationsTest {

    private static String PATH_INPUT_FILE = de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_EDITOR, "test/data/LabelTestCases.premise").getPath();
    private static AdapterFactory itemAdapterFactory;
    private static AdapterFactoryLabelProvider labelAdapterFactory;

    private static ProjectRepository rep;
    private static SystemComponent sc1;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // load model from .premise file
        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);
        
        // initialize top components
        rep = (ProjectRepository) resource.getContents().get(0);
        sc1 = rep.getProjects().get(0).getReferencedChildren().get(1);
        // initialize upper level test
        SystemLabelProviderMyTest.initialize(rep);

        // copy sc1 and append it to other namespace
        EcoreUtil.Copier cp = new EcoreUtil.Copier();
        SystemComponent sc1copy = (SystemComponent) cp.copy(sc1);
        cp.copyReferences();
        // check copy quickly
        assertTrue(sc1copy.getParameters().get(0) != sc1.getParameters().get(0));
        assertEquals(8, sc1copy.getBalancings().size());
        assertEquals("P1", sc1copy.getBalancings().get(4).getTarget().getName());
        assertEquals(6, PremiseHelper.getTransitions(sc1copy).size());
        assertEquals("M1 \\nname", PremiseHelper.getTransitions(sc1copy).get(4).getSource().getName());
        assertEquals(7, sc1copy.getRelations().size());
        assertEquals("SC2 \\nname", ((ANameItem) sc1copy.getRelations().get(4).getTarget()).getName());
        // append copy of sc1 to repository namespace B
        rep.getProjects().get(1).getChildren().add(sc1copy);
        assertEquals("SC1 \\nname", rep.getProjects().get(1).getReferencedChildren().get(2).getName());
    }

    @Test
    public void testSystemComponent() {
        SystemLabelProviderMyTest.testSystemComponentCode();
    }

    @Test
    public void testNote() {
        SystemLabelProviderMyTest.testNoteCode();
    }

    @Test
    public void testParameter() {
        SystemLabelProviderMyTest.testParameterCode();
    }

    @Test
    public void testBalancing() {
        SystemLabelProviderMyTest.testBalancingCode("");
    }

    @Test
    public void testMetaData() {
        SystemLabelProviderMyTest.testMetaDataCode("");
    }

    @Test
    public void testConnection() {
        // initialize validated adapter factory (to allow calling getText())
        itemAdapterFactory = new ElementItemProviderAdapterFactoryMy();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals("", labelAdapterFactory.getText(sc1.getConnections().get(0)));
        assertEquals("", labelAdapterFactory.getText(sc1.getConnections().get(1)));
        assertEquals("{Namespace A.SC1 name} > In1 name {Namespace A.SC1 name}", labelAdapterFactory.getText(sc1.getConnections().get(2)));
        assertEquals("Out1 name {Namespace A.SC1 name} > In1 name {Namespace A.SC1 name}", labelAdapterFactory.getText(sc1.getConnections().get(3)));
    }

    @Test
    public void testMode() {
        SystemLabelProviderMyTest.testModeCode();
    }

    @Test
    public void testTransition() {
        SystemLabelProviderMyTest.testTransitionCode("");
    }

    @Test
    public void testRelation() {
        // initialize premise adapter factory (to allow calling getText())
        itemAdapterFactory = PremiseAdapterFactoryHelper.createComposedAdapterFactory();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        // SC2 has no qualified name because its name is unique in repository!
        assertEquals("SC1 name {Namespace A}", labelAdapterFactory.getText(sc1.getRelations().get(0)));
        assertEquals("SC1 name {Namespace A}", labelAdapterFactory.getText(sc1.getRelations().get(1)));
        assertEquals("SC1 name {Namespace A}", labelAdapterFactory.getText(sc1.getRelations().get(2)));
        assertEquals("SC1 name {Namespace A} > SC2 name", labelAdapterFactory.getText(sc1.getRelations().get(3)));
        assertEquals("SC1 name {Namespace A} > SC2 name", labelAdapterFactory.getText(sc1.getRelations().get(4)));
        assertEquals("SC1 name {Namespace A} > SC2 name", labelAdapterFactory.getText(sc1.getRelations().get(5)));
    }
}
