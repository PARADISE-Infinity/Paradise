/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.excel.tradeoff.xtend

import de.dlr.exchange.base.xtend.ICharsetProvidingFileSystemAccess
import de.dlr.exchange.base.xtend.IGeneratorMy
import de.dlr.premise.element.AElement
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet

/**
 * @author enge_do
 */
class AbsoluteTradeoffGenerator implements IGeneratorMy {

	public static String OPT_TECH = "TradeOff"
	public static String OPT_TEMPLATE_FILE = "Template"
	public static String OPT_REFERENCE_FILE = "Reference"

	public static String NAME_OUTPUT_FILE = "TradeOffStudyAbsolute.xlsx"
	
	override doGenerateFromAElements(ResourceSet resSet, List<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa) {
		throw new UnsupportedOperationException()
	}
	
	override doGenerateFromResources(ResourceSet resSet, List<Resource> selectedFiles, ICharsetProvidingFileSystemAccess fsa) {
		var generator = new BaseTradeoffGenerator
		generator.doGenerate(resSet, fsa, new Options, false)
	}
	
}
