/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import de.dlr.premise.component.ComponentFactory;
import de.dlr.premise.component.ISatisfieable;
import de.dlr.premise.component.ISatisfying;
import de.dlr.premise.component.Satisfies;
import de.dlr.premise.constraints.handlers.TestRecordingConstraintViolationHandler;
import de.dlr.premise.constraints.util.ConstraintCheckTestHelper;
import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.element.Mode;
import de.dlr.premise.element.ModeGuard;
import de.dlr.premise.element.StateMachine;
import de.dlr.premise.element.Transition;
import de.dlr.premise.functions.ARange;
import de.dlr.premise.functions.RangeConstraint;
import de.dlr.premise.functions.RequiredParameter;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.functions.UseCaseFactory;
import de.dlr.premise.functions.UseCaseRepository;
import de.dlr.premise.graph.INode;
import de.dlr.premise.registry.Constant;
import de.dlr.premise.registry.Registry;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.system.TransitionParameter;
import de.dlr.premise.util.TestHelper;

public class ConstraintCheckTest {

    private ResourceSet commonRoot;
    private ProjectRepository prRepo;
    private UseCaseRepository ucRepo;
    private Registry registry;
    private UseCase useCase1;
    private UseCase useCase2;
    private UseCase requirement;
    private SystemComponent comp1;
    private SystemComponent comp2;
    private StateMachine sm1;
    private StateMachine sm2;
    private Mode m1;
    private Mode m2;
    private RequiredParameter reqParam1;
    private RequiredParameter reqParam2;
    private RequiredParameter reqParam3;
    private Parameter param1;
    private Parameter param2;
    private Parameter param3;
    private Constant constant;
    private Transition transition;
    private TransitionParameter transitionParameter;

    private TestRecordingConstraintViolationHandler violations;
    private ConstraintChecker constrChecker;

    @Before
    public void setUp() {
        // repos
        prRepo = SystemFactory.eINSTANCE.createProjectRepository();
        ucRepo = UseCaseFactory.eINSTANCE.createUseCaseRepository();
        registry = RegistryFactory.eINSTANCE.createRegistry();

        // Normally, the repositories share a resource set as common root (since they are opened in the same editor). As we don't have
        // ResourceSets when running as a JUnit test, we need to provide the root ourselves.
        commonRoot = TestHelper.createCommonResourceSet(prRepo, ucRepo, registry);

        // components
        comp1 = SystemFactory.eINSTANCE.createSystemComponent();
        prRepo.getProjects().add(comp1);

        comp2 = SystemFactory.eINSTANCE.createSystemComponent();
        comp1.getChildren().add(comp2);

        // state machines
        sm1 = ElementFactory.eINSTANCE.createStateMachine();
        comp2.getStatemachines().add(sm1);        
        sm2 = ElementFactory.eINSTANCE.createStateMachine();
        comp2.getStatemachines().add(sm2);
        
        // modes
        m1 = ElementFactory.eINSTANCE.createMode();
        m1.setName("m1");
        sm1.getModes().add(m1);
        m2 = ElementFactory.eINSTANCE.createMode();
        m2.setName("m2");
        sm2.getModes().add(m2);

        // requirements
        useCase1 = UseCaseFactory.eINSTANCE.createUseCase();
        ucRepo.getUsecases().add(useCase1);

        useCase2 = UseCaseFactory.eINSTANCE.createUseCase();
        useCase1.getChildren().add(useCase2);

        requirement = UseCaseFactory.eINSTANCE.createUseCase();
        useCase2.getChildren().add(requirement);

        // constraints
        reqParam1 = UseCaseFactory.eINSTANCE.createRequiredParameter();
        reqParam1.setName("reqParam1");
        reqParam1.setValueConstraint(ConstraintCheckTestHelper.createRangeConstraint(0, 10));
        reqParam1.getModeValueConstraints().add(ConstraintCheckTestHelper.createModeRangeConstraint(20, 30, m1));
        reqParam1.getModeValueConstraints().add(ConstraintCheckTestHelper.createModeRangeConstraint(40, 50, m2));
        requirement.getRequiredParameters().add(reqParam1);

        reqParam2 = UseCaseFactory.eINSTANCE.createRequiredParameter();
        reqParam2.setName("reqParam2");
        reqParam2.setValueConstraint(ConstraintCheckTestHelper.createRangeConstraint(100, 200));
        requirement.getRequiredParameters().add(reqParam2);

        reqParam3 = UseCaseFactory.eINSTANCE.createRequiredParameter();
        reqParam3.setName("reqParam3");
        reqParam3.setValueConstraint(ConstraintCheckTestHelper.createRangeConstraint(2000, 3000));
        requirement.getRequiredParameters().add(reqParam3);

        // parameter
        param1 = SystemFactory.eINSTANCE.createParameter();
        param1.setName("param1");
        addSatisfies(param1, reqParam1);
        param1.setValue(ConstraintCheckTestHelper.createValue(5));
        param1.getModeValues().add(ConstraintCheckTestHelper.createModeValueRef(25, m1));
        param1.getModeValues().add(ConstraintCheckTestHelper.createModeValueRef(45, m2));
        comp2.getParameters().add(param1);

        // parameter
        param2 = SystemFactory.eINSTANCE.createParameter();
        param2.setName("param2");
        addSatisfies(param2, reqParam2);
        param2.setValue(ConstraintCheckTestHelper.createValue(150));
        comp2.getParameters().add(param2);

        // parameter
        param3 = SystemFactory.eINSTANCE.createParameter();
        param3.setName("param3");
        addSatisfies(param3, reqParam3);
        param3.setValue(ConstraintCheckTestHelper.createValue(2500));
        param3.getModeValues().add(ConstraintCheckTestHelper.createModeValueRef(2500, m2));
        comp2.getParameters().add(param3);

        // constant (for checking that they are ignored)
        constant = RegistryFactory.eINSTANCE.createConstant();
        constant.setValue(RegistryFactory.eINSTANCE.createValue());
        registry.getConstants().add(constant);

        // transition parameter (for checking that they are ignored)
        transition = ElementFactory.eINSTANCE.createTransition();
        transitionParameter = SystemFactory.eINSTANCE.createTransitionParameter();
        transitionParameter.setValue(RegistryFactory.eINSTANCE.createValue());
        transition.getParameters().add(transitionParameter);
        sm1.getTransitions().add(transition);

        // add adapter to common root
        violations = new TestRecordingConstraintViolationHandler();
        constrChecker = new ConstraintChecker(violations);
        commonRoot.eAdapters().add(constrChecker);

        // first validation
        constrChecker.recheck(ucRepo);
    }

    @Test
    public void testRecheck() {
        param2.getValue().setValue("250");

        violations.removeViolations();
        assertEquals(0, violations.getViolatorNumber());

        constrChecker.recheck(ucRepo);

        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param2.getValue()));
        assertTrue(violations.checkIsViolation(param2.getValue(), ConstraintViolationKind.VIOLATED, reqParam2.getValueConstraint()));
    }

    @Test
    public void testValid() {
        assertEquals(0, violations.getViolatorNumber());
    }

    @Test
    public void testValueChange() {
        param2.getValue().setValue("250");
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param2.getValue()));
        assertTrue(violations.checkIsViolation(param2.getValue(), ConstraintViolationKind.VIOLATED, reqParam2.getValueConstraint()));
    }

    @Test
    public void testValueChangeConstant() {
        // value changes of constants should be ignored
        constant.getValue().setValue("123");

        assertEquals(0, violations.getViolatorNumber());
    }

    @Test
    public void testValueChangeTransitionParameter() {
        // value changes of transition parameters should be ignored
        transitionParameter.getValue().setValue("123");

        assertEquals(0, violations.getViolatorNumber());
    }

    @Test
    public void testValueAdd() {
        param2.setValue(ConstraintCheckTestHelper.createValue(250));
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param2.getValue()));
        assertTrue(violations.checkIsViolation(param2.getValue(), ConstraintViolationKind.VIOLATED, reqParam2.getValueConstraint()));
    }

    @Test
    public void testValueDelete() {
        param3.setValue(ConstraintCheckTestHelper.createValue(3500));
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param3.getValue()));
        assertTrue(violations.checkIsViolation(param3.getValue(), ConstraintViolationKind.VIOLATED, reqParam3.getValueConstraint()));

        param3.setValue(null);
        assertEquals(0, violations.getViolatorNumber());
    }

    @Test
    public void testModeValueValueChange() {
        param1.getModeValues().get(0).setValue("-10");
        param1.getModeValues().get(1).setValue("-10");
        assertEquals(2, violations.getViolatorNumber());

        assertTrue(violations.checkIsViolation(param1.getModeValues().get(0), ConstraintViolationKind.VIOLATED, reqParam1
                .getModeValueConstraints().get(0)));
        assertTrue(violations.checkIsViolation(param1.getModeValues().get(1), ConstraintViolationKind.VIOLATED, reqParam1
                .getModeValueConstraints().get(1)));
    }

    @Test
    public void testModeValueModeChange() {
        param1.getModeValues().get(0).getModePointers().add(ConstraintCheckTestHelper.createDirectPointer(m2));

        assertEquals(1, violations.getViolatorNumber());
        assertTrue(violations.checkIsViolation(param1.getModeValues().get(0), ConstraintViolationKind.VIOLATED,
                                                 reqParam1.getModeValueConstraints().get(1)));
    }

    @Test
    public void testModeValueModeEmpty() {
        param1.getModeValues().get(0).getModePointers().clear();

        System.out.println(violations);
        
        assertEquals(1, violations.getViolatorNumber());
        assertTrue(violations.checkIsViolation(param1.getValue(), ConstraintViolationKind.VIOLATED, reqParam1
                .getModeValueConstraints().get(0)));
    }

    @Test
    public void testModeValueAdd() {
        param1.getModeValues().clear();
        // value of parameter1 should violate now, as it also has to satisfy the mode value constraints if no matching mode values exist
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(2, violations.getViolationNumber(param1.getValue()));
        assertTrue(violations.checkIsViolation(param1.getValue(), ConstraintViolationKind.VIOLATED, reqParam1
                .getModeValueConstraints().get(0)));
        assertTrue(violations.checkIsViolation(param1.getValue(), ConstraintViolationKind.VIOLATED, reqParam1
                .getModeValueConstraints().get(1)));

        param1.getModeValues().add(ConstraintCheckTestHelper.createModeValueRef(-10, m1));
        param1.getModeValues().add(ConstraintCheckTestHelper.createModeValueRef(-10, m2));

        assertEquals(2, violations.getViolatorNumber());
        assertTrue(violations.checkIsViolation(param1.getModeValues().get(0), ConstraintViolationKind.VIOLATED, reqParam1
                .getModeValueConstraints().get(0)));
        assertTrue(violations.checkIsViolation(param1.getModeValues().get(1), ConstraintViolationKind.VIOLATED, reqParam1
                .getModeValueConstraints().get(1)));

        param1.getModeValues().clear();
        // value of parameter1 should violate now, as it also has to satisfy the mode value constraints if no matching mode values exist
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(2, violations.getViolationNumber(param1.getValue()));
        assertTrue(violations.checkIsViolation(param1.getValue(), ConstraintViolationKind.VIOLATED, reqParam1
                .getModeValueConstraints().get(0)));
        assertTrue(violations.checkIsViolation(param1.getValue(), ConstraintViolationKind.VIOLATED, reqParam1
                .getModeValueConstraints().get(1)));
    }

    @Test
    public void testParameterSatisfiesChange() {
        param1.getValue().setValue("15");
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param1.getValue()));
        assertTrue(violations.checkIsViolation(param1.getValue(), ConstraintViolationKind.VIOLATED, reqParam1.getValueConstraint()));

        param1.getSatisfies().clear();
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(reqParam1));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));

        addSatisfies(param1, reqParam1);
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param1.getValue()));
        assertTrue(violations.checkIsViolation(param1.getValue(), ConstraintViolationKind.VIOLATED, reqParam1.getValueConstraint()));
    }
    

    @Test
    public void testSatisfiesTargetChange() {
        param1.getValue().setValue("15");
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param1.getValue()));
        assertTrue(violations.checkIsViolation(param1.getValue(), ConstraintViolationKind.VIOLATED, reqParam1.getValueConstraint()));

        param1.getSatisfies().get(0).setTarget(null);
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(reqParam1));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));

        param1.getSatisfies().get(0).setTarget(reqParam1);
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param1.getValue()));
        assertTrue(violations.checkIsViolation(param1.getValue(), ConstraintViolationKind.VIOLATED, reqParam1.getValueConstraint()));
    }

    @Test
    public void testValueConstraintChange() {
        // make range smaller so that the value lies outside
        ((RangeConstraint) reqParam1.getValueConstraint()).setUpper("4");
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param1.getValue()));
        assertTrue(violations.checkIsViolation(param1.getValue(), ConstraintViolationKind.VIOLATED, reqParam1.getValueConstraint()));

        reqParam1.setValueConstraint(null);
        assertEquals(0, violations.getViolatorNumber());
    }

    @Test
    public void testValueConstraintAdd() {
        // make range smaller so that the value lies outside
        reqParam1.setValueConstraint(ConstraintCheckTestHelper.createRangeConstraint(0, 4));
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param1.getValue()));
        assertTrue(violations.checkIsViolation(param1.getValue(), ConstraintViolationKind.VIOLATED, reqParam1.getValueConstraint()));

        reqParam1.setValueConstraint(null);
        assertEquals(0, violations.getViolatorNumber());
    }

    @Test
    public void testModeValueConstraintChange() {
        // make range smaller so that the value lies outside
        ((ARange) reqParam1.getModeValueConstraints().get(0)).setUpper("24");
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param1.getModeValues().get(0)));
        assertTrue(violations.checkIsViolation(param1.getModeValues().get(0), ConstraintViolationKind.VIOLATED, reqParam1
                .getModeValueConstraints().get(0)));
    }

    @Test
    public void testModeValueConstraintModeEmpty() {
        reqParam1.getModeValueConstraints().get(0).getModePointers().clear();

        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param1.getModeValues().get(0)));
        assertTrue(violations.checkIsViolation(param1.getModeValues().get(0), ConstraintViolationKind.VIOLATED,
                reqParam1.getValueConstraint()));
    }

    @Test
    public void testParameterConstraintAdd() {
        RequiredParameter reqParamNew = UseCaseFactory.eINSTANCE.createRequiredParameter();
        reqParamNew.setValueConstraint(ConstraintCheckTestHelper.createRangeConstraint(0, 100));
        requirement.getRequiredParameters().add(reqParamNew);

        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(reqParamNew));
        assertTrue(violations.checkIsViolation(reqParamNew, ConstraintViolationKind.NOT_SATISFIED, null));

        param2.getSatisfies().clear();
        addSatisfies(param2, reqParamNew);

        assertEquals(2, violations.getViolatorNumber());

        assertEquals(1, violations.getViolationNumber(param2.getValue()));
        assertEquals(1, violations.getViolationNumber(reqParam2));
        assertTrue(violations.checkIsViolation(param2.getValue(), ConstraintViolationKind.VIOLATED, reqParamNew.getValueConstraint()));
        assertTrue(violations.checkIsViolation(reqParam2, ConstraintViolationKind.NOT_SATISFIED, null));
    }

    @Test
    public void testParameterConstraintsAdd() {
        List<RequiredParameter> constraints = new ArrayList<RequiredParameter>();

        RequiredParameter reqParamNew1 = UseCaseFactory.eINSTANCE.createRequiredParameter();
        reqParamNew1.setValueConstraint(ConstraintCheckTestHelper.createRangeConstraint(0, 100));
        constraints.add(reqParamNew1);

        RequiredParameter reqParamNew2 = UseCaseFactory.eINSTANCE.createRequiredParameter();
        reqParamNew2.setValueConstraint(ConstraintCheckTestHelper.createRangeConstraint(0, 100));
        constraints.add(reqParamNew2);

        requirement.getRequiredParameters().addAll(constraints);

        assertEquals(2, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(reqParamNew1));
        assertEquals(1, violations.getViolationNumber(reqParamNew2));
        assertTrue(violations.checkIsViolation(reqParamNew1, ConstraintViolationKind.NOT_SATISFIED, null));
        assertTrue(violations.checkIsViolation(reqParamNew2, ConstraintViolationKind.NOT_SATISFIED, null));

        param2.getSatisfies().clear();
        addSatisfies(param2, reqParamNew1);

        assertEquals(3, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param2.getValue()));
        assertEquals(1, violations.getViolationNumber(reqParam2));
        assertEquals(1, violations.getViolationNumber(reqParamNew2));
        assertTrue(violations.checkIsViolation(param2.getValue(), ConstraintViolationKind.VIOLATED, reqParamNew1.getValueConstraint()));
        assertTrue(violations.checkIsViolation(reqParam2, ConstraintViolationKind.NOT_SATISFIED, null));
        assertTrue(violations.checkIsViolation(reqParamNew2, ConstraintViolationKind.NOT_SATISFIED, null));
    }

    @Test
    public void testParameterConstraintDelete() {
        // create a violation so we can test if it gets cleaned up properly
        param2.setValue(ConstraintCheckTestHelper.createValue(250));
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param2.getValue()));
        assertTrue(violations.checkIsViolation(param2.getValue(), ConstraintViolationKind.VIOLATED, reqParam2.getValueConstraint()));

        EcoreUtil.delete(reqParam2);
        assertEquals(0, violations.getViolatorNumber());
    }

    @Test
    public void testParameterConstraintMultiReference() {
        Parameter clone = EcoreUtil.copy(param1);
        clone.setName("clone");
        comp2.getParameters().add(clone);
        
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(reqParam1));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.MULTIPLY_SATISFIED,
                ImmutableSet.of(param1, clone)));
    }

    @Test
    public void testParameterDelete() {
        EcoreUtil.delete(param1);

        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(reqParam1));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));
    }

    @Test
    public void testMultipleParameterDelete() {
        comp2.getParameters().clear();

        assertEquals(3, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(reqParam1));
        assertEquals(1, violations.getViolationNumber(reqParam2));
        assertEquals(1, violations.getViolationNumber(reqParam3));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));
        assertTrue(violations.checkIsViolation(reqParam2, ConstraintViolationKind.NOT_SATISFIED, null));
        assertTrue(violations.checkIsViolation(reqParam3, ConstraintViolationKind.NOT_SATISFIED, null));
    }

    @Test
    public void testProjectRepositoryChildDelete() {
        // create a violation so we can test if it gets cleaned up properly
        param2.setValue(ConstraintCheckTestHelper.createValue(250));
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param2.getValue()));
        assertTrue(violations.checkIsViolation(param2.getValue(), ConstraintViolationKind.VIOLATED, reqParam2.getValueConstraint()));

        EcoreUtil.delete(comp1, true);

        assertEquals(3, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(reqParam1));
        assertEquals(1, violations.getViolationNumber(reqParam2));
        assertEquals(1, violations.getViolationNumber(reqParam3));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));
    }

    @Test
    public void testProjectRepositoryChildAdd() {
        // first rescue the modes to the UseCase, so references from the ModeValueRefConstraints don't get lost when we delete the component
        StateMachine sm3 = ElementFactory.eINSTANCE.createStateMachine();
        useCase2.getStatemachines().add(sm3);
        sm3.getModes().add(m1);
        sm3.getModes().add(m2);

        // this shouldn't change validity at all
        assertEquals(0, violations.getViolatorNumber());

        EcoreUtil.delete(comp1, true);

        // create a violation so we can test if it gets reported once we readd the component
        param2.setValue(ConstraintCheckTestHelper.createValue(250));

        assertEquals(3, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(reqParam1));
        assertEquals(1, violations.getViolationNumber(reqParam2));
        assertEquals(1, violations.getViolationNumber(reqParam3));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));

        prRepo.getProjects().add(comp1);

        System.out.println(violations.toString());
        
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param2.getValue()));
        assertTrue(violations.checkIsViolation(param2.getValue(), ConstraintViolationKind.VIOLATED, reqParam2.getValueConstraint()));

    }

    @Test
    public void testSystemComponentChildDelete() {
        // create a violation so we can test if it gets cleaned up properly
        param2.setValue(ConstraintCheckTestHelper.createValue(250));
        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param2.getValue()));
        assertTrue(violations.checkIsViolation(param2.getValue(), ConstraintViolationKind.VIOLATED, reqParam2.getValueConstraint()));

        EcoreUtil.delete(comp2, true);

        assertEquals(3, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(reqParam1));
        assertEquals(1, violations.getViolationNumber(reqParam2));
        assertEquals(1, violations.getViolationNumber(reqParam3));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));
    }

    @Test
    public void testProjectRepositoryProblemDelete() {
        // create violations so we can test if they get cleaned up properly
        param2.setValue(ConstraintCheckTestHelper.createValue(250));
        param1.getSatisfies().clear();
        assertEquals(2, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param2.getValue()));
        assertEquals(1, violations.getViolationNumber(reqParam1));
        assertTrue(violations.checkIsViolation(param2.getValue(), ConstraintViolationKind.VIOLATED, reqParam2.getValueConstraint()));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));

        EcoreUtil.delete(useCase1, true);

        assertEquals(0, violations.getViolatorNumber());
    }

    @Test
    public void testProjectRepositoryProblemAdd() {
        EcoreUtil.delete(useCase1, true);

        assertEquals(0, violations.getViolatorNumber());

        // create a violation so we can test if it gets reported once we readd the use case
        param2.setValue(ConstraintCheckTestHelper.createValue(250));

        ucRepo.getUsecases().add(useCase1);

        assertEquals(3, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(reqParam1));
        assertEquals(1, violations.getViolationNumber(reqParam2));
        assertEquals(1, violations.getViolationNumber(reqParam3));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));

        addSatisfies(param1, reqParam1);
        addSatisfies(param2, reqParam2);
        addSatisfies(param3, reqParam3);

        assertEquals(1, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param2.getValue()));
        assertTrue(violations.checkIsViolation(param2.getValue(), ConstraintViolationKind.VIOLATED, reqParam2.getValueConstraint()));
    }

    @Test
    public void testUseCaseChildDelete() {
        // create violations so we can test if they get cleaned up properly
        param2.setValue(ConstraintCheckTestHelper.createValue(250));
        param1.getSatisfies().clear();
        assertEquals(2, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param2.getValue()));
        assertEquals(1, violations.getViolationNumber(reqParam1));
        assertTrue(violations.checkIsViolation(param2.getValue(), ConstraintViolationKind.VIOLATED, reqParam2.getValueConstraint()));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));

        EcoreUtil.delete(useCase2, true);

        assertEquals(0, violations.getViolatorNumber());
    }

    @Test
    public void testUseCaseRequirementDelete() {
        // create violations so we can test if they get cleaned up properly
        param2.setValue(ConstraintCheckTestHelper.createValue(250));
        param1.getSatisfies().clear();
        assertEquals(2, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param2.getValue()));
        assertEquals(1, violations.getViolationNumber(reqParam1));
        assertTrue(violations.checkIsViolation(param2.getValue(), ConstraintViolationKind.VIOLATED, reqParam2.getValueConstraint()));
        assertTrue(violations.checkIsViolation(reqParam1, ConstraintViolationKind.NOT_SATISFIED, null));

        EcoreUtil.delete(requirement, true);

        assertEquals(0, violations.getViolatorNumber());
    }

    @Test
    public void testNoValues() {
        param1.setValue(null);
        param1.getModeValues().clear();

        assertEquals(1, violations.getViolatorNumber());
        assertEquals(3, violations.getViolationNumber(param1));
        assertTrue(violations.checkIsViolation(param1, ConstraintViolationKind.VIOLATED, reqParam1.getValueConstraint()));
        assertTrue(violations.checkIsViolation(param1, ConstraintViolationKind.VIOLATED, reqParam1.getModeValueConstraints().get(0)));
        assertTrue(violations.checkIsViolation(param1, ConstraintViolationKind.VIOLATED, reqParam1.getModeValueConstraints().get(1)));
    }

    @Test
    public void testRequirementMode() {
        assertEquals(0, violations.getViolatorNumber());
        
        ModeGuard modeGuardM1 = ElementFactory.eINSTANCE.createModeGuard();
        modeGuardM1.setMode(m1);
        
        requirement.setCondition(modeGuardM1);

        assertEquals(2, violations.getViolatorNumber());
        assertEquals(1, violations.getViolationNumber(param1.getValue()));
        assertEquals(1, violations.getViolationNumber(param1.getModeValues().get(0)));

        assertTrue(violations.checkIsViolation(param1.getValue(), ConstraintViolationKind.VIOLATED, reqParam1
                .getModeValueConstraints().get(1)));

        assertTrue(violations.checkIsViolation(param1.getModeValues().get(0), ConstraintViolationKind.VIOLATED,
                reqParam1.getValueConstraint()));
    }
    
    private <S extends INode, T extends ISatisfieable> void addSatisfies(ISatisfying<S, T> satisfying, T satisfieable) {
        Satisfies<S, T> satisfies = ComponentFactory.eINSTANCE.createSatisfies();
        satisfies.setTarget(satisfieable);
        satisfying.getSatisfies().add(satisfies);
    }
}
