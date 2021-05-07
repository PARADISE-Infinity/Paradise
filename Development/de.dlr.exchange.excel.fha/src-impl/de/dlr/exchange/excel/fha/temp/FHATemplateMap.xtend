/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.excel.fha.temp

import java.util.HashMap

class FHATemplateMap {

	private val map = new HashMap<String, Integer>
	
	// required function keys
	public static String FUN_F_ID = "f_id"
	public static String FUN_NAME = "f_function_name"
	public static String FUN_DESC = "f_description"	
	public static String FUN_UUID = "f_uuid"
	public static String FUN_SAFE = "f_safety_objective"
	public static String FUN_MITI = "f_mitigation_objective"

	// required hazard keys
	public static String HAZ_F_ID = "h_function_id"
	public static String HAZ_NAME = "h_function_name"
	public static String HAZ_UUID = "h_hazrad_uuid"
	public static String HAZ_HAZA = "h_hazard"
	public static String HAZ_PHAS = "h_phase"
	public static String HAZ_COND = "h_conditions"
	public static String HAZ_EFFE = "h_effects"
	public static String HAZ_CLAS = "h_classification"
	public static String HAZ_PROB = "h_probability"
	public static String HAZ_FAIL = "h_failsafe"
	public static String HAZ_SAOB = "h_safety_objective"
	public static String HAZ_SACO = "h_objective_comp"
	public static String HAZ_FUIN = "h_further_investigation"

	// required mitigation keys
	public static String MIT_F_ID = "m_fid"
	public static String MIT_NAME = "m_hazard"
//	public static String MIT_UUID = "m_hazard_uuid"	
	public static String MIT_PHAS = "m_phase"
	public static String MIT_SAOB = "m_classification"
	public static String MIT_MITI = "m_probability"
	public static String MIT_CLAS = "m_lassification"
	public static String MIT_PROB = "m_probability"
	public static String MIT_FAIL = "m_failsafe"
	public static String MIT_MIOB = "m_objective"

	def public int get(String key) {
		return map.get(key)
	}
	
	def public setIdx(String key, Integer value) {
		map.put(key,value)
	}	
}