/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/


package de.dlr.calc.engine.dsl

import de.dlr.calc.engine.combined.CombinedCalculator
import de.dlr.calc.engine.dsl.scope.IBalancingScope
import de.dlr.calc.engine.dsl.xtext.calcDsl.AdditiveExpression
import de.dlr.calc.engine.dsl.xtext.calcDsl.ExponentiationExpression
import de.dlr.calc.engine.dsl.xtext.calcDsl.FunctionCall
import de.dlr.calc.engine.dsl.xtext.calcDsl.MultiplicativeExpression
import de.dlr.calc.engine.dsl.xtext.calcDsl.NumberLiteral
import de.dlr.calc.engine.dsl.xtext.calcDsl.ParameterLiteral
import de.dlr.calc.engine.dsl.xtext.calcDsl.UnaryExpression
import de.dlr.calc.my.CalculatorParameter
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.states.data.State
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static extension com.google.common.primitives.Doubles.*
import static extension de.dlr.premise.states.StateHelper.*

@FinalFieldsConstructor class ExpressionEvaluator {
	val IBalancingScope scope
	val State state
	
	def dispatch double evaluate(AdditiveExpression it) {
		switch (operand) {
			case ADD: left.evaluate + right.evaluate
			case SUBSTRACT: left.evaluate - right.evaluate
			default: throw new IllegalArgumentException
		}
	}
	
	def dispatch double evaluate(MultiplicativeExpression it) {
		switch(operand) {
			case MULTIPLY: left.evaluate * right.evaluate
			case DIVIDE: left.evaluate / right.evaluate
			case MODULO: left.evaluate % right.evaluate
			default: throw new IllegalArgumentException
		}
	}
	
	def dispatch double evaluate(ExponentiationExpression it) {
		return left.evaluate ** right.evaluate
	}
	
	def dispatch double evaluate(UnaryExpression it) {
		switch(operand) {
			case PLUS: right.evaluate
			case MINUS: -right.evaluate
			default: throw new IllegalArgumentException
		}
	}
	
	def dispatch double evaluate(NumberLiteral it) {
		value
	}
	
	def dispatch double evaluate(ParameterLiteral it) {
		it.AParameterDef.getValue(state)?.tryParse() ?: Double.NaN
	}
	
	def dispatch double evaluate(FunctionCall it) {
		val arguments = it.arguments
			.map[
				switch(it) {
					ParameterLiteral: new CalculatorParameter(it.getAParameterDef())
					default: new CalculatorParameter(it.evaluate)
				}
			]
			
		val fn = scope.getFunction(it.function.name)
				
		val calculator = new CombinedCalculator()
		return calculator.calculate(fn, arguments, state)
	}
	
	def private AParameterDef getAParameterDef(ParameterLiteral literal) {
		val resultingParameter = scope.getSourceParameter(literal.value.name)
		if (resultingParameter == null) {
			throw new NullPointerException('''Calculation Parameter «literal.value.name» could not be resolved!''')
		}
		return resultingParameter
	}
}