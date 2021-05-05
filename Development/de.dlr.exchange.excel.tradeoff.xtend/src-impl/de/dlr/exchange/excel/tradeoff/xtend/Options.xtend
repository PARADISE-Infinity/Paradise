/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.tradeoff.xtend

import de.dlr.exchange.base.xtend.StructuredOptions
import java.util.Map

import static de.dlr.exchange.excel.tradeoff.xtend.AbsoluteTradeoffGenerator.OPT_REFERENCE_FILE
import static de.dlr.exchange.excel.tradeoff.xtend.RelativeTradeoffGenerator.*

class Options extends StructuredOptions {

	public static Map<String, ?> DEFAULT_OPTIONS = #{
		EXPORT -> #{
			OPT_TECH -> #{
				OPT_TEMPLATE_FILE -> "../_GLOBAL/TradeOffTemplate.xlsx",
				OPT_REFERENCE_FILE -> "Reference.premise"
			}
		}
	}

	override Map<String, ?> getDefaultOptions() {
		return DEFAULT_OPTIONS
	}
}
