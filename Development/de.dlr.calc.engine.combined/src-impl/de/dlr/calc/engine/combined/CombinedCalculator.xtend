/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Tillman Stehr
*
*/
package de.dlr.calc.engine.combined

import de.dlr.calc.my.CalculatorParameter
import de.dlr.calc.my.ICalculator
import de.dlr.premise.functionpool.AFnDef
import de.dlr.premise.states.data.State
import java.util.List
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.Platform

// TODO cache calculator instances
class CombinedCalculator implements ICalculator<AFnDef> {
	
	static val calculators = {
		Platform.getExtensionRegistry().getConfigurationElementsFor("de.dlr.calc.engine.combined.calculator")
			.map[
				try {
					createExecutableExtension("class")
				} catch (CoreException e) {
					null
				}
			]
			.filter(IFnDefCalculator)
			.toList as List<?> as List<IFnDefCalculator<AFnDef>> 
	}
	
	override calculate(AFnDef fnDef, List<CalculatorParameter> parameters, State state) {
		val calculator = calculators.findFirst[fndDefType.isInstance(fnDef)] 
		if (calculator != null) {
			return calculator.calculate(fnDef, parameters, state)
		}
		throw new IllegalArgumentException('''No calculator present for «fnDef.class.name»''')
	}

}