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
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.element.provider.my.ModeItemProviderMy;
import de.dlr.premise.element.provider.my.RelationItemProviderMy;
import de.dlr.premise.element.provider.my.TransitionItemProviderMy;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.functions.UseCaseRepository;
import de.dlr.premise.functions.provider.my.UseCaseItemProviderAdapterFactoryMy;
import de.dlr.premise.functions.provider.my.UseCaseItemProviderMy;
import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.TestHelper;

public class UseCaseItemProviderMyWithDuplicatesTest {

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
        rep = (UseCaseRepository) resource.getContents().get(0);
        uc0 = rep.getUsecases().get(0).getChildren().get(0);
        uc1 = rep.getUsecases().get(0).getChildren().get(1);
        uc00 = rep.getUsecases().get(1).getChildren().get(0);
        uc2 = rep.getUsecases().get(1).getChildren().get(1);
        // initialize upper level test
        UseCaseItemProviderMyTest.initialize(rep);

        // copy sc1 and append it to other namespace
        EcoreUtil.Copier cp = new EcoreUtil.Copier();
        UseCase uc1copy = (UseCase) cp.copy(uc1);
        cp.copyReferences();
        // check copy quickly
        assertEquals(6, PremiseHelper.getTransitions(uc1copy).size());
        assertEquals("M1 \\nname", PremiseHelper.getTransitions(uc1copy).get(4).getSource().getName());
        assertEquals(6, uc1copy.getRelations().size());
        assertEquals("SC2 \\nname", ((ANameItem) uc1copy.getRelations().get(4).getTarget()).getName());
        // append copy of sc1 to repository namespace B
        rep.getUsecases().get(1).getChildren().add(uc1copy);
        assertEquals("SC1 \\nname", rep.getUsecases().get(1).getChildren().get(2).getName());

        // initialize adapter factory
        useCaseAdapterFactory = new UseCaseItemProviderAdapterFactoryMy();
    }

    @Test
    public void testUseCaseCode() {
        // initialize item adapter by using usecase factory (to allow calling getText())
        itemAdapter = new UseCaseItemProviderMy(useCaseAdapterFactory);

        // check labels
        assertEquals("UseCase  {Namespace A}", itemAdapter.getText(uc0));
        assertEquals("UseCase SC1 name {Namespace A}", itemAdapter.getText(uc1));
        assertEquals("Container  {Namespace B}", itemAdapter.getText(uc00));
        assertEquals("Container SC2 name : M1 \\nname & M2 \\nname", itemAdapter.getText(uc2));
    }

    @Test
    public void testNote() {
        UseCaseItemProviderMyTest.testNoteCode();
    }

    @Test
    public void testMetaData() {
        UseCaseItemProviderMyTest.testMetaDataCode();
    }

    @Test
    public void testMode() {
        // initialize item adapter by using usecase factory (to allow calling getText())
        itemAdapter = new ModeItemProviderMy(useCaseAdapterFactory);

        // check labels
        
        assertEquals("Mode  {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getModes(uc1).get(0)));
        assertEquals("Mode M1 name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getModes(uc1).get(1)));
        assertEquals("Mode M2 name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getModes(uc1).get(2)));
        
    }

    @Test
    public void testTransition() {
        // initialize item adapter by using usecase factory (to allow calling getText())
        itemAdapter = new TransitionItemProviderMy(useCaseAdapterFactory);

        // check labels
        assertEquals("Transition  {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(uc1).get(0)));
        assertEquals("Transition test name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(uc1).get(1)));
        assertEquals("Transition M1 name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(uc1).get(2)));
        assertEquals("Transition  > M2 name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(uc1).get(3)));
        assertEquals("Transition M1 name > M2 name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(uc1).get(4)));
        assertEquals("Transition test name {Namespace A.SC1 name.SC1 name}", itemAdapter.getText(PremiseHelper.getTransitions(uc1).get(5)));
    }

    @Test
    public void testRelation() {
        // initialize item adapter by using usecase factory (to allow calling getText())
        itemAdapter = new RelationItemProviderMy(useCaseAdapterFactory);

        // check labels
        // SC2 has no qualified name because its name is unique in repository!
        assertEquals("Relation UseCase SC1 name {Namespace A}", itemAdapter.getText(uc1.getRelations().get(0)));
        assertEquals("Relation UseCase SC1 name {Namespace A}", itemAdapter.getText(uc1.getRelations().get(1)));
        assertEquals("Relation UseCase SC1 name {Namespace A}", itemAdapter.getText(uc1.getRelations().get(2)));
        assertEquals("Relation UseCase SC1 name {Namespace A} > Container SC2 name : M1 \\nname & M2 \\nname", itemAdapter.getText(uc1.getRelations().get(3)));
        assertEquals("Relation UseCase SC1 name {Namespace A} > Container SC2 name : M1 \\nname & M2 \\nname", itemAdapter.getText(uc1.getRelations().get(4)));
        assertEquals("Relation UseCase SC1 name {Namespace A} > Container SC2 name : M1 \\nname & M2 \\nname", itemAdapter.getText(uc1.getRelations().get(5)));
    }
}
