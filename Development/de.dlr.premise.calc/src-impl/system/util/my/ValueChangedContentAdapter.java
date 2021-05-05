/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package system.util.my;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.IdentityCommand;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Injector;

import de.dlr.calc.engine.dsl.ParameterRenamer;
import de.dlr.calc.engine.dsl.xtext.ui.internal.CalcDslActivator;
import de.dlr.premise.calculation.BalancingCalculator;
import de.dlr.premise.calculation.IBalancingCalculator;
import de.dlr.premise.calculation.transition.ITransitionBalancingCalculator;
import de.dlr.premise.calculation.transition.TransitionBalancingCalculator;
import de.dlr.premise.component.ParameterDefinition;
import de.dlr.premise.element.ElementPackage;
import de.dlr.premise.element.GuardCombination;
import de.dlr.premise.element.Mode;
import de.dlr.premise.element.ModeGuard;
import de.dlr.premise.element.StateMachine;
import de.dlr.premise.element.Transition;
import de.dlr.premise.functionpool.ACalculationEngine;
import de.dlr.premise.functionpool.AFnDef;
import de.dlr.premise.functionpool.CalcEngineJava;
import de.dlr.premise.functionpool.CalcEngineScript;
import de.dlr.premise.functionpool.CalcEngineSpreadSheet;
import de.dlr.premise.functionpool.FnDefJava;
import de.dlr.premise.functionpool.FnDefScript;
import de.dlr.premise.functionpool.FnDefSpreadSheet;
import de.dlr.premise.functionpool.FnInputSpreadSheet;
import de.dlr.premise.functionpool.FnOutputSpreadSheet;
import de.dlr.premise.functionpool.FunctionpoolPackage;
import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.registry.RegistryPackage;
import de.dlr.premise.registry.Value;
import de.dlr.premise.states.data.State;
import de.dlr.premise.system.ABalancing;
import de.dlr.premise.system.Balancing;
import de.dlr.premise.system.ComponentReference;
import de.dlr.premise.system.ComponentReferenceSubstitutionMapping;
import de.dlr.premise.system.ModeValueRef;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.SystemPackage;
import de.dlr.premise.system.TransitionBalancing;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.SubstitutionMappingHelper;
import de.dlr.premise.util.cyclecheck.BalancingCycleChecker;
import de.dlr.premise.util.cyclecheck.IBalancingCycleChecker;
import de.dlr.premise.util.scope.ScopedEObjectFactory;
import de.dlr.premise.util.scope.ScopedEObjectHelper;

public class ValueChangedContentAdapter extends EContentAdapter {

    private static int depth = 0;
    protected static int notifications = 0;
    public final static String LOGMSG_IDENTIFIED = "(identified)";
    public final static String LOGMSG_HANDLED = "(handled)";
    // verbose level from 0 to 3
    public static int verboseLevel = 0;

    private boolean noCycle = true;
    private boolean recalculating = false;
    
    private final ParameterRenamer parameterRenamer;

    public ValueChangedContentAdapter() {
        super();
        parameterRenamer = createParameterRenamer();
    }

    @Override
    public void notifyChanged(Notification notification) {
        super.notifyChanged(notification);

        if (recalculating == true) {
            return;
        }
        
        if (!notification.isTouch()) {
            Object notifier = notification.getNotifier();

            if (notifier instanceof Value) {
                handleValue(notification);
            } else if (notifier instanceof AParameterDef) {
                handleAParameterDef(notification);
            } else if (notifier instanceof ParameterDefinition) {
                handleParameterDefinition(notification);
            } else if (notifier instanceof ModeValueRef) {
                handleModeValueRef(notification);
            } else
            // update system states
            if (notifier instanceof StateMachine) {
                handleStateMachine(notification);
            } else if (notifier instanceof ComponentReference) {
                handleComponentReference(notification);
            } else if (notifier instanceof Mode) {
                handleMode(notification);
            } else if (notifier instanceof Transition) {
                handleTransition(notification);
            } else if (notifier instanceof GuardCombination) {
                handleGuardCombination(notification);
            } else if (notifier instanceof ModeGuard) {
                handleModeGuard(notification);
            } else
            // continue re-calculation
            if (notifier instanceof ABalancing) {
                handleABalancing(notification);
            } else if (notifier instanceof FnDefSpreadSheet) {
                handleFnDefSpreadSheet(notification);
            } else if (notifier instanceof FnDefScript) {
                handleFnDefScript(notification);
            } else if (notifier instanceof FnDefJava) {
                handleFnDefJava(notification);
            } else if (notifier instanceof FnInputSpreadSheet) {
                handleFnInputSpreadSheet(notification);
            } else if (notifier instanceof FnOutputSpreadSheet) {
                handleFnOutputSpreadSheet(notification);
            } else if (notifier instanceof CalcEngineSpreadSheet) {
                handleCalcEngineSpreedSheetChanges(notification);
            } else if (notifier instanceof CalcEngineScript) {
                handleCalcEngineScriptChanges(notification);
            } else if (notifier instanceof CalcEngineJava) {
                handleCalcEngineJavaChanges(notification);
            } else if (notifier instanceof ComponentReferenceSubstitutionMapping) {
                handleComponentReferenceSubstitutionMapping(notification);
            }
        }

        depth--;
    }


    

    public synchronized void recalculate(ResourceSet resSet, IProgressMonitor monitor) {
        recalculating = true;
        
        List<ABalancing<?>> balsInOrder = getBalancingsInDependencyOrder(resSet);

        SubMonitor calculationMonitor = SubMonitor.convert(monitor, balsInOrder.size());
        
        // Execute the calculations in a command, so that the balancing calculator can associate the calculation commands with it
        CommandStack commandStack = AdapterFactoryEditingDomain.getEditingDomainFor(resSet).getCommandStack();
        commandStack.execute(new IdentityCommand() {
            public void execute() {
                for (ABalancing<?> bal : balsInOrder) {
                    if (calculationMonitor.isCanceled()) {
                        break;
                    }
                    
                    calculationMonitor.subTask("Calculating: " + PremiseHelper.getMeaningfulName(bal));
                    calculateBal(bal, "", null, true);
                    calculationMonitor.worked(1);
                }
            }
        });
        
        recalculating = false;
    }

    
    private List<ABalancing<?>> getBalancingsInDependencyOrder(ResourceSet resSet) {
        EcoreUtil.resolveAll(resSet);
        
        @SuppressWarnings("unchecked")
        Iterator<ABalancing<AParameterDef>> allBalsWrapped = (Iterator<ABalancing<AParameterDef>>) (Iterator<?>) ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(resSet, ABalancing.class).iterator();
        
        ImmutableSetMultimap.Builder<ABalancing<?>, AParameterDef> balSourcesBuilder = ImmutableSetMultimap.builder();
        ImmutableSetMultimap.Builder<ABalancing<?>, AParameterDef> balTargetsBuilder = ImmutableSetMultimap.builder();
        
        ImmutableSet.Builder<ABalancing<?>> balsWithoutSourceBuilder = ImmutableSet.builder();

        int allBalsWrappedSize = 0;

        while (allBalsWrapped.hasNext()) {
            ABalancing<AParameterDef> bal = allBalsWrapped.next();
            
            checkCycle(bal);
            if (!noCycle) {
                break;
            }
            
            EList<AParameterDef> sources = bal.getSources();
            if (sources.isEmpty()) {
                balsWithoutSourceBuilder.add(bal);
            } else {
                balSourcesBuilder.putAll(bal, sources);
            }
            if (bal.getTarget() != null) {
                balTargetsBuilder.put(bal, bal.getTarget());
            }

            allBalsWrappedSize++;
        }

        ImmutableSetMultimap<ABalancing<?>, AParameterDef> balSources = balSourcesBuilder.build();
        ImmutableSetMultimap<AParameterDef, ABalancing<?>> balsBySource = balSources.inverse();
        
        ImmutableSetMultimap<ABalancing<?>, AParameterDef> balTargets = balTargetsBuilder.build();
        ImmutableSetMultimap<AParameterDef, ABalancing<?>> balsByTarget = balTargets.inverse();
        
        ImmutableSet<ABalancing<?>> balsWithoutSource = balsWithoutSourceBuilder.build();

        // Initial sources are parameters that are source of a balancing but not target of any balancing
        Set<AParameterDef> initialSources = Sets.difference(balsBySource.keySet(), balsByTarget.keySet());
        
        // Start building up a list of balancings in order of dependency
        List<ABalancing<?>> balsInOrder = Lists.newArrayList();
        
        // Add balancings without source first..
        balsInOrder.addAll(balsWithoutSource);

        // ... then add initial balancings by initial sources ...
        for (AParameterDef initialSource : initialSources) {
            for (ABalancing<?> bal : balsBySource.get(initialSource)) {
                if (initialSources.containsAll(ImmutableList.copyOf(bal.getSources()))) {
                    if (!balsInOrder.contains(bal)) {
                        balsInOrder.add(bal);
                    }
                }
            }
        }
        
        // iterate over the initial balancings and sucessively add dependencies
        for (int i = 0; i < balsInOrder.size(); i++) {
            ABalancing<?> bal = balsInOrder.get(i);
            ImmutableSet<AParameterDef> targets = balTargets.get(bal);
            for (AParameterDef target : targets) {
                for (ABalancing<?> nextBal : balsBySource.get(target)) {
                    int index = balsInOrder.indexOf(nextBal);

                    if (index != -1) {
                        // balancing is already in list, but depends on the target, move it back
                        if (index < i) {
                            balsInOrder.remove(index);
                            i--;
                            balsInOrder.add(nextBal);
                        }
                    } else {
                        balsInOrder.add(nextBal);
                    }
                }
            }
        }

        if (balsInOrder.size() != allBalsWrappedSize) {
            System.err.println("Balancings missed: " + (allBalsWrappedSize - balsInOrder.size()));
        }

        return balsInOrder;
    }
    
    private String getLogMsg(Notification notification) {

        Object notifier = notification.getNotifier();

        String logMsg = "";

        if (verboseLevel >= 1) {
            // creates a notification log message of kind: { }* 1. ClassName
            // 'Name' EventType {FeatureName} {(handled)}
            // Example: "  2. ParameterImpl 'ParName' SET value"
            if (depth == 0) {
                notifications = 1;
            }

            for (int i = 0; i < depth; i++) {
                logMsg += "  ";
            }

            // set notification number
            logMsg += notifications + ".";
            depth++;
            logMsg += " " + notifier.getClass().getSimpleName();

            // get name
            if (notifier instanceof ANameItem) {
                String name = ((ANameItem) notifier).getName();
                logMsg += " '" + (name == null ? null : name.replace(" ", "")) + "'";
            } else if (((EObject) notifier).eContainer() != null) {
                EObject parent = ((EObject) notifier).eContainer();
                if (parent instanceof ANameItem) {
                    String name = ((ANameItem) parent).getName();
                    logMsg += " '" + (name == null ? null : name.replace(" ", "")) + "'";
                } else {// else AUniqueNameItem is still possible
                    logMsg += " ''";
                }
            } else {
                logMsg += " ''";
            }

            // add event type
            switch (notification.getEventType()) {
            case Notification.SET:
                logMsg += " SET";
                break;
            case Notification.ADD:
                logMsg += " ADD";
                break;
            case Notification.REMOVE:
                logMsg += " REMOVE";
                break;
            case Notification.REMOVE_MANY:
                logMsg += " REMOVE_MANY";
                break;
            case Notification.REMOVING_ADAPTER:
                logMsg += " REMOVING_ADAPTER";
                break;
            case Notification.RESOLVE:
                logMsg += " RESOLVE";
                break;
            default:
                logMsg += " " + notification.getEventType();
            }

            // add feature name
            if (notification.getFeature() != null) {
                String name = ((ENamedElement) notification.getFeature()).getName();
                logMsg += " " + name;
            }
        } else {
            depth = 0;
        }
        return logMsg;
    }

    private void handleFnOutputSpreadSheet(Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);

        Object notifier = notification.getNotifier();

        int featureId = notification.getFeatureID(FnOutputSpreadSheet.class);
        if (featureId == FunctionpoolPackage.FN_OUTPUT_SPREAD_SHEET__CELL) {

            log(logMsg + " " + LOGMSG_IDENTIFIED);

            for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingFnDef((AFnDef) ((FnOutputSpreadSheet) notifier).eContainer())) {
                calculateBal(bal, logMsg, null, false);
            }
        } else {
            log3(logMsg);
        }
    }

    private void handleFnInputSpreadSheet(Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);

        Object notifier = notification.getNotifier();

        int featureId = notification.getFeatureID(FnInputSpreadSheet.class);
        if (featureId == FunctionpoolPackage.FN_INPUT_SPREAD_SHEET__CELL) {
            log(logMsg + " " + LOGMSG_IDENTIFIED);
            for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingFnDef((AFnDef) ((FnInputSpreadSheet) notifier).eContainer())) {
                calculateBal(bal, logMsg, null, false);
            }
        } else {
            log3(logMsg);
        }
    }    
    
    private void handleFnDefJava(Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);

        Object notifier = notification.getNotifier();

        int featureId = notification.getFeatureID(FnDefJava.class);
        if (featureId == FunctionpoolPackage.FN_DEF_JAVA__CALC_ENGINE || featureId == FunctionpoolPackage.FN_DEF_JAVA__INPUTS
                || featureId == FunctionpoolPackage.FN_DEF_JAVA__OUTPUT
                || featureId == FunctionpoolPackage.FN_DEF_JAVA__LAST_INPUT_MULTIPLE
                || featureId == FunctionpoolPackage.FN_DEF_JAVA__METHOD_NAME) {
            log(logMsg + " " + LOGMSG_IDENTIFIED);
            for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingFnDef((AFnDef) notifier)) {
                calculateBal(bal, logMsg, null, false);
            }
        } else {
            log3(logMsg);
        }
    }

    private void handleFnDefScript(Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);

        Object notifier = notification.getNotifier();

        int featureId = notification.getFeatureID(FnDefScript.class);
        if (featureId == FunctionpoolPackage.FN_DEF_SCRIPT__CALC_ENGINE || featureId == FunctionpoolPackage.FN_DEF_SCRIPT__OUTPUT
                || featureId == FunctionpoolPackage.FN_DEF_SCRIPT__INPUTS
                || featureId == FunctionpoolPackage.FN_DEF_SCRIPT__LAST_INPUT_MULTIPLE
                || featureId == FunctionpoolPackage.FN_DEF_SCRIPT__SCRIPT_NAME) {
            log(logMsg + " " + LOGMSG_IDENTIFIED);
            for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingFnDef((AFnDef) notifier)) {
                calculateBal(bal, logMsg, null, false);
            }
        } else {
            log3(logMsg);
        }
    }

    private void handleFnDefSpreadSheet(Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);

        Object notifier = notification.getNotifier();

        int featureId = notification.getFeatureID(FnDefSpreadSheet.class);
        if (featureId == FunctionpoolPackage.FN_DEF_SPREAD_SHEET__CALC_ENGINE
                || featureId == FunctionpoolPackage.FN_DEF_SPREAD_SHEET__OUTPUT
                || featureId == FunctionpoolPackage.FN_DEF_SPREAD_SHEET__INPUTS
                || featureId == FunctionpoolPackage.FN_DEF_SPREAD_SHEET__LAST_INPUT_MULTIPLE
                || featureId == FunctionpoolPackage.FN_DEF_SPREAD_SHEET__MULTIPLE_INPUTS_CELL_DIRECTION
                || featureId == FunctionpoolPackage.FN_DEF_SPREAD_SHEET__SHEET_NAME) {
            log(logMsg + " " + LOGMSG_IDENTIFIED);
            for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingFnDef((AFnDef) notifier)) {
                calculateBal(bal, logMsg, null, false);
            }
        } else {
            log3(logMsg);
        }
    }

    private void handleABalancing(Notification notification) {
        
        // create log message
        String logMsg = getLogMsg(notification);
        Object notifier = notification.getNotifier();
        
        int featureId = notification.getFeatureID(Balancing.class);

        boolean isBalancingFeature = featureId == SystemPackage.BALANCING__FUNCTION;

        boolean isTransitionBalancingFeature =
                featureId == SystemPackage.TRANSITION_BALANCING__TARGET 
                || featureId == SystemPackage.TRANSITION_BALANCING__SOURCES;

        if ((notifier instanceof Balancing && isBalancingFeature)
                || (notifier instanceof TransitionBalancing && isTransitionBalancingFeature)) {
            log(logMsg + " " + LOGMSG_IDENTIFIED);
            ABalancing<?> bal = (ABalancing<?>) notifier;
            // Cycle-Check with Tarjan Algorithm
            checkCycle(bal);
            calculateBal(bal, logMsg, null, false);  
        } else {
            log3(logMsg);
        }
        
    }

    private void handleModeGuard(Notification notification) {

        
        // create log message
        String logMsg = getLogMsg(notification);

        Object notifier = notification.getNotifier();

        int featureId = notification.getFeatureID(Mode.class);
        if (featureId == ElementPackage.MODE_GUARD__MODE_POINTER) {
            log(logMsg + " " + LOGMSG_IDENTIFIED);
            
            EObject trans = (EObject) notifier;
            while (!(trans instanceof Transition)) {
                trans = trans.eContainer();
                if (trans == null) {
                    return;
                }
            }
            
            // trans bals
            for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingTransition((Transition) trans)) {
                // do not re-calculate modeValues
                calculateBal(bal, logMsg, null, false);
            }
            
            // bals
            for (Balancing bal : CalcHelper.getBalancingsToRecalculateForStateMachine((StateMachine) trans.eContainer())) {
                calculateBal(bal, logMsg, null, false);
            }
        } else {
            log3(logMsg);
        }
    }

    private void handleGuardCombination(Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);

        Object notifier = notification.getNotifier();

        int featureId = notification.getFeatureID(GuardCombination.class);
        if (featureId == ElementPackage.GUARD_COMBINATION__JUNCTION
                || featureId == ElementPackage.GUARD_COMBINATION__CHILDREN) {

            log3(logMsg + " " + LOGMSG_IDENTIFIED);

            
            EObject trans = (EObject) notifier;
            while (!(trans instanceof Transition)) {
                trans = trans.eContainer();
                if (trans == null) {
                    return;
                }
            }
            
            // trans bals
            for (TransitionBalancing tb : ((Transition) trans).getBalancings()) {
                calculateBal(tb, logMsg, null, false);
            }
            
            // Bals
            for (Balancing bal : CalcHelper.getBalancingsToRecalculateForStateMachine((StateMachine) trans.eContainer())) {
                calculateBal(bal, logMsg, null, false);
            }
            
            
        } else {
            log3(logMsg);
        }
    }


    void handleTransition(Notification notification) {

        Object notifier = notification.getNotifier();
        
        // create log message
        String logMsg = getLogMsg(notification);

        int featureId = notification.getFeatureID(Transition.class);
        if (featureId == ElementPackage.TRANSITION__SOURCE_POINTER || featureId == ElementPackage.TRANSITION__TARGET_POINTER
                || featureId == ElementPackage.TRANSITION__CONDITION || featureId == ElementPackage.TRANSITION__PARAMETERS 
                || featureId == ElementPackage.TRANSITION__BEHAVIOR) {
            log3(logMsg + " " + LOGMSG_IDENTIFIED);
            // trans bals  
            for (TransitionBalancing tb : ((Transition)notifier).getBalancings()) {
                calculateBal(tb, logMsg, null, false);
            }
            // bals
            for (Balancing bal : CalcHelper.getBalancingsToRecalculateForStateMachine((StateMachine) ((Transition)notifier).eContainer())){
                calculateBal(bal, logMsg, null, false);
            }
        } else {
            log3(logMsg);
        }
    }

    private void handleMode(final Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);

        // check if the notifier was an Mode object
        Object notifier = notification.getNotifier();
        int featureId = notification.getFeatureID(Mode.class);

        if (featureId == ElementPackage.MODE__ENTRY_MODE) {
            log3(logMsg + " " + LOGMSG_IDENTIFIED);
            for (Balancing bal : CalcHelper.getBalancingsToRecalculateForStateMachine((StateMachine) ((Mode)notifier).eContainer())){
                calculateBal(bal, logMsg, null, false);
            }
        } else {
            log3(logMsg);
        }
    }

    /**
     * If a state machine was changed an update of all system modes is required.
     */
    private void handleStateMachine(final Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);

        // check if a system mode was changed
        Object notifier = notification.getNotifier();
        int featureId = notification.getFeatureID(StateMachine.class);
        if (featureId == ElementPackage.STATE_MACHINE__MODES || featureId == ElementPackage.STATE_MACHINE__TRANSITIONS) {
            log3(logMsg + " " + LOGMSG_IDENTIFIED);
            
            for (Balancing bal : CalcHelper.getBalancingsToRecalculateForStateMachine((StateMachine) notifier)) {
                calculateBal(bal, logMsg, null, false);
            }
        } else {
            log3(logMsg);
        }
    }
    
    private void handleComponentReference(final Notification notification) {
        // create log message
        String logMsg = getLogMsg(notification);
        
        Object notifier = notification.getNotifier();
        int featureId = notification.getFeatureID(ComponentReference.class);
        if (featureId == SystemPackage.COMPONENT_REFERENCE__ACTIVE_IMPLEMENTATION ||
                featureId == SystemPackage.COMPONENT_REFERENCE__COMPONENT_DEFINITION) {
            // recalculate all Related
            log(logMsg + " " + LOGMSG_IDENTIFIED);
            for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingSource((ComponentReference) notifier)) {
                checkCycle(bal);
                // TODO steh_ti review, is ModeValueCalculation needed here?
                calculateBal(bal, logMsg, null, false);
            }
            if (((ComponentReference) notifier).getActiveImplementation() == null) {
                ((ComponentReference) notifier).getSubstitutionMap().clear();
            }
        } else {
            log3(logMsg);
        }
    }

    private void handleModeValueRef(Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);

        // check notifier object
        Object notifier = notification.getNotifier();
        int featureId = notification.getFeatureID(ModeValueRef.class);
        if (featureId == SystemPackage.MODE_VALUE_REF__MODE_POINTERS || featureId == SystemPackage.MODE_VALUE_REF__VALUE) {
            log(logMsg + " " + LOGMSG_IDENTIFIED);

            AParameterDef paramDef = (AParameterDef) ((ModeValueRef) notifier).eContainer();
            List<AParameterDef> paramDefsWrapped = getWrappedParameters(paramDef);

            for (AParameterDef paramDefWrapped : paramDefsWrapped) {
                for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingSource(paramDefWrapped)) {
                    // if value was changed only
                    if (featureId == SystemPackage.MODE_VALUE_REF__VALUE) {
                        try {
                            State modeValueState = new State(((ModeValueRef) notifier).getModes());
                            calculateBal(bal, logMsg, s -> s.isCompatibleSuperstateOf(modeValueState), false);
                        } catch (IllegalArgumentException | IllegalStateException e) {
                            e.printStackTrace();
                        }
                    } else {
                        calculateBal(bal, logMsg, null, false);
                    }
                }
            }
        } else {
            log3(logMsg);
        }
    }

    private void handleAParameterDef(Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);

        Object notifier = notification.getNotifier();
        int featureId = notification.getFeatureID(AParameterDef.class);

        AParameterDef paramDef = (AParameterDef) notifier;
        List<AParameterDef> paramDefsWrapped = getWrappedParameters(paramDef);

        // Fix balancing functions that might reference unknown parameters
        if (featureId == RegistryPackage.ANAME_ITEM__NAME) {
            for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingSource(paramDef)) {
                if (bal instanceof Balancing) {
                    parameterRenamer.doRename((Balancing) bal);
                }
            }
        }

        // re-calculate if parameter value or (fully specified) modeValue is
        // added/copied/removed
        // (change is considered by the respective Value or ModeValueRef)
        if (featureId == SystemPackage.PARAMETER__VALUE || featureId == SystemPackage.TRANSITION_PARAMETER__VALUE) {
            log(logMsg + " " + LOGMSG_IDENTIFIED);
            for (AParameterDef paramDefWrapped : paramDefsWrapped) {
                for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingSource(paramDefWrapped)) {
                    if (paramDef instanceof Parameter) {
                        // recalculate all states for which there are no mode values
                        calculateBal(bal, logMsg, getParameterValueTargetStatePredicate((Parameter) paramDef), false);
                    } else {
                        // TransitionParameter has no ModeValues
                        calculateBal(bal, logMsg, null, false);
                    }
                }
            }
        } else if (featureId == SystemPackage.PARAMETER__MODE_VALUES
                && (notification.getEventType() == Notification.REMOVE || notification.getEventType() == Notification.REMOVE_MANY || ((ModeValueRef) notification.getNewValue()).getValue() != null)) {
            log(logMsg + " " + LOGMSG_IDENTIFIED);
            for (AParameterDef paramDefWrapped : paramDefsWrapped) {
                for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingSource(paramDefWrapped)) {
                    // re-calculate modeValues
                    calculateBal(bal, logMsg, null, false);
                }
            }
        } else {
            log3(logMsg);
        }
    }

    private void handleParameterDefinition(Notification notification) {
        Object notifier = notification.getNotifier();
        int featureId = notification.getFeatureID(ParameterDefinition.class);

        // Fix balancing functions that might reference unknown parameters
        if (featureId == RegistryPackage.ANAME_ITEM__NAME) {
            for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingSource((ParameterDefinition) notifier)) {
                if (bal instanceof Balancing) {
                    parameterRenamer.doRename((Balancing) bal);
                }
            }
            for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingTarget((ParameterDefinition) notifier)) {
                if (bal instanceof Balancing) {
                    parameterRenamer.doRename((Balancing) bal);
                }
            }
        }
    }
    
    private void handleValue(Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);

        // check if the notifier object is an Value object
        Object notifier = notification.getNotifier();
        int featureId = notification.getFeatureID(Value.class);
        if (featureId == RegistryPackage.VALUE__VALUE) {

            // recalculate all Related
            log(logMsg + " " + LOGMSG_IDENTIFIED);

            AParameterDef paramDef = (AParameterDef) ((Value) notifier).eContainer();
            
            Predicate<State> targetStatePredicate = null;
            if (paramDef instanceof Parameter) {
                // recalculate all states for which there are no mode values
                targetStatePredicate = getParameterValueTargetStatePredicate((Parameter) paramDef);
            } 
            
            List<AParameterDef> paramDefsWrapped = getWrappedParameters(paramDef);

            for (AParameterDef paramDefWrapped : paramDefsWrapped) {
                for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingSource(paramDefWrapped)) {
                    calculateBal(bal, logMsg, targetStatePredicate, false);
                }
            }
        } else {
            log3(logMsg);
        }
    }

    private void handleCalcEngineSpreedSheetChanges(Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);

        Object notifier = notification.getNotifier();

        int featureId = notification.getFeatureID(CalcEngineSpreadSheet.class);
        if (featureId == FunctionpoolPackage.CALC_ENGINE_SPREAD_SHEET__FILE_URI
                || featureId == FunctionpoolPackage.CALC_ENGINE_SPREAD_SHEET__PROPERTIES) {
            log(logMsg + " " + LOGMSG_IDENTIFIED);

            // get balancing
            EList<ABalancing<?>> balancings = CalcHelper.getBalancingsReferencingCalcEngine((ACalculationEngine) notifier);

            // handle balancing
            for (ABalancing<?> bal : balancings) {
                calculateBal(bal, logMsg, null, false);
            }
        } else {
            log3(logMsg);
        }
    }

    private void handleCalcEngineScriptChanges(Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);

        Object notifier = notification.getNotifier();

        int featureId = notification.getFeatureID(CalcEngineScript.class);
        if (featureId == FunctionpoolPackage.CALC_ENGINE_SCRIPT__WORK_DIR_URI
                || featureId == FunctionpoolPackage.CALC_ENGINE_SCRIPT__LIB_UR_IS
                || featureId == FunctionpoolPackage.CALC_ENGINE_SCRIPT__PROPERTIES) {
            log(logMsg + " " + LOGMSG_IDENTIFIED);
            for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingCalcEngine((ACalculationEngine) notifier)) {
                calculateBal(bal, logMsg, null, false);
            }
        } else {
            log3(logMsg);
        }
    }

    private void handleCalcEngineJavaChanges(Notification notification) {
        // create log message
        String logMsg = getLogMsg(notification);

        Object notifier = notification.getNotifier();

        int featureId = notification.getFeatureID(CalcEngineJava.class);
        if (featureId == FunctionpoolPackage.CALC_ENGINE_JAVA__CLASS_NAME) {
            log(logMsg + " " + LOGMSG_IDENTIFIED);
            for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingCalcEngine((ACalculationEngine) notifier)) {
                calculateBal(bal, logMsg, null, false);
            }
        } else {
            log3(logMsg);
        }
        
    }

    private void handleComponentReferenceSubstitutionMapping(Notification notification) {

        // create log message
        String logMsg = getLogMsg(notification);
        
        Object notifier = notification.getNotifier();
        int featureId = notification.getFeatureID(ComponentReferenceSubstitutionMapping.class);
        if (featureId == SystemPackage.COMPONENT_REFERENCE_SUBSTITUTION_MAPPING__PINNED) {

            // recalculate all Related
            log(logMsg + " " + LOGMSG_IDENTIFIED);

            EObject substitution = ((ComponentReferenceSubstitutionMapping) notifier).getSubstitution();
            
            if (substitution instanceof AParameterDef) {
                List<AParameterDef> paramDefsWrapped = getWrappedParameters((AParameterDef) substitution);

                for (AParameterDef paramDefWrapped : paramDefsWrapped) {
                    for (ABalancing<?> bal : CalcHelper.getBalancingsReferencingSource(paramDefWrapped)) {
                        calculateBal(bal, logMsg, null, false);
                    }
                }
            }
        } else {
            log3(logMsg);
        }
    }

    private void checkCycle(ABalancing<?> bal) {
        boolean hasCycle = false;
        for (ABalancing<?> scopedBal : ScopedEObjectFactory.INSTANCE.createAllScopedEObjects(bal)) {
            IBalancingCycleChecker cycleCheck = createCycleChecker(scopedBal);
            hasCycle = hasCycle || cycleCheck.hasCycle();
        }
        
        if (hasCycle) {
            noCycle = false;
            System.err.println("Cycle detected! " + PremiseHelper.getMeaningfulName(bal));
        } else {
            noCycle = true;
        }
    }

    private Predicate<State> getParameterValueTargetStatePredicate(Parameter parameter) {
        Set<State> notValueDependentStates = parameter.getModeValues().stream()
                .map(mv -> State.createNewStateOrNull(mv.getModes()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        return (targetParameterState -> !notValueDependentStates.stream().anyMatch(fs -> fs.isCompatibleSubstateOf(targetParameterState)));
    }

    private List<AParameterDef> getWrappedParameters(AParameterDef paramDef) {
        List<AParameterDef> parameters = new ArrayList<>();

        if (paramDef.eContainer() instanceof ComponentReferenceSubstitutionMapping) {
            // If the parameter is in a substitution mapping, we just consider it in the given scope
            ComponentReferenceSubstitutionMapping mapping = (ComponentReferenceSubstitutionMapping) paramDef.eContainer();

            ComponentReference compRef = (ComponentReference) mapping.eContainer();
            ImmutableList<ComponentReference> scope =
                    ImmutableList.<ComponentReference> builder().add(compRef).addAll(mapping.getRelativeScope()).build();

            AParameterDef original = (AParameterDef) mapping.getOriginal();
            
            parameters.add(ScopedEObjectFactory.INSTANCE.createScopedEObjectForScope(original, scope));
        } else {
            // The "original" parameter is changed, consider all instances that are not substituted
            for (AParameterDef parameterWrapped : ScopedEObjectFactory.INSTANCE.createAllScopedEObjects(paramDef)) {
                ImmutableSet<ComponentReference> scope = ScopedEObjectHelper.getScope(parameterWrapped);
                if (scope.isEmpty()) {
                    parameters.add(parameterWrapped);
                } else {
                    ComponentReference compRef = scope.iterator().next();
                    // TODO steh_ti remove tmp cast
                    if (!SubstitutionMappingHelper.containsKey(compRef.getSubstitutionMap(), parameterWrapped)) {
                        parameters.add(parameterWrapped);
                    }
                }
            }
        }

        return parameters;
    }
    
    protected void log(String msg) {
        if (verboseLevel >= 1) {
            System.out.println(msg);
            // increase notifications counter
            notifications++;
        }
    }

    protected void log2(String msg) {
        if (verboseLevel >= 2) {
            System.out.println(msg);
        }
    }

    protected void log3(String msg) {
        if (verboseLevel >= 3) {
            System.out.println(msg);
        }
    }

    private void calculateBal(ABalancing<?> aBal, String logMsg, Predicate<State> targetStatePredicate, boolean sync) {
        if (aBal instanceof Balancing) {
            Balancing bal = (Balancing) aBal;
            for (Balancing balancingWrapped : ScopedEObjectFactory.INSTANCE.createAllScopedEObjects(bal)) {
                IBalancingCalculator balCalc = createBalancingCalculator();
                if (noCycle) {
                    balCalc.calculate(balancingWrapped , targetStatePredicate, sync);
                    log2(logMsg + " " + LOGMSG_HANDLED);
                }
            }
        } else if (aBal instanceof TransitionBalancing) {
            ITransitionBalancingCalculator balCalc = createTransitionBalancingCalculator();
            if (noCycle) {
                boolean result = balCalc.calculate((TransitionBalancing) aBal);
                if (result) {
                    log2(logMsg + " " + LOGMSG_HANDLED);
                }
            }
        }
    }

    protected IBalancingCalculator createBalancingCalculator() {
        return new BalancingCalculator();
    }

    protected ITransitionBalancingCalculator createTransitionBalancingCalculator() {
        return new TransitionBalancingCalculator();
    }

    protected IBalancingCycleChecker createCycleChecker(ABalancing<?> bal) {
        return new BalancingCycleChecker(PremiseHelper.getRootNotifier(bal), bal);
    }
    
    protected ParameterRenamer createParameterRenamer() {
        CalcDslActivator activator = CalcDslActivator.getInstance();
        Injector injector = activator.getInjector(CalcDslActivator.DE_DLR_CALC_ENGINE_DSL_XTEXT_CALCDSL);
        return injector.getInstance(ParameterRenamer.class);
    }

}
