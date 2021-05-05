/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Tilman Stehr
*
*/
package de.dlr.calc.my;

import java.util.List;

import de.dlr.premise.functionpool.AFnDef;
import de.dlr.premise.states.data.State;


public interface ICalculator<T extends AFnDef> {
	double calculate(T fnDef, List<CalculatorParameter> parameters, State state);

} 
