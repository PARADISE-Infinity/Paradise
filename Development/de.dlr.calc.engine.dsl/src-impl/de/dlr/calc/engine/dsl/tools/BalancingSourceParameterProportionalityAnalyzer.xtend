/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.dsl.tools

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.google.inject.Inject
import de.dlr.calc.engine.dsl.BalancingScopeFactory
import de.dlr.calc.engine.dsl.DocumentCreator
import de.dlr.calc.engine.dsl.ResourceProviderMy
import de.dlr.calc.engine.dsl.scope.IBalancingScope
import de.dlr.calc.engine.dsl.xtext.calcDsl.AdditiveExpression
import de.dlr.calc.engine.dsl.xtext.calcDsl.ExponentiationExpression
import de.dlr.calc.engine.dsl.xtext.calcDsl.Expression
import de.dlr.calc.engine.dsl.xtext.calcDsl.Model
import de.dlr.calc.engine.dsl.xtext.calcDsl.MultiplicativeExpression
import de.dlr.calc.engine.dsl.xtext.calcDsl.NumberLiteral
import de.dlr.calc.engine.dsl.xtext.calcDsl.OpAdd
import de.dlr.calc.engine.dsl.xtext.calcDsl.OpMulti
import de.dlr.calc.engine.dsl.xtext.calcDsl.OpUnary
import de.dlr.calc.engine.dsl.xtext.calcDsl.ParameterLiteral
import de.dlr.calc.engine.dsl.xtext.calcDsl.UnaryExpression
import de.dlr.calc.engine.dsl.xtext.ui.internal.CalcDslActivator
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.ParameterProportionality
import de.dlr.premise.system.SystemFactory
import java.util.Map
import java.util.Set
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtext.validation.CheckMode
import org.eclipse.xtext.validation.IResourceValidator

import static extension de.dlr.premise.util.PremiseHelper.eAllContentsIncludingRoot
import de.dlr.premise.system.impl.my.util.BalancingParameterProprotionalityAdapter.IBalancingSourceParameterProportionalityAnalyzer

class BalancingSourceParameterProportionalityAnalyzer implements IBalancingSourceParameterProportionalityAnalyzer {
	val static AParameterDef CONSTANT_TERM = SystemFactory.eINSTANCE.createParameter => [ id = "CONSTANT_TERM" ]
	val public static UNKNOWN_PROPORTIONALITY = createParameterProportionality(null, null, null)
	
	@Data static class Summand {
		val boolean positive
		val Expression expression
		
		def getNegation() {
			return new Summand(!positive, expression)
		}
	}
	
	@Inject val ResourceProviderMy resourceProvider = null
	@Inject val IResourceValidator validator = null
	//@Inject val ISerializer serializer = null
	
	new() {
		val activator = CalcDslActivator::getInstance()
		val injector = activator.getInjector(CalcDslActivator::DE_DLR_CALC_ENGINE_DSL_XTEXT_CALCDSL)
		injector.injectMembers(this)
	}
	
	override getAnalysis(Balancing balancing) {
		val scope = new BalancingScopeFactory().createUsedScope(balancing)
		val document = DocumentCreator.createDocument(scope, balancing)
		val resource = resourceProvider.createResource(document)
		
		val issues = validator.validate(resource, CheckMode.ALL, null)
		if (issues.size != 0) {
			System.err.println('''Calculation omitted: Function '«balancing.function»' for Balancing '«balancing.name»' is not valid!''');
			System.err.println(issues.map[severity.name + ": " + message].join("\r\n"));
			return null
		}
				
		val model = resource.contents.head as Model
		
		val tmpResult = extractTopLevelAdditions(model.body).map[getSummandProportionality(scope)].reduce[p1, p2|p1+p2]
		
		return tmpResult
	}
	
	def dispatch Set<Summand> extractTopLevelAdditions(Expression e) {
		return #{new Summand(true, e)}
	}
	
	def dispatch Set<Summand> extractTopLevelAdditions(AdditiveExpression e) {
		val left = extractTopLevelAdditions(e.left)
		val right = switch(e.operand) {
			case ADD: extractTopLevelAdditions(e.right)
			case SUBSTRACT: extractTopLevelAdditions(e.right).map[negation].toSet
		}
		
		return Sets.union(left, right)
	}
	
	def getSummandProportionality(Summand summand, IBalancingScope scope) {
		val params = summand.expression.eAllContentsIncludingRoot().filter(ParameterLiteral).map[scope.getSourceParameter(value.name)].toSet
		var exprProp = summand.expression.getExpressionProportionality(scope)
		if (!summand.positive) {
			exprProp = exprProp.negation
		}
		
		switch (params.size) {
			case 0: #{CONSTANT_TERM -> exprProp}
			case 1: #{params.head -> exprProp}
			default: Maps.toMap(params, [UNKNOWN_PROPORTIONALITY])
		}		
	}
	
	def dispatch ParameterProportionality getExpressionProportionality(Expression expr, IBalancingScope scope) {
		UNKNOWN_PROPORTIONALITY
	}
	
	def dispatch ParameterProportionality getExpressionProportionality(AdditiveExpression expr, IBalancingScope scope) {
		val left = expr.left.getExpressionProportionality(scope)
		val right = if (expr.operand == OpAdd.SUBSTRACT) expr.right.getExpressionProportionality(scope)?.negation else expr.right.getExpressionProportionality(scope)
		if (left == UNKNOWN_PROPORTIONALITY || right == UNKNOWN_PROPORTIONALITY) {
			return UNKNOWN_PROPORTIONALITY
		}
		
		// both have no parameter
		if (left.p == null && right.p == null) {
			return createParameterProportionality(null, left.value + right.value, 1.0)
		}
		
		return left + right
	}
	
	def dispatch ParameterProportionality getExpressionProportionality(MultiplicativeExpression expr, IBalancingScope scope) {
		val left = expr.left.getExpressionProportionality(scope)
		val right = expr.right.getExpressionProportionality(scope)
		if (left == UNKNOWN_PROPORTIONALITY || right == UNKNOWN_PROPORTIONALITY) {
			return UNKNOWN_PROPORTIONALITY
		}
		
		// both have no parameter
		if (left.p == null && right.p == null) {
			val value = switch(expr.operand) {
				case MULTIPLY: left.value * right.value
				case DIVIDE: left.value / right.value
				case MODULO: left.value % right.value
				default: throw new IllegalArgumentException
			}
			return createParameterProportionality(null, value, 1.0)
		} 
		
		// both have a parameter
		if (left.p != null && right.p != null) {
			// must be the same parameter, also we can't do modulo symbolically
			if (left.p != right.p || expr.operand == OpMulti.MODULO) {
				return UNKNOWN_PROPORTIONALITY
			}
			
			val computedRight = if (expr.operand == OpMulti.DIVIDE) right.reciprocal else right
			
			return createParameterProportionality(left.p, left.a * computedRight.a, left.b + computedRight.b)
		}
		
		// one has a parameter
		try {	
			return createParameterProportionality(left.p, right.value * left.a, left.b)
		} catch (IllegalStateException e) {
			return createParameterProportionality(right.p, left.value * right.a, right.b)
		}
	}
	
	def dispatch ParameterProportionality getExpressionProportionality(ExponentiationExpression expr, IBalancingScope scope) {
		val base = expr.left.getExpressionProportionality(scope)
		val exponent = expr.right.getExpressionProportionality(scope)
		// we dont do e^p (only polynomials, no exponents)
		if (base == UNKNOWN_PROPORTIONALITY || exponent == UNKNOWN_PROPORTIONALITY || exponent.p != null) {
			return UNKNOWN_PROPORTIONALITY
		}
		
		if (base.p != null) {
			createParameterProportionality(base.p, base.a**exponent.value, base.b*exponent.value)
		} else {
			createParameterProportionality(null, base.value**exponent.value, 1.0)
		}
	}
	
	def dispatch ParameterProportionality getExpressionProportionality(ParameterLiteral expr, IBalancingScope scope) {
		return createParameterProportionality(scope.getSourceParameter(expr.value.name), 1.0, 1.0)
	}
	
	def dispatch ParameterProportionality getExpressionProportionality(UnaryExpression expr, IBalancingScope scope) {
		val value = expr.right.getExpressionProportionality(scope)
		
		if (value != null && expr.operand == OpUnary.MINUS) {
			value.negation
		} else {
			value
		}
	}
	
	def dispatch ParameterProportionality getExpressionProportionality(NumberLiteral expr, IBalancingScope scope) {
		return createParameterProportionality(null, expr.value, 1.0)
	}
	
	def operator_plus(ParameterProportionality left, ParameterProportionality other) {
		// different parameters or different exponents
		if (left == UNKNOWN_PROPORTIONALITY || other == UNKNOWN_PROPORTIONALITY || left.p != other.p || Math.abs(left.b - other.b) > 1e-10) {
			return UNKNOWN_PROPORTIONALITY
		}
		return createParameterProportionality(left.p, left.a + other.a, left.b)
	}
	
	def operator_plus(Map<AParameterDef, ParameterProportionality> left, Map<AParameterDef, ParameterProportionality> right) {
		val result = newHashMap
		for (key : Sets.intersection(left.keySet, right.keySet)) {
			result.put(key, left.get(key) + right.get(key))
		}
		result.putAll(left.filter[k,v | !right.containsKey(k)])
		result.putAll(right.filter[k,v | !left.containsKey(k)])
		return result
	}
	
	def static createParameterProportionality(AParameterDef p, Double a, Double b) {
		val prop = SystemFactory.eINSTANCE.createParameterProportionality
		prop.p = p
		prop.a = a
		prop.b = b
		return prop
	}
	
	def getNegation(ParameterProportionality it) {
		return createParameterProportionality(p, -a, b)
	}
	
	def getReciprocal(ParameterProportionality it) {
		return createParameterProportionality(p, 1/a, -b)
	}	
	
	def getValue(ParameterProportionality it) {
		if (p != null) {
			throw new IllegalStateException
		}
		a ** b
	}
}
					