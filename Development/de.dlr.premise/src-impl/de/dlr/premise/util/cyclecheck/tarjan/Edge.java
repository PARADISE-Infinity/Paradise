/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.util.cyclecheck.tarjan;

public class Edge {
	   
	   final Node from, to;
	   
	   public Edge(final Node argFrom, final Node argTo){
	       from = argFrom;
	       to = argTo;
	   }
	  
	}

