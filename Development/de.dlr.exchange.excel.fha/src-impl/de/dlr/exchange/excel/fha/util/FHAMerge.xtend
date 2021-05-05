/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.excel.fha.util

import FHA.Analysis
import org.eclipse.emf.common.util.BasicEList
import FHA.Hazard
import de.dlr.premise.registry.IMetaTypable
import com.smartxls.WorkBook
import java.util.HashMap
import de.dlr.exchange.excel.fha.temp.FHATemplate

/**
 * This class is used to merge two FHA Model instances (Premise, Excel) and also serves as helper class for the plugin
 */
class FHAMerge {
	
	// TODO should be taken from the Base.registry
//	public static final String METATYPE_PHASE_ID = "4b21a2c9-2c1b-4c29-8fa0-488246036795"
//	public static final String METATYPE_FAILURE_ID = "67943974-27b7-4e15-85cd-59ee99afe08c"
//	public static final String METATYPE_HAZARD_ID = "8021351c-a503-4afc-8d65-768d9ec44f62"
//	public static final String METATYPE_SSF_ID = "6522e246-f8f7-41ec-8f9b-70bf51484eb6"
	
	/** This function merges a premise with an excel based model of an FHA 
	 * assuming the premise model contains the latest information*/
	public def mergeP2E(Analysis premise, Analysis excel){
		
		

		
		// identify functions as there may be changes
		for (pFunction : premise.functions) {
			
			// check if a function is already existing
			if (!excel.functions.filter[eF| eF.fid == pFunction.fid].nullOrEmpty){
				
				// if it does exist, overwrite it
				var eFunc = excel.functions.findFirst[eF|eF.fid == pFunction.fid]
				eFunc.name = pFunction.name
				eFunc.mitigationobjective = pFunction.mitigationobjective
				eFunc.safetyobjective = pFunction.safetyobjective
				eFunc.uuid = pFunction.uuid
			} else {
				
				// just add it
				excel.functions.add(pFunction)
			}
		}


		// identify hazards and compare them to each other
		for (pHaz : premise.hazards){
			
			// check if a hazard exists in excel
			var eHazs = excel.hazards.filter[
				eH|eH.uuid == pHaz.uuid
				&& eH.phases.phase == pHaz.phases.phase
			]
			
			if (eHazs.nullOrEmpty){
				
				// if there is no hazard for a premise hazard, create one
				excel.hazards.add(pHaz)
				
			} else {
				
				// if there is one, overwrite its name
				eHazs.findFirst[true].name = pHaz.name
			}
		}
		
		// check for deleted functions
		val deletedFIDs = new BasicEList<String>()
		for (eFunc : excel.functions){
			
			// delete every function, that has no correspondence in premise anymore
			if (premise.functions.filter[f|f.fid == eFunc.fid].nullOrEmpty){
				deletedFIDs.add(eFunc.fid)
			}
		}
		
		// need to go over FIDs to prevent concurrent modification exception
		for (fid : deletedFIDs){
			
			// TODO: optimize by deleting a collection
			// remove every hazard associated with this FID
			while (!excel.hazards.filter[h|h.function.fid == fid].nullOrEmpty){
				excel.hazards.remove(excel.hazards.findFirst[h|h.function.fid == fid])
			}
			
			// remove the function itself
			excel.functions.remove(excel.functions.findFirst[f|f.fid == fid])
		}
		
		// check for deleted hazards
		val deletedHazards = new BasicEList<Hazard>()
		for (hazard : excel.hazards){
			if (premise.hazards.findFirst[
				h|h.uuid == hazard.uuid
				&& h.phases.phase == hazard.phases.phase] == null){
					// if there is a hazard that does not exist in premise, note it for later deletion (CME)
					deletedHazards.add(hazard)
			}
		}
		
		// remove all hazards without correspondence
		for (hazard : deletedHazards){
			excel.hazards.remove(hazard)
		}
				
		return excel	
	}
	
	
	/** This function merges a premise with an excel based model of an FHA 
	 * assuming the excel model contains the latest information*/
	public def mergeE2P(Analysis excel, Analysis premise){
		
		// identify changes to a function
		for (eFunc : excel.functions){
			if (!premise.functions.filter[pF|pF.fid == eFunc.fid].nullOrEmpty){
				
				// there is already a corresponding function
				var pFunc = premise.functions.findFirst[pF|pF.fid == eFunc.fid]
				// pFunc.name = eFunc.name
				pFunc.mitigationobjective = eFunc.mitigationobjective
				pFunc.safetyobjective = eFunc.safetyobjective
				if (eFunc.uuid != null && eFunc.uuid != ""){
					pFunc.uuid = eFunc.uuid
				}
				
			} else {
				
				// just add the function
				premise.functions.add(eFunc)
			}
		}
		
		// identify new hazards
		for (eHaz : excel.hazards){
			var pHazs = premise.hazards.filter[
				pH|pH.uuid == eHaz.uuid
				&& pH.phases.phase == eHaz.phases.phase
			]
			if (pHazs.nullOrEmpty){
				// add the new hazard
				premise.hazards.add(eHaz)
			} else {
				// change the name
				pHazs.findFirst[true].name = eHaz.name
			}
		}
		
		return premise
	}
	
	/** This function checks if a MetaTypeable object has a MetaT */
	static public def hasObjectMetaType(IMetaTypable obj, String id){
		if (obj instanceof IMetaTypable){
			for (mt : obj.metaTypes){
				if (mt.id == id){
					return true
				}
			}
		}
		return false
	}
	
	/** sets up a HashMap for storing column values bound to a String representing the headline of a column */
	static public def setupMap(WorkBook wb){
		val map = new HashMap<String, Integer>
		
		// setup function page
		var sheet = FHATemplate.FUNCTION_SHEET_IDX
		map.put("fid",findHeader(wb,sheet,FHATemplate.FUN_HEAD_F_ID))
		map.put("function",findHeader(wb,sheet,FHATemplate.FUN_HEAD_NAME))
		map.put("fdesc",findHeader(wb,sheet,FHATemplate.FUN_HEAD_DESC))
		map.put("fuuid",findHeader(wb,sheet,FHATemplate.FUN_HEAD_UUID))
		map.put("fsobj",findHeader(wb,sheet,FHATemplate.FUN_HEAD_SAFE))
		map.put("fmobj",findHeader(wb,sheet,FHATemplate.FUN_HEAD_MITI))
		
		// setup hazard page
		sheet = FHATemplate.HAZARD_SHEET_IDX
		map.put("hfid",findHeader(wb,sheet,FHATemplate.HAZ_HEAD_F_ID))
		map.put("hfunction",findHeader(wb,sheet,FHATemplate.HAZ_HEAD_NAME))
		map.put("huuid",findHeader(wb,sheet,FHATemplate.HAZ_HEAD_UUID))
		map.put("hazard",findHeader(wb,sheet,FHATemplate.HAZ_HEAD_HAZA))
		map.put("hphase",findHeader(wb,sheet,FHATemplate.HAZ_HEAD_PHAS))
		map.put("hconditions",findHeader(wb,sheet,FHATemplate.HAZ_HEAD_COND))
		map.put("heffects",findHeader(wb,sheet,FHATemplate.HAZ_HEAD_EFFE))
		map.put("hclassification",findHeader(wb,sheet,FHATemplate.HAZ_HEAD_CLAS))
		map.put("hprobability",findHeader(wb,sheet,FHATemplate.HAZ_HEAD_PROB))
		map.put("hfailsafe",findHeader(wb,sheet,FHATemplate.HAZ_HEAD_FAIL))
		map.put("hsobj",findHeader(wb,sheet,FHATemplate.HAZ_HEAD_SAOB))
		map.put("hobjcomp",findHeader(wb,sheet,FHATemplate.HAZ_HEAD_SACO))
		map.put("hdoc",findHeader(wb,sheet,FHATemplate.HAZ_HEAD_FUIN))
		
		// setup mitigation page
		sheet = FHATemplate.MITIGATION_SHEET_IDX
		map.put("mfid",findHeader(wb,sheet,"F#ID"))
		map.put("mhaz",findHeader(wb,sheet,"Hazard"))
		map.put("mitigation",findHeader(wb,sheet,"Mitigation"))
		map.put("mclassification",findHeader(wb,sheet,"Classification"))
		map.put("mprobability",findHeader(wb,sheet,"Probability"))
		map.put("mfailsafe",findHeader(wb,sheet,"Fail-Safe"))
		map.put("msobj",findHeader(wb,sheet,"Safety Objective"))
		
		for (key : map.keySet) {
			if (map.get(key) == null){
				System.err.println("Could not find a Header Cell. Check your XLS File\nMissing Key identifier:" + key)
				return null
			}
		}
		
		return map;
	}
	
	/** tries to find a cell in the first row, with contents matching the value */
	static private def findHeader(WorkBook wb, int sheetNo, String value){

		var col = 0;
		// pretty unlikely that one has more than 50 headlines but whatever...
		while (col < 500) {
			val cellContent = wb.getText(sheetNo, 0, col)
			if (cellContent != null) {
				var cleanContent = cellContent.replaceAll("\\p{C}", " ")
				if (cleanContent == value) {
				return col;
				}
			}
			col++;	
		}
		
		return null
	}
}
