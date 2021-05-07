/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.calculation.valuechangedcontentadapter

import de.dlr.premise.calculation.IBalancingCalculator
import de.dlr.premise.calculation.transition.ITransitionBalancingCalculator
import de.dlr.premise.states.data.State
import de.dlr.premise.system.ABalancing
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.TransitionBalancing
import java.util.List
import java.util.function.Predicate
import org.eclipse.xtend.lib.annotations.Data

class BalancingCalculatorMock implements IBalancingCalculator, ITransitionBalancingCalculator {
	val List<CalculateArguments> calculateCalls = newArrayList()

	override calculate(Balancing balancing, Predicate<State> targetStatePredicate, boolean sync) {
		recordCall(balancing, targetStatePredicate) 
	}
	
	override calculate(TransitionBalancing balancing) {
		recordCall(balancing, null) 
		return true 
	}
	
	def private void recordCall(ABalancing<?> balancing, Predicate<State> targetStatePredicate) {
		calculateCalls.add(new CalculateArguments(balancing, targetStatePredicate)) 
	}
	
	def List<CalculateArguments> getCalculateCalls() {
		return calculateCalls 
	}
	
	def void reset() {
		calculateCalls.clear() 
	}
	
	@Data static class CalculateArguments {
		val ABalancing<?> balancing
		val Predicate<State> targetStatePredicate
	}
}