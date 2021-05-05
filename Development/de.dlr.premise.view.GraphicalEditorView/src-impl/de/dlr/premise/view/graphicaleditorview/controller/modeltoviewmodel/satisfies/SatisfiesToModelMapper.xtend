/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.satisfies

import de.dlr.premise.component.ComponentFactory
import de.dlr.premise.component.ComponentPackage
import de.dlr.premise.component.ISatisfieable
import de.dlr.premise.component.ISatisfying
import de.dlr.premise.component.Satisfies
import de.dlr.premise.graph.IEdge
import de.dlr.premise.graph.INode
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.EContext
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.shared.APremiseToModelMapper
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.edit.command.CreateChildCommand

import static de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry.getEditingDomain

/**
 * 
 */
class SatisfiesToModelMapper extends APremiseToModelMapper {

	override def IEdge<? extends INode,? extends INode> createEdge(EObject source, EObject target, EObject root, String name) {
		val satisfyingType = source.eClass.satisfyingSuperType
		val satisfyingSourceType = satisfyingType?.ETypeArguments?.get(0)
		val satisfyingTargetType = satisfyingType?.ETypeArguments?.get(1)
			
		if (satisfyingType == null || !satisfyingSourceType.isInstance(source) || !satisfyingTargetType.isInstance(target)) {
			println("Not a valid satisfy-Relation.")
			return null
		}
		
		
		val satisfying = source as ISatisfying<INode, ISatisfieable>
		val satisfieable = target as ISatisfieable
		
		var rel = ComponentFactory.eINSTANCE.createSatisfies()
		rel.target = satisfieable

		var command = new CreateChildCommand(editingDomain, satisfying, ComponentPackage.Literals.ISATISFYING__SATISFIES, rel, #[root])
		if (!command.executeCommand) {
			println('''Failed to add Satisfies «rel.source.name» > «rel.target.name» to «root.name»''')
			return null
		}
		return rel
	}

	override protected EObject createNode(EObject root, String name) {
		println("Creation of nodes is not yet implemented.")
		return null
	}

	override getContext() {
		EContext.SATISFIES
	}
	
	def private getSatisfyingSuperType(EClass sourceClass) {
		sourceClass.EAllGenericSuperTypes.findFirst[EClassifier == ComponentPackage.Literals.ISATISFYING_ROOT] ?: 
			sourceClass.EAllGenericSuperTypes.findFirst[EClassifier == ComponentPackage.Literals.ISATISFYING]
	}
	
	override protected isNecessaryForEditing(IEdge<? extends INode, ? extends INode> edge) {
		edge instanceof Satisfies<?, ?>
	}

}
