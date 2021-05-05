/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.dlr.premise.component.ComponentPackage;
import de.dlr.premise.component.IParameterSatisfieable;
import de.dlr.premise.component.ISatisfying;
import de.dlr.premise.component.Satisfies;
import de.dlr.premise.constraints.handlers.IConstraintViolationHandler;
import de.dlr.premise.constraints.valueconstraints.ConstraintToValueMapper;
import de.dlr.premise.constraints.valueconstraints.ValueConstraintChecker;
import de.dlr.premise.functions.AConstraint;
import de.dlr.premise.functions.AModeValueConstraint;
import de.dlr.premise.functions.RequiredParameter;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.functions.UseCasePackage;
import de.dlr.premise.functions.UseCaseRepository;
import de.dlr.premise.graph.APointer;
import de.dlr.premise.graph.DirectPointer;
import de.dlr.premise.graph.GraphPackage;
import de.dlr.premise.registry.AValueDef;
import de.dlr.premise.registry.RegistryPackage;
import de.dlr.premise.registry.Value;
import de.dlr.premise.system.ModeValueRef;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.SystemPackage;
import de.dlr.premise.util.PremiseHelper;

/**
 * Responds to changes in a {@link Resource} to keep constraint violations up to date.
 * 
 * This class is mainly concerned with the model elements {@link Parameter} and {@link RequiredParameter} and their children (Values and
 * ValueConstraints). Acting as a change listener to the model it reacts to changes in the model that affect those elements.
 * 
 * Changes in violations are reported to a {@link IConstraintViolationHandler} which is responsible for keeping track of the current
 * violations.
 * 
 * Extends {@link ECrossReferenceAdapter}, as it needs to get the Parameters which satisfy a given RequiredParameter whilst only the
 * opposite relation is given by model.
 * 
 * @author steh_ti
 */
public class ConstraintChecker extends ECrossReferenceAdapter {

    private final IConstraintViolationHandler violations;

    private final ConstraintToValueMapper constraintToValueMapper;

    private final ValueConstraintChecker valueConstraintChecker;

    /**
     * Uses the IConstraintViolationHandler given and report violations to it.
     * 
     * @param The IConstraintViolationHandler to use by this instance.
     */
    public ConstraintChecker(IConstraintViolationHandler violations) {
        super();
        this.violations = violations;

        constraintToValueMapper = new ConstraintToValueMapper();
        valueConstraintChecker = new ValueConstraintChecker();
    }

    public void recheck(ResourceSet resSet) {
        for (Resource res : resSet.getResources()) {
            recheck(res);
        }
    }

    public void recheck(Resource res) {
        for (EObject obj : res.getContents()) {
            recheck(obj);
        }
    }

    public void recheck(EObject obj) {
        List<EObject> reqParams = PremiseHelper.getAll(EcoreUtil.getRootContainer(obj), RequiredParameter.class);
        for (EObject reqParam : reqParams) {
            recalculateParameterConstraint((RequiredParameter) reqParam);
        }
    }

    /**
     * Handles a change notification by delegation to the appropriate handler depending on the the model element that was changed.
     */
    @Override
    public void notifyChanged(Notification notification) {
        super.notifyChanged(notification);

        if (!notification.isTouch()) {
            Object notifier = notification.getNotifier();

            // delegate to the appropriate handling method
            if (notifier instanceof ResourceSet) {
                handleResourceSet(notification);
            } else if (notifier instanceof Resource) {
                handleResource(notification);
            } else if (notifier instanceof ProjectRepository) {
                handleProjectRepository(notification);
            } else if (notifier instanceof UseCaseRepository) {
                handleUseCaseRepository(notification);
            } else if (notifier instanceof SystemComponent) {
                handleSystemComponent(notification);
            } else if (notifier instanceof Parameter) {
                handleParameter(notification);
            } else if (notifier instanceof Value) {
                handleValue(notification);
            } else if (notifier instanceof ModeValueRef) {
                handleModeValueRef(notification);
            } else if (notifier instanceof UseCase) {
                handleUseCase(notification);
            } else if (notifier instanceof RequiredParameter) {
                handleParameterConstraint(notification);
            } else if (notifier instanceof AConstraint) {
                handleParameterConstraintChild(notification);
            } else if (notifier instanceof Satisfies<?, ?>) {
                handleSatisfies(notification);
            } else if (notifier instanceof DirectPointer<?>) {
                handleDirectPointer(notification);
            }
        }

    }

    /**
     * Handles changes to a ResourceSet, e.g. adding/removing a Rresource
     */
    private void handleResourceSet(Notification notification) {
        recheck((ResourceSet) notification.getNotifier());
    }

    /**
     * Handles changes to a Resource, e.g. adding/removing one to/from a ResourceSet.
     */
    private void handleResource(Notification notification) {
        ResourceSet resourceSet = ((Resource) notification.getNotifier()).getResourceSet();
        if (resourceSet != null) {
            recheck(resourceSet);
        }
    }

    /**
     * Handles changes to a ProjectRepository. There is only one case to handle here, which is add/remove of SystemComponents.
     */
    private void handleProjectRepository(Notification notification) {
        switch (notification.getFeatureID(SystemComponent.class)) {
        case SystemPackage.PROJECT_REPOSITORY__PROJECTS:
            handleParametersInSubtree(notification);
            break;
        }

    }

    /**
     * Handles changes to a UseCaseRepository. There is only one case to handle here, which is add/remove of UseCases.
     */
    private void handleUseCaseRepository(Notification notification) {
        switch (notification.getFeatureID(UseCase.class)) {
        case UseCasePackage.USE_CASE_REPOSITORY__USECASES:
            handleParameterConstaintsInSubtree(notification);
            break;
        }

    }

    /**
     * Handles add/remove of any element(s) that might have ParametersConstraints in their containment tree.
     */
    private void handleParameterConstaintsInSubtree(Notification notification) {
        // for all old values (removal case) remove all violations for all ParameterConstraints
        for (EObject oldValue : getValueList(notification.getOldValue())) {
            List<EObject> reqParams = PremiseHelper.getAll(oldValue, RequiredParameter.class);
            for (EObject obj : reqParams) {
                violations.removeViolations(obj);
            }
        }
        // for all new values (addition case) check all ParameterConstraints
        for (EObject newValue : getValueList(notification.getNewValue())) {
            List<EObject> reqParams = PremiseHelper.getAll(newValue, RequiredParameter.class);
            for (EObject obj : reqParams) {
                recalculateParameterConstraint((RequiredParameter) obj);
            }
        }
    }

    /**
     * Handle changes in SystemComponent by rechecking all Parameters that were influenced.
     */
    private void handleSystemComponent(Notification notification) {
        switch (notification.getFeatureID(SystemComponent.class)) {
        case SystemPackage.SYSTEM_COMPONENT__PARAMETERS:
        case SystemPackage.SYSTEM_COMPONENT__CHILDREN:
            handleParametersInSubtree(notification);
            break;
        }

    }

    /**
     * Handles add/remove of any element(s) that might have Parameters in their containment tree.
     */
    private void handleParametersInSubtree(Notification notification) {
        // for all old values (removal case)
        for (EObject oldValue : getValueList(notification.getOldValue())) {
            // get all Parameters in the old values containment tree (including the old value itself, if it's a Parameter)
            List<EObject> params = PremiseHelper.getAll(oldValue, Parameter.class);
            for (EObject obj : params) {
                Parameter param = (Parameter) obj;
                // remove violations for the Parameter itself
                removeParameterViolations(param);
                // parameter removal can make RequiredParameter unsatisfied or resolve multiply satisfied, therefore it needs to be
                // recalculated
                recalculateParameterConstraints(param.getSatisfiesRequiredParameters());
            }
        }
        // for all new values (add case)
        for (EObject newValue : getValueList(notification.getNewValue())) {
            // get all Parameters in the new values containment tree (including the new value itself, if it's a Parameter)
            List<EObject> params = PremiseHelper.getAll(newValue, Parameter.class);
            for (EObject obj : params) {
                // recalculate added Parameter
                recalculateParameter((Parameter) obj);
            }
        }
    }

    /**
     * Handles changes to Parameters.
     * 
     * This can either be a add/remove of Value and ModeValuesRefs or changes to the "satisfies" reference.
     */
    private void handleParameter(Notification notification) {
        switch (notification.getFeatureID(Parameter.class)) {
        case SystemPackage.PARAMETER__VALUE:
        case SystemPackage.PARAMETER__MODE_VALUES:
            // remove violations for removed values
            for (EObject oldValue : getValueList(notification.getOldValue())) {
                violations.removeViolations(oldValue);
            }
            // recalculate the parameter itself
            recalculateParameter((Parameter) notification.getNotifier());
            break;
        case SystemPackage.PARAMETER__SATISFIES:
            // Remove violations of Parameter before rechecking constraints
            removeParameterViolations((Parameter) notification.getNotifier());
            // recalculate removed ParameterConstraints, as they might become unsatisfied
            for (EObject oldValue : getValueList(notification.getOldValue())) {
                Satisfies<?, ?> satisfies = (Satisfies<?, ?>) oldValue;
                if (satisfies.getTarget() instanceof RequiredParameter) {
                    recalculateParameterConstraint((RequiredParameter) satisfies.getTarget());
                }
            }
            // recalculate added ParameterConstraints
            for (EObject newValue : getValueList(notification.getNewValue())) {
                Satisfies<?, ?> satisfies = (Satisfies<?, ?>) newValue;
                if (satisfies.getTarget() instanceof RequiredParameter) {
                    recalculateParameterConstraint((RequiredParameter) satisfies.getTarget());
                }
            }
            break;
        }
    }

    /**
     * Handles changes to a Value.
     */
    private void handleValue(Notification notification) {
        // constraint validation is only concerned with the Values value.
        if (notification.getFeatureID(Value.class) == RegistryPackage.VALUE__VALUE) {
            // Find the containing parameter and recheck it (needs type check, as value is also used in constant etc.)
            if (((EObject) notification.getNotifier()).eContainer() instanceof Parameter) {
                Parameter container = (Parameter) ((EObject) notification.getNotifier()).eContainer();
                if (container != null) {
                    recalculateParameter(container);
                }
            }
        }
    }

    /**
     * Handles changes to a ModeValueRef
     */
    private void handleModeValueRef(Notification notification) {
        switch (notification.getFeatureID(ModeValueRef.class)) {
        case SystemPackage.MODE_VALUE_REF__VALUE:
        case SystemPackage.MODE_VALUE_REF__MODE_POINTERS:
            // Find the containing parameter and recheck it
            if (((EObject) notification.getNotifier()).eContainer() instanceof Parameter) {
                Parameter container = (Parameter) ((EObject) notification.getNotifier()).eContainer();
                if (container != null) {
                    recalculateParameter(container);
                }
            }
        }
    }

    /**
     * Handles changes to a UseCase
     */
    private void handleUseCase(Notification notification) {
        switch (notification.getFeatureID(UseCase.class)) {
        // add/remove of ParameterConstraints
        case UseCasePackage.USE_CASE__REQUIRED_PARAMETERS:
            // for all old values (removal case) remove all violations for all ParameterConstraints
            for (EObject oldValue : getValueList(notification.getOldValue())) {
                violations.removeViolations(oldValue);
            }
            // for all new values (addition case) recheck them
            for (EObject newValue : getValueList(notification.getNewValue())) {
                recalculateParameterConstraint((RequiredParameter) newValue);
            }
            break;
        // changes to the condition of a UseCase
        case UseCasePackage.USE_CASE__CONDITION:
            // influences all ParameterConstraints, recheck them all
            recalculateParameterConstraints(((UseCase) notification.getNotifier()).getRequiredParameters());
            break;
        case UseCasePackage.USE_CASE__CHILDREN:
            handleParameterConstaintsInSubtree(notification);
            break;
        }
    }

    /**
     * Handles changes to a RequiredParameter.
     */
    private void handleParameterConstraint(Notification notification) {
        switch (notification.getFeatureID(RequiredParameter.class)) {
        case UseCasePackage.REQUIRED_PARAMETER__VALUE_CONSTRAINT:
        case UseCasePackage.REQUIRED_PARAMETER__MODE_VALUE_CONSTRAINTS:
            recalculateParameterConstraint((RequiredParameter) notification.getNotifier());
            break;
        }
    }

    /**
     * Handles changes to children of ParameterConstraints (that is ValueConstraint and ModeValueConstraint)
     */
    private void handleParameterConstraintChild(Notification notification) {
        EObject root = (EObject) notification.getNotifier();
        while (root != null && !(root instanceof RequiredParameter)) {
            root = root.eContainer();
        }
        if (root != null) {
            recalculateParameterConstraint((RequiredParameter) root);
        }
    }
    
    /**
     * Handles changes to a Satisfies edge
     */
    private void handleSatisfies(Notification notification) {
        if (notification.getFeatureID(Satisfies.class) == ComponentPackage.SATISFIES__TARGET_POINTER) {
            Satisfies<?, ?> satisfies = (Satisfies<?, ?>) notification.getNotifier();
            if (!(satisfies.eContainer() instanceof Parameter)) {
                return;
            }
            // Remove violations of Parameter before rechecking constraints
            removeParameterViolations((Parameter) satisfies.eContainer());
            // recalculate removed ParameterConstraint, as it might become unsatisfied
            Object oldValue = notification.getOldValue();
            if (oldValue instanceof APointer && ((APointer<?>) oldValue).getTarget() instanceof RequiredParameter) {
                recalculateParameterConstraint((RequiredParameter) ((APointer<?>) oldValue).getTarget());
            }
            // recalculate added ParameterConstraint
            Object newValue = notification.getNewValue();
            if (newValue instanceof APointer && ((APointer<?>) newValue).getTarget() instanceof RequiredParameter) {
                recalculateParameterConstraint((RequiredParameter) ((APointer<?>) newValue).getTarget());
            }
        }
    }

    /**
     * Handles changes to DirectPointer, for target of satisfied edge
     */
    private void handleDirectPointer(Notification notification) {
        if (notification.getFeatureID(DirectPointer.class) == GraphPackage.DIRECT_POINTER__TARGET) {
            DirectPointer<?> directPointer = (DirectPointer<?>) notification.getNotifier();
            if (!(directPointer.eContainer() instanceof Satisfies<?, ?>) || !(directPointer.eContainer().eContainer() instanceof Parameter)) {
                return;
            }
            // Remove violations of Parameter before rechecking constraints
            removeParameterViolations((Parameter) directPointer.eContainer().eContainer());
            // recalculate removed ParameterConstraint, as it might become unsatisfied
            Object oldValue = notification.getOldValue();
            if (oldValue instanceof RequiredParameter) {
                recalculateParameterConstraint((RequiredParameter) oldValue);
            }
            // recalculate added ParameterConstraint
            Object newValue = notification.getNewValue();
            if (newValue instanceof RequiredParameter) {
                recalculateParameterConstraint((RequiredParameter) newValue);
            }
        }
    }

    /**
     * Performs rechecking of a Parameter
     */
    private void recalculateParameter(Parameter param) {
        // Recheck the ParameterConstraints
        recalculateParameterConstraints(param.getSatisfiesRequiredParameters());
    }

    /**
     * Performs rechecking of multiple parameter constraints. Convenience method that simply calls recalculateParameterConstraint for each
     * Constraint.
     */
    private void recalculateParameterConstraints(List<RequiredParameter> reqParams) {
        for (RequiredParameter reqParam : reqParams) {
            recalculateParameterConstraint(reqParam);
        }
    }

    /**
     * Rechecks a RequiredParameter.
     * 
     * Uses the fact that this is subclass of {@link ECrossReferenceAdapter} to find all Parameters that try to satisfy the Parameter
     * constraint. Creates an appropriate error, depending on the number of Parameters.
     */
    private void recalculateParameterConstraint(RequiredParameter reqParam) {
        // remove old violations for this constraint
        violations.removeViolations(reqParam);

        Set<Parameter> crossReferences = Sets.newHashSet(getReferencingParameters(reqParam));

        if (crossReferences.size() == 0) {
            // RequiredParameter is not satisfied by any Parameter ...
            if (hasSystemModel(reqParam.eResource().getResourceSet())) {
                // .. and there exist parameters which could satisfy it
                violations.addViolation(reqParam, ConstraintViolationKind.NOT_SATISFIED, null);
            }
        } else if (crossReferences.size() == 1) {
            // there is exactly one, check it!
            Parameter param = crossReferences.iterator().next();
            checkParameterConstraintForParameter(reqParam, param);
        } else {
            // there are more than one Parameters referencing the constraint
            Set<EObject> violated = ImmutableSet.copyOf(crossReferences);
            violations.addViolation(reqParam, ConstraintViolationKind.MULTIPLY_SATISFIED, violated);
        }
    }

    /**
     * Checks whether a RequiredParameter is valid for a given Parameter.
     * 
     * It will first clear all old violations for the Parameter, and then add new violations if necessary.
     * 
     * @param RequiredParameter
     * @param Parameter
     */
    private void checkParameterConstraintForParameter(RequiredParameter reqParam, Parameter param) {
        // remove old violations before checking
        removeParameterViolations(param);

        // get a map that identifies for each constraint the value/modevalues for which it applies
        Multimap<AConstraint, AValueDef> constraintToValueMap = constraintToValueMapper.createConstraintToValueMap(reqParam, param);

        // do the checking
        for (AConstraint constr : getAllConstraints(reqParam)) {
            Collection<AValueDef> matchedVals = constraintToValueMap.get(constr);

            if (matchedVals.size() == 0) {
                // the constraint can't be satisfied by any value on the given Parameter
                violations.addViolation(param, ConstraintViolationKind.VIOLATED, constr);
            } else {
                for (AValueDef val : matchedVals) {
                    if (!valueConstraintChecker.check(val, constr)) {
                        violations.addViolation(val, ConstraintViolationKind.VIOLATED, constr);
                    }
                }
            }
        }

    }

    /**
     * Gets all constraints of a ParamterConstraint.
     * 
     * {@link RequiredParameter#getValueConstraint()} combined with {@link RequiredParameter#getModeValueConstraints()}.
     * 
     * @param reqParam
     * @return
     */
    private Set<AConstraint> getAllConstraints(RequiredParameter reqParam) {
        Set<AConstraint> constrs = Sets.newHashSet();
        if (reqParam.getValueConstraint() != null) {
            constrs.add(reqParam.getValueConstraint());
        }
        for (AModeValueConstraint modeValConstr : reqParam.getModeValueConstraints()) {
            // ModeValueConstraint must have at least one Mode, disregard if it has none
            if (!modeValConstr.getModes().isEmpty()) {
                constrs.add(modeValConstr);
            }
        }
        return constrs;
    }

    /**
     * Gets all Parameters that reference a given RequiredParameter.
     * 
     * Makes sure that all the Parameters share a common root with the RequiredParameter in the containment tree.
     * 
     * @param reqParam
     * @return
     */
    private Iterator<Parameter> getReferencingParameters(final RequiredParameter reqParam) {
        Collection<Setting> allCrossReferences = this.getNonNavigableInverseReferences(reqParam, true);
        EClass filterClass = GraphPackage.Literals.APOINTER;

        Iterator<Parameter> crossReferences =
                new EcoreUtil.AbstractFilteredSettingsIterator<Parameter>(allCrossReferences, null, filterClass) {

                    @Override
                    protected boolean isIncluded(EStructuralFeature.Setting setting) {
                        if (!super.isIncluded(setting)) {
                            return false;
                        }
                        
                        if (!(setting.getEObject().eContainer() instanceof Satisfies<?, ?>)) {
                            return false;
                        }
                        
                        @SuppressWarnings("unchecked")
                        Satisfies<Parameter, IParameterSatisfieable> satisfies = (Satisfies<Parameter, IParameterSatisfieable>) setting.getEObject().eContainer();
                        ISatisfying<Parameter, IParameterSatisfieable> source = satisfies.getSource();
                        
                        
                        // make sure the Parameter shares a common root with the RequiredParameter to exclude Parameters that were removed
                        // from the tree
                        return source != null && getRoot(source) == getRoot(reqParam);
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    protected Parameter yield(Setting setting) {
                        return (Parameter) ((Satisfies<Parameter, IParameterSatisfieable>) setting.getEObject().eContainer()).getSource();
                    }
                };
        return crossReferences;
    }

    /**
     * Gets the root container of a given EObject.
     * 
     * Searches first for the root EObject using {@link EcoreUtil#getRootContainer(EObject)}. Then if possible goes up to the containing
     * {@link Resource} and the {@link ResourceSet}
     * 
     * @param EObject to find root for
     * @return The highest possible root of the EObject
     */
    private Notifier getRoot(EObject obj) {
        Notifier root = null;

        EObject rootObj = EcoreUtil.getRootContainer(obj, true);

        if (rootObj.eResource() == null) {
            root = rootObj;
        } else {
            Resource res = rootObj.eResource();
            if (res.getResourceSet() == null) {
                root = res;
            } else {
                root = res.getResourceSet();
            }
        }

        return root;
    }

    /**
     * Normalizes the result of {@link Notification#getOldValue()} and {@link Notification#getNewValue()}.
     * 
     * These methods can return either a single EObject or a collection of EObjects (the conditions on which this depends are described
     * below). To make it possible to handle both cases at once, wrap single values in a Collection.
     * 
     * getOldValue and Notification return a EObject if notification describes the removal/addition of a single model element. They will
     * return a Collection of EObjects, if multiple model elements were removed/added at once.
     * 
     * @param Return value of {@link Notification#getOldValue()} or {@link Notification#getNewValue()}.
     * @return Normalized version of the value
     */
    private Collection<EObject> getValueList(Object value) {
        Collection<EObject> elems;

        if (value instanceof Collection) {
            elems = new ArrayList<>();
            // actually loop over collection and add EObjects to new list to make sure we are type safe here
            for (Object obj : (Collection<?>) value) {
                if (obj instanceof EObject) {
                    elems.add((EObject) obj);
                }
            }
        } else if (value instanceof EObject) {
            elems = Collections.singleton((EObject) value);
        } else {
            elems = Collections.emptyList();
        }
        return elems;
    }

    /**
     * Removes all violations for a Parameter and its Value and ModeValueRefs
     * 
     * @param Parameter to remove all violations for.
     */
    private void removeParameterViolations(Parameter param) {
        violations.removeViolations(param);
        if (param.getValue() != null) {
            violations.removeViolations(param.getValue());
        }
        for (ModeValueRef modeVal : param.getModeValues()) {
            violations.removeViolations(modeVal);
        }
    }

    /**
     * Check if the given ResourceSet contains a system model
     * @param resSet
     * @return
     */
    private boolean hasSystemModel(ResourceSet resSet) {
        Stream<Object> resSetRootObjects = resSet.getResources().stream().flatMap(res -> res.getContents().stream());
        boolean hasSystemModelInResSet = resSetRootObjects.anyMatch(ProjectRepository.class::isInstance);
        return hasSystemModelInResSet;
    }

}
