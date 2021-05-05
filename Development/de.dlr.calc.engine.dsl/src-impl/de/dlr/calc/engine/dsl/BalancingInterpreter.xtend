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

import com.google.inject.Inject
import de.dlr.calc.engine.dsl.scope.IBalancingScope
import de.dlr.calc.engine.dsl.xtext.calcDsl.Model
import de.dlr.calc.engine.dsl.xtext.ui.internal.CalcDslActivator
import de.dlr.premise.states.data.State
import de.dlr.premise.system.Balancing
import org.eclipse.xtext.validation.CheckMode
import org.eclipse.xtext.validation.IResourceValidator

class BalancingInterpreter {
	def static getNewInstance() {
		// This is a facade that hides DI for the XtextDialog from clients
		val activator = CalcDslActivator::getInstance()
		val injector = activator.getInjector(CalcDslActivator::DE_DLR_CALC_ENGINE_DSL_XTEXT_CALCDSL)
		return injector.getInstance(BalancingInterpreter)
	}
		
	@Inject val ResourceProviderMy resourceProvider = null
	@Inject var IResourceValidator validator
	
	def calculate(Balancing balancing, IBalancingScope scope, State state) {
		val document = DocumentCreator.createDocument(scope, balancing)
		val resource = resourceProvider.createResource(document)
		
		val issues = validator.validate(resource, CheckMode.ALL, null)
		if (issues.size != 0) {
			System.err.println('''Calculation omitted: Function '«balancing.function»' for Balancing '«balancing.name»' is not valid!''');
			System.err.println(issues.map[severity.name + ": " + message].join("\r\n"));
			return null
		}
				
		val model = resource.contents.head as Model
		
		val expressionEvaluator = new ExpressionEvaluator(scope, state)
		
		var double result
		var Throwable exception
		try {
			result = expressionEvaluator.evaluate(model.body)
		} catch (Throwable e) {
			exception = e
		}
		
		if (exception != null) {
			System.err.println('''Calculation failed: Function '«balancing.function»' for Balancing '«balancing.name»' returned invalid result!''');
			exception.printStackTrace()
			return null
		}
		
		return result.toString()
	}

}