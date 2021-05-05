/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.util.cyclecheck.tarjan;

public class Node implements Comparable<Node> {
	   
	   final int name;
	   boolean visited = false;   // used for Kosaraju's algorithm and Edmonds's algorithm
	   int lowlink = -1;          // used for Tarjan's algorithm
	   int index = -1;            // used for Tarjan's algorithm
	   
	   public Node(final int argName) {
	       name = argName;
	   }
	   
	   public int compareTo(final Node argNode) {
	       return argNode == this ? 0 : -1;
	   }
	   
	   public int getName(){
		   return name;
	   }
	}

