/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.trace.xtend;

import de.dlr.exchange.base.xtend.StructuredOptions
import java.util.Map

import static de.dlr.exchange.excel.trace.xtend.AbstractGenerator.*

public class DSMOptions extends StructuredOptions {

	public static String OPT_TECH = "Design Structure Matrix (DSM)"

	public static Map<String, ?> DEFAULT_OPTIONS = #{
		EXPORT -> #{
			OPT_TECH -> #{
				OPT_DEPENDENCY_TYPES -> #{
					OPT_DEPENDENCY_TYPES_RELATIONS -> "on",
					OPT_DEPENDENCY_TYPES_CONNECTIONS -> "on",
					OPT_DEPENDENCY_TYPES_BALANCINGS -> "on",
					OPT_DEPENDENCY_TYPES_SATISFIES -> "on"
				},
				OPT_DEPENDENCY_COUNT -> "off",
				OPT_PARENT_NAMES -> "one",
				OPT_INCLUDE_PARENTS -> "off"
			}
		}
	}

	override getDefaultOptions() {
		return DEFAULT_OPTIONS
	}

}
