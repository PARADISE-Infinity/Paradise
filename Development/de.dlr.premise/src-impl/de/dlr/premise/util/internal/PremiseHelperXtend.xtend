/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.util.internal

import de.dlr.premise.element.GuardCombination
import de.dlr.premise.element.ModeGuard
import java.util.Collection

import static extension com.google.common.collect.Lists.*
import de.dlr.premise.element.AGuardCondition
import de.dlr.premise.registry.Junction

/**
 * Some PremiseHelper methods are way simpler and clearer if implemented in xtend. Put them here and call through from the normal helper.
 */
class PremiseHelperXtend {
	/**
	 * OCL closure operation
	 */
	def static <T> Collection<T> closure(Iterable<? extends T> base, (T) => Iterable<? extends T> fn) {
		val workingSet = base.newArrayList
		
		for (var i = 0; i < workingSet.size; i++) {
			val result = fn.apply(workingSet.get(i))
			if (result != null) {
				workingSet += result.filterNull.filter[!workingSet.contains(it)]
			}
		}
		
		workingSet
	}
	
	/**
	 * OCL closure operation
	 */
	def static <T> Collection<T> closure(T base, (T) => Iterable<? extends T> fn) {
		#[base].closure(fn)
	}
	
	/**
	 * Recursive OCL closure operation with depth parameter
	 */
	def static <T> Collection<T> closure(Iterable<? extends T> base, (T)=>Iterable<? extends T> fn, int depth) {
		val result = newArrayList
		result.addAll(base)
		if (depth != 0) {
			result.addAll(base.map[fn.apply(it)].map[closure(fn, depth - 1)].flatten)
		}
		return result
	}

	/**
	 * Recursive OCL closure operation with depth parameter
	 */
	def static <T> Collection<T> closure(T base, (T)=>Iterable<? extends T> fn, int depth) {
		#[base].closure(fn, depth)
	}
	
	
	static def dispatch String getGuardConditionString(AGuardCondition gc, boolean parenthesized) {
		throw new UnsupportedOperationException
	} 
	
	static def dispatch String getGuardConditionString(Void gc, boolean parenthesized) {
		null
	} 
		
	static def dispatch String getGuardConditionString(ModeGuard mg, boolean parenthesized) {
		mg.mode?.name
	}
	
	static def dispatch String getGuardConditionString(GuardCombination gc, boolean parenthesized) {
		if (gc.children.empty) {
			return null
		}
		
		if (gc.junction == Junction.NOT) {
			if (gc.children.length == 1) {	
				return '''!«gc.children.head.getGuardConditionString(true)»'''
			} else {
				return "<INVALID>"
			}
		}
		
		if (gc.children.length == 1) {
			return gc.children.head.getGuardConditionString(parenthesized)
		}
		
		val junctionString = switch(gc.junction) {
			case AND: "&"
			case OR: "|"
			default: gc.junction.toString
		}
		
		val combinationStr = gc.children
			.map[getGuardConditionString(true)]
			.filterNull
			.join(''' «junctionString» ''')
			
		if (parenthesized) {
			return '''(«combinationStr»)'''
		} 
		
		return combinationStr
	}
}
