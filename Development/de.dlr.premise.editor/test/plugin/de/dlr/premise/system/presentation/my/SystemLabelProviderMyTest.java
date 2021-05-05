/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.system.presentation.my;

import static org.junit.Assert.assertEquals;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;
import de.dlr.premise.element.provider.my.ElementItemProviderAdapterFactoryMy;
import de.dlr.premise.provider.util.PremiseAdapterFactoryHelper;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.presentation.my.AdapterFactoryLabelProviderMy;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.TestHelper;

public class SystemLabelProviderMyTest {

    private static String PATH_INPUT_FILE = de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_EDITOR, "test/data/LabelTestCases.premise").getPath();
    private static AdapterFactory itemAdapterFactory;
    private static AdapterFactoryLabelProvider labelAdapterFactory;

    private static ProjectRepository rep;
    private static SystemComponent sc0;
    private static SystemComponent sc1;
    private static SystemComponent sc00;
    private static SystemComponent sc2;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // load model from .premise file
        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);

        // initialize top components
        initialize((ProjectRepository) resource.getContents().get(0));
    }

    /**
     * Initializes top components used by tests
     */
    public static void initialize(ProjectRepository repository) {
        rep = repository;
        sc0 = rep.getProjects().get(0).getReferencedChildren().get(0);
        sc1 = rep.getProjects().get(0).getReferencedChildren().get(1);
        sc00 = rep.getProjects().get(1).getReferencedChildren().get(0);
        sc2 = rep.getProjects().get(1).getReferencedChildren().get(1);
    }

    @Test
    public void testSystemComponent() {
        testSystemComponentCode();
    }

    public static void testSystemComponentCode() {
        // initialize premise adapter factory (to allow calling getText())
        itemAdapterFactory = new SystemItemProviderAdapterFactoryMy();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals("", labelAdapterFactory.getText(sc0));
        assertEquals("SC1 name", labelAdapterFactory.getText(sc1));
        assertEquals("", labelAdapterFactory.getText(sc00));
        assertEquals("SC2 name", labelAdapterFactory.getText(sc2));
    }

    @Test
    public void testNote() {
        testNoteCode();
    }

    public static void testNoteCode() {
        // no adaption of label
    }

    @Test
    public void testParameter() {
        testParameterCode();
    }

    public static void testParameterCode() {
        // initialize validated adapter factory (to allow calling getText())
        itemAdapterFactory = new SystemItemProviderAdapterFactoryMy();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals("", labelAdapterFactory.getText(sc1.getParameters().get(0)));
        assertEquals("P1", labelAdapterFactory.getText(sc1.getParameters().get(1)));
        assertEquals("P2 name", labelAdapterFactory.getText(sc1.getParameters().get(2)));
    }

    @Test
    public void testBalancing() {
        testBalancingCode("");
    }

    public static void testBalancingCode(String prefix) {
        // initialize validated adapter factory (to allow calling getText())
        itemAdapterFactory = new SystemItemProviderAdapterFactoryMy();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals(prefix + "= <unknown function>()", labelAdapterFactory.getText(sc1.getBalancings().get(0)));
        assertEquals(prefix + "Bal1 name", labelAdapterFactory.getText(sc1.getBalancings().get(1)));
        assertEquals(prefix + "= Fn1()", labelAdapterFactory.getText(sc1.getBalancings().get(2)));
        assertEquals(prefix + "Bal2 name", labelAdapterFactory.getText(sc1.getBalancings().get(3)));
        assertEquals(prefix + "P1 = Fn1()", labelAdapterFactory.getText(sc1.getBalancings().get(4)));
        assertEquals(prefix + "Bal3 name > P1", labelAdapterFactory.getText(sc1.getBalancings().get(5)));
        assertEquals(prefix + "P1 = Fn1(P2__nname)", labelAdapterFactory.getText(sc1.getBalancings().get(6)));
        assertEquals(prefix + "P1 = <unknown function>()", labelAdapterFactory.getText(sc1.getBalancings().get(7)));
    }

    @Test
    public void testMetaData() {
        testMetaDataCode("");
    }

    public static void testMetaDataCode(String prefix) {
        // initialize registry adapter factory (to allow calling getText())
        itemAdapterFactory = new RegistryItemProviderAdapterFactoryMy();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals(prefix, labelAdapterFactory.getText(sc1.getMetaData().get(0)));
        assertEquals(prefix + "test \\nname", labelAdapterFactory.getText(sc1.getMetaData().get(1)));
        assertEquals(prefix + " = test \\nvalue", labelAdapterFactory.getText(sc1.getMetaData().get(2)));
        assertEquals(prefix + "test \\nname = test \\nvalue", labelAdapterFactory.getText(sc1.getMetaData().get(3)));
    }

    @Test
    public void testConnection() {
        testConnectionCode();
    }

    public static void testConnectionCode() {
        // initialize validated adapter factory (to allow calling getText())
        itemAdapterFactory = new ElementItemProviderAdapterFactoryMy();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals("", labelAdapterFactory.getText(sc1.getConnections().get(0)));
        assertEquals("", labelAdapterFactory.getText(sc1.getConnections().get(1)));
        assertEquals("{SC1 name} > In1 name {SC1 name}", labelAdapterFactory.getText(sc1.getConnections().get(2)));
        assertEquals("Out1 name {SC1 name} > In1 name {SC1 name}", labelAdapterFactory.getText(sc1.getConnections().get(3)));
    }

    @Test
    public void testMode() {
        testModeCode();
    }

    public static void testModeCode() {
        // initialize validated adapter factory (to allow calling getText())
        itemAdapterFactory = new ElementItemProviderAdapterFactoryMy();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals("", labelAdapterFactory.getText(PremiseHelper.getModes(sc1).get(0)));
        assertEquals("M1 name", labelAdapterFactory.getText(PremiseHelper.getModes(sc1).get(1)));
        assertEquals("M2 name", labelAdapterFactory.getText(PremiseHelper.getModes(sc1).get(2)));
    }

    @Test
    public void testTransition() {
        testTransitionCode("");
    }

    public static void testTransitionCode(String prefix) {
        // initialize validated adapter factory (to allow calling getText())
        itemAdapterFactory = new ElementItemProviderAdapterFactoryMy();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals(prefix, labelAdapterFactory.getText( sc1.getStatemachines().get(0).getTransitions().get(0) ));
        assertEquals(prefix + "test name", labelAdapterFactory.getText(PremiseHelper.getTransitions(sc1).get(1)));
        assertEquals(prefix + "M1 name", labelAdapterFactory.getText(PremiseHelper.getTransitions(sc1).get(2)));
        assertEquals(prefix + " > M2 name", labelAdapterFactory.getText(PremiseHelper.getTransitions(sc1).get(3)));
        assertEquals(prefix + "M1 name > M2 name", labelAdapterFactory.getText(PremiseHelper.getTransitions(sc1).get(4)));
        assertEquals(prefix + "test name", labelAdapterFactory.getText(PremiseHelper.getTransitions(sc1).get(5)));
    }

    @Test
    public void testRelation() {
        testRelationCode();
    }

    public static void testRelationCode() {
        // initialize premise adapter factory (to allow calling getText())
        itemAdapterFactory = PremiseAdapterFactoryHelper.createComposedAdapterFactory();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals("SC1 name", labelAdapterFactory.getText(sc1.getRelations().get(0)));
        assertEquals("SC1 name", labelAdapterFactory.getText(sc1.getRelations().get(1)));
        assertEquals("SC1 name", labelAdapterFactory.getText(sc1.getRelations().get(2)));
        assertEquals("SC1 name > SC2 name", labelAdapterFactory.getText(sc1.getRelations().get(3)));
        assertEquals("SC1 name > SC2 name", labelAdapterFactory.getText(sc1.getRelations().get(4)));
        assertEquals("SC1 name > SC2 name", labelAdapterFactory.getText(sc1.getRelations().get(5)));
    }
}
