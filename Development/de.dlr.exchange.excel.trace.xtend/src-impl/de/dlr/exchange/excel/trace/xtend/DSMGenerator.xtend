/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.excel.trace.xtend

import com.google.common.collect.Sets
import com.smartxls.WorkBook
import de.dlr.exchange.base.xtend.ICharsetProvidingFileSystemAccess
import de.dlr.exchange.base.xtend.RepresentationHelper
import de.dlr.premise.component.ISatisfieable
import de.dlr.premise.component.ISatisfying
import de.dlr.premise.component.ISystemComponentSatisfieable
import de.dlr.premise.component.Satisfies
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ARepository
import de.dlr.premise.element.Connection
import de.dlr.premise.element.Relation
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.functions.UseCase
import de.dlr.premise.functions.UseCaseRepository
import de.dlr.premise.util.PremiseHelper
import de.dlr.premise.util.scope.ScopedEObjectFactory
import java.util.ArrayList
import java.util.List
import java.util.Set
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.generator.IFileSystemAccess

import static extension de.dlr.premise.util.PremiseHelper.*

class DSMGenerator extends AbstractGenerator {

	override doGenerateFromAElements(ResourceSet resSet, List<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa, IProgressMonitor monitor) {
		var ARepository rootRepo
		var EObject toproot
		var EObject leftroot
		var String fileName
		try {

			rootRepo = PremiseHelper::getRoot(selectedElements.get(0)) as ARepository

			toproot = leftroot = selectedElements.get(0)
			fileName = selectedElements.get(0).name

			if (selectedElements.length > 1) {
				leftroot = selectedElements.get(1)
			}

			if (selectedElements.length > 2) {
				println('''Warning: Number of selected elements is «selectedElements.length». The first two elements will be used.''')
			}

			doGenerate(resSet, rootRepo, toproot, leftroot, fileName, fsa, monitor)

		} catch (IndexOutOfBoundsException e) {
			println("Could not export. No elements could be found.")
		}
	}

	override doGenerateFromResources(ResourceSet resSet, List<Resource> selectedFiles, ICharsetProvidingFileSystemAccess fsa, IProgressMonitor monitor) {
		var ARepository rootRepo
		var EObject toproot
		var EObject leftroot
		var String fileName
		try {
			var repos = selectedFiles.figureReposToUse
			if (!repos.nullOrEmpty) {
				rootRepo = repos.get(0)

				toproot = rootRepo
				leftroot = repos.get(1)

				fileName = rootRepo.firstElement.name

				doGenerate(resSet, rootRepo, toproot, leftroot, fileName, fsa, monitor)
				repHelper.dispose()
			}

		} catch (IndexOutOfBoundsException e) {
			println("Could not export. No elements could be found.")
		}
	}

	def void doGenerate(ResourceSet resSet, ARepository rootRepo, EObject toproot, EObject leftroot, String fileName,
		IFileSystemAccess fsa, IProgressMonitor monitor) {
	
		// set options and init colors
		new TraceOptions().setOptions(rootRepo)
		initOption(rootRepo, DSMOptions.OPT_TECH)
		repHelper = RepresentationHelper.getInstance(resSet)
		
		var toprow = ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(toproot).eAllContentsIncludingRoot.filter(AElement).toList
		var leftcol = ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(leftroot).eAllContentsIncludingRoot.filter(AElement).toList

		if (OPT_INCLUDE_PARENTS.option == "off") { // leaves only
			toprow = toprow.filter[children.nullOrEmpty].toList
			leftcol = leftcol.filter[children.nullOrEmpty].toList
		}
		fsa.compile(resSet, toprow, leftcol, fileName, monitor)
	}
	
	override protected String getTitle() {
		return "Design Structure Matrix (DSM)"
	}
	
	override protected void grayOutUnneededRow(List<AElement> toprow, WorkBook workbook, Integer i) {
	}
	override protected void grayOutUnneededCol(List<AElement> leftcol, WorkBook workbook, Integer i) {
	}
	
	override protected void printRelations(List<AElement> leftcol, Set<Satisfies<?, ?>> satisfies, AElement top, WorkBook workbook, Integer i, Set<Relation> relations, Set<Connection<?>> connections, Set<Balancing> balancings) {
		leftcol.forEach [ left, j |
			// count the relations/connections/...
			var List<Pair<String, Integer>> counters = new ArrayList
			
			// relations
			if (OPT_DEPENDENCY_TYPES_RELATIONS.option == "on") {
				counters.add("r" -> relations.filter[source == top && target == left].length)
			}
			
			// connections
			if (OPT_DEPENDENCY_TYPES_CONNECTIONS.option == "on") {
				counters.add("c" -> connections.filter[source == top && target == left].length)
			}
			
			// balancings
			if (OPT_DEPENDENCY_TYPES_BALANCINGS.option == "on") {
				if (top instanceof SystemComponent && left instanceof SystemComponent) {
					val parameters = (top as SystemComponent).parameters.toSet
					counters.add("b" -> balancings.filter [ b |
						!Sets.intersection(parameters, b.sources.toSet).empty && !parameters.contains(b.target)
					].length)
				}
			}
			
			// satisfies
			if (OPT_DEPENDENCY_TYPES_SATISFIES.option == "on") {						
				if (top instanceof SystemComponent && left instanceof ISystemComponentSatisfieable) {
					val leftSat = left as ISystemComponentSatisfieable
					val topSC = top as SystemComponent
					
					if (topSC.satisfiedSatisfieables.contains(leftSat) && topSC.satisfieableApplies(leftSat)) {
						val leftChildSatisfieables = leftSat.eAllContents.filter(ISatisfieable).toSet
						if (leftChildSatisfieables.size == 0) {
							// if there are no parameters we set to one, to indicate the relation between Component and ComponentSatisfieable
							counters.add("s" -> 1)
						} else {
							val satisfiedChildSatisfieablesCount = topSC.eAllContents.filter(ISatisfying).filter[satisfying |
								satisfying.satisfiedSatisfieables.exists[satisfieable | leftChildSatisfieables.contains(satisfieable)]
							].size
							counters.add("s" -> satisfiedChildSatisfieablesCount)
						}
					}
				}
			}
			
			if (OPT_DEPENDENCY_COUNT.option == "on") {
				var count = counters.map[value].reduce[a, b|a + b]
				// print the counter
				if (count > 0) {
					workbook.setNumber(j + 5, i + 2, count)
				}
			} else {
				var text = counters.filter[value > 0].map[key].join
				// print the text
				if(!text.nullOrEmpty){
					workbook.setText(j + 5, i + 2, text)	
				}
			}
		]
	}

	// --------------------------------------
	// extension methods --------------------
	// --------------------------------------
	/**
	 * figure out which Repositories to use for the Matrix
	 */
	private def figureReposToUse(List<Resource> resources) {
		var List<? extends ARepository> repositories = resources.map[contents.head as ARepository]
		if (repositories.length >= 2) {
			if (repositories.length > 2) {
				println('''Warning: Number of selected files is «repositories.length». The first two files will be used.''')
				repositories = repositories.subList(0, 2)
			}
			if (repositories.get(0) instanceof UseCaseRepository && repositories.get(1) instanceof ProjectRepository) {
				return repositories.reverseView
			} else {
				return repositories
			}
		} else if (repositories.length == 1) {
			return #[repositories.get(0), repositories.get(0)]
		} else {
			println("Could not export. Neither ProjectRepositories nor UseCaseRepositories found.")
			return null
		}
	}

	/**
	 * @return the value of the '<em>Children</em>' containment reference list.
	 */
	private def getChildren(AElement element) {
		if (element instanceof SystemComponent) {
			return element.referencedChildren
		} else if (element instanceof UseCase) {
			return element.children
		}
	}

	private def AElement getFirstElement(ARepository repo) throws IndexOutOfBoundsException{
		if (repo instanceof ProjectRepository) {
			return repo.projects?.get(0)
		} else if (repo instanceof UseCaseRepository) {
			return repo.usecases?.get(0)
		}
	}
	
}
