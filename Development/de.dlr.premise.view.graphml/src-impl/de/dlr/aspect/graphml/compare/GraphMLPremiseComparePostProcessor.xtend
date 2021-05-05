/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.aspect.graphml.compare

import de.dlr.premise.element.AElement
import de.dlr.premise.element.ElementPackage
import de.dlr.premise.element.Relation
import de.dlr.premise.functions.UseCasePackage
import de.dlr.premise.util.PremiseHelper
import java.util.regex.Pattern
import org.eclipse.emf.common.util.Monitor
import org.eclipse.emf.compare.AttributeChange
import org.eclipse.emf.compare.Comparison
import org.eclipse.emf.compare.Diff
import org.eclipse.emf.compare.DifferenceKind
import org.eclipse.emf.compare.Match
import org.eclipse.emf.compare.ReferenceChange
import org.eclipse.emf.compare.postprocessor.BasicPostProcessorDescriptorImpl
import org.eclipse.emf.compare.postprocessor.IPostProcessor
import org.eclipse.emf.ecore.util.EcoreUtil
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.SystemPackage
import de.dlr.premise.registry.ADataItem
import de.dlr.premise.registry.RegistryPackage

/**
 * A PostProcessor for a comparison between two premise models where one was transformed from GraphML. 
 * 
 * Filters differences that solely stem from the model-to-model information loss so that only true user made changes remain.
 * 
 * Since premise and graphml operate at different semantic levels information is lost when doing a model-to-model 
 * transform from premise to graphml. Later we transform the graphml back into premise and get a partial premise model,
 * which only contains the information present in the graphml.
 * 
 * We now want to merge this partial premise model with the original full model to transfer the changes made in a graphml
 * editor. So we perform a comparison between the two models which gives us all changes made between them. However, we 
 * also see all information which was lost previously in the model-to-model transform as deletion changes, even if the 
 * graphml wasn't changed at all.
 * 
 * At this point this GraphMLPremiseComparePostProcessor comes into play. It removes all the unneeded differences, leaving 
 * only the changes that were truly made by the user.
 */
class GraphMLPremiseComparePostProcessor implements IPostProcessor {

	def register(IPostProcessor.Descriptor.Registry<String> postProcessorRegistry) {
		val processor = this
		
		val descriptor = new BasicPostProcessorDescriptorImpl(new GraphMLPremiseComparePostProcessor,
			Pattern.compile(".*"), null)
		postProcessorRegistry.put(GraphMLPremiseComparePostProcessor.toString, descriptor)
		
		return processor
	}

	override postMatch(Comparison comparison, Monitor monitor) {
		// Today I don't feel like doing anything
	}

	override postDiff(Comparison comparison, Monitor monitor) {
		comparison.differences.filter[!isPossibleDiff].forEach[EcoreUtil.remove(it)]
		
		createNewIdsForCopies(comparison)
	}

	override postRequirements(Comparison comparison, Monitor monitor) {
		// Don't feel like picking up my phone
	}

	override postEquivalences(Comparison comparison, Monitor monitor) {
		// So leave a message at the tone
	}

	override postConflicts(Comparison comparison, Monitor monitor) {
		// 'Cause today I swear I'm not doing anything.
	}

	override postComparison(Comparison comparison, Monitor monitor) {
		// Nothing at all
	}
	
	
	/**
	 * Fix duplicated ids on element copies performed in yEd
	 * 
	 * Copying an elemen in yEd will result in the original elements id being copied as well. We recognize those elements as added anyways 
	 * thanks to the {@link DuplicateAwareIdentifierEObjectMatcher}. One instance is matched to the corresponding element in the premise 
	 * model and the other one is marked as newly added.
	 * 
	 * For the newly added element, we want to generate a new id to prevent id collisons.
	 */
	def private createNewIdsForCopies(Comparison comparison) {
		val allMatches = comparison.matches.map[eAllContents.toIterable].flatten.filter(Match)
		val allADataItems  = allMatches.map[right].filter(ADataItem)
		
		val duplicateIds = allADataItems
			.filter[ dataItem |
				val id = dataItem.id
				if (id != null) {
					val resource = dataItem.eResource
					if (resource != null) {
						val otherEObject = resource.getEObject(id)
						if (dataItem != otherEObject) {
							return true
						}
					}
				}
				return false
			]
			.map[id]
			.toSet
			
		comparison.differences
			.filter(ReferenceChange)
			.filter[kind == DifferenceKind.ADD]
			.map[value]
			.filter(ADataItem)
			.forEach[
				if (duplicateIds.contains(it.id)) {		
					it.id = PremiseHelper.createId()
				}
			]
	}

	/**
	 * Check if a given {@link Diff} corresponds to a change that could have been made in graphml.
	 */
	def private isPossibleDiff(Diff it) {
		switch (it) {
			ReferenceChange: {
				isSystemComponentChildrenChange ||
					isSystemComponentRelationsChange ||
					reference == UseCasePackage.Literals.USE_CASE__CHILDREN ||
					reference == UseCasePackage.Literals.USE_CASE__RELATIONS ||
					isRelationSourceChange ||
					reference == ElementPackage.Literals.RELATION__TARGET ||
					reference == SystemPackage.Literals.PROJECT_REPOSITORY__PROJECTS ||
					reference == UseCasePackage.Literals.USE_CASE_REPOSITORY__USECASES
			}
			AttributeChange: {
				attribute == RegistryPackage.Literals.ANAME_ITEM__NAME
			}
			default:
				false
		}
	}
	
	private def isSystemComponentChildrenChange(ReferenceChange change) {
		if (change.reference != SystemPackage.Literals.SYSTEM_COMPONENT__CHILDREN) {
			return false
		}
		
		if (change.kind == DifferenceKind.DELETE) {
			val wasComponentReference = change.value instanceof ComponentReference
			
			return !wasComponentReference
		} else {
			return true
		}
	}
	
	/**
	 * Check if a SystemComponent Relation change is a user-made change.
	 * 
	 * Invalid Relations are not transformed to GraphML, and will therefore appear as deleted after the reverse transformation.
	 * 
	 * We ignore those changes by never allowing deletes on invalid Relations.
	 */
	def private isSystemComponentRelationsChange(ReferenceChange change ) {
		if (change.reference != SystemPackage.Literals.SYSTEM_COMPONENT__RELATIONS) {
			return false
		}
		
		if (change.kind == DifferenceKind.DELETE) {
			// invalid references aren't transformed and therefore not transformed to GraphML
			val wasInvalidReference = (change.value as Relation).target == null
			
			return !wasInvalidReference
		} else {
			return true
		}
	}
	
	/**
	 * Check if a Reference change is a user-made change to a relation source.
	 * 
	 * We need to ignore some of the source changes, since there are different configurations which have the same meaning in PREMISE.
	 * 
	 * Specifically, if a relations source is null the source is assumed to be the relations container. This means, if 
	 * the source was change from the container to null or inverse, nothing was actually changed.
	 */
	def private isRelationSourceChange(ReferenceChange change) {
		if (change.reference != ElementPackage.Literals.RELATION__SOURCE) {
			return false
		}
		
		if (change.sourceObj != null && change.targetObj != null) {
			val oldValue = (change.sourceObj as Relation).source
			val oldContainer = change.sourceObj.eContainer as AElement
			val newValue = (change.targetObj as Relation).source
			val newContainer = change.targetObj.eContainer as AElement
			
			// we ignore changes of the source from null to the relations container and reverse, since that both means the same thing
			val ignoredChange = (oldValue == null && newValue == newContainer) || (newValue == null && oldValue == oldContainer)
			
			return !ignoredChange
		}  else {
			return true
		}
	}
	
	def private getSourceObj(Diff diff) {
		diff.match.origin ?: switch(diff.source) {
			case LEFT: diff.match.right
			case RIGHT: diff.match.left
		}
	}
	
	def private getTargetObj(Diff diff) {
		switch(diff.source) {
			case LEFT: diff.match.left
			case RIGHT: diff.match.right
		}
	}
}
