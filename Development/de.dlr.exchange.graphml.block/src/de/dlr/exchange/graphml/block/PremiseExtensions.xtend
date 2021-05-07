/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.block

import de.dlr.premise.element.Connection
import de.dlr.premise.element.Transition
import de.dlr.premise.graph.IEdge
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.TransitionBalancing
import org.eclipse.emf.ecore.EObject

class PremiseExtensions {	
	def static dispatch getLabel(ANameItem c) {
		c.name
	}
	
	def static dispatch getLabel(Balancing b) {
		b.function
	}
	
	def static dispatch getLabel(TransitionBalancing b) {
		b.description
	}
	
	def static dispatch getLabel(Transition t) {
		t.description
	}
	
	def static dispatch isBidirectional(IEdge<?,?> e) {
		false
	}
	
	def static dispatch isBidirectional(Connection<?> c) {
		c.bidirectional
	}
    
	def static eContentsReferenced(EObject obj) {
		obj.eContents.map[child |
			switch (child) {
				ComponentReference: child.activeImplementation
				default: child
			}
		]
	}
}