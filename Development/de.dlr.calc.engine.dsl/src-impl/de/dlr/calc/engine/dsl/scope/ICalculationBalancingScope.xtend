/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.calc.engine.dsl.scope

interface ICalculationBalancingScope extends IUsedBalancingScope {
	/**
	 * May not mutate the model!
	 */
	def boolean needsPrepare()
	
	/**
	 * Will mutate the model, only call in main thread!
	 */
	def void prepareModel()
	
	/**
	 * Allow a scope to opt out of its balancing being calculated
	 */
	def boolean needsCalculation()
	
}