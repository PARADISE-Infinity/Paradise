/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.constraints.valueconstraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Multimap;

import de.dlr.premise.constraints.util.ConstraintCheckTestHelper;
import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.element.Mode;
import de.dlr.premise.element.ModeGuard;
import de.dlr.premise.element.StateMachine;
import de.dlr.premise.functions.AConstraint;
import de.dlr.premise.functions.AModeValueConstraint;
import de.dlr.premise.functions.AValueConstraint;
import de.dlr.premise.functions.RequiredParameter;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.functions.UseCaseFactory;
import de.dlr.premise.registry.AValueDef;
import de.dlr.premise.registry.Value;
import de.dlr.premise.system.ModeValueRef;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.SystemFactory;

public class ConstraintToValueMapperTest {

    private ConstraintToValueMapper constraintToValueMapper;

    private UseCase useCase;
    private RequiredParameter reqParam;

    private StateMachine sm1;
    private Mode m1;
    private StateMachine sm2;
    private Mode m2;

    private Parameter param;

    private Value val;
    private ModeValueRef modeValM1;
    private ModeValueRef modeValM2;
    private ModeValueRef modeValM1M2;

    private AValueConstraint valConstr;
    private AModeValueConstraint modeValConstrEmpty;
    private AModeValueConstraint modeValConstrM1;
    private AModeValueConstraint modeValConstrM2;
    private AModeValueConstraint modeValConstrM1M2;
    
    private ModeGuard modeGuardM1;

    @Before
    public void setUp() {
        // create test instance
        constraintToValueMapper = new ConstraintToValueMapper();

        // set up model
        useCase = UseCaseFactory.eINSTANCE.createUseCase();

        reqParam = UseCaseFactory.eINSTANCE.createRequiredParameter();
        useCase.getRequiredParameters().add(reqParam);

        sm1 = ElementFactory.eINSTANCE.createStateMachine();
        sm1.setName("SM1");
        useCase.getStatemachines().add(sm1);
        m1 = ElementFactory.eINSTANCE.createMode();
        m1.setName("M1");
        sm1.getModes().add(m1);
        
        
        sm2 = ElementFactory.eINSTANCE.createStateMachine();
        sm2.setName("SM2");
        useCase.getStatemachines().add(sm2);
        m2 = ElementFactory.eINSTANCE.createMode();
        m2.setName("M2");
        sm2.getModes().add(m2);

        param = SystemFactory.eINSTANCE.createParameter();

        // set up some values, modevalues, constraints, modevalueconstraints for testing (these aren't attached to anything yet)
        val = ConstraintCheckTestHelper.createValue(0);
        modeValM1 = ConstraintCheckTestHelper.createModeValueRef(0, m1);
        modeValM2 = ConstraintCheckTestHelper.createModeValueRef(0, m2);
        modeValM1M2 = ConstraintCheckTestHelper.createModeValueRef(0, m1, m2);

        valConstr = ConstraintCheckTestHelper.createRangeConstraint(0, 0);
        modeValConstrEmpty = ConstraintCheckTestHelper.createModeRangeConstraint(0, 0);
        modeValConstrM1 = ConstraintCheckTestHelper.createModeRangeConstraint(0, 0, m1);
        modeValConstrM2 = ConstraintCheckTestHelper.createModeRangeConstraint(0, 0, m2);
        modeValConstrM1M2 = ConstraintCheckTestHelper.createModeRangeConstraint(0, 0, m1, m2);
        
        modeGuardM1 = ElementFactory.eINSTANCE.createModeGuard();
        modeGuardM1.setMode(m1);
    }
    

    @Test
    public void testEmptyMapping() {
        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(0, constraintToValueMap.size());
    }
    
    @Test 
    public void testNoModeMapping() {
        // simple case of one val and one valConstr
        reqParam.setValueConstraint(valConstr);
        param.setValue(val);

        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(1, constraintToValueMap.size());
        assertTrue(constraintToValueMap.containsEntry(valConstr, val));
    }

    @Test
    public void testNoModeNoValueMapping() {
        // a valConstr without value doesn't constrain anything
        reqParam.setValueConstraint(valConstr);

        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(0, constraintToValueMap.size());
    }

    @Test
    public void testOneToOneModeMapping() {
        // case in which modes are exactly matching
        reqParam.getModeValueConstraints().add(modeValConstrM1);
        param.getModeValues().add(modeValM1);
        
        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(1, constraintToValueMap.size());
        assertTrue(constraintToValueMap.containsEntry(modeValConstrM1, modeValM1));
    }

    @Test
    public void testSubsetModeMapping() {
        // modeValConstr has to contain subset of modes of modeVal
        reqParam.getModeValueConstraints().add(modeValConstrM1);
        param.getModeValues().add(modeValM1M2);

        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(1, constraintToValueMap.size());
        assertTrue(constraintToValueMap.containsEntry(modeValConstrM1, modeValM1M2));
    }

    @Test
    public void testSubsetAndOneToOneModeMapping() {
        reqParam.getModeValueConstraints().add(modeValConstrM1);
        param.getModeValues().add(modeValM1);
        param.getModeValues().add(modeValM1M2);

        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(2, constraintToValueMap.size());
        assertTrue(constraintToValueMap.containsEntry(modeValConstrM1, modeValM1));
        assertTrue(constraintToValueMap.containsEntry(modeValConstrM1, modeValM1M2));
    }

    @Test
    public void testFallbackValueMapping() {
        // if there is no modeVal for a modeValConstr, it falls back to the value
        reqParam.getModeValueConstraints().add(modeValConstrM1);

        param.setValue(val);
        param.getModeValues().add(modeValM2);

        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(1, constraintToValueMap.size());
        assertTrue(constraintToValueMap.containsEntry(modeValConstrM1, val));
    }

    @Test
    public void testFallbackValueMappingNoValue() {
        // trying to fall back to the value if there is none results in no mapping
        reqParam.getModeValueConstraints().add(modeValConstrM1);

        param.getModeValues().add(modeValM2);

        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(0, constraintToValueMap.size());
    }

    @Test
    public void testUnmatchedModeValRefsMapping() {
        // if there is no modeValConstr for a modeVal, it is constrained by the valConstr
        reqParam.setValueConstraint(valConstr);
        reqParam.getModeValueConstraints().add(modeValConstrM1M2);

        param.setValue(val);
        param.getModeValues().add(modeValM1);
        param.getModeValues().add(modeValM2);
        param.getModeValues().add(modeValM1M2);

        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(4, constraintToValueMap.size());
        assertTrue(constraintToValueMap.containsEntry(valConstr, val));
        assertTrue(constraintToValueMap.containsEntry(valConstr, modeValM1));
        assertTrue(constraintToValueMap.containsEntry(valConstr, modeValM2));
        assertTrue(constraintToValueMap.containsEntry(modeValConstrM1M2, modeValM1M2));
    }

    @Test
    public void testEmptyModeValueConstraintMapping() {
        // empty modeValConstrs should be ignored
        reqParam.getModeValueConstraints().add(modeValConstrEmpty);
        reqParam.setValueConstraint(valConstr);

        param.setValue(val);

        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(1, constraintToValueMap.size());
        assertTrue(constraintToValueMap.containsEntry(valConstr, val));
    }

    @Test
    public void testUseCaseConditionSimpleMapping() {
        useCase.setCondition(modeGuardM1);
        reqParam.getModeValueConstraints().add(modeValConstrM1);
        param.setValue(val);

        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(1, constraintToValueMap.size());
        assertTrue(constraintToValueMap.containsEntry(modeValConstrM1, val));
    }

    @Test
    public void testUseCaseConditionModeValueMapping() {
        useCase.setCondition(modeGuardM1);

        reqParam.getModeValueConstraints().add(modeValConstrM2);
        param.getModeValues().add(modeValM1M2);

        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(1, constraintToValueMap.size());
        assertTrue(constraintToValueMap.containsEntry(modeValConstrM2, modeValM1M2));
    }

    @Test
    public void testUseCaseConditionValueConstraintMapping() {
        useCase.setCondition(modeGuardM1);
        reqParam.setValueConstraint(valConstr);

        param.getModeValues().add(modeValM1);

        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(1, constraintToValueMap.size());
        assertTrue(constraintToValueMap.containsEntry(valConstr, modeValM1));
    }

    @Test
    public void testUseCaseConditionEmptyModeValueConstraintMapping() {
        // empty modeValConstrs should be ignored, even if they could map to something in combination with the use case condition
        useCase.setCondition(modeGuardM1);
        reqParam.getModeValueConstraints().add(modeValConstrEmpty);

        param.getModeValues().add(modeValM1);

        Multimap<AConstraint, AValueDef> constraintToValueMap = createConstraintToValueMap();

        assertEquals(0, constraintToValueMap.size());
    }

    private Multimap<AConstraint, AValueDef> createConstraintToValueMap() {
        return constraintToValueMapper.createConstraintToValueMap(reqParam, param);
    }
}
