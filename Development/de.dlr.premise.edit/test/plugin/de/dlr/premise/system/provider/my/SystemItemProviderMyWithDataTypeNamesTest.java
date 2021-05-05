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

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.element.provider.my.ConnectionItemProviderMy;
import de.dlr.premise.element.provider.my.ModeItemProviderMy;
import de.dlr.premise.element.provider.my.RelationItemProviderMy;
import de.dlr.premise.provider.util.ItemProviderAdapterMy;
import de.dlr.premise.provider.util.PremiseAdapterFactoryHelper;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.TestHelper;

public class SystemItemProviderMyWithDataTypeNamesTest {

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

        // switch on option to show dataTypeNames
        PremiseHelper.getMetaData(rep, "dataTypeNames").setValue("on");

        // initialize adapter factory
        premiseAdapterFactory = PremiseAdapterFactoryHelper.createComposedAdapterFactory();
    }

    @Test
    public void testSystemComponent() {
        // initialize item adapter by using premise factory (to allow calling getText())
        itemAdapter = new SystemComponentItemProviderMy(premiseAdapterFactory);

        // check labels
        assertEquals("SystemComponent  {Namespace A}", itemAdapter.getText(sc0));
        assertEquals("SystemComponent SC1 name", itemAdapter.getText(sc1));
        assertEquals("Container  {Namespace B}", itemAdapter.getText(sc00));
        assertEquals("Container SC2 name", itemAdapter.getText(sc2));
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
        assertEquals("Parameter ", itemAdapter.getText(sc1.getParameters().get(0)));
        assertEquals("Parameter P1", itemAdapter.getText(sc1.getParameters().get(1)));
        assertEquals("Parameter P2 name", itemAdapter.getText(sc1.getParameters().get(2)));
    }

    @Test
    public void testBalancing() {
        SystemItemProviderMyTest.testBalancingCode("Balancing ");
    }

    @Test
    public void testMetaData() {
        SystemItemProviderMyTest.testMetaDataCode("MetaData ");
    }

    @Test
    public void testConnection() {
        testConnectionCode();
    }

    @Test
    public void testConnectionCode() {
        // initialize item adapter by using premise factory (to allow calling getText())
        itemAdapter = new ConnectionItemProviderMy(premiseAdapterFactory);

        // check labels
        assertEquals("Connection ", itemAdapter.getText(sc1.getConnections().get(0)));
        assertEquals("Connection ", itemAdapter.getText(sc1.getConnections().get(1)));
        assertEquals("Connection Output {SC1 name} > Input In1 name {SC1 name}", itemAdapter.getText(sc1.getConnections().get(2)));
        assertEquals("Connection Output Out1 name {SC1 name} > Input In1 name {SC1 name}", itemAdapter.getText(sc1.getConnections().get(3)));
    }

    @Test
    public void testMode() {
        testModeCode();
    }

    public void testModeCode() {
        // initialize item adapter by using premise factory (to allow calling getText())
        itemAdapter = new ModeItemProviderMy(premiseAdapterFactory);

        // check labels
        assertEquals("Mode ", itemAdapter.getText(PremiseHelper.getModes(sc1).get(0)));
        assertEquals("Mode M1 name", itemAdapter.getText(PremiseHelper.getModes(sc1).get(1)));
        assertEquals("Mode M2 name", itemAdapter.getText(PremiseHelper.getModes(sc1).get(2)));
    }

    @Test
    public void testTransition() {
        SystemItemProviderMyTest.testTransitionCode("Transition ");
    }

    public void testTransitionParameterCode() {
        // initialize item adapter by using premise factory (to allow calling getText())
        itemAdapter = new TransitionParameterItemProviderMy(premiseAdapterFactory);

        // check labels
        assertEquals("TransitionParameter ", itemAdapter.getText(PremiseHelper.getTransitions(sc1).get(0).getParameters().get(0)));
        assertEquals("TransitionParameter TP1 name", itemAdapter.getText(PremiseHelper.getTransitions(sc1).get(0).getParameters().get(1)));
    }

    @Test
    public void testRelation() {
        testRelationCode();
    }

    public void testRelationCode() {
        // initialize item adapter by using premise factory (to allow calling getText())
        itemAdapter = new RelationItemProviderMy(premiseAdapterFactory);

        // check labels
        assertEquals("Relation SystemComponent SC1 name", itemAdapter.getText(sc1.getRelations().get(0)));
        assertEquals("Relation SystemComponent SC1 name", itemAdapter.getText(sc1.getRelations().get(1)));
        assertEquals("Relation SystemComponent SC1 name", itemAdapter.getText(sc1.getRelations().get(2)));
        assertEquals("Relation SystemComponent SC1 name > Container SC2 name", itemAdapter.getText(sc1.getRelations().get(3)));
        assertEquals("Relation SystemComponent SC1 name > Container SC2 name", itemAdapter.getText(sc1.getRelations().get(4)));
        assertEquals("Relation SystemComponent SC1 name > Container SC2 name", itemAdapter.getText(sc1.getRelations().get(5)));
        assertEquals("Relation SystemComponent SC1 name > UseCase SC1 name", itemAdapter.getText(sc1.getRelations().get(6)));
    }
}
