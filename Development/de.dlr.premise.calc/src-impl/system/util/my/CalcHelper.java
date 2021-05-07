/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package system.util.my;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import de.dlr.premise.component.ComponentDefinition;
import de.dlr.premise.component.ComponentReferenceDirectPointer;
import de.dlr.premise.component.ComponentReferencePointer;
import de.dlr.premise.component.IDefinition;
import de.dlr.premise.component.ParameterDefinition;
import de.dlr.premise.element.Mode;
import de.dlr.premise.element.StateMachine;
import de.dlr.premise.element.Transition;
import de.dlr.premise.functionpool.ACalculationEngine;
import de.dlr.premise.functionpool.AFnDef;
import de.dlr.premise.graph.APointer;
import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.system.ABalancing;
import de.dlr.premise.system.Balancing;
import de.dlr.premise.system.ComponentReference;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.TransitionBalancing;
import de.dlr.premise.system.TransitionParameter;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.scope.ScopedEObjectFactory;

public class CalcHelper {

    /**
     * Gets all ABalancings that are referencing a given (Transition)Parameter as source
     * 
     * @param param
     * @return List of ABalancings that are referencing param like Balancing, NestedBalancing, TransitionBalancing
     */
    public static EList<ABalancing<?>> getBalancingsReferencingSource(final AParameterDef param) {
        EList<ABalancing<?>> result = new BasicEList<ABalancing<?>>();
        for (EObject balObj : ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(PremiseHelper.getRootNotifier(param), ABalancing.class)) {
            if (balObj instanceof Balancing) {
                Balancing bal = (Balancing) balObj;
                if (bal.getSources().contains(param)) {
                    result.add(bal);
                }
            } else { // TransitionBalancing
                ABalancing<?> bal = (ABalancing<?>) balObj;
                if (bal.getSources().contains(param)) {
                    result.add(bal);
                }
            }
        }
        return result;
    }    
    
    
    /**
     * Gets all ABalancings that are using a given ComponentReference in their sources or whose sources use a given component reference in their mode values
     * 
     * @param compRef
     * @return List of ABalancings that are referencing ComponentReference
     */
    public static EList<ABalancing<?>> getBalancingsReferencingSource(final ComponentReference compRef) {
        EList<ABalancing<?>> result = new BasicEList<ABalancing<?>>();
        for (EObject balObj : ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(PremiseHelper.getRootNotifier(compRef), ABalancing.class)) {
            if (balObj instanceof Balancing) {
                Balancing bal = (Balancing) balObj;
                for (APointer<AParameterDef> sourcePointer : bal.getSourcePointers()) {
                    if (pointerUsesCompRef(sourcePointer, compRef)) {
                        result.add(bal);
                    } else if (sourcePointer.getTarget() instanceof Parameter) {
                        Parameter sourceParam = (Parameter) sourcePointer.getTarget();
                        Stream<APointer<Mode>> modePointers = sourceParam.getModeValues().stream().flatMap(mv -> mv.getModePointers().stream());
                        if (modePointers.anyMatch(p -> pointerUsesCompRef(p, compRef))) {
                            result.add(bal);
                        }
                    }
                }
                if (bal.getTargetPointer() != null) {
                    APointer<AParameterDef> targetPointer = bal.getTargetPointer();
                    if (pointerUsesCompRef(targetPointer, compRef)) {
                        result.add(bal);
                    }
                }
            }  else if (balObj instanceof TransitionBalancing) {
                TransitionBalancing bal = (TransitionBalancing) balObj;
                for (APointer<TransitionParameter> sourcePointer : bal.getSourcePointers()) {
                    if (pointerUsesCompRef(sourcePointer, compRef)) {
                        result.add(bal);
                    }
                }
            }
        }
        return result;
    }


    private static boolean pointerUsesCompRef(final APointer<?> targetPointer, final ComponentReference compRef) {
        if (targetPointer instanceof ComponentReferencePointer) {
            ComponentReferencePointer<?> componentReferencePointer = (ComponentReferencePointer<?>) targetPointer;
            if (Objects.equals(componentReferencePointer.getComponentReference(), compRef)) {
               return true;
            }
        } else if (targetPointer instanceof ComponentReferenceDirectPointer) {
            ComponentReferenceDirectPointer<?> componentReferencePointer = (ComponentReferenceDirectPointer<?>) targetPointer;
            if (componentReferencePointer.getRelativeScope().contains(compRef)) {
               return true;
            }
        }
        return false;
    }

    
    /**
     * Gets all ABalancings that are using a given ParameterDefinition in their sources
     * 
     * @param paramDef
     * @return List of ABalancings that are referencing ParameterDefinition
     */
    public static EList<ABalancing<?>> getBalancingsReferencingSource(final ParameterDefinition paramDef) {
        EList<ABalancing<?>> result = new BasicEList<ABalancing<?>>();
        for (EObject balObj : ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(PremiseHelper.getRootNotifier(paramDef), ABalancing.class)) {
            if (balObj instanceof Balancing) {
                Balancing bal = (Balancing) balObj;
                for (APointer<AParameterDef> sourcePointer : bal.getSourcePointers()) {
                    if (sourcePointer instanceof ComponentReferencePointer) {
                        ComponentReferencePointer<AParameterDef> componentReferencePointer = (ComponentReferencePointer<AParameterDef>) sourcePointer;
                        @SuppressWarnings("unchecked")
                        IDefinition<AParameterDef> paramDefAsIDefinition = (IDefinition<AParameterDef>) (IDefinition<?>) paramDef;
                        if (componentReferencePointer.getDefinition() == paramDefAsIDefinition) {
                            result.add(bal);
                        }
                    }
                }
            } 
        }
        return result;
    }
    
    /**
     * Gets all ABalancings that are using a given ComponentDefinition in their sources
     * 
     * @param compDef
     * @return List of ABalancings that are referencing ComponentDefinition
     */
    public static EList<ABalancing<?>> getBalancingsReferencingSource(final ComponentDefinition compDef) {
        EList<ABalancing<?>> result = new BasicEList<ABalancing<?>>();
        for (EObject balObj : ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(PremiseHelper.getRootNotifier(compDef), ABalancing.class)) {
            if (balObj instanceof Balancing) {
                Balancing bal = (Balancing) balObj;
                for (APointer<AParameterDef> sourcePointer : bal.getSourcePointers()) {
                    if (sourcePointer instanceof ComponentReferencePointer) {
                        ComponentReferencePointer<AParameterDef> componentReferencePointer = (ComponentReferencePointer<AParameterDef>) sourcePointer;
                        if (componentReferencePointer.getComponentReference().getComponentDefinition() == compDef) {
                            result.add(bal);
                        }
                    }
                }
                if (bal.getTargetPointer() != null) {
                    APointer<AParameterDef> targetPointer = bal.getTargetPointer();
                    if (targetPointer instanceof ComponentReferencePointer) {
                        ComponentReferencePointer<AParameterDef> componentReferencePointer = (ComponentReferencePointer<AParameterDef>) targetPointer;
                        if (componentReferencePointer.getComponentReference().getComponentDefinition() == compDef) {
                            result.add(bal);
                        }
                    }
                }
            } 
        }
        return result;
    }

    /**
     * Gets that ABalancing<?> that is referencing a given (Transition)Parameter as target
     * 
     * @param param
     * @return ABalancing<?> that is referencing param like Balancing, NestedBalancing, TransitionBalancing
     */
    public static ABalancing<?> getBalancingReferencingTarget(AParameterDef param) {
        ABalancing<?> result = null;
        if (param instanceof Parameter) {
            for (EObject balObj : ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(PremiseHelper.getRootNotifier(param), Balancing.class)) {
                ABalancing<?> bal = (ABalancing<?>) balObj;
                if (bal instanceof Balancing && ((Balancing) bal).getTarget() == param) {
                    result = bal;
                    break;
                }
            }
        } else {
            for (EObject balObj : ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(PremiseHelper.getRootNotifier(param), TransitionBalancing.class)) {
                ABalancing<?> bal = (ABalancing<?>) balObj;
                if (bal instanceof TransitionBalancing && ((TransitionBalancing) bal).getTarget() == param) {
                    result = bal;
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * Gets all ABalancings that are using a given ParameterDefinition as target
     * 
     * @param paramDef
     * @return List of ABalancings that are referencing ParameterDefinition
     */
    public static EList<ABalancing<?>> getBalancingsReferencingTarget(final ParameterDefinition paramDef) {
        EList<ABalancing<?>> result = new BasicEList<ABalancing<?>>();
        for (EObject balObj : ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(PremiseHelper.getRootNotifier(paramDef), ABalancing.class)) {
            if (balObj instanceof Balancing) {
                Balancing bal = (Balancing) balObj;
                for (APointer<AParameterDef> targetPointer : bal.getTargetPointers()) {
                    if (targetPointer instanceof ComponentReferencePointer) {
                        ComponentReferencePointer<AParameterDef> componentReferencePointer = (ComponentReferencePointer<AParameterDef>) targetPointer;
                        @SuppressWarnings("unchecked")
                        IDefinition<AParameterDef> paramDefAsIDefinition = (IDefinition<AParameterDef>) (IDefinition<?>) paramDef;
                        if (componentReferencePointer.getDefinition() == paramDefAsIDefinition) {
                            result.add(bal);
                        }
                    }
                }
            } 
        }
        return result;
    }

    /**
     * Gets all ABalancings that are referencing a given function def
     * 
     * @param fnDef
     * @return List of ABalancings that are referencing fnDef like Balancing, NestedBalancing, TransitionBalancing
     */
    public static EList<ABalancing<?>> getBalancingsReferencingFnDef(AFnDef fnDef) {
        EList<ABalancing<?>> result = new BasicEList<ABalancing<?>>();
        if (PremiseHelper.getRootNotifier(fnDef) != null) {
            for (EObject balObj : ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(PremiseHelper.getRootNotifier(fnDef), ABalancing.class)) {
                ABalancing<?> bal = (ABalancing<?>) balObj;
                if (bal instanceof TransitionBalancing) {
                    // functionDefs were here once...
                } else {
                    // TODO steh_ti reimplement referencing of functions by balancing
                    // if (((Balancing) bal).getFunctionDef() == fnDef) {
                    //     result.add(bal);
                    // }
                }
            }
        }
        return result;
    }

    /**
     * Gets all ABalancings that have functionDefs that are referencing a given calculation engine
     * 
     * @param engine
     * @return List of ABalancings that have functionDefs that are referencing engine like Balancing, NestedBalancing, TransitionBalancing
     */
    public static EList<ABalancing<?>> getBalancingsReferencingCalcEngine(ACalculationEngine engine) {
        EList<ABalancing<?>> result = new BasicEList<ABalancing<?>>();
        for (EObject object : ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(PremiseHelper.getRootNotifier(engine), AFnDef.class)) {
            AFnDef fnDef = (AFnDef) object;
            if (fnDef.getCalcEngine() == engine) {
                result.addAll(getBalancingsReferencingFnDef(fnDef));
            }
        }
        return result;
    }

    /**
     * Gets all TransitionBalancings that are referencing a given transition (Transition)
     * 
     * @param transition
     * @return List of TransitionBalancingsMode that are referencing transition
     */
    public static EList<ABalancing<?>> getBalancingsReferencingTransition(Transition transition) {
        EList<ABalancing<?>> result = new BasicEList<ABalancing<?>>();
        
        if (transition instanceof Transition) {
            for (EObject object : ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(PremiseHelper.getRootNotifier(transition), TransitionBalancing.class)) {
                TransitionBalancing bal = (TransitionBalancing) object;
                if (((Transition) transition).getParameters().contains(bal.getTarget())) {
                    result.add(bal);
                }
            }
        }
        
        return result;
    }
    
    public static Set<Balancing> getBalancingsToRecalculateForStateMachine(StateMachine sm) {
        Notifier root = PremiseHelper.getRootNotifier(sm);
        Set<Balancing> allBalancings = ImmutableSet.copyOf(ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(root, Balancing.class));
        Set<Parameter> allParameters = ImmutableSet.copyOf(ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(root, Parameter.class));

        Set<Mode> modes = StreamSupport.stream(ScopedEObjectFactory.INSTANCE.createAllScopedEObjects(sm).spliterator(), false)
                .flatMap(scopedSM -> scopedSM.getModes().stream())
                .collect(Collectors.toSet());
        
        Set<Balancing> resultBalancings = new HashSet<>();
        
        for (Parameter parameter : allParameters) {
            // ignore parameters which are calculated
            if (allBalancings.stream().anyMatch(bal -> parameter.equals(bal.getTarget()))) {
                continue;
            }
            
            Set<Mode> modeValueModes = parameter.getModeValues().stream()
                    .flatMap(mv -> mv.getModes().stream())
                    .collect(Collectors.toSet());
            if (Sets.intersection(modeValueModes, modes).size() > 0) {
                Set<Balancing> sourceBalancings = allBalancings.stream()
                    .filter(bal -> bal.getSources().contains(parameter))
                    .collect(Collectors.toSet());
               resultBalancings.addAll(sourceBalancings);
            }
        }
        
        
        return resultBalancings;
    }

    /**
     * Gets all Transitions (transitions) that are referencing a given mode
     * 
     * @param mode
     * @return List of Transitions (transitions) that are referencing mode
     */
    public static EList<Transition> getTransitionsReferencingMode(Mode mode) {
        EList<Transition> result = new BasicEList<Transition>();
        for (EObject object : ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(PremiseHelper.getRootNotifier(mode), Transition.class)) {
            Transition transition = (Transition) object;
            if (Objects.equals(transition.getTarget(), mode)) {
                result.add(transition);
            }
        }
        return result;
    }

}
