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
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.element.provider.my.ConnectionItemProviderMy;
import de.dlr.premise.element.provider.my.ModeItemProviderMy;
import de.dlr.premise.element.provider.my.RelationItemProviderMy;
import de.dlr.premise.element.provider.my.TransitionItemProviderMy;
import de.dlr.premise.provider.util.ItemProviderAdapterMy;
import de.dlr.premise.provider.util.PremiseAdapterFactoryHelper;
import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.TestHelper;

public class SystemItemProviderMyWithDuplicationsTest {

    private static String PATH_INPUT_FILE = de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_EDIT, "test/data/LabelTestCases.premise").getPath();
    private static AdapterFactory premiseAdapterFactory;
    private static ItemProviderAdapterMy itemAdapter;

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
        rep = (ProjectRepository) resource.getContents().get(0);
        sc0 = rep.getProjects().get(0).getReferencedChildren().get(0);
        sc1 = rep.getProjects().get(0).getReferencedChildren().get(1);
        sc00 = rep.getProjects().get(1).getReferencedChildren().get(0);
        sc2 = rep.getProjects().get(1).getReferencedChildren().get(1);
        // initialize upper level test
        SystemItemProviderMyTest.initialize(rep);

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
        assertEquals("TP1 name", PremiseHelper.getTransitions(sc1copy).get(3).getParameters().get(0).getName());
        // append copy of sc1 to repository namespace B
        rep.getProjects().get(1).getChildren().add(sc1copy);
        assertEquals("SC1 \\nname", rep.getProjects().get(1).getReferencedChildren().get(2).getName());

        // initialize adapter factory
        premiseAdapterFactory = PremiseAdapterFactoryHelper.createComposedAdapterFactory();
    }

    @Test
    public void testSystemComponent() {
        // initialize item adapter by using premise factory (to allow calling getText())
        itemAdapter = new SystemComponentItemProviderMy(premiseAdapterFactory);

        // check labels
        assertEquals(" {Namespace A}", itemAdapter.getText(sc0));
        assertEquals("SC1 name {Namespace A}", itemAdapter.getText(sc1));
        assertEquals(" {Namespace B}", itemAdapter.getText(sc00));
        assertEquals("SC2 name", itemAdapter.getText(sc2));
    }

    @Test
    public void testNote() {
        SystemItemProviderMyTest.testNoteCode();
    }

    @Test
    public void testParameter() {
        // initialize item adapter by using premise factory (to allow calling getText())
        itemAdapter = new ParameterItemProviderMy(premiseAdapterFactory);

        // check labels
        assertEquals(" {Namespace A.SC1 name}", itemAdapter.getText(sc1.getParameters().get(0)));
        assertEquals("P1 {Namespace A.SC1 name}", itemAdapter.getText(sc1.getParameters().get(1)));
        assertEquals("P2 name {Namespace A.SC1 name}", itemAdapter.getText(sc1.getParameters().get(2)));
    }

    @Test
    public void testBalancing() {
        SystemItemProviderMyTest.testBalancingCode("");
    }

    @Test
    public void testMetaData() {
        SystemItemProviderMyTest.testMetaDataCode("");
    }

    @Test
    public void testConnection() {
        // initialize item adapter by using premise factory (to allow calling getText())
        itemAdapter = new ConnectionItemProviderMy(premiseAdapterFactory);

        // check labels
        assertEquals("", itemAdapter.getText(sc1.getConnections().get(0)));
        assertEquals("", itemAdapter.getText(sc1.getConnections().get(1)));
        assertEquals("{Namespace A.SC1 name} > In1 name {Namespace A.SC1 name}", itemAdapter.getText(sc1.getConnections().get(2)));
        assertEquals("Out1 name {Namespace A.SC1 name} > In1 name {Namespace A.SC1 name}", itemAdapter.getText(sc1.getConnections().get(3)));
    }

    @Test
    public void testMode() {
        // initialize item adapter by using premise factory (to allow calling getText())
        itemAdapter = new ModeItemProviderMy(premiseAdapterFactory);

        // check labels
        assertEquals(" {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getModes(sc1).get(0)));
        assertEquals("M1 name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getModes(sc1).get(1)));
        assertEquals("M2 name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getModes(sc1).get(2)));
    }

    @Test
    public void testTransition() {
        // initialize item adapter by using premise factory (to allow calling getText())
        itemAdapter = new TransitionItemProviderMy(premiseAdapterFactory);

        // check labels
        assertEquals(" {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(sc1).get(0)));
        assertEquals("test name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(sc1).get(1)));
        assertEquals("M1 name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(sc1).get(2)));
        assertEquals(" > M2 name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(sc1).get(3)));
        assertEquals("M1 name > M2 name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(sc1).get(4)));
        assertEquals("test name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(sc1).get(5)));
    }

    @Test
    public void testTransitionParameterCode() {
        // initialize item adapter by using premise factory (to allow calling getText())
        itemAdapter = new TransitionParameterItemProviderMy(premiseAdapterFactory);

        // check labels
        assertEquals("TP1 name {Namespace A.SC1 name.SC1 name>M2 name}", itemAdapter.getText(PremiseHelper.getTransitions(sc1).get(3).getParameters().get(0)));
    }

    @Test
    public void testRelation() {
        // initialize item adapter by using premise factory (to allow calling getText())
        itemAdapter = new RelationItemProviderMy(premiseAdapterFactory);

        // check labels
        // SC2 has no qualified name because its name is unique in repository!
        assertEquals("SC1 name {Namespace A}", itemAdapter.getText(sc1.getRelations().get(0)));
        assertEquals("SC1 name {Namespace A}", itemAdapter.getText(sc1.getRelations().get(1)));
        assertEquals("SC1 name {Namespace A}", itemAdapter.getText(sc1.getRelations().get(2)));
        assertEquals("SC1 name {Namespace A} > SC2 name", itemAdapter.getText(sc1.getRelations().get(3)));
        assertEquals("SC1 name {Namespace A} > SC2 name", itemAdapter.getText(sc1.getRelations().get(4)));
        assertEquals("SC1 name {Namespace A} > SC2 name", itemAdapter.getText(sc1.getRelations().get(5)));
        assertEquals("SC1 name {Namespace A} > UseCase SC1 name", itemAdapter.getText(sc1.getRelations().get(6)));
    }
}
