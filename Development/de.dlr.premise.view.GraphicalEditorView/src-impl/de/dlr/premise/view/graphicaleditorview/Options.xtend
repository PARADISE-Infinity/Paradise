/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview

import de.dlr.exchange.base.xtend.StructuredOptions
import java.util.Map

import static de.dlr.premise.view.graphicaleditorview.GraphicalEditorView.*

class Options extends StructuredOptions {

	public static Map<String, ?> DEFAULT_OPTIONS = #{
		VIEW -> #{
			OPT_TECH -> #{
				OPT_CONTEXT_HIERARCHY -> #{
					OPT_GROUP_LAYOUT -> "true",
					OPT_GROUP_EDGES -> "false"
				},
				OPT_CONTEXT_RELATION -> #{
					OPT_GROUP_LAYOUT -> "true",
					OPT_GROUP_EDGES -> "false"
				},
				OPT_CONTEXT_CONNECTION -> #{
					OPT_GROUP_LAYOUT -> "false",
					OPT_GROUP_EDGES -> "false"
				},
				OPT_CONTEXT_SATISFIES -> #{
					OPT_GROUP_LAYOUT -> "true",
					OPT_GROUP_EDGES -> "false"
				},
				OPT_CONTEXT_STATES -> #{
					OPT_GROUP_LAYOUT -> "true",
					OPT_GROUP_EDGES -> "false"
				}
			}
		}
	}

	override getDefaultOptions() {
		return DEFAULT_OPTIONS
	}
}
