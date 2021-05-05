/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.trace.xtend

import com.smartxls.WorkBook
import de.dlr.exchange.base.xtend.ICharsetProvidingFileSystemAccess
import de.dlr.exchange.base.xtend.RepresentationHelper
import de.dlr.premise.component.ISatisfying
import de.dlr.premise.component.Satisfies
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ARepository
import de.dlr.premise.element.Connection
import de.dlr.premise.element.Relation
import de.dlr.premise.graph.INode
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.functions.UseCase
import de.dlr.premise.util.PremiseHelper
import de.dlr.premise.util.scope.ScopedEObjectFactory
import java.util.Collection
import java.util.List
import java.util.Set
import java.util.function.Predicate
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet

import static extension de.dlr.premise.util.PremiseHelper.*
import de.dlr.premise.component.ISatisfieable

class TraceGenerator extends AbstractGenerator {

	override doGenerateFromAElements(ResourceSet resSet, List<AElement> selectedElements,
		ICharsetProvidingFileSystemAccess fsa, IProgressMonitor monitor) {
		var pr = PremiseHelper::getRoot(selectedElements.head) as ARepository
		setUpGenerator(pr)
		var toprow = selectedElements.allSolutionComponents
		var leftcol = resSet.allUseCases.map[ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(it)]
		if (leftcol.nullOrEmpty) {
			println("No UseCases could be found.")
			return
		}

		compile(fsa, resSet, toprow, leftcol, selectedElements.head.name, monitor)
	}

	override doGenerateFromResources(ResourceSet resSet, List<Resource> selectedFiles,
		ICharsetProvidingFileSystemAccess fsa, IProgressMonitor monitor) {
		val pr = selectedFiles.flatMap[contents].filter(ProjectRepository).head
		if (pr == null) {
			println("No ProjectRepository could be found.")
			return
		}
		setUpGenerator(pr)

		val toprow = selectedFiles.flatMap[contents].allSolutionComponents
		if (toprow.empty) {
			println("No SystemComponents could be found.")
			return
		}

		val leftcol = resSet.allUseCases.map[ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(it)]
		if (leftcol.empty) {
			println("No UseCases could be found.")
			return
		}

		compile(fsa, resSet, toprow, leftcol, pr.projects.get(0).name, monitor)
		repHelper.dispose()
	}

	override protected String getTitle() {
		return "Traceability Matrix"
	}

	def getAllSolutionComponents(Iterable<? extends EObject> elements) {
		val allContents = elements.iterator.flatMap [
			ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(it).eAllContentsIncludingRoot
		].toSet
		if (OPT_DEPTH_INFERENCE.isEnabled) {
			return getDynamicDepthSolutionComponents(allContents).filter(AElement).toList
		}
		getLeavesOnlySolutionComponents(allContents)
	}

	private def List<AElement> getLeavesOnlySolutionComponents(Set<EObject> allContents) {
		val Predicate<SystemComponent> parents = if (OPT_INCLUDE_PARENTS.isEnabled) {
				[true]
			} else {
				[children.isEmpty]
			}
		(allContents.filter(SystemComponent).filter(parents).toList) as List<?> as List<AElement>
	}

	def private getDynamicDepthSolutionComponents(Set<EObject> allContents) {
		val allSatisfying = allContents.filter(SystemComponent).filter[!satisfies.map[target].filter(UseCase).empty].
			toList
		val theSiblings = allSatisfying.map[parent].toSet.flatMap[children]
		val theParents = theSiblings.filter(EObject).closure[newArrayList(parent)]
		val unsatisfied = allContents - theParents
		val unsatisfiedSiblings = unsatisfied.filter[elem|theParents.exists[isSiblingOf(elem)]].toSet
		if (OPT_INCLUDE_PARENTS.isEnabled) {
			allContents.retainAll((theParents + unsatisfiedSiblings).toList)
			return allContents
		}
		val unsatisfiedLeaves = unsatisfiedSiblings.filter[children.forall[!unsatisfiedSiblings.contains(it)]]
		allContents.retainAll((theSiblings + unsatisfiedLeaves).toList)
		return allContents
	}

	def private <T> Set<T> operator_minus(Set<T> xs, Collection<T> ys) {
		val clone = <T>newHashSet(xs)
		clone.removeAll(ys)
		return clone
	}

	def private isSiblingOf(EObject it, EObject sibling) {
		it == sibling || parent == sibling.parent
	}

	def private dispatch getChildren(SystemComponent sc) {
		return sc.children.map[if(it instanceof ComponentReference) it.activeImplementation else it as SystemComponent]
	}

	def private dispatch getChildren(ProjectRepository rep) {
		return rep.projects
	}

	def private dispatch getChildren(EObject obj) {
		return newArrayList()
	}

	def getAllUseCases(ResourceSet resSet) {
		resSet.allContents.filter(UseCase).toList as List<?> as List<AElement>
	}

	def private void setUpGenerator(ARepository pr) {
		repHelper = RepresentationHelper.getInstance(pr.eResource.resourceSet)
		initOption(pr, TraceOptions.OPT_TECH)
	}

	override protected grayOutUnneededRow(List<AElement> toprow, WorkBook workbook, Integer i) {
		// check all columns if the i'th row is unneeded
		var unneeded = (0 .. toprow.length).forall[c|workbook.getText(i + 5, c + 1).nullOrEmpty]
		if (unneeded) {
			workbook.markUnneeded(i + 5, 2, i + 5, toprow.length + 1)
		}
	}

	override protected grayOutUnneededCol(List<AElement> leftcol, WorkBook workbook, Integer i) {
		// check all rows if the i'th column is unneeded
		var unneeded = (0 .. leftcol.length - 1).forall[r|workbook.getText(r + 5, i + 2).nullOrEmpty]
		if (unneeded) {
			workbook.markUnneeded(5, i + 2, 4 + leftcol.length, i + 2)
		}
	}

	override protected printRelations(List<AElement> leftcol, Set<Satisfies<?, ?>> satisfies, AElement top,
		WorkBook workbook, Integer i, Set<Relation> relations, Set<Connection<?>> connections,
		Set<Balancing> balancings) {
		leftcol.forEach [ left, j |
			var satisfy = (top as ISatisfying<? extends INode, ? extends ISatisfieable>).satisfies.findFirst[target == left]
			if (satisfy != null) {
				var name = "X"
				if (!satisfy.name.nullOrEmpty) {
					name = satisfy.name
				}
				try {
					workbook.setNumber(j + 5, i + 2, Double.parseDouble(name))
				} catch (Exception e) {
					workbook.setText(j + 5, i + 2, name)
				}
			}
		]
	}

}
