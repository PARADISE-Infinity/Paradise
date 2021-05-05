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

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.element.provider.my.ElementItemProviderAdapterFactoryMy;
import de.dlr.premise.provider.util.PremiseAdapterFactoryHelper;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.presentation.my.AdapterFactoryLabelProviderMy;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.TestHelper;

public class SystemLabelProviderMyWithDataTypeNamesTest {

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
        rep = (ProjectRepository) resource.getContents().get(0);
        sc0 = rep.getProjects().get(0).getReferencedChildren().get(0);
        sc1 = rep.getProjects().get(0).getReferencedChildren().get(1);
        sc00 = rep.getProjects().get(1).getReferencedChildren().get(0);
        sc2 = rep.getProjects().get(1).getReferencedChildren().get(1);
        // initialize upper level test
        SystemLabelProviderMyTest.initialize(rep);

        // switch on option to show dataTypeNames
        PremiseHelper.getMetaData(rep, "dataTypeNames").setValue("on");
    }

    @Test
    public void testSystemComponent() {
        // initialize premise adapter factory (to allow calling getText())
        itemAdapterFactory = new SystemItemProviderAdapterFactoryMy();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals("SystemComponent ", labelAdapterFactory.getText(sc0));
        assertEquals("SystemComponent SC1 name", labelAdapterFactory.getText(sc1));
        assertEquals("Container ", labelAdapterFactory.getText(sc00));
        assertEquals("Container SC2 name", labelAdapterFactory.getText(sc2));
    }

    @Test
    public void testNote() {
        SystemLabelProviderMyTest.testNoteCode();
    }

    @Test
    public void testParameter() {
        // initialize validated adapter factory (to allow calling getText())
        itemAdapterFactory = new SystemItemProviderAdapterFactoryMy();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals("Parameter ", labelAdapterFactory.getText(sc1.getParameters().get(0)));
        assertEquals("Parameter P1", labelAdapterFactory.getText(sc1.getParameters().get(1)));
        assertEquals("Parameter P2 name", labelAdapterFactory.getText(sc1.getParameters().get(2)));
    }

    @Test
    public void testBalancing() {
        SystemLabelProviderMyTest.testBalancingCode("Balancing ");
    }

    @Test
    public void testMetaData() {
        SystemLabelProviderMyTest.testMetaDataCode("MetaData ");
    }

    @Test
    public void testConnection() {
        // initialize validated adapter factory (to allow calling getText())
        itemAdapterFactory = new ElementItemProviderAdapterFactoryMy();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals("Connection ", labelAdapterFactory.getText(sc1.getConnections().get(0)));
        assertEquals("Connection ", labelAdapterFactory.getText(sc1.getConnections().get(1)));
        assertEquals("Connection Output {SC1 name} > Input In1 name {SC1 name}", labelAdapterFactory.getText(sc1.getConnections().get(2)));
        // this differs from PremiseLabelProviderMyTest.testConnectionCode():
        assertEquals("Connection Output Out1 name {SC1 name} > Input In1 name {SC1 name}", labelAdapterFactory.getText(sc1.getConnections().get(3)));
    }

    @Test
    public void testMode() {
        // initialize validated adapter factory (to allow calling getText())
        itemAdapterFactory = new ElementItemProviderAdapterFactoryMy();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals("Mode ", labelAdapterFactory.getText(PremiseHelper.getModes(sc1).get(0)));
        assertEquals("Mode M1 name", labelAdapterFactory.getText(PremiseHelper.getModes(sc1).get(1)));
        assertEquals("Mode M2 name", labelAdapterFactory.getText(PremiseHelper.getModes(sc1).get(2)));
    }

    @Test
    public void testTransition() {
        SystemLabelProviderMyTest.testTransitionCode("Transition ");
    }

    @Test
    public void testRelation() {
        // initialize premise adapter factory (to allow calling getText())
        itemAdapterFactory = PremiseAdapterFactoryHelper.createComposedAdapterFactory();
        labelAdapterFactory = new AdapterFactoryLabelProviderMy(itemAdapterFactory, null, null, null);

        // check labels
        assertEquals("Relation SystemComponent SC1 name", labelAdapterFactory.getText(sc1.getRelations().get(0)));
        assertEquals("Relation SystemComponent SC1 name", labelAdapterFactory.getText(sc1.getRelations().get(1)));
        // this differs from PremiseLabelProviderMyTest.testConnectionCode():
        assertEquals("Relation SystemComponent SC1 name", labelAdapterFactory.getText(sc1.getRelations().get(2)));
        assertEquals("Relation SystemComponent SC1 name > Container SC2 name", labelAdapterFactory.getText(sc1.getRelations().get(3)));
        assertEquals("Relation SystemComponent SC1 name > Container SC2 name", labelAdapterFactory.getText(sc1.getRelations().get(4)));
        assertEquals("Relation SystemComponent SC1 name > Container SC2 name", labelAdapterFactory.getText(sc1.getRelations().get(5)));
    }
}
