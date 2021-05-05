/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.testsuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import de.dlr.testsuite.runner.RandomSuite;

@RunWith(RandomSuite.class)
@SuiteClasses({

    // de.dlr.premise
    de.dlr.premise.impl.my.PremiseFactoryImplMyTest.class,
    de.dlr.premise.util.PremiseHelperTest.class,
    de.dlr.premise.registry.impl.my.RegistryFactoryImplMyTest.class,

    // de.dlr.premise.edit
    de.dlr.premise.element.provider.my.ElementImageProviderMyTest.class,
    de.dlr.premise.element.provider.my.ModeItemProviderMyTest.class,
    de.dlr.premise.element.provider.my.GuardCombinationItemProviderMyTest.class,
    de.dlr.premise.element.provider.my.RelationItemProviderMyTest.class,
    de.dlr.premise.element.provider.my.ModeGuardItemProviderMyTest.class,
    de.dlr.premise.functions.provider.my.ModeRangeConstraintItemProviderMyTest.class,
    de.dlr.premise.functions.provider.my.RangeConstraintItemProviderMyTest.class,
    de.dlr.premise.functions.provider.my.RequiredParameterItemProviderMyTest.class,

    // changed while refactoring A-Transition -> Transition
    de.dlr.premise.system.provider.my.TransitionItemProviderMyTest.class,
    de.dlr.premise.functions.provider.my.UseCaseImageProviderMyTest.class,
    de.dlr.premise.functions.provider.my.UseCaseItemProviderMyTest.class,
    de.dlr.premise.functions.provider.my.UseCaseItemProviderMyWithDuplicatesTest.class,
    de.dlr.premise.functions.provider.my.UseCaseItemProviderMyWithoutDataTypeNamesTest.class,
    de.dlr.premise.functionpool.provider.my.CalcEngineScriptItemProviderMyTest.class,
    de.dlr.premise.functionpool.provider.my.CalcEngineSpreadSheetItemProviderMyTest.class,
    de.dlr.premise.functionpool.provider.my.FnDefScriptItemProviderMyTest.class,
    de.dlr.premise.functionpool.provider.my.FnDefSpreadSheetItemProviderMyTest.class,
    de.dlr.premise.functionpool.provider.my.FnInputSpreadSheetItemProviderMyTest.class,
    de.dlr.premise.functionpool.provider.my.FnOutputItemProviderMyTest.class,
    de.dlr.premise.functionpool.provider.my.FnOutputSpreadSheetItemProviderMyTest.class,
    de.dlr.premise.functionpool.provider.my.FunctionpoolImageProviderMyTest.class,
    de.dlr.premise.functionpool.provider.my.FunctionpoolItemProviderMyTest.class,
    de.dlr.premise.functionpool.provider.my.LabelHelperTest.class,
    de.dlr.premise.system.provider.my.BalancingItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.ConnectionItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.ModeValueRefItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.ParameterItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.SystemImageProviderMyTest.class,
    de.dlr.premise.system.provider.my.SystemItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.SystemItemProviderMyWithDataTypeNamesTest.class,
    de.dlr.premise.system.provider.my.SystemItemProviderMyWithDuplicationsTest.class,
    de.dlr.premise.system.provider.my.SystemComponentItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.TransitionBalancingItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.TransitionItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.TransitionParameterItemProviderMyTest.class,
    de.dlr.premise.registry.provider.my.RegistryImageProviderMyTest.class,
    de.dlr.premise.registry.provider.my.RegistryItemProviderMyTest.class,

    // de.dlr.premise.editor
    de.dlr.premise.system.presentation.my.SystemLabelProviderMyTest.class,
    de.dlr.premise.system.presentation.my.SystemLabelProviderMyWithDataTypeNamesTest.class,
    de.dlr.premise.system.presentation.my.SystemLabelProviderMyWithDuplicationsTest.class,

    // de.dlr.premise.migration
    de.dlr.premise.migration.MigrationModelTest.class,
    de.dlr.premise.migration.ModelVersionTest.class,
    de.dlr.premise.migration.strategies.MigrateUnversionedToV102Test.class,
    de.dlr.premise.migration.strategies.MigrateV102ToV103Test.class,
    de.dlr.premise.migration.strategies.MigrateV103ToV104Test.class,
    de.dlr.premise.migration.strategies.MigrateV104ToV105Test.class,
    de.dlr.premise.migration.strategies.MigrateV105ToV106Test.class,
    de.dlr.premise.migration.strategies.MigrateV106ToV107Test.class,
    de.dlr.premise.migration.strategies.MigrateV107ToV108Test.class,
    de.dlr.premise.migration.strategies.MigrateV108ToV109Test.class,
    de.dlr.premise.migration.strategies.MigrateV109ToV110Test.class,
    de.dlr.premise.migration.strategies.MigrateV110ToV111Test.class,
    de.dlr.premise.migration.strategies.MigrateV111ToV112Test.class,
    de.dlr.premise.migration.strategies.MigrateV112ToV113Test.class,

    // de.dlr.premise.validation
    de.dlr.premise.validation.BalancingValidationTest.class,
    de.dlr.premise.validation.ModeValidationTest.class,
    de.dlr.premise.validation.ModeValueRefValidationTest.class,
    de.dlr.premise.validation.ParameterValidationTest.class,
    de.dlr.premise.validation.TransitionBalancingValidationTest.class,
    de.dlr.premise.validation.TransitionValidationTest.class,

    // de.dlr.premise.constraints
    de.dlr.premise.constraints.MarkerCreatingConstraintViolationHandlerTest.class,
    de.dlr.premise.constraints.ConstraintCheckTest.class,
    de.dlr.premise.constraints.valueconstraints.ConstraintToValueMapperTest.class,
    de.dlr.premise.constraints.valueconstraints.ValueConstraintCheckerTest.class,
})

public class ModelTestsRandom {

}
