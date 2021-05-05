/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.hierarchy

import de.dlr.premise.graph.IEdge
import de.dlr.premise.graph.INode
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.EContext
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.shared.APremiseToModelMapper
import org.eclipse.emf.ecore.EObject

/**
 * 
 */
class HierarchyToModelMapper extends APremiseToModelMapper {

	override def IEdge<? extends INode, ? extends INode> createEdge(EObject source, EObject target,
		EObject commonAncestor, String name) {
		Thread.sleep(10); // avoid NPE in yFiles when the edge is deleted before yFiles is ready
		println("Changing the hierarchy is only possible in the tree editor.")
		return null
	}

	override protected EObject createNode(EObject root, String name) {
		println("Creation of nodes is not yet implemented.")
		return null
	}

	override getContext() {
		EContext.HIERARCHY
	}

	override protected isNecessaryForEditing(IEdge<? extends INode, ? extends INode> edge) {
		false
	}

	override isEdgeCreationAllowed() {
		return false;
	}

}
