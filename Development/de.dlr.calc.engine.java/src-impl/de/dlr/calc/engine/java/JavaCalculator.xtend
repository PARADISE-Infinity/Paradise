/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.calc.engine.java

import de.dlr.calc.engine.combined.IFnDefCalculator
import de.dlr.calc.my.CalculatorParameter
import de.dlr.premise.functionpool.CalcEngineJava
import de.dlr.premise.functionpool.FnDefJava
import de.dlr.premise.states.data.State
import java.util.List

class JavaCalculator implements IFnDefCalculator<FnDefJava> {
	override getFndDefType() {
		return FnDefJava
	}
	
	override calculate(FnDefJava fnDef, List<CalculatorParameter> parameters, State state) {
		if (!(fnDef.calcEngine instanceof CalcEngineJava)) {
			throw new IllegalArgumentException("A java function must be backed by a java calc engine")
		}
		
		val engine = fnDef.calcEngine as CalcEngineJava
		val className = engine.className
		val methodName = fnDef.methodName
		
		val method = className.getMethod(methodName)
		val result = method.invoke(null, parameters) as Double

		return result
	}

	def getMethod(String className, String methodName) {
		val clazz = Class.forName(className)
		if (!clazz.package.name.startsWith("de.dlr.calc.engine.java.implementations")) {
			throw new IllegalArgumentException("Calculator class can't be used")
		}
		
		val method = clazz.getMethod(methodName, #[List])
		val returnType = method.returnType
		if (!double.isAssignableFrom(returnType) && !Double.isAssignableFrom(returnType)) {
			throw new IllegalArgumentException("Java functions must return double")
		}
		return method
	}
}