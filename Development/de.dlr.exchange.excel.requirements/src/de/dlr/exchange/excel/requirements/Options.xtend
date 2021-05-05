/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.requirements;

import de.dlr.exchange.base.xtend.StructuredOptions
import java.util.Map

import static de.dlr.exchange.excel.requirements.RequirementsTableGenerator.OPT_TEMPLATE_PATH

public class Options extends StructuredOptions {

	public static String OPT_TECH = "Requirements Exporter"

	public static Map<String, ?> DEFAULT_OPTIONS = #{
		EXPORT -> #{
			OPT_TECH -> #{
				OPT_TEMPLATE_PATH -> ""
			}
		}
	}

	override getDefaultOptions() {
		return DEFAULT_OPTIONS
	}

}
