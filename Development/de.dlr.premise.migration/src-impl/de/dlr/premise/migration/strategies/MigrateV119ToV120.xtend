/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.migration.strategies

import de.dlr.premise.migration.MigrationModel
import de.dlr.premise.migration.ModelVersion
import de.dlr.premise.migration.util.MigrationHelper
import java.util.List
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * 
 */
class MigrateV119ToV120 extends AMigration {

	MigrationModel model
	Element modelRoot
	Document doc

	var List<Node> rootTransitionConstraints
	var Iterable<Element> transitionConstraints
	var Iterable<Element> nestedTransitionConstraints
	var List<String> transitionIDs
	var Iterable<Element> modes

	override getTargetVersion() {
		ModelVersion.V120.toString
	}

	override protected migrateRoot(MigrationModel model, Element modelRoot) {
		this.model = model
		this.modelRoot = modelRoot
		doc = model.modelDocument

		obtainAllElements()

		renameTagConstraintToCondition()
		convertTransitionConstraintToModeGuard()
		convertNestedTransitionConstraintToGuardCombination()

		setEntryModeDefaultToTrue()
	}

	private def obtainAllElements() {
		rootTransitionConstraints = MigrationHelper.listFromNodeList(modelRoot.getElementsByTagName("constraint"))
		val aTransitionConstraints = rootTransitionConstraints.plusAllChildren

		transitionConstraints = aTransitionConstraints.filter[transitionConstraint]
		nestedTransitionConstraints = aTransitionConstraints.filter[nestedTransitionConstraint]

		transitionIDs = MigrationHelper.listFromNodeList(modelRoot.getElementsByTagName("transitions")).filter(Element).
			map[getAttribute("id")].toList

		modes = MigrationHelper.listFromNodeList(modelRoot.getElementsByTagName("modes")).filter(Element)
	}

	private def renameTagConstraintToCondition() {
		rootTransitionConstraints.forEach [
			// rename tag constraint to condition
			doc.renameNode(it, namespaceURI, "condition")
			model.setChange()
		]
	}

	private def convertTransitionConstraintToModeGuard() {
		transitionConstraints.forEach [
			setAttribute("xsi:type", "elem:ModeGuard")
			MigrationHelper.renameAttribute(it, "trigger", "mode")
			model.setChange()
			if (referenceIsTransition(it)) {
				throw new Exception("ModeGuard referenced Transition with id " + getAttribute("mode") +
					". This is no longer applicable. Call support.")
			}
		]
	}

	private def referenceIsTransition(Element it) {
		transitionIDs.contains(getAttribute("mode"))
	}

	private def convertNestedTransitionConstraintToGuardCombination() {
		nestedTransitionConstraints.forEach [
			setAttribute("xsi:type", "elem:GuardCombination")
			model.setChange()
		]
	}

	private def setEntryModeDefaultToTrue() {
		modes.filter[!hasAttribute("entryMode")].forEach [
			setAttribute("entryMode", "true")
			model.setChange()
		]
	}

	private def List<Element> plusAllChildren(Iterable<Node> elements) {
		val List<Node> children = newArrayList(elements)
		val allChildren = MigrationHelper.listFromNodeList(modelRoot.getElementsByTagName("children"))
		children.addAll(allChildren)
		return children.filter(Element).toList
	}

	private def isTransitionConstraint(Element element) {
		element.getAttribute("xsi:type") == "elem:TransitionConstraint"
	}

	private def isNestedTransitionConstraint(Element element) {
		element.getAttribute("xsi:type") == "elem:NestedTransitionConstraint"
	}

}
