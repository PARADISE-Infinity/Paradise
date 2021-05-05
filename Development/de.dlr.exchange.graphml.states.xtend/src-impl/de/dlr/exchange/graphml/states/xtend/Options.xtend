/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.graphml.states.xtend

import de.dlr.exchange.base.xtend.StructuredOptions
import java.util.Map

import static de.dlr.exchange.graphml.states.xtend.Generator.*

class Options extends StructuredOptions {

	public static Map<String, ?> DEFAULT_OPTIONS = #{
		EXPORT -> #{
			OPT_TECH -> #{
				OPT_CONSTRAINTS_NAME -> OPT_CONSTRAINTS_VAL,
				OPT_EDGE_REF_NAME -> OPT_EDGE_REF_VAL,
				OPT_COLOR_GREY_NAME -> OPT_COLOR_GREY_VAL,
				OPT_CALC_STATES_NAME -> OPT_CALC_STATES_VAL,
				OPT_CALC_TREE_NAME -> OPT_CALC_TREE_VAL
			}
		}
	}

	override getDefaultOptions() {
		return DEFAULT_OPTIONS
	}
}
