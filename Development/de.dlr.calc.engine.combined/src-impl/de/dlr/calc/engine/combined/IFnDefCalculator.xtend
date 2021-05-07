/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Tilman Stehr
*
*/

package de.dlr.calc.engine.combined

import de.dlr.calc.my.ICalculator
import de.dlr.premise.functionpool.AFnDef

interface IFnDefCalculator<T extends AFnDef> extends ICalculator<T> {
	def Class<T> getFndDefType()
}