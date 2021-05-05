/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.calculation.transition;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

public class StochasticCalcEngine {
	

	
	
	/**
	 * returns the probability that all events occur
	 * 
	 * @param input, all values to multiply
	 * @return
	 */
	public double and(double[] input) {
		// initialize to 1 as we are about to solely multiply
		double result = 1;
		for (int i = 0; i < input.length; i++) {
			result = result * input[i];
		}
		return result;
	}
	
	
	/**
	 * returns the probability that at least one event occurs
	 * 
	 */
	public double or(EList<Double> input) {
		
		double result = 0;
		double hold = 1;
		
		// else proceed with accurate calculation
		// at least one is true if Not(everything else untrue)
		// 1-((Product)neg(d))
		
		for (double d : input){
			hold = hold * not(d);
		}
		result = 1-hold;
		
		return result;
	}
	
	/**
	 * returns the probability that exactly one event occurs
	 * @param input
	 * @return
	 */
	public double xor(EList<Double> input) {
		EList<Double> hold = new BasicEList<Double>();
		
		// iterate over all tree paths and add them up
		for (int count = 0; count < input.size(); count++) {
			double interRes = 1;
			
			//iterate over one path, negating all but the current item
			for (int i=0; i < input.size(); i++) {
				// if we are at the point, where no negation is needed
				if (i == count) {
					interRes = interRes * input.get(i);
				} else {
					interRes = interRes * not(input.get(i));
				}
			}
			
			hold.add(interRes);
		}
		
		
		
		
		return or(hold);
	}
	
	/**
	 * 1-input
	 * 
	 * @param input, the one argument to negate
	 * @return
	 */
	public double not(EList<Double> input) {		
		if (input.size() != 1) {
			return 0;
		} else {
			return 1-input.get(0);
		}
	}
	
	/**
	 * 1-input
	 * 
	 * @param input, the one argument to negate
	 * @return
	 */
	public double not(double input) {		
			return 1-input;
	}
	
	/**
	 *  returns the probability, that at least half of the events occur
	 *  
	 * @param input
	 * @return
	 */
	public double vote(EList<Double> input) {
		// a list that saves all events, that may occur
		int n = input.size();
		
		// warn the user if gate input is larger than 25 events
		if (n > 25) 
			System.err.println("Warning: VOTE-GATES with more than 25 inputs require significant computation time.");	
		
		
		// get System maximum Thread Count
		final int SYSTEM_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
		
		// create maximum amount of threads
		StochasticCalcThread[] threadArray = new StochasticCalcThread[SYSTEM_THREAD_COUNT];
		for (int i = 0; i < SYSTEM_THREAD_COUNT; i++) {
			// i is later used as the starting event / threadOffset
			threadArray[i] = new StochasticCalcThread(input, SYSTEM_THREAD_COUNT, i);
		}
		
		// start them all
		for (int i = 0; i < SYSTEM_THREAD_COUNT; i++) {
			threadArray[i].start();
		}
	    
	    // join them all
	    try {
	    	for (int i = 0; i < SYSTEM_THREAD_COUNT; i++) {
				threadArray[i].join();
			}
		} catch (InterruptedException e) {
			System.err.println("An error occured while waiting for the threads to finish");
			e.printStackTrace();
		}
	    
	    // finally add up the individual thread results
	    double result = 1;
	    for (int i = 0; i < SYSTEM_THREAD_COUNT; i++) {
	    	// these are counter-event-probabilities so they can be multiplied
			result = result * threadArray[i].getThreadValue();
		}
	    
	    // 1-NOT(AND_of_all_counter_events) = OR
	    return 1-result;  
	}
	
	
	

	
	
		
}
