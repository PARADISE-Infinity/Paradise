/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.petrinet.base.xtend

import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.base.xtend.ICharsetProvidingFileSystemAccess
import de.dlr.exchange.base.xtend.IGeneratorMy
import de.dlr.premise.element.AElement
import de.dlr.premise.element.AGuardCondition
import de.dlr.premise.element.ARepository
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.element.GuardCombination
import de.dlr.premise.element.Mode
import de.dlr.premise.element.ModeGuard
import de.dlr.premise.element.Transition
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.registry.Junction
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.functions.UseCaseRepository
import de.dlr.premise.util.PremiseHelper
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet

import static extension de.dlr.premise.util.PremiseHelper.*

abstract class AGenerator implements IGeneratorMy, IPetriNetContent {

	public static String OPT_TECH = "Petri Net"
	/*public static String OPT_PARAM_NAMES_NAME = "Names of parameters to be shown in diagram:"
	public static String OPT_PARAM_NAMES_VAL = "ParameterName1, ParameterName2"
	public static String OPT_DUMMY_COMPONENT_NAME = "Component names containing one of the following are skipped (parent name is taken instead):"
	public static String OPT_DUMMY_COMPONENT_VAL = "modes, _"*/

	// Layout
	private static int coordStep
	private static int coordYOffset
	protected static int coordX
	protected static int coordY
	// Priority
	protected static int lowPrio = 1

	override void doGenerateFromAElements(ResourceSet resSet, List<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa) {
		reset()
		if(!resSet.resources.nullOrEmpty && resSet.resources.get(0).contents.get(0) instanceof ARepository){
			if(!selectedElements.nullOrEmpty){
				selectedElements.compile(fsa)
			} else {
				println("No elements were found to export.")
			}
		}
	}

	override void doGenerateFromResources(ResourceSet resSet, List<Resource> selectedFiles, ICharsetProvidingFileSystemAccess fsa) {
		// create selectedElements list from ARepositories
		var selectedElements = selectedFiles.flatMap[contents].filter(ARepository).flatMap[elements].toList

		this.doGenerateFromAElements(resSet, selectedElements, fsa)
	}
	
	def reset() {
		GeneratorHelper::clearRefs
	}

	def compile(List<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa) {
		var rep = PremiseHelper::getRoot(selectedElements.get(0)) as ProjectRepository
		val fileName = GeneratorHelper::encodeFileName(rep.projects.get(0).name) + getFileExtension()
		
	
		//var EList<AElement> roots = PremiseHelper::getAll(selectedElements, typeof(AElement))
		
		var content = new StringBuilder()
		coordStep = 60
		coordYOffset = 12
		coordX = 4*coordStep
		coordY = 0

		content.append(createFileHeader(rep, fsa.getFileCharset(fileName)))
		
		for (sc : selectedElements) {
			// create places and transitions
			if(sc instanceof SystemComponent) {
				content.append((sc as SystemComponent).traverseComponents)
			}
		}
		
		 // take center position (without initiating re-initialization by inc* methods > prime number 11 taken)
		coordX = 8 * coordStep - 11
		coordY = 4 * coordStep - 11
		// create additional places/transitions/edges for constraints
		// get all top level constraints in repository
		
		val allTopConstraints = PremiseHelper::getAll(selectedElements.get(0), typeof(AGuardCondition))
				.filter(c|c.eContainer instanceof Transition)
		for (constr : allTopConstraints) {
			content.append(traverseGuardConditions(constr.eContainer as Transition, null, constr as AGuardCondition))
		}

		content.append(createFileFooter())

		fsa.generateFile(fileName, content)
	}

	def CharSequence traverseComponents(SystemComponent sc) {
		var result = new StringBuilder()
		for (mode : PremiseHelper.getModes(sc)) {
			incPlaceCoordinates
			result.append(mode.createPlace)
		}
		for (trans : PremiseHelper.getTransitions(sc)) {
			incTransCoordinates
			// set priorities
			var prio = 0
			val constr = trans.getCondition
			if (constr != null) {
				prio = getMaxPriority() / 10
				if (constr instanceof GuardCombination && (constr as GuardCombination).getJunction==Junction::OR) {
					prio = prio - 1
				}
			}
			result.append(trans.createTransition(prio))
		}

		if (!PremiseHelper.getModes(sc).nullOrEmpty) {
			incComponentCoordinates
		}
		for (child : sc.referencedChildren) {
			result.append(child.traverseComponents)
		}
		return result
	}

	def CharSequence traverseGuardConditions(Transition trans, Mode helperPlace, AGuardCondition condition) {
		var result = new StringBuilder()
		if (condition instanceof ModeGuard) {
			val aTrigger = (condition as ModeGuard).getMode
			if (helperPlace == null) {
				// part of AND junction or no junction
				result.append(createEdge(trans, aTrigger as Mode))
				result.append(createEdge(aTrigger as Mode, trans))	
			} else {
				// part of OR junction
				// create helper transition for every OR combination child
				val Transition helpTrans = ElementFactory::eINSTANCE.createTransition
				helpTrans.source = aTrigger as Mode
				helpTrans.target = helperPlace
				incTransCoordinates
				result.append(helpTrans.createTransition(lowPrio))
				result.append(createEdge(helpTrans, helpTrans.getSource)) // bi-directive
			}
		} else if (condition instanceof GuardCombination) {
			val guardCombination = condition as GuardCombination
			var helper = null as Mode
			if (guardCombination.getJunction==Junction::OR) {
				// create helper place for triggering OR constraints
				helper = ElementFactory::eINSTANCE.createMode
				incPlaceCoordinates
				result.append(helper.createPlace)
				result.append(createEdge(helper, trans)) // helper place to transition with constraint
			}
			for (subConditions : guardCombination.children) {
				result.append(traverseGuardConditions(trans, helper, subConditions))
			}
		}
		return result
	}
	
	def String getLabel(ANameItem item) {
		var result = GeneratorHelper::encodeFileName(item.name)
		if (item instanceof Mode) {
			if (result.nullOrEmpty) {
				result = "p"
			}
			result = result + GeneratorHelper::getRef(item)
		} else if (item instanceof Transition) {
			if (result.nullOrEmpty) {
				result = "t"
			}
			// get edge id starting from 1
			//result = result + (Integer::valueOf(GeneratorHelper::edgeID) + 1)
			result = result + GeneratorHelper::getRef(item)
		}
	}

    /**
     * Returns 1 for first mode of components which is an entry mode and 0 for all others
     */
	def int getDefaultTokens(Mode mode) {
		var result = 0
		val sc = PremiseHelper.getRootComponent(mode)
		if (sc != null && PremiseHelper.getModes(sc).findFirst(m|m.isEntryMode) == mode) {
			result = 1
		}
		return result
	}

	def void incPlaceCoordinates() {
		if (coordX % (4*coordStep) == 0) {
			// left place
			coordX = coordX - 2*coordStep
			coordY = coordY + 2*coordStep
		} else {
			// right place, with slight y-offset because of long mode names
			coordX = coordX + 2*coordStep
			coordY = coordY + coordYOffset
		}
	}
	def void incTransCoordinates() {
		if (coordX % (4*coordStep) == 0) {
			// init coordinates when current x-coord is on right side (even count of places)
			coordX = coordX - coordStep
			coordY = 2*coordStep
		} else if (coordX % (2*coordStep) == 0) {
			// init coordinates when current x-coord is on left side (uneven count of places)
			coordX = coordX + coordStep
			coordY = 2*coordStep
		} else {
			coordY = coordY + coordStep - coordYOffset
		}
	}
	def void incComponentCoordinates() {
		// shift next component's state machine to the right 
		coordX = coordX + 5 * coordStep
		coordY = 0
	}

	override createFileHeader(ARepository repository, String charset) '''
	'''

	override createFileFooter() '''
	'''

	override getMaxPriority() {
		127
	}
	
	def dispatch getElements(ProjectRepository pr) {
		pr.projects.map(p|p as AElement)
	}
	def dispatch getElements(UseCaseRepository ur) {
		ur.usecases.map(u|u as AElement)
	}
}
