/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
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
    de.dlr.premise.registry.impl.my.RegistryFactoryImplMyTest.class,
    de.dlr.premise.representation.impl.my.RepresentationFactoryImplMyTest.class,
    de.dlr.premise.util.cyclecheck.BalancingCycleCheckerTest.class,
    de.dlr.premise.util.cyclecheck.tarjan.NodeTest.class,
    de.dlr.premise.util.cyclecheck.tarjan.TarjanTest.class,
    de.dlr.premise.util.PremiseHelperTest.class,
    de.dlr.premise.util.LabelHelperTest.class,

    // de.dlr.premise.constraints
    de.dlr.premise.constraints.ConstraintCheckTest.class,
    de.dlr.premise.constraints.valueconstraints.ConstraintToValueMapperTest.class,
    de.dlr.premise.constraints.valueconstraints.ValueConstraintCheckerTest.class,

    // de.dlr.premise.constraints.ui
    de.dlr.premise.constraints.MarkerCreatingConstraintViolationHandlerTest.class,

    // de.dlr.premise.edit
    de.dlr.premise.element.provider.my.ElementImageProviderMyTest.class,
    de.dlr.premise.element.provider.my.GuardCombinationItemProviderMyTest.class,
    de.dlr.premise.element.provider.my.ModeGuardItemProviderMyTest.class,
    de.dlr.premise.element.provider.my.ModeItemProviderMyTest.class,
    de.dlr.premise.element.provider.my.RelationItemProviderMyTest.class,
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
    de.dlr.premise.functions.provider.my.ModeRangeConstraintItemProviderMyTest.class,
    de.dlr.premise.functions.provider.my.RangeConstraintItemProviderMyTest.class,
    de.dlr.premise.functions.provider.my.RequiredParameterItemProviderMyTest.class,
    de.dlr.premise.functions.provider.my.UseCaseImageProviderMyTest.class,
    de.dlr.premise.functions.provider.my.UseCaseItemProviderMyTest.class,
    de.dlr.premise.functions.provider.my.UseCaseItemProviderMyWithDuplicatesTest.class,
    de.dlr.premise.functions.provider.my.UseCaseItemProviderMyWithoutDataTypeNamesTest.class,
    de.dlr.premise.registry.provider.my.RegistryImageProviderMyTest.class,
    de.dlr.premise.registry.provider.my.RegistryItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.BalancingItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.ConnectionItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.ModeValueRefItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.ParameterItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.SystemComponentItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.SystemImageProviderMyTest.class,
    de.dlr.premise.system.provider.my.SystemItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.SystemItemProviderMyWithDataTypeNamesTest.class,
    de.dlr.premise.system.provider.my.SystemItemProviderMyWithDuplicationsTest.class,
    de.dlr.premise.system.provider.my.TransitionBalancingItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.TransitionItemProviderMyTest.class,
    de.dlr.premise.system.provider.my.TransitionParameterItemProviderMyTest.class,

    // de.dlr.premise.editor
    de.dlr.premise.system.handler.ComponentDefinitionExtractorTest.class,
    de.dlr.premise.system.presentation.my.SystemLabelProviderMyTest.class,
    de.dlr.premise.system.presentation.my.SystemLabelProviderMyWithDataTypeNamesTest.class,
    de.dlr.premise.system.presentation.my.SystemLabelProviderMyWithDuplicationsTest.class,

    // de.dlr.premise.migration
    de.dlr.premise.migration.MigrationModelTest.class,
    de.dlr.premise.migration.ModelFileTypeTest.class,
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
    de.dlr.premise.migration.strategies.MigrateV113ToV114Test.class,
    de.dlr.premise.migration.strategies.MigrateV114ToV115Test.class,
    de.dlr.premise.migration.strategies.MigrateV115ToV116Test.class,
    de.dlr.premise.migration.strategies.MigrateV116ToV117Test.class,
    de.dlr.premise.migration.strategies.MigrateV117ToV118Test.class,
    de.dlr.premise.migration.strategies.MigrateV118ToV119Test.class,
    de.dlr.premise.migration.strategies.MigrateV119ToV120Test.class,
    de.dlr.premise.migration.strategies.MigrateV120ToV121Test.class,
    de.dlr.premise.migration.strategies.MigrateV121ToV122Test.class,
    de.dlr.premise.migration.strategies.MigrateV122ToV123Test.class,
    de.dlr.premise.migration.strategies.MigrateV123ToV124Test.class,
    de.dlr.premise.migration.strategies.MigrateV124ToV125Test.class,
    de.dlr.premise.migration.strategies.MigrateV125ToV126Test.class,
    de.dlr.premise.migration.strategies.MigrateV126ToV127Test.class,
    de.dlr.premise.migration.strategies.MigrateV127ToV128Test.class,
    de.dlr.premise.migration.strategies.MigrateV128ToV129Test.class,
    de.dlr.premise.migration.strategies.MigrateV129ToV130Test.class,
    de.dlr.premise.migration.strategies.MigrateV130ToV131Test.class,
    de.dlr.premise.migration.strategies.MigrateV131ToV132Test.class,
    de.dlr.premise.migration.strategies.MigrateV132ToV133Test.class,

    // de.dlr.premise.query
    de.dlr.premise.query.persistent.MetadataQueryAccessTest.class,

    // de.dlr.premise.states
    de.dlr.premise.states.test.reachability.mode.ModeReachabilityCheckerTest.class,
    de.dlr.premise.states.test.reachability.StateReachabilityCheckerTest.class,
    de.dlr.premise.states.test.stability.StableSubstatesGeneratorTest.class,
    de.dlr.premise.states.test.StateCheckerAdapterTest.class,
    de.dlr.premise.states.test.util.StateCheckingHelperTest.class,

    // de.dlr.premise.util
    // de.dlr.premise.util.logging.LoggerFacadeTest.class,

    // de.dlr.premise.validation.ui
    de.dlr.premise.validation.BalancingValidationTest.class,
    de.dlr.premise.validation.ModeValidationTest.class,
    de.dlr.premise.validation.ModeValueRefValidationTest.class,
    de.dlr.premise.validation.ParameterValidationTest.class,
    de.dlr.premise.validation.SatisfiesValidationTest.class,
    de.dlr.premise.validation.TransitionBalancingValidationTest.class,
    de.dlr.premise.validation.TransitionValidationTest.class,

    // de.dlr.calc.engine.dsl
    de.dlr.calc.engine.dsl.BalancingInterpreterTest.class,
    de.dlr.calc.engine.dsl.BalancingScopeTest.class,
    de.dlr.calc.engine.dsl.CalculationBalancingScopeTest.class,
    de.dlr.calc.engine.dsl.ParameterRenamerTest.class,

    // de.dlr.calc.engine.excel
    de.dlr.calc.engine.excel.ExcelEngineSXTest.class,
    de.dlr.calc.engine.excel.ExcelUtilTest.class,
    de.dlr.calc.engine.excel.SpreadsheetCalculatorTest.class,

    // de.dlr.calc.engine.java
    de.dlr.calc.engine.java.JavaCalculatorGetMethodTest.class,
    de.dlr.calc.engine.java.JavaCalculatorTest.class,

    // de.dlr.calc.engine.matlab
    de.dlr.calc.engine.matlab.MatlabConfigTest.class,
    de.dlr.calc.engine.matlab.MatlabEngineJMBridgeTest.class,
    de.dlr.calc.engine.matlab.MatlabEngineTest.class,
    de.dlr.calc.engine.matlab.MatlabHelperTest.class,

    // de.dlr.premise.calc
    de.dlr.premise.calculation.ABalancingTest.class,
    de.dlr.premise.calculation.impl.PremiseCalcFactoryImplMyTest.class,
    de.dlr.premise.calculation.impl.TransitionParameterImplMyTest.class,
    de.dlr.premise.calculation.legacy.ABalancingNotificationTest.class,
    de.dlr.premise.calculation.TransitionBalancingTest.class,
    de.dlr.premise.calculation.valuechangedcontentadapter.ValueChangedContentAdapterTest.class,

    // de.dlr.exchange.base.xtend
    de.dlr.exchange.base.xtend.test.GeneratorHelperTest.class,

    // de.dlr.exchange.excel.fha
    de.dlr.exchange.excel.fha.test.SynchronizationTest.class,

    // de.dlr.exchange.excel.trace.xtend
    de.dlr.exchange.excel.trace.xtend.GeneratorTest.class,

    // de.dlr.exchange.excel.tradeoff.xtend
    de.dlr.exchange.excel.tradeoff.xtend.GeneratorTest.class,

    // de.dlr.exchange.graphml.block
    de.dlr.exchange.graphml.block.GraphMLBlockGeneratorTest.class,
    de.dlr.exchange.graphml.block.GraphMLBlockSettingsTest.class,

    // de.dlr.exchange.graphml.cfta.xtend
    de.dlr.exchange.graphml.cfta.xtend.test.CFTAExporterTest.class,

    // de.dlr.exchange.graphml.fta.xtend
    de.dlr.exchange.graphml.fta.xtend.test.GeneratorControlSysQualitativeTest.class,
    de.dlr.exchange.graphml.fta.xtend.test.GeneratorControlSysTest.class,
    de.dlr.exchange.graphml.fta.xtend.test.GeneratorPowertrainTest.class,
    de.dlr.exchange.graphml.fta.xtend.test.GeneratorVFW614Test.class,

    // de.dlr.exchange.graphml.states.xtend
    de.dlr.exchange.graphml.states.xtend.test.GeneratorAircraftTest.class,
    de.dlr.exchange.graphml.states.xtend.test.GeneratorPowerTrainTest.class,
    de.dlr.exchange.graphml.states.xtend.test.GeneratorVFWTest.class,

    // de.dlr.exchange.matlab.struct.xtend
    de.dlr.exchange.matlab.struct.xtend.GeneratorTest.class,

    // de.dlr.exchange.petrinet.base.xtend

    // de.dlr.exchange.petrinet.pnml.pipe.xtend
    de.dlr.exchange.petrinet.pnml.pipe.test.GeneratorTest.class,

    // de.dlr.exchange.petrinet.snoopy.xtend
    de.dlr.exchange.petrinet.snoopy.test.GeneratorTest.class,

    // de.dlr.premise.view.graphml
    de.dlr.aspect.graphml.GraphMLToPremiseTest.class,
    de.dlr.aspect.graphml.PremiseToGraphMLTest.class,

    // de.dlr.premise.view.GraphicalEditorView
    de.dlr.premise.view.graphicaleditorview.controller.APremiseToModelMapperTest.class,
    de.dlr.premise.view.graphicaleditorview.model.GraphableModelTest.class,
})

public class AllTestsRandom {

}
