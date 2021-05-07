/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.calculation.transition;

import org.eclipse.emf.common.util.EList;

public class StochasticCalcThread extends Thread {
	
	// the event probabilities are stored here
	private EList<Double> input;
	// the total number of threads active on this problem
	private int threadCount;
	// this is basically just a thread number/title
	private int threadOffset;
	// the result of all possible branches in this thread will be stored here
	double internalHold = 1;
	
	public StochasticCalcThread (EList<Double> input, int threadCount, int threadOffset) {
		this.input = input;
		this.threadCount = threadCount;
		this.threadOffset = threadOffset;
	}
	
	public void run(){
		// determine how many events are there to permute
		int gateWidth = input.size();
		// set the starting event to the threads number, as each thread will then have a unique starting point
		long currentEvent = threadOffset;
		// displays the number representation of "all events occurred" e.g. 1111..1
		long allEventsTrue = (long)(Math.pow(2, gateWidth));
		while (currentEvent < allEventsTrue) {
			
			char[] chars = fillEventArray(gateWidth, currentEvent);
	        
	        calculateInternalHold(gateWidth, currentEvent, chars);
	        
	        // determine the next event by adding the amount of threads on this problem
	        // the events between currentEvent and currentEvent+thread count are calculated by other threads
	        // as each thread has an initial offset threadOffset
	        currentEvent = currentEvent+threadCount;
	    }
	}
	
	private char[] fillEventArray(int gateWidth, long currentEvent){
		// transform the current event to a string of fixed length
        String bin = Long.toBinaryString(currentEvent);
        // calculate how many leading zeroes there need to be
        int leadingZeroes = gateWidth -bin.length();
        // transform the event string to a char array for convenience
        char[] chars = new char[gateWidth];
        
		for (int cInd = 0; cInd < gateWidth; cInd++) {
        	if (cInd < leadingZeroes) {
        		// add leading zeroes
        		chars[cInd] = '0';
        	}
        	else {
        		// add the string values
        		chars[cInd] = bin.charAt(cInd-leadingZeroes);
        	}
        }
		return chars;
	}

	private void calculateInternalHold(int gateWidth, long currentEvent, char[] chars) {
		// it is ok that the twos complement is used as we are only accepting positive values
		// this statement filters all events with less than half occurred events
		if (Long.bitCount(currentEvent) > ceil(gateWidth/2)) {
			
			// if the branch is a failure branch, store the counter-events probability in internalHold
			// as per law of distribution permitted, one can just multiply with all other probabilities calculated yet
			internalHold = internalHold * (1-calculatePathProb(chars));
		}
	}
	
	// access the result from super instances
	public double getThreadValue() {
		return internalHold;
	}
	
	
	//-------------------------------------
	// Helper Methods
	
	/**
	 * calculate the probability of one path (branch), given an array of events and a one-to-mapped probability array
	 * @param ca, a char array representing a set of events
	 * @return
	 */
	private double calculatePathProb(char[] ca) {
		double onePath = 1;
		for (int bIndex = 0; bIndex < input.size(); bIndex++){
			char b = ca[bIndex];
			if (b == '1') {
				onePath = onePath * input.get(bIndex);
			} else {
				onePath = onePath * not(input.get(bIndex));
			}
		}
		return onePath;
	}
	
	
	/**
	 * Returns the amount of events that can be false for a branch to not be a failure branch
	 * @param d
	 * @return
	 */
	private int ceil(double d){
		double validation = d - (int)d;
		if (validation == 0) {
			return (int)d;
		} else {
			return ((int)d) +1;
		}	
	}
	
	public double not(double input) {		
		return 1-input;
	}
}
