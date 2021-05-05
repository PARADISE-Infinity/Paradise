/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.aspect.graphml.ui

import de.dlr.aspect.graphml.transform.p2g.PremiseToGraphMLTransform
import de.dlr.exchange.base.xtend.StructuredOptions
import de.dlr.premise.registry.IPremiseObject
import de.dlr.premise.util.PremiseHelper
import java.util.Map
import org.eclipse.emf.ecore.EObject

import static extension de.dlr.premise.util.PremiseHelper.getMetaData
import static extension de.dlr.premise.util.PremiseHelper.isOn

class CreateGraphMLOptions extends StructuredOptions {
	public static val OPT_TECH = "GraphML/Edit"
	public static val OPT_GROUP = "Create Groups"

	public static Map<String, ?> DEFAULT_OPTIONS = #{
		EXPORT -> #{
			OPT_TECH -> #{
				OPT_GROUP -> "on"
			}
		}
	}

	override getDefaultOptions() {
		return DEFAULT_OPTIONS
	}

    def getContainment(EObject root) {
		val group = root.getMetaData(PremiseHelper.NAME_META_SECTION_EXPORT)?.getMetaData(OPT_TECH)?.getMetaData(OPT_GROUP)
		
		 if (group.on) {
			PremiseToGraphMLTransform.Containment.GROUPS
		} else {
			PremiseToGraphMLTransform.Containment.EDGES
		} 
    }
    
    def dispatch getMetaData(IPremiseObject obj) {
    	obj.metaData
    }
    
    def dispatch getMetaData(EObject eObject) {
    	// resolve dispatch to correct super-type
    }
}