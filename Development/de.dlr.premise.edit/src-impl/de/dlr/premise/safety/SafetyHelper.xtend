/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.safety

import de.dlr.premise.element.ARepository
import de.dlr.premise.util.PremiseHelper
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.edit.command.ChangeCommand
import de.dlr.premise.element.AElement
import de.dlr.premise.registry.MetaData
import de.dlr.premise.registry.RegistryFactory

class SafetyHelper {
		
	/** Checks if a given premise model already has a safety extension attached to it */
	public static def boolean doesSafetyExtensionExist(EObject element) {
		
		val repo = getRepository(element)
		if (repo != null){	
			if (repo.extensions.exists[ext| ext instanceof SafetyAnalyses]) {
				return true;
			}
		}
		return false
	}
	
	/** Appends and returns a new SafetyRepository 
	 * Note that this function will not execute,
	 * if doesSafetyExtensionExist() returned true
	 * 
	 * @param a premise model element
	 * @return a SafetyRepository and the according change command*/
	public static def CommandEObjectWrapper appendNewSafetyRepository(EObject element) {
		
		if (doesSafetyExtensionExist(element) == false) {
			
			val resSet = element.eResource.resourceSet
			val projectURI = element.eResource.URI
			val Resource resource = resSet.createResource(URI.createURI(projectURI+".safety"))
			val repo = getRepository(element)
			val safetyExt = SafetyFactory.eINSTANCE.createSafetyAnalyses
			
			var cchange = new ChangeCommand(repo) {	
				override doExecute(){				
					resource.contents.add(safetyExt)
				}
			}

			val wrapper = new CommandEObjectWrapper(cchange,safetyExt)
			return wrapper
		}
		return null;
	}
	
	def static getCurrentSafetyRepository(EObject element) {
		
		val repo = getRepository(element)
		if (repo != null){	
			val ext = repo.extensions.findFirst[ext| ext instanceof SafetyAnalyses]
			if ( ext != null) {
				return ext as SafetyAnalyses;
			}
		}
		return null
		
	}

//	/** Calculates MRPN, MSEV, MOCC, MDIS for a given assessment and returns the values in an integer[4] array */
//	// FIXME add DATA class quantitative Assessment
//	static public def calculateMitigationValues(QuantitativeAssessment qa) {
//		
//		var mrpn = qa.rpn
//		var msev = qa.severity
//		var mocc = qa.occurrence
//		var mdis = qa.discovery
//		
//		for (mit : qa.appliedMitigations) {
//			msev = msev - mit.severityInfluence
//			mocc = mocc - mit.occurrenceInfluence
//			mdis = mdis - mit.discoveryInfluence
//		}
//		
//		if (msev < 1) msev = 1
//		if (mocc < 1) mocc = 1
//		if (mdis < 1) mdis = 1
//		
//		mrpn = msev * mocc * mdis	
//		return #[mrpn,msev,mocc,mdis]
//	}

	/**
	 * TODO Move to PremiseHelper !!!
	 */
	private static def getRepository(EObject element) {
		val root = PremiseHelper.getRoot(element)
		if (root instanceof ARepository) {
			return root as ARepository
		} else {
			return null
		}
	}
	
		// TODO shall be moved into FHA implementation
	def static public createFHAMetadata(AElement parent) {
		
		val MetaData mdFHA = RegistryFactory.eINSTANCE.createMetaData
		mdFHA.name = "FHA"
				
		val mdFID = RegistryFactory.eINSTANCE.createMetaData
		mdFID.name = "F#ID"
		mdFID.value = parent.id.substring(0,8)
		mdFHA.metaData.add(mdFID)
		
		val mdSAss = RegistryFactory.eINSTANCE.createMetaData
		mdSAss.name = "Safety Objective"
		mdFHA.metaData.add(mdSAss)
		
		val mdMAss = RegistryFactory.eINSTANCE.createMetaData
		mdMAss.name = "Mitigation Objective"
		mdFHA.metaData.add(mdMAss)

		// add meta data to parent
		parent.metaData.add(mdFHA)
	
		return mdFHA
	}
}