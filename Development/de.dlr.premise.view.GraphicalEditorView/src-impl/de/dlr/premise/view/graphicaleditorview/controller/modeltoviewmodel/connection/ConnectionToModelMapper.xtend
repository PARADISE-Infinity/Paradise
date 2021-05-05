/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.connection

import de.dlr.premise.element.AElement
import de.dlr.premise.element.Connection
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.IEdge
import de.dlr.premise.graph.INode
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.system.SystemPackage
import de.dlr.premise.functions.UseCase
import de.dlr.premise.functions.UseCasePackage
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.EContext
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.shared.APremiseToModelMapper
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.edit.command.CreateChildCommand

import static de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry.getEditingDomain

/**
 * 
 */
class ConnectionToModelMapper extends APremiseToModelMapper {

	override def IEdge<? extends INode,? extends INode> createEdge(EObject source, EObject target, EObject commonAncestor, String name) {
		if (!(source instanceof AElement || source instanceof ComponentReference) || !(target instanceof AElement || target instanceof ComponentReference)) {
			println("Source and target of connections have either to be of type SystemComponent, UseCase or ComponentDefinition")
			return null
		}
		val root = if (commonAncestor instanceof ComponentReference) commonAncestor.eContainer else commonAncestor
		
		var EReference feature
		if (root instanceof UseCase) {
			feature = UseCasePackage.Literals.USE_CASE__CONNECTIONS
		} else if (root instanceof SystemComponent) {
			feature = SystemPackage.Literals.SYSTEM_COMPONENT__CONNECTIONS
		} else {
			println("Connection needs to be child of a SystemComponent or UseCase")
			return null
		}
		
		var rel = ElementFactory.eINSTANCE.createConnection()
		rel.sourcePointer = source.toPointer as APointer<AElement>
		rel.targetPointer = target.toPointer as APointer<AElement>
		rel.name = name

		var command = new CreateChildCommand(editingDomain, root, feature, rel, #[root])
		if (!command.executeCommand) {
			println('''Failed to add Connection «rel.source» > «rel.target» to «root.name»''')
			return null
		}
		return rel
	}

	override protected EObject createNode(EObject root, String name) {
		println("Creation of nodes is not yet implemented.")
		return null
	}

	override getContext() {
		EContext.CONNECTION
	}
	
	override protected isNecessaryForEditing(IEdge<? extends INode, ? extends INode> edge) {
		edge instanceof Connection<?>
	}
	
}
