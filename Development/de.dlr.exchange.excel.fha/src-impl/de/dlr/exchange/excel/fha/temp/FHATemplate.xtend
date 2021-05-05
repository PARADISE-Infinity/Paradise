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

class FHATemplate {
	
	// relative path and template name	
	public static String NAME = "template/fha.xlsx"

	// overview sheet constants
	public static String OVERVIEW_SHEET_NAME = "Overview"	
	public static int OVERVIEW_SHEET_IDX = 0

	// function sheet constants
	public static String FUNCTION_SHEET_NAME = "Functions"
	public static int FUNCTION_SHEET_IDX = 1
	public static int FUNCTION_SHEET_START_IDX = 1
	
	public static int FUN_F_ID_COL_IDX = 0
	public static int FUN_NAME_COL_IDX = 1
	public static int FUN_DESC_COL_IDX = 2
	public static int FUN_UUID_COL_IDX = 3
	public static int FUN_SAFE_COL_IDX = 4
	public static int FUN_MITI_COL_IDX = 5

	public static String FUN_HEAD_F_ID = "F#ID"
	public static String FUN_HEAD_NAME = "Function"
	public static String FUN_HEAD_DESC = "Description"
	public static String FUN_HEAD_UUID = "F-UUID"
	public static String FUN_HEAD_SAFE = "Safety Objective"
	public static String FUN_HEAD_MITI = "Mitigation Objective"

	// hazard sheet constants
	public static String HAZARD_SHEET_NAME = "Hazards"	
	public static int HAZARD_SHEET_IDX = 2;
	public static int HAZARD_SHEET_START_IDX = 1
	
	public static int HAZ_F_ID_COL_IDX = 0
	public static int HAZ_FNAM_COL_IDX = 1
	public static int HAZ_UUID_COL_IDX = 2		
	public static int HAZ_NAME_COL_IDX = 3
	public static int HAZ_DESC_COL_IDX = 4
	public static int HAZ_PHAS_COL_IDX = 5
	public static int HAZ_COND_COL_IDX = 6
	public static int HAZ_EFFE_COL_IDX = 7
	public static int HAZ_CLAS_COL_IDX = 8
	public static int HAZ_PROB_COL_IDX = 9
	public static int HAZ_FAIL_COL_IDX = 10
	public static int HAZ_COMP_COL_IDX = 12
	public static int HAZ_DOCU_COL_IDX = 13

	public static String HAZ_HEAD_F_ID = "F#ID"
	public static String HAZ_HEAD_NAME = "Function"
	public static String HAZ_HEAD_UUID = "Hazard UUID"
	public static String HAZ_HEAD_HAZA = "Hazard"
	public static String HAZ_HEAD_PHAS = "Flight Phase"
	public static String HAZ_HEAD_COND = "Conditions"
	public static String HAZ_HEAD_EFFE = "Effects"
	public static String HAZ_HEAD_CLAS = "Classification"
	public static String HAZ_HEAD_PROB = "Probability"
	public static String HAZ_HEAD_FAIL = "Fail-Safe"
	public static String HAZ_HEAD_SAOB = "Safety Objective"
	public static String HAZ_HEAD_SACO = "Safety Objective Compliance"
	public static String HAZ_HEAD_FUIN = "Further Investigation"

	// mitigation sheet constants
	public static String MITIGATION_SHEET_NAME = "Mitigation"	
	public static int MITIGATION_SHEET_IDX = 3
	
	public static int MIT_F_ID_COL_IDX = 0
	public static int MIT_NAME_COL_IDX = 1
//	public static int MIT_UUID_COL_IDX = 1
	public static int MIT_PHAS_COL_IDX = 2
	public static int MIT_SAOB_COL_IDX = 3
	public static int MIT_MITI_COL_IDX = 4
	public static int MIT_CLAS_COL_IDX = 5
	public static int MIT_PROB_COL_IDX = 6
	public static int MIT_FAIL_COL_IDX = 7
	public static int MIT_MIOB_COL_IDX = 8

	public static String MIT_HEAD_F_ID = "F#ID"
	public static String MIT_HEAD_NAME = "Hazard"
//	public static String MIT_HEAD_UUID = "Hazard UUID"	
	public static String MIT_HEAD_PHAS = "Flight Phase"
	public static String MIT_HEAD_SAOB = "Safety Objective"
	public static String MIT_HEAD_MITI = "Mitigation"
	public static String MIT_HEAD_CLAS = "Classification"
	public static String MIT_HEAD_PROB = "Probability"
	public static String MIT_HEAD_FAIL = "Fail-Safe"
	public static String MIT_HEAD_MIOB = "Mitigation Objective"

	// help sheet constants
	public static String HELP_SHEET_NAME = "Help"	
	public static int HELP_SHEET_IDX = 4	
}
