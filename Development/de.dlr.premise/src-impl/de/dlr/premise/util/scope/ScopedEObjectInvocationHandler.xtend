/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.util.scope

import com.google.common.collect.ImmutableSet
import de.dlr.premise.component.ComponentFactory
import de.dlr.premise.component.ComponentReferenceDirectPointer
import de.dlr.premise.component.ComponentReferencePointer
import de.dlr.premise.component.IDefinition
import de.dlr.premise.component.ISatisfying
import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.DirectPointer
import de.dlr.premise.graph.GraphFactory
import de.dlr.premise.graph.IEdge
import de.dlr.premise.graph.INode
import de.dlr.premise.registry.ADataItem
import de.dlr.premise.registry.RegistryPackage
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.ComponentReferenceSubstitutionMapping
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.system.SystemPackage
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.Collections
import java.util.Map
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import org.eclipse.emf.common.util.AbstractTreeIterator
import org.eclipse.emf.common.util.ECollections
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EOperation
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.emf.ecore.InternalEObject
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static extension de.dlr.premise.util.PremiseHelper.*
import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.*

/**
 * Invocation handler for proxies of model elements.
 */
class ScopedEObjectInvocationHandler<T extends EObject> implements InvocationHandler {
	def package static <T extends EObject> T createScopedEObject(T obj, Iterable<ComponentReference> inputScope) {
		// Prevent scoping a already scoped object, re-scope instead
		if (obj.invocationHandler != null) {
			if (inputScope == obj.invocationHandler.scope) {
				return obj
			} else {				
				return createScopedEObject(obj.invocationHandler.delegate, inputScope)
			}
		}
		
		val interfaceToImplement = obj.class.modelInterfaceToImplement
		val handler = new ScopedEObjectInvocationHandler(ImmutableSet.copyOf(inputScope), obj)
		return Proxy.newProxyInstance(obj.class.classLoader, #[interfaceToImplement, InternalEObject], handler) as T
	} 
	
	def private static Class<?> getModelInterfaceToImplement(Class<?> clazz) {
		switch (clazz) {
			case null: null
			case clazz.interfaces.empty: clazz.superclass.modelInterfaceToImplement
			default: clazz.interfaces.head
		}
	}
	
	private static val Map<Pair<Class<?>, Method>, Optional<Method>> equivalentPointerMethodCache = new ConcurrentHashMap
	private static val Map<EStructuralFeature, Optional<EStructuralFeature>> equivalentPointerFeatureCache = new ConcurrentHashMap
	private static val Map<MethodSignature, Method> methodCache = new ConcurrentHashMap

	@Accessors val ImmutableSet<ComponentReference> scope
	@Accessors val T delegate
	
	@FinalFieldsConstructor package new() {
		
	}

	override invoke(Object proxy, Method method, Object[] args) throws Throwable {
		val equivalentPointerMethod = method.equivalentPointerMethod
		// Use pointer method then resolve pointers if possible
		if (equivalentPointerMethod != null) {
			return equivalentPointerMethod.invoke(delegate, args?.map[createPointer]?.toArray).transformResult([resolvePointer], [createPointer])
			
		}
				
		switch (method) {
			// default methods go 
			// Methods of Object
			case Object.getMethodCached("toString"): this.toStringScoped()
			case Object.getMethodCached("equals", Object): this.equalsScoped(args.get(0))
			case Object.getMethodCached("hashCode"): delegate.hashCode()
			// Special cased getter methods (EStructuralFeature) for model elements
			case SystemComponent.getMethodCached("getReferencedChildren"): getReferencedChildrenScoped(delegate as SystemComponent)
			case ComponentReference.getMethodCached("getActiveImplementation"): getActiveImplementationScoped(delegate as ComponentReference)
			case ComponentReferenceSubstitutionMapping.getMethodCached("getOriginal"): getOriginalScoped(delegate as ComponentReferenceSubstitutionMapping)
			case ComponentReferenceSubstitutionMapping.getMethodCached("getSubstitution"): getSubstitutionScoped(delegate as ComponentReferenceSubstitutionMapping)
			case ADataItem.getMethodCached("getId"): getIdWithScope()
			// Special cased methods (EOperation) for model elements
			case APointer.getMethodCached("getTarget"): (delegate as APointer<?>).resolvePointer
			case IEdge.getMethodCached("getReferencedSources"): (delegate as IEdge<?,?>).sourcePointers.transformResult([resolvePointer], [createPointer])
			case IEdge.getMethodCached("getReferencedTargets"): (delegate as IEdge<?,?>).targetPointers.transformResult([resolvePointer], [createPointer])
			// Methods of EObject
			case EObject.getMethodCached("eContainer"): eContainer()
			case EObject.getMethodCached("eContainmentFeature"): eContainmentFeature()
			case EObject.getMethodCached("eContainingFeature"): eContainingFeature()
			case EObject.getMethodCached("eContents"): eContents()
			case EObject.getMethodCached("eAllContents"): new MagicWrapperTreeIterator(proxy, false)
			case EObject.getMethodCached("eGet", EStructuralFeature): eGet(args.get(0) as EStructuralFeature, true)
			case EObject.getMethodCached("eGet", EStructuralFeature, boolean): eGet(args.get(0) as EStructuralFeature, args.get(1) as Boolean)
			case EObject.getMethodCached("eSet", EStructuralFeature, Object): { eSet(args.get(0) as EStructuralFeature, args.get(1)); null}
			case EObject.getMethodCached("eCrossReferences"): throw new UnsupportedOperationException()
			case EObject.getMethodCached("eInvoke", EOperation, EList): throw new UnsupportedOperationException()
			// by default, just wrap/unwrap
			default: method.invoke(delegate, args).transformResult([wrapIfPossible], [unwrapIfPossible])
		}
	}
	
	def private <T extends EObject> T create(ImmutableSet<ComponentReference> scope, T wrappedElement) {
		createScopedEObject(wrappedElement, scope) as T
	}
	
	def private String toStringScoped() '''
		ScopedEObject [
		    scope=(«scope.map['''"«name»"'''].join(', ')»)
		    delegate=«delegate»
		]
	'''
	
	def private equalsScoped(Object other) {
		if (other instanceof EObject) {
			val invocationHandler = other.invocationHandler
			if (invocationHandler != null) {
				return  this.scope == invocationHandler.scope  && this.delegate === invocationHandler.delegate
			}
		}
		return other === this.delegate
	}
	
	def private getReferencedChildrenScoped(SystemComponent sc) {
		if (sc.hasCyclicComponentReference) {
			return ECollections.emptyEList
		}
		return ScopedEObjectEListInvocationHandler.create(
			sc.children, 
			[child |
				switch(child) {
					ComponentReference: child.activeImplementationScoped
					SystemComponent: child
				}
			], 
			[it]
		)
	}
	
	def private SystemComponent getActiveImplementationScoped(ComponentReference compRef) {
		if (compRef.activeImplementation == null) {
			return null
		}
		val newScope = buildScopeByAppending(compRef)	
		if (newScope == null) {
			return null
		}
		return create(newScope, compRef.activeImplementation)
	}
	
	def private EObject getOriginalScoped(ComponentReferenceSubstitutionMapping subMap) {
		return create(ImmutableSet.builder.add(subMap.eContainer as ComponentReference).addAll(subMap.relativeScope).build, subMap.original)
	}
	
	def private EObject getSubstitutionScoped(ComponentReferenceSubstitutionMapping subMap) {
		return create(ImmutableSet.builder.add(subMap.eContainer as ComponentReference).addAll(subMap.relativeScope).build, subMap.substitution)
	}
	
	/**
	 * Creates a unique id by appending scope and delegate element idenitier
	 */
	def getIdWithScope() {
		(scope + #[delegate]).map[EObjectIdenfification].join("_")
	}

	
	def private eContainer() {
		if (activeImplementationRoot) {
			create(scope.lastDropped, scope.last)
		} else if (delegate.eContainer instanceof ComponentReferenceSubstitutionMapping) {
			// see getSubstitutionScoped() for the reverse direction
			create(ScopedEObjectFactory.NULLSCOPE, delegate.eContainer)
		} else if (delegate.eContainer == null) {
			null
		} else {
			create(scope, delegate.eContainer)
		}
	}
	
	def private eContainmentFeature() {
		if (activeImplementationRoot) {
			SystemPackage.Literals.COMPONENT_REFERENCE__ACTIVE_IMPLEMENTATION
		} else {
			delegate.eContainmentFeature
		}
	}
	
	def private eContainingFeature() {
		if (activeImplementationRoot) {
			SystemPackage.Literals.COMPONENT_REFERENCE__ACTIVE_IMPLEMENTATION
		} else {
			delegate.eContainingFeature
		}
	}
	
	def private eContents() {
		switch (delegate) {
			ComponentReference: ECollections.singletonEList(delegate.activeImplementationScoped)
			default: delegate.eContents.filter[!(it instanceof APointer<?>)].transformResult([wrapIfPossible], null)
		}
	}
	
	def private Object eGet(EStructuralFeature feature, boolean resolve) {
		val equivalentPointerFeature = feature.equivalentPointerFeature
		
		switch(feature) {
			case SystemPackage.Literals.SYSTEM_COMPONENT__REFERENCED_CHILDREN: getReferencedChildrenScoped(delegate as SystemComponent)
			case SystemPackage.Literals.COMPONENT_REFERENCE__ACTIVE_IMPLEMENTATION: getActiveImplementationScoped(delegate as ComponentReference)
			case SystemPackage.Literals.COMPONENT_REFERENCE_SUBSTITUTION_MAPPING__ORIGINAL: getOriginalScoped(delegate as ComponentReferenceSubstitutionMapping)
			case SystemPackage.Literals.COMPONENT_REFERENCE_SUBSTITUTION_MAPPING__SUBSTITUTION: getSubstitutionScoped(delegate as ComponentReferenceSubstitutionMapping)
			case RegistryPackage.Literals.ADATA_ITEM__ID: getIdWithScope()
			// Use pointer feature then resolve pointers if possible
			case equivalentPointerFeature != null: delegate.eGet(equivalentPointerFeature, resolve).transformResult([resolvePointer], [createPointer])
			default: delegate.eGet(feature, resolve).transformResult([wrapIfPossible], [unwrapIfPossible])
		}
	}
	
	def private void eSet(EStructuralFeature feature, Object newValue) {
		val equivalentPointerFeature = feature.equivalentPointerFeature
		
		switch(feature) {
			// Use pointer feature then resolve pointers if possible
			case equivalentPointerFeature != null: delegate.eSet(feature, newValue.createPointer)
			default: delegate.eSet(feature, newValue.unwrapIfPossible)
		}
	}
	
	def private Method getMethodCached(Class<?> clazz, String name, Class<?>... parameterTypes) {
		methodCache.computeIfAbsent(new MethodSignature(clazz, name, parameterTypes))[clazz.getMethod(name, parameterTypes)]
	}
	
	def private getEquivalentPointerMethod(Method method) {
		equivalentPointerMethodCache
			.computeIfAbsent(delegate.class -> method)[
				try {
					Optional.of(delegate.class.getMethod(method.name.pointerFeatureName, method.parameters.map[APointer]))
				} catch(NoSuchMethodException e) {
					Optional.empty
				}
			]
			.orElse(null)
	}
	
	def private getEquivalentPointerFeature(EStructuralFeature feature) {
		equivalentPointerFeatureCache
			.computeIfAbsent(feature)[
				Optional.ofNullable((feature.eContainer as EClass).getEStructuralFeature(feature.name.pointerFeatureName))
			]
			.orElse(null)
	}
	
	def private boolean isActiveImplementationRoot() {
		delegate == scope.last?.activeImplementation
	}
	
	def private Object transformResult(Object wrapperResult, Function<Object, Object> transform, Function<Object, Object> inverseTransform) {
		switch (wrapperResult) {
			EObject: transform.apply(wrapperResult)
			EList<Object>: ScopedEObjectEListInvocationHandler.create(wrapperResult, transform, inverseTransform)
			Iterable<?>: wrapperResult.map[transform.apply(it)].filterNull.toUnmodifieableEList
			default: wrapperResult
		}
	}	
	
	def private dispatch wrapIfPossible(Object it) {
		it
	}	
	
	def private dispatch wrapIfPossible(EObject it) {
		try {
			ScopedEObjectFactory.INSTANCE.createScopedEObjectForScope(it, scope)
		} catch (IllegalArgumentException e) {
			ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(it)
		}
	}
	
	def private <T> T unwrapIfPossible(T obj) {
		switch (obj) {
			EObject: (obj as EObject).wrappedElement as T ?: obj
			default: obj
		}
	}
	
	def private dispatch resolvePointer(Object it) {
		it
	}
	
	def private dispatch resolvePointer(APointer<?> it) {
		throw new IllegalArgumentException
	}
	
	def private dispatch resolvePointer(DirectPointer<?> it) {
		if (target == null) {
			return null
		}
		
		try {
			ScopedEObjectFactory.INSTANCE.createScopedEObjectForScope(target as EObject, scope)
		} catch (IllegalArgumentException e) {
			ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(target as EObject)
		}
	}
	
	def private dispatch resolvePointer(ComponentReferencePointer<?> it) {
		if (componentReference == null) {
			return null
		}
		val newScope = buildScopeByAppending(componentReference)
		if (newScope == null) {
			return null
		}
		
		if (target == null) {
			return null
		}
		
		try {
			ScopedEObjectFactory.INSTANCE.createScopedEObjectForScope(target as EObject, newScope)
		} catch (IllegalArgumentException e) {
			try {
				ScopedEObjectFactory.INSTANCE.createScopedEObjectForScope(target as EObject, #[componentReference])
			} catch (IllegalArgumentException e2) {
				null
			}
		}
	}
	
	def private dispatch resolvePointer(ComponentReferenceDirectPointer<?> it) {
		val newScope = ImmutableSet.builder.addAll(scope).addAll(relativeScope).build
		
		try {
			ScopedEObjectFactory.INSTANCE.createScopedEObjectForScope(target as EObject, newScope)
		} catch (IllegalArgumentException e) {
			try {
				ScopedEObjectFactory.INSTANCE.createScopedEObjectForScope(target as EObject, relativeScope)
			} catch (IllegalArgumentException e2) {
				null
			}
		}
	}
			
	def public dispatch createPointer(Object targetObject) {
		return targetObject
	}
	
	def public dispatch createPointer(EObject targetObject) {
        if (targetObject instanceof ISatisfying<?, ?>) {
        	val compRef = targetObject.scope?.last
	        
	        if (compRef != null) {
		        // target must be exactly one level deeper than source
		        val targetObjectNeededScope = ImmutableSet.copyOf(scope + #[compRef])
		        if (targetObjectNeededScope == targetObject.scope) {
			        val def = (targetObject as ISatisfying<?, ?>).unwrapIfPossible.satisfiedSatisfieables.filter(IDefinition).findFirst[definition |		        	
			        	definition == compRef.componentDefinition ||
			        	definition.eContainer == compRef.componentDefinition
			        ]
			        if (def != null) {
			            return (ComponentFactory.eINSTANCE.<INode>createComponentReferencePointer => [
				            it.definition = def
				            it.componentReference = compRef
				        ]) as APointer<? extends EObject>
			        }
		        }
	        }
    	}
    	
    	if (targetObject.isScopedEObject && targetObject.scope.length > scope.length) {
    		val relativeScopeCandiate = ImmutableSet.copyOf(targetObject.scope.toList.subList(scope.length, targetObject.scope.length))
    		val reconstrucedTargetScope = ImmutableSet.builder.addAll(scope).addAll(relativeScopeCandiate).build
    		if (targetObject.scope == reconstrucedTargetScope) {
    			return ComponentFactory.eINSTANCE.createComponentReferenceDirectPointer => [
    				target = targetObject.unwrapIfPossible
    				relativeScope.addAll(relativeScopeCandiate)
    			]
    		}
    		
    	} 
    	
		return GraphFactory.eINSTANCE.createDirectPointer => [target = targetObject.unwrapIfPossible]
    }
	
	
	private def buildScopeByAppending(ComponentReference it) {
		if (scope.contains(it)) {
			return null
		}
		return ImmutableSet.builder.addAll(scope).add(it).build
	}
	
	
	def private lastDropped(ImmutableSet<ComponentReference> scope) {
		ImmutableSet.copyOf(scope.toList.subList(0, scope.length - 1))
	}
	
	def private getPointerFeatureName(String name) {
		name.replaceAll("^(.*[^s])(s?)$", "$1Pointer$2")
	}
		
	static class MagicWrapperTreeIterator extends AbstractTreeIterator<EObject> {		
		new(Object object, boolean includeRoot) {
			super(object, includeRoot)
		}
		
		
		def protected dispatch getChildren(Void it) {
			Collections.emptyIterator
		}
		
		def protected dispatch getChildren(Object it) {
			Collections.emptyIterator
		}
		
		def protected dispatch getChildren(EObject it) {
			eContents.iterator
		}
	}    
	
	@Data private static class MethodSignature {
		val Class<?> clazz
		val String name
		val Class<?>[] parameterTypes
	}	
}
