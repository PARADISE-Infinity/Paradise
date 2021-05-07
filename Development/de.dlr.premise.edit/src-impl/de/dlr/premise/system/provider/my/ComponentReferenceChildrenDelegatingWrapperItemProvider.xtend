/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.system.provider.my

import com.google.common.collect.ImmutableSet
import de.dlr.premise.provider.util.DelegatingWrapperItemProviderMy
import de.dlr.premise.system.ComponentReference
import java.util.List
import java.util.Objects
import org.eclipse.emf.common.command.Command
import org.eclipse.emf.common.command.UnexecutableCommand
import org.eclipse.emf.common.notify.AdapterFactory
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.emf.edit.EMFEditPlugin
import org.eclipse.emf.edit.command.CommandParameter
import org.eclipse.emf.edit.command.RemoveCommand
import org.eclipse.emf.edit.domain.EditingDomain
import org.eclipse.emf.edit.provider.ComposedImage
import org.eclipse.emf.edit.provider.DelegatingWrapperItemProvider
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor
import org.eclipse.emf.edit.provider.IWrapperItemProvider
import org.eclipse.emf.edit.provider.ItemPropertyDescriptorDecorator
import org.eclipse.emf.edit.provider.WrapperItemProvider
import org.eclipse.xtend.lib.annotations.Accessors
import de.dlr.premise.system.ComponentReferenceSubstitutionMapping
import de.dlr.premise.system.SystemPackage

class ComponentReferenceChildrenDelegatingWrapperItemProvider extends DelegatingWrapperItemProviderMy {
	
	@Accessors val ImmutableSet<ComponentReference> scope
	
	new(Object value, Object owner, EStructuralFeature feature, int index,
		ImmutableSet<ComponentReference> scope, AdapterFactory adapterFactory) {
		super(value, owner, feature, index, adapterFactory)
		
		this.scope = scope
	}
	
	protected new(Object value, Object owner, ImmutableSet<ComponentReference> scope, AdapterFactory adapterFactory) {
		super(value, owner, adapterFactory)
		
		this.scope = scope
	}
	
	protected override createWrapper(Object value, Object owner, AdapterFactory adapterFactory) {
		var Object actualValue = value
		if (actualValue instanceof WrapperItemProvider) {
			actualValue = actualValue.value
		}
		
		new ComponentReferenceChildrenDelegatingWrapperItemProvider(actualValue, owner, owner.createNewScope(), adapterFactory)
	}
	
	private def createNewScope(Object owner) {
		if (owner instanceof DelegatingWrapperItemProvider) {
			val value = owner.value
			if (value instanceof ComponentReference) {
				return ImmutableSet.builder.addAll(scope).add(value).build
			}
		}
		
		return scope
	}
	
	private def getSubstitutionMapping() {
		if (scope == null) {
			return null
		}
		
		val compRef = scope.head
		val filterRelativeScope = scope.tail.toList
		
		return compRef.substitutionMap.findFirst[
			original == value && relativeScope == filterRelativeScope
		]
	}
	
	public override getDelegateValue() {
		if (substitutionMapping != null) {
			substitutionMapping.substitution
		} else {
			value
		}
	}
	
	def boolean isSubstitutionRoot() {
		return substitutionMapping != null
	}
	
	def boolean isSubsituted() {
		if (owner instanceof ComponentReferenceChildrenDelegatingWrapperItemProvider) {
			if (owner.subsituted) {
				return true
			}
		}
		
		return substitutionRoot
	}
	
	override getImage(Object object) {
		val image = super.getImage(object)
		if (!substitutionRoot) {
			return image
		}
		if (substitutionMapping.pinned) {
			return new ComposedImage(#[image, EMFEditPlugin.INSTANCE.getImage("full/ovr16/ControlledObjectTarget")])
		} else {
			return new ComposedImage(#[image, EMFEditPlugin.INSTANCE.getImage("full/ovr16/ControlledObject")])
		}
	}
	
	override getUpdateableText(Object object) {
		if (object != this) {
			throw new IllegalStateException
		}
		
		if (subsituted) {
			super.getUpdateableText(delegateValue)
		} else {
			return null
		}
	}
	
	override setText(Object object, String text) {
		if (object != this) {
			throw new IllegalStateException
		}
		
		if (subsituted) {
			super.setText(delegateValue, text)
		}
	}
	
	override List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
		val originals = super.getPropertyDescriptors(object)
		
		if (subsituted) {
			originals
		} else {
			originals.map[original | 
				new ItemPropertyDescriptorDecorator(object, original) {
					override canSetProperty(Object object) {
						false
					}
				}
			]
		}
	}
	
	override Command createCommand(Object object, EditingDomain domain, Class<? extends Command> commandClass, CommandParameter commandParameter) {
		if (object != this) {
			throw new IllegalStateException
		}
		
		if (subsituted) {
			return super.createCommand(object, domain, commandClass, commandParameter)
		}
		
		if (commandClass == RemoveCommand 
			&& commandParameter.collection.forall[it instanceof ComponentReferenceChildrenDelegatingWrapperItemProvider] 
			&& commandParameter.collection.filter(ComponentReferenceChildrenDelegatingWrapperItemProvider).forall[substitutionRoot]
		) {
			val collection = commandParameter.collection
				.filter(ComponentReferenceChildrenDelegatingWrapperItemProvider)
				.map[delegateValue]
				.filter(EObject)
				.map[eContainer]
				.filter(ComponentReferenceSubstitutionMapping)
				.toList
			val owner = collection.head.eContainer
			return domain.createCommand(RemoveCommand, new CommandParameter(owner, SystemPackage.Literals.COMPONENT_REFERENCE__SUBSTITUTION_MAP, collection))
		}
		
		return UnexecutableCommand.INSTANCE
	}
	
	def static Object unwrap(Object object) {
		var result = object
		while (result instanceof IWrapperItemProvider) {
			result = switch (result) {
				ComponentReferenceChildrenDelegatingWrapperItemProvider: result.delegateValue
				default: result.value
			}
		}
		return result
	}
	
	override equals(Object other) {
		if (this === other) {
			return true
		}
		if (other instanceof ComponentReferenceChildrenDelegatingWrapperItemProvider) {
			return value == other.value && scope == other.scope
		}
		return false
	}
	
	override hashCode() {
		return Objects.hash(value, scope)
	}
}