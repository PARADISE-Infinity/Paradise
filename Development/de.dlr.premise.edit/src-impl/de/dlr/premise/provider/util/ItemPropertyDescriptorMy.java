/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.provider.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;

import com.google.common.collect.Lists;

import de.dlr.premise.component.ComponentFactory;
import de.dlr.premise.component.ComponentPackage;
import de.dlr.premise.component.ComponentReferencePointer;
import de.dlr.premise.component.IDefinition;
import de.dlr.premise.graph.APointer;
import de.dlr.premise.graph.DirectPointer;
import de.dlr.premise.graph.GraphFactory;
import de.dlr.premise.graph.GraphPackage;
import de.dlr.premise.graph.INode;
import de.dlr.premise.system.ComponentReference;
import de.dlr.premise.system.SystemPackage;

/**
 * @author steh_ti
 *
 */
public class ItemPropertyDescriptorMy extends ItemPropertyDescriptor {

    public ItemPropertyDescriptorMy(AdapterFactory adapterFactory, ResourceLocator resourceLocator, String displayName, String description,
            EStructuralFeature feature, boolean isSettable, boolean multiLine, boolean sortChoices, Object staticImage, String category,
            String[] filterFlags) {
        super(adapterFactory, resourceLocator, displayName, description, feature, isSettable, multiLine, sortChoices, staticImage,
              category, filterFlags);
    }

    @Override
    public Collection<?> getChoiceOfValues(Object object) {

        Collection<?> ret = null;

        if (object instanceof EObject) {
            EObject eObject = (EObject) object;
            
            if (this.feature.getEType() == GraphPackage.Literals.APOINTER && this.feature instanceof EReference
                    && ((EReference) this.feature).isContainment()) {
                ret = getChoiceOfValueForPointer(eObject);
            } else {
                // First, we try to find a OCL method that specifies the available values ..
                ret = getValueListByQueryMethod(eObject);

                // ... if that fails, we try to populate the reference and consider generics on the way ...
                if (ret == null) {
                    ret = getReferenceForETypeParameter(eObject);
                }

                // ... if we still didn't get anything, we delegate back to the default implementation.
                if (ret == null) {
                    ret = super.getChoiceOfValues(eObject);
                }

                // Finally we allow the result list to be filtered.
                if (ret != null) {
                    ret = filterValueListByQueryMethod(eObject, ret);
                }
            }

            // finally, add a null value to remove selection
            if (ret instanceof Collection && !feature.isMany() && !ret.contains(null)) {
                ret.add(null);
            }
        }

        return ret;
    }

    private Collection<?> getChoiceOfValueForPointer(EObject eObject) {
        Collection<Object> pointers = Lists.newArrayList();

        // Get pointer target type
        EGenericType type = eObject.eClass().getFeatureType(this.feature);
        EGenericType pointerTargetType = null;
        EGenericType unresolvedPointerTargetType = type.getETypeArguments().get(0);
        if (unresolvedPointerTargetType != null) {
            pointerTargetType = createReifiedType(eObject, unresolvedPointerTargetType);
        }

        // DirectPointers
        List<DirectPointer<Object>> directPointers = getDirectPointers(eObject, pointerTargetType);
        pointers.addAll(directPointers);

        // ComponentReferencePointers
        if (pointerTargetType != null) {
            List<ComponentReferencePointer<INode>> compRefPointers = getComponentReferencePointers(eObject, pointerTargetType);
            pointers.addAll(compRefPointers);
        }

        return pointers;
    }

    private List<DirectPointer<Object>> getDirectPointers(EObject eObject, EGenericType pointerTargetType) {
        Collection<?> directPointerTargets = getValueListByQueryMethod(eObject);

        if (directPointerTargets == null) {
            if (pointerTargetType != null) {
                directPointerTargets = getReachableObjectsOfType(eObject, pointerTargetType);
            }
        }

        if (directPointerTargets != null) {
            directPointerTargets = filterValueListByQueryMethod(eObject, directPointerTargets);
        }

        List<DirectPointer<Object>> directPointers = Lists.newArrayList();
        if (directPointerTargets != null) {
            for (Object pointerTarget : directPointerTargets) {
                DirectPointer<Object> pointer = GraphFactory.eINSTANCE.<Object> createDirectPointer();
                pointer.setTarget(pointerTarget);
                directPointers.add(pointer);
            }
        }
        return directPointers;
    }

    private List<ComponentReferencePointer<INode>> getComponentReferencePointers(EObject eObject, EGenericType pointerTargetType) {
        List<ComponentReferencePointer<INode>> compRefPointers = new ArrayList<>();

        Collection<ComponentReference> compRefs = getParentComponentReferenceListByQueryMethod(eObject);
        
        if (compRefs == null) {
            Collection<EObject> compRefsTemp = getReachableObjectsOfType(eObject, SystemPackage.Literals.COMPONENT_REFERENCE);
            @SuppressWarnings("unchecked")
            Collection<ComponentReference> compRefsCast = (Collection<ComponentReference>) (Collection<?>) compRefsTemp;
            
            compRefs = compRefsCast;
        }

        if (compRefs != null) {
            compRefs = filterParentComponentReferenceListByQueryMethod(eObject, compRefs);
        }

        EGenericType definitionType = EcoreFactory.eINSTANCE.createEGenericType();
        definitionType.setEClassifier(ComponentPackage.Literals.IDEFINITION);
        definitionType.getETypeArguments().add(EcoreUtil.copy(pointerTargetType));

        for (ComponentReference compRef : compRefs) {
            if (compRef.getComponentDefinition() != null) {
                List<EObject> candidates = new ArrayList<>();
                candidates.add(compRef.getComponentDefinition());
                candidates.addAll(compRef.getComponentDefinition().eContents());

                for (EObject candidate : candidates) {
                    if (definitionType.isInstance(candidate)) {
                        // Cast is safe due to metamodel based check
                        @SuppressWarnings("unchecked")
                        IDefinition<INode> definition = (IDefinition<INode>) candidate;
   
                        ComponentReferencePointer<INode> pointer = ComponentFactory.eINSTANCE.<INode> createComponentReferencePointer();
                        pointer.setComponentReference(compRef);
                        pointer.setDefinition(definition);

                        compRefPointers.add(pointer);
                    }
                }
            }
        }
        return compRefPointers;
    }

    /**
     * Tries to find a method on the model object that specifies valid targets for a {@link EStructuralFeature}.
     * 
     * @param object
     * @return
     */
    private Collection<?> getValueListByQueryMethod(EObject object) {
        // search for a method named queryValid[%PropertyName%List] on
        // class, e.g queryValidTargetList for a property named "target"
        String methodName = "queryValid" + getFeatureNameUppercased() + "List";
        
        EOperation eOperation = getEOperationByName(object.eClass(), methodName);
        
        if (eOperation == null) {
            return null;
        }

        try {
            Object result = object.eInvoke(eOperation, ECollections.EMPTY_ELIST);
            return Lists.newArrayList((Collection<?>) result);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tries to find a method on the model object that filters valid targets for a {@link EStructuralFeature}.
     * 
     * @param object
     * @return
     */
    private Collection<?> filterValueListByQueryMethod(EObject object, Collection<?> choices) {
        // search for a method named queryIsValid[%PropertyName%]Value on
        // class, e.g queryIsValidActiveImplmentationValue for a property named "activeImplementation"
        String methodName = "queryIsValid" + getFeatureNameUppercased() + "Value";

        EOperation eOperation = getEOperationByName(object.eClass(), methodName);

        if (eOperation == null) {
            return choices;
        }

        try {
            List<Object> filtered = Lists.newArrayList();
            for (Object choice : choices) {
                boolean matchesFilter = (boolean) object.eInvoke(eOperation, ECollections.asEList(choice));
                if (matchesFilter) {
                    filtered.add(choice);
                }
            }
            return filtered;
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<ComponentReference> getParentComponentReferenceListByQueryMethod(EObject object) {
        // search for a method named queryValid[%PropertyName%]ParentComponentReferenceList on
        // class, e.g queryValidTargetPointerParentComponentReferenceList for a property named "targetPointer"
        String methodName = "queryValid" + getFeatureNameUppercased() + "ParentComponentReferenceList";

        EOperation eOperation = getEOperationByName(object.eClass(), methodName);

        if (eOperation == null) {
            return null;
        }

        try {
            Object result = object.eInvoke(eOperation, ECollections.EMPTY_ELIST);
            @SuppressWarnings("unchecked")
            List<ComponentReference> resultList = Lists.newArrayList((Collection<ComponentReference>) result);
            return resultList;
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<ComponentReference> filterParentComponentReferenceListByQueryMethod(EObject object,
            Collection<ComponentReference> choices) {
        // search for a method named queryIsValid[%PropertyName%]ParentComponentReference on
        // class, e.g queryIsValidSourcePointerParentComponentReference for a property named "sourcePointer"
        String methodName = "queryIsValid" + getFeatureNameUppercased() + "ParentComponentReference";

        EOperation eOperation = getEOperationByName(object.eClass(), methodName);

        if (eOperation == null) {
            return choices;
        }

        try {
            List<ComponentReference> filtered = Lists.newArrayList();
            for (ComponentReference choice : choices) {
                boolean matchesFilter = (boolean) object.eInvoke(eOperation, ECollections.asEList(choice));
                if (matchesFilter) {
                    filtered.add(choice);
                }
            }
            return filtered;
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFeatureNameUppercased() {
        String name = this.feature.getName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }

    /**
     * Gets a all values for a {@link EReference} whilst considering the type bounds from EMF generics
     * 
     * @param object
     * @param ret
     * @return
     */
    private Collection<?> getReferenceForETypeParameter(Object object) {
        if (feature instanceof EReference) {
            EGenericType genericType = ((EReference) feature).getEGenericType();

            if (genericType.getETypeParameter() != null) {
                EGenericType resolvedGenericType = resolveETypeParameter((EObject) object, genericType);
                return getReachableObjectsOfType((EObject) object, resolvedGenericType);
            }
        }

        return null;
    }
    
    private EGenericType createReifiedType(EObject containingObject, EGenericType eGenericType) {
        if (eGenericType.getETypeParameter() != null) {
            return resolveETypeParameter(containingObject, eGenericType);
        }
        
        if (!eGenericType.getETypeArguments().isEmpty()) {
            List<EGenericType> reifiedTypeArguments = Lists.newArrayList();
            for (EGenericType typeArgument : eGenericType.getETypeArguments()) {
                reifiedTypeArguments.add(createReifiedType(containingObject, typeArgument));
            }
        
            EGenericType reifiedType = EcoreUtil.copy(eGenericType);
            reifiedType.getETypeArguments().clear();
            reifiedType.getETypeArguments().addAll(reifiedTypeArguments);
            return reifiedType;
        }
        
        return eGenericType;
    }

    /**
     * Recursively resolve the typeParameter of a EGenericType to find a EGenericType that actually represents an EClassifier
     * 
     * A {@link EGenericType} can be specified either by an actual {@link EClassifier}, that is a real meta model class, or an
     * {@link ETypeParameter} which is specified by the using class. 
     * 
     * @param containingObject
     * @param eGenericType
     * @return
     */
    private EGenericType resolveETypeParameter(EObject containingObject, EGenericType eGenericType) {
        ETypeParameter typeParameter = eGenericType.getETypeParameter();

        if (typeParameter != null) {            
            // the type parameter can be bound on a superclass
            EGenericType reifiedType = EcoreUtil.getReifiedType(containingObject.eClass(), eGenericType);
            if (reifiedType != eGenericType) {
                return reifiedType;
            }
            
            // get the index of the type parameter, as they are mapped by order to the arguments
            EClass eClass = (EClass) typeParameter.eContainer();
            int index = eClass.getETypeParameters().indexOf(typeParameter);

            if (index != -1) {
                // find the argument for the type parameter, which is a generic type itself
                EReference containmentFeature = containingObject.eContainmentFeature();
                List<EGenericType> containmentFeatureTypeArguments = containmentFeature.getEGenericType().getETypeArguments();
                EGenericType containmentFeatureGenericType = containmentFeatureTypeArguments.get(index);

                // resolve that generic type
                return resolveETypeParameter(containingObject.eContainer(), containmentFeatureGenericType);
            }
        }

        return eGenericType;
    }
    
    private EOperation getEOperationByName(EClass eClass, String name) {
        for (EOperation eOperation : eClass.getEAllOperations()) {
            if (name.equals(eOperation.getName())) {
                return eOperation;
            }
        }
        return null;
    }

    /**
     * Performs the task of setting the feature to the given value on the object. Alternatively, value can also be a {@link Command}. In
     * this case, the command is executed to perform the change.
     * 
     */
    @Override
    public void setPropertyValue(final Object object, final Object value) {
        if (value instanceof Command) {
            Command command = (Command) value;
            EditingDomain editingDomain = getEditingDomain(object);
            if (editingDomain != null) {
                editingDomain.getCommandStack().execute(command);
            } else {
                command.execute();
            }
        } else if (feature != null && feature.getEType() instanceof EClass
                && GraphPackage.Literals.APOINTER.isSuperTypeOf((EClass) feature.getEType()) 
                && (value instanceof APointer || value instanceof Collection<?>)
                && object instanceof EObject) {
            // Workaround emf generic limitations for APointer
            EditingDomain editingDomain = getEditingDomain(object);
            if (editingDomain != null) {
                editingDomain.getCommandStack().execute(new ChangeCommand((EObject) object) {
                    @Override
                    protected void doExecute() {
                        ((EObject) object).eSet(feature, value);
                    }
                });
            } else {
                ((EObject) object).eSet(feature, value);
            }
        } else {
            super.setPropertyValue(object, value);
        }

    }
}