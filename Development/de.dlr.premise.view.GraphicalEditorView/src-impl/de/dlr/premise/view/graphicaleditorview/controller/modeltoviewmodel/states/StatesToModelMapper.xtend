/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.states

import de.dlr.premise.element.ElementFactory
import de.dlr.premise.element.ElementPackage
import de.dlr.premise.element.Mode
import de.dlr.premise.element.StateMachine
import de.dlr.premise.element.Transition
import de.dlr.premise.graph.IEdge
import de.dlr.premise.graph.INode
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.EContext
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.IAspectToModelMapper
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.shared.AFilteredRepositoryToModelMapper
import de.dlr.premise.view.graphicaleditorview.viewmodel.Icon
import java.awt.Color
import java.util.Collection
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.edit.command.AddCommand
import org.eclipse.emf.edit.command.CreateChildCommand

import static de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry.getEditingDomain

/** 
 * 
 */
class StatesToModelMapper extends AFilteredRepositoryToModelMapper {
	/* (non-Javadoc)
	 * @see IAspectToModelMapper#getContext()
	 */
	override EContext getContext() {
		return EContext.STATES
	}

	/* (non-Javadoc)
	 * @see IAspectToModelMapper#isSelectionEmptyForMapper(java.util.Collection)
	 */
	override boolean isSelectionEmptyForMapper(Collection<?> selection) {
		return selection.filter(EObject).filter[node].nullOrEmpty
	}

	/* (non-Javadoc)
	 * @see AFilteredRepositoryToModelMapper#createEdge(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, java.lang.String)
	 */
	override protected IEdge<? extends INode,? extends INode> createEdge(EObject source, EObject target, EObject root, String name) {
		if (!(source instanceof Mode && target instanceof Mode)) {
			println("Source and target of Transitions have to be of type Mode")
			return null
		}
		var Transition rel
		var EReference feature
		
		var EObject rootComponent = root
		if(root instanceof Mode){
			rootComponent = root.eContainer
		}

		
		feature = ElementPackage.Literals.STATE_MACHINE__TRANSITIONS
		rel = ElementFactory.eINSTANCE.createTransition()

		rel.setSource(source as Mode)
		rel.setTarget(target as Mode)
		rel.name = name

		var command = new CreateChildCommand(editingDomain, rootComponent, feature, rel, #[root])
		if (!command.executeCommand) {
			println('''Failed to add Transition «rel.source.name» > «rel.target.name» to «root.name»''')
			return null
		}
		return rel
	}

	override protected EObject createNode(EObject root, String name) {
		if (!(root instanceof StateMachine)) {
			println("Modes can only be added to StateMachines")
			return null
		}
		var Mode node = ElementFactory.eINSTANCE.createMode
		node.name = name
		node.entryMode = !(root as StateMachine).getModes().exists[entryMode]
		setDefaultIcon(root as StateMachine)
		var feature = ElementPackage.Literals.STATE_MACHINE__MODES

		var command = new AddCommand(editingDomain, root, feature, node)
		if (!command.executeCommand) {
			println('''Failed to add Mode «node.name» to «root.name»''')
			return null
		}
		return node
	}

	/* (non-Javadoc)
	 * @see AFilteredRepositoryToModelMapper#isNode(org.eclipse.emf.ecore.EObject)
	 */
	override protected boolean isNode(EObject node) {
		switch node {
			StateMachine: return true
			Mode: return true
		}
		return false
	}

	
	override protected getNodes(Collection<? extends EObject> selection) {
		return selection.filter(StateMachine).map[modes].flatten.filter(EObject).filter[!affectedByFilterQuery].toList
	}

	/* (non-Javadoc)
	 * @see AFilteredRepositoryToModelMapper#getRelations(java.util.Collection)
	 */
	override protected Collection<IEdge<? extends INode, ? extends INode>> getRelations(
		Collection<? extends EObject> selection) {
		return selection.filter(StateMachine).map[transitions].flatten.filter [
			!affectedByFilterQuery || it instanceof Transition
		].filter(IEdge).toList as Collection<?> as Collection<IEdge<? extends INode, ? extends INode>>
	}

	/* (non-Javadoc)
	 * @see AFilteredRepositoryToModelMapper#getName(org.eclipse.emf.ecore.EObject)
	 */
	override protected String getName(EObject object) {
		if (object instanceof ANameItem) {
			return object.name ?: ""
		}
		return '''Type "«object.class.name»" not supported'''
	}

	/* (non-Javadoc)
	 * @see AFilteredRepositoryToModelMapper#getChildren(org.eclipse.emf.ecore.EObject)
	 */
	override protected Collection<? extends EObject> getChildren(EObject object) {
		var result = newArrayList()
		if (object instanceof StateMachine) {
			result.addAll(object.getModes())
		}
		return result
	}

	/* (non-Javadoc)
	 * @see AFilteredRepositoryToModelMapper#isSecondary(org.eclipse.emf.ecore.EObject)
	 */
	override protected boolean isSecondary(EObject object) {
		return false // we will only display one tree
	}

	override protected getLabelForEdge(IEdge<? extends INode, ? extends INode> edge) {
		return edge.name
	}

	override protected getColorForEdge(IEdge<? extends INode, ? extends INode> edge) {
		return Color.black
	}

	override protected getBidirectionalForEdge(IEdge<? extends INode, ? extends INode> edge) {
		return false
	}

	override protected Icon getIcon(EObject node) {
		switch node {
			Mode case node.isEntryMode: {
				return Icon.ROUND_DOUBLE_BORDER
			}
			Mode: {
				return Icon.ROUND
			}
		}
		return Icon.ROUND_RECT
	}

	override selectionChanged(Collection<EObject> selection) {
		super.selectionChanged(selection)
		if (selection.length > 0 && selection.get(0) instanceof StateMachine) {
			setDefaultIcon(selection.get(0) as StateMachine)
		}
	}

	protected def void setDefaultIcon(StateMachine root) {
		if (root.getModes().exists[entryMode]) {
			MapperRegistry.graphableModel.defaultNodeIcon = Icon.ROUND
		} else {
			MapperRegistry.graphableModel.defaultNodeIcon = Icon.ROUND_DOUBLE_BORDER
		}
	}
	
	override isNodeCreationAllowed(Collection<EObject> selection) {
		
		var isInStateMachine = false
		for (sel : selection) {
			var EObject it = sel;
	        while (it != null && !(it instanceof StateMachine)) {
	            it = it.eContainer();
	        }
	        isInStateMachine = it != null
	        if (isInStateMachine) {
	        	return true
	        }
        }
		
		return false
	}

}
