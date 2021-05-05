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

import static org.junit.Assert.assertEquals;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.registry.provider.my.MetaDataItemProviderMy;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;
import de.dlr.premise.element.provider.my.ModeItemProviderMy;
import de.dlr.premise.element.provider.my.RelationItemProviderMy;
import de.dlr.premise.element.provider.my.TransitionItemProviderMy;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.functions.UseCaseRepository;
import de.dlr.premise.functions.provider.my.UseCaseItemProviderAdapterFactoryMy;
import de.dlr.premise.functions.provider.my.UseCaseItemProviderMy;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.TestHelper;

public class UseCaseItemProviderMyTest {

    private static String PATH_INPUT_FILE = de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_EDIT, "test/data/LabelTestCases.usecase").getPath();
    private static AdapterFactory useCaseAdapterFactory;
    private static ItemProviderAdapter itemAdapter;

    private static UseCaseRepository rep;
    private static UseCase uc0;
    private static UseCase uc1;
    private static UseCase uc00;
    private static UseCase uc2;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // load model from .usecase file
        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);

        // initialize top components
        initialize((UseCaseRepository) resource.getContents().get(0));

        // initialize adapter factory
        useCaseAdapterFactory = new UseCaseItemProviderAdapterFactoryMy();
    }

    /**
     * Initializes top components used by tests
     */
    public static void initialize(UseCaseRepository repository) {
        rep = repository;
        uc0 = rep.getUsecases().get(0).getChildren().get(0);
        uc1 = rep.getUsecases().get(0).getChildren().get(1);
        uc00 = rep.getUsecases().get(1).getChildren().get(0);
        uc2 = rep.getUsecases().get(1).getChildren().get(1);
    }

    @Test
    public void testUseCase() {
        testUseCaseCode();
    }

    public static void testUseCaseCode() {
        // initialize item adapter by using usecase factory (to allow calling getText())
        itemAdapter = new UseCaseItemProviderMy(useCaseAdapterFactory);

        // check labels
        assertEquals("UseCase  {Namespace A}", itemAdapter.getText(uc0));
        assertEquals("UseCase SC1 name", itemAdapter.getText(uc1));
        assertEquals("Container  {Namespace B}", itemAdapter.getText(uc00));
        assertEquals("Container SC2 name : M1 \\nname & M2 \\nname", itemAdapter.getText(uc2));
    }

    @Test
    public void testNote() {
        testNoteCode();
    }

    public static void testNoteCode() {
        // no adaption of label
    }

    @Test
    public void testMetaData() {
        testMetaDataCode();
    }

    public static void testMetaDataCode() {
        // initialize item adapter by using usecase factory (to allow calling getText())
        //itemAdapter = new MetaDataItemProviderMy(useCaseAdapterFactory);
        itemAdapter = new MetaDataItemProviderMy(new RegistryItemProviderAdapterFactoryMy());

        // check labels
        assertEquals("MetaData ", itemAdapter.getText(uc1.getMetaData().get(0)));
        assertEquals("MetaData test \\nname", itemAdapter.getText(uc1.getMetaData().get(1)));
        assertEquals("MetaData  = test \\nvalue", itemAdapter.getText(uc1.getMetaData().get(2)));
        assertEquals("MetaData test \\nname = test \\nvalue", itemAdapter.getText(uc1.getMetaData().get(3)));
    }

    @Test
    public void testMode() {
        testModeCode();
    }

    public static void testModeCode() {
        // initialize item adapter by using usecase factory (to allow calling getText())
        itemAdapter = new ModeItemProviderMy(useCaseAdapterFactory);

        // check labels
        
        assertEquals("Mode ", itemAdapter.getText(PremiseHelper.getModes(uc1).get(0)));
        assertEquals("Mode M1 name", itemAdapter.getText(PremiseHelper.getModes(uc1).get(1)));
        assertEquals("Mode M2 name", itemAdapter.getText(PremiseHelper.getModes(uc1).get(2)));
        
    }

    @Test
    public void testTransition() {
        testTransitionCode();
    }

    public static void testTransitionCode() {
        // initialize item adapter by using usecase factory (to allow calling getText())
        itemAdapter = new TransitionItemProviderMy(useCaseAdapterFactory);

        // check labels
        assertEquals("Transition ", itemAdapter.getText(PremiseHelper.getTransitions(uc1).get(0)));
        assertEquals("Transition test name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(uc1).get(1)));
        assertEquals("Transition M1 name", itemAdapter.getText(PremiseHelper.getTransitions(uc1).get(2)));
        assertEquals("Transition  > M2 name", itemAdapter.getText(PremiseHelper.getTransitions(uc1).get(3)));
        assertEquals("Transition M1 name > M2 name", itemAdapter.getText(PremiseHelper.getTransitions(uc1).get(4)));
        assertEquals("Transition test name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(uc1).get(5)));
    }

    @Test
    public void testRelation() {
        testRelationCode();
    }

    public static void testRelationCode() {
        // initialize item adapter by using usecase factory (to allow calling getText())
        itemAdapter = new RelationItemProviderMy(useCaseAdapterFactory);

        // check labels
        assertEquals("Relation UseCase SC1 name", itemAdapter.getText(uc1.getRelations().get(0)));
        assertEquals("Relation UseCase SC1 name", itemAdapter.getText(uc1.getRelations().get(1)));
        assertEquals("Relation UseCase SC1 name", itemAdapter.getText(uc1.getRelations().get(2)));
        assertEquals("Relation UseCase SC1 name > Container SC2 name : M1 \\nname & M2 \\nname", itemAdapter.getText(uc1.getRelations().get(3)));
        assertEquals("Relation UseCase SC1 name > Container SC2 name : M1 \\nname & M2 \\nname", itemAdapter.getText(uc1.getRelations().get(4)));
        assertEquals("Relation UseCase SC1 name > Container SC2 name : M1 \\nname & M2 \\nname", itemAdapter.getText(uc1.getRelations().get(5)));
    }
}
