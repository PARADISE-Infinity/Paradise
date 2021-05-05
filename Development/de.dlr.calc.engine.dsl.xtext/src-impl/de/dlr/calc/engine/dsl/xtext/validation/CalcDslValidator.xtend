/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.dsl.xtext.validation

import de.dlr.calc.engine.dsl.xtext.calcDsl.CalcDslPackage
import de.dlr.calc.engine.dsl.xtext.calcDsl.FunctionCall
import org.eclipse.xtext.validation.Check

/**
 * Custom validation rules. 
 *
 * see http://www.eclipse.org/Xtext/documentation.html#validation
 */
class CalcDslValidator extends AbstractCalcDslValidator {

//  public static val INVALID_NAME = 'invalidName'
//
//	@Check
//	def checkGreetingStartsWithCapital(Greeting greeting) {
//		if (!Character.isUpperCase(greeting.name.charAt(0))) {
//			warning('Name should start with a capital', 
//					MyDslPackage.Literals.GREETING__NAME,
//					INVALID_NAME)
//		}
//	}

	@Check
	def validateArity(FunctionCall it) {
		if (function.lastParameterMultiple) {
			if (arguments.length < function.parameters.length) {
				error('''Function «function.name» must have «function.parameters.length» or more arguments''', CalcDslPackage.Literals.FUNCTION_CALL__ARGUMENTS)
			}
		} else {
			if (arguments.length != function.parameters.length) {
				error('''Function «function.name» must have exactly «function.parameters.length» argument«IF function.parameters.length != 1»s«ENDIF»''', 
					CalcDslPackage.Literals.FUNCTION_CALL__ARGUMENTS
				)
			}
		}
	}
}
