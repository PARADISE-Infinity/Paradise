/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.handler

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.SetMultimap
import de.dlr.premise.component.ChildComponentDefinition
import de.dlr.premise.component.ComponentDefinition
import de.dlr.premise.component.ComponentFactory
import de.dlr.premise.component.ComponentRepository
import de.dlr.premise.component.IDefinition
import de.dlr.premise.component.ISatisfieable
import de.dlr.premise.component.ISatisfying
import de.dlr.premise.component.ModeDefinition
import de.dlr.premise.component.ParameterDefinition
import de.dlr.premise.component.Satisfies
import de.dlr.premise.element.Mode
import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.DirectPointer
import de.dlr.premise.graph.GraphPackage
import de.dlr.premise.graph.INode
import de.dlr.premise.provider.util.PremiseAdapterFactoryHelper
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.Parameter
import de.dlr.premise.system.ParameterNameMapping
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.system.SystemFactory
import java.util.Collection
import java.util.Collections
import java.util.Map
import java.util.Set
import org.eclipse.emf.common.notify.AdapterFactory
import org.eclipse.emf.common.notify.Notifier
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.EStructuralFeature.Setting
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.emf.edit.provider.IItemLabelProvider
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static extension com.google.common.collect.Maps.*
import static extension com.google.common.collect.Multimaps.*
import static extension de.dlr.premise.util.PremiseHelper.*
import static extension org.eclipse.emf.ecore.util.EcoreUtil.*

class ComponentDefinitionExtractor {

	@FinalFieldsConstructor static class Error {		
		val public String title
		val public String message
	}
	
	static class IncomingCrossReferencer extends EcoreUtil.CrossReferencer {
		
		val EObject subtreeRoot
		
		protected new(Notifier root, EObject subtreeRoot) {
			super(Collections.singleton(root))
			this.subtreeRoot = subtreeRoot
		}
	
		override protected containment(EObject eObject) {
			eObject != subtreeRoot
		}
		
		override protected boolean crossReference(EObject eObject, EReference eReference, EObject crossReferencedEObject) {
			subtreeRoot.isAncestor(crossReferencedEObject)  && !subtreeRoot.isAncestor(eObject) && !eReference.derived 
		}
		
		def static find(Notifier root, EObject subtreeRoot) {
			val result = new IncomingCrossReferencer(root, subtreeRoot)
			result.crossReference()
			result.done()
			result
		}
	}
	
	static class OutgoingCrossReferencer extends EcoreUtil.CrossReferencer {
		
		val EObject subtreeRoot
		
		protected new(EObject subtreeRoot) {
			super(Collections.singleton(subtreeRoot))
			this.subtreeRoot = subtreeRoot
		}
		
		override protected boolean crossReference(EObject eObject, EReference eReference, EObject crossReferencedEObject) {
			!subtreeRoot.isAncestor(crossReferencedEObject) && !eReference.derived && !ignoredCrossReference(eObject, eReference, crossReferencedEObject)
		}
		
		def protected ignoredCrossReference(EObject eObject, EReference eReference, EObject crossReferencedEObject) {
			val crossReferencedEObjectRoot = EcoreUtil.getRootContainer(crossReferencedEObject, true)
			
			!(crossReferencedEObjectRoot instanceof ProjectRepository)// && !(crossReferencedEObjectRoot instanceof ComponentRepository)
		}
		
		def static find(EObject subtreeRoot) {
			val result = new OutgoingCrossReferencer(subtreeRoot)
			result.crossReference()
			result.done()
			result
		}
	}
	
	val AdapterFactory adapterFactory = PremiseAdapterFactoryHelper.createComposedAdapterFactory

	val SystemComponent sysComp
	
	var int sysCompIndex
	var SystemComponent parent
	var ResourceSet root
	var ComponentRepository componentRepository
	
	
	var SetMultimap<EObject, Setting> allOutgoingReferences
	var SetMultimap<EObject, Setting> pointerIncomingReferences
	var SetMultimap<EObject, Setting> unallowedIncomingReferences

	var Set<EObject> referencedByExternal

	var Map<ISatisfying<INode, ISatisfieable>, IDefinition<INode>> referencedToDefinition
	var ComponentDefinition componentDefinition

	var ComponentReference componentReference

	var boolean preparedAndValid = false
	

	new(SystemComponent sysComp) {
		this.sysComp = sysComp
	}

	def prepare() {
		readComponentRepository()
		createReferenceData()

		val error = checkPreconditions()
		if (error == null) {
			preparedAndValid = true
		}

		return error
	}

	def execute() {
		if (!preparedAndValid) {
			throw new IllegalStateException
		}

		doExecute()
	}

	def private readComponentRepository() {
		parent = sysComp.parent
		if (parent != null) {
			sysCompIndex = parent.children.indexOf(sysComp)
		}
		root = sysComp.eResource.resourceSet
		val rootEObjects = root.resources.map[contents].flatten
		componentRepository = rootEObjects.filter(ComponentRepository).head
	}

	def private createReferenceData() {
		allOutgoingReferences = OutgoingCrossReferencer.find(sysComp).toSetMultimap
		
		val allIncomingReferences = IncomingCrossReferencer.find(root, sysComp).toSetMultimap
		
		pointerIncomingReferences = allIncomingReferences
			.filterEntries[isAllowedIncomingReference]
		
		unallowedIncomingReferences = allIncomingReferences
			.filterEntries[!allowedIncomingReference]
		
		referencedByExternal = pointerIncomingReferences.keySet
	}
	
	def private isAllowedIncomingReference(Map.Entry<EObject, Setting> it) {
		value.EStructuralFeature == GraphPackage.Literals.DIRECT_POINTER__TARGET
	}

	def private checkPreconditions() {
		if (componentRepository == null) {
			return new Error("No ComponentRepository", "Please load a ComponentRepository")
		}

		if (parent == null) {
			return new Error("Selected SystemComponent is a root",
				"A root SystemComponent can't be converted to a ComponentDefinition")
		}

		if (!allOutgoingReferences.empty) {
			return new Error("Selected SystemComponent references external elements",
				'''
					The following external elements are referenced by the SystemComponent or its children:
					
					«allOutgoingReferences.referencesText»
					
					References to external elements must be removed before a ComponentDefinition can be extracted.
				'''
			)
		}

		if (!unallowedIncomingReferences.empty) {
			return new Error("Selected SystemComponent is referenced by external elements",
				'''
					The following external elements reference the SystemComponent or its children:
					
					«unallowedIncomingReferences.referencesText»
					
					References from external elements must be removed before a ComponentDefinition can be extracted.
				'''
			)
		}

		return null
	}

	def private doExecute() {
		val name = sysComp.name

		// create ParamDefs
		referencedToDefinition = (referencedByExternal.toSet.filter(ISatisfying) as Iterable<?> as Iterable<ISatisfying<INode, ISatisfieable>>).toMap[createDefinition as IDefinition<INode>]
		// create and assign CompDef
		componentDefinition = referencedToDefinition.get(sysComp) as Object as ComponentDefinition ?: ComponentFactory.eINSTANCE.createComponentDefinition
		componentDefinition.name = name + "Definition"
		componentDefinition.description = sysComp.description
		componentDefinition.parameters += referencedToDefinition.values.filter(ParameterDefinition)
		componentDefinition.children += referencedToDefinition.values.filter(ChildComponentDefinition)
		componentDefinition.modes += referencedToDefinition.values.filter(ModeDefinition)
		componentRepository.definitions += componentDefinition

		// make SysComp satisfy CompDef
		sysComp.satisfies += componentDefinition
		referencedToDefinition.forEach[ p1, p2 |
			p1.satisfies += p2 as Object as ISatisfieable
		]
		
		// rename SysComp
		sysComp.name = name + "Reference"

		// move to component repository
		componentRepository.components += sysComp

		// create CompRef to replace SysComp
		componentReference = SystemFactory.eINSTANCE.createComponentReference
		componentReference.name = name
		componentReference.componentDefinition = componentDefinition
		componentReference.activeImplementation = sysComp

		parent.children.add(sysCompIndex, componentReference)
		
		for (obj : referencedByExternal) {
			val definition = referencedToDefinition.get(obj)
			val references = pointerIncomingReferences.get(obj).map[EObject].filter(DirectPointer)
			
			for (directPointer : references) {
				val componentReferencePointer = ComponentFactory.eINSTANCE.createComponentReferencePointer
				componentReferencePointer.componentReference = componentReference
				componentReferencePointer.definition = definition
				
				if (directPointer.eContainingFeature.many) {
					val list = (directPointer.eContainer.eGet(directPointer.eContainingFeature) as EList<Object>)
					val index = list.indexOf(directPointer)
					list.remove(index)
					list.add(index, componentReferencePointer)
				} else {
					directPointer.eContainer.eSet(directPointer.eContainingFeature, componentReferencePointer)
				}
			}
		}
	}
	
	def private dispatch IDefinition<INode> createDefinition(ISatisfying<? extends INode, ? extends ISatisfieable> node) {
		throw new IllegalArgumentException()
	}
	
	def private dispatch IDefinition<Parameter> createDefinition(Parameter parameter) {
		val definition = ComponentFactory.eINSTANCE.createParameterDefinition()
		definition.name = parameter.name
		definition.description = parameter.description
		definition.unit = parameter.unit
		return definition
	}

	def private dispatch IDefinition<SystemComponent> createDefinition(SystemComponent systemComponent) {
		if (systemComponent == sysComp) {
			// Don't initialize here, is initialized later on globally
			return ComponentFactory.eINSTANCE.createComponentDefinition
		} else {
			val definition = ComponentFactory.eINSTANCE.createChildComponentDefinition()
			definition.name = systemComponent.name
			definition.description = systemComponent.description
			return definition
		}
	}
		
	def private dispatch IDefinition<Mode> createDefinition(Mode mode) {
		val definition = ComponentFactory.eINSTANCE.createModeDefinition
		definition.name = mode.name
		definition.description = mode.description
		return definition
	}
	
	def private String getReferencesText(Multimap<EObject, Setting> mm) '''
		«FOR entry : mm.asMap.entrySet»
			"«entry.key.text»" is referenced by 
			«entry.value.settingsText»
		«ENDFOR»
	'''
	
	def private getSettingsText(Collection<Setting> settings) {
		"- " + settings.map[text].join("- \n")
	}
	
	def private dispatch String getText(Object obj) {
		obj.getText(adapterFactory) ?: obj.toString
	}
	
	def private dispatch String getText(Setting setting) '''
		"«setting.EObject.text»"
	'''
	
	def private dispatch String getText(APointer<?> pointer) {
		pointer.eContainer.text
	}
	
	def private dispatch String getText(ParameterNameMapping mapping) {
		mapping.eContainer.text
	}
	
	def private getText(Object obj, AdapterFactory factory) {
		if (factory.isFactoryForType(IItemLabelProvider)) {
			val labelProvider = factory.adapt(obj, IItemLabelProvider) as IItemLabelProvider;
			if (labelProvider != null) {
				return labelProvider.getText(obj);
			}
		}
		return null;
	}
	
	def private <K, V, I extends Iterable<V>> toSetMultimap(Map<K, I> map) {
		val mm = HashMultimap.<K, V>create()
		map.entrySet.forEach[mm.putAll(key, value)]
		return mm
	}
	
	def private <S extends INode, T extends ISatisfieable> operator_add(EList<Satisfies<S, T>> satisfiesCollection, T satisfieable) {
		val satisfies = ComponentFactory.eINSTANCE.<S, T>createSatisfies => [
			target = satisfieable
		]
		satisfiesCollection += satisfies
	}
}
