/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.util

import de.dlr.premise.registry.IMetaTypable
import de.dlr.premise.registry.MetaTypeDef
import de.dlr.premise.registry.Registry
import de.dlr.premise.system.ProjectRepository
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet

import static extension de.dlr.premise.util.urihandlers.PremiseLibraryURIHandler.PREMISE_GLOBAL_REGISTRY_URI

class BaseRegistryHelper {
	
	public static final String METATYPE_Container_ID  = "id_Container"	
	public static final String METATYPE_PHASES_ID     = "4b21a2c9-2c1b-4c29-8fa0-488246036795"
	public static final String METATYPE_FAILURE_ID    = "67943974-27b7-4e15-85cd-59ee99afe08c"
	public static final String METATYPE_HAZARD_ID     = "8021351c-a503-4afc-8d65-768d9ec44f62"
	public static final String METATYPE_HARMFUL_ID    = "31c93553-0890-486f-9668-d17e8f67e814"
	public static final String METATYPE_SSF_ID        = "6522e246-f8f7-41ec-8f9b-70bf51484eb6"
	public static final String METATYPE_CAL_OUTPUT_ID = "627ec0e4-a626-485e-b62d-92f4ad9285be"
	public static final String METATYPE_CAL_INPUT_ID  = "c11a5a68-7574-4dd7-bc2f-d5946a6a0dfa"
	
	def static getFailureMetaType(ResourceSet resSet) {
        return resSet.getMetaType(METATYPE_FAILURE_ID)
	}
	
	def static getHazardMetaType(ResourceSet resSet) {
        return resSet.getMetaType(METATYPE_HAZARD_ID)
	}

	def static getHarmfulHazardMetaType(ResourceSet resSet) {
        return resSet.getMetaType(METATYPE_HARMFUL_ID)
	}
	
	def static getSSFMetaType(ResourceSet resSet) {
        return resSet.getMetaType(METATYPE_SSF_ID)
	}
	
	def static getCalcOutputMetatype(ResourceSet resSet) {
        return resSet.getMetaType(METATYPE_CAL_OUTPUT_ID)
	}
	
	def static getCalcInputMetatype(ResourceSet resSet) {
        return resSet.getMetaType(METATYPE_CAL_INPUT_ID)
	}

	/**
	 * 
	 */
	public def static List<EObject> getElementsByMetaTypes(String metaType, ProjectRepository repository) {
		return repository.eAllContents.filter(IMetaTypable).filter[e | e.metaTypes.exists[m | m.id == metaType]].toList
	}

	/** 
	 * Returns the base registry by using the specified EObject 
	 */
	protected def static Registry getBaseRegistry(ResourceSet resSet) {
		val baseRegistryResource = resSet.getResource(PREMISE_GLOBAL_REGISTRY_URI, true)
		val baseRegistry = baseRegistryResource.contents.get(0) as Registry
		return baseRegistry
	}

	/**
	 * Returns meta type for the given meta type id 
	 */
	private def static getMetaType(ResourceSet resSet, String id) {
		val registry = resSet.baseRegistry
        return registry?.eAllContents.filter(MetaTypeDef).findFirst[mt| mt.id == id]
	}	
}