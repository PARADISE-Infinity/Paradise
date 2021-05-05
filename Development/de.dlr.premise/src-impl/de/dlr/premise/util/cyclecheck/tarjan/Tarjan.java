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

import java.util.ArrayList;

/*
 * Tarjan's Algorithm is a graph theory algorithm for finding the strongly 
 * connected components of a graph.
 */
public class Tarjan {

   private int index = 0;
   private ArrayList<Node> stack = new ArrayList<Node>();
   private ArrayList<ArrayList<Node>> SCC = new ArrayList<ArrayList<Node>>();

   public ArrayList<ArrayList<Node>> tarjan(Node v, AdjacencyList list){
       v.index = index;
       v.lowlink = index;
       index++;
       stack.add(0, v);
       if(list.getAdjacent(v)!=null){
	       for(Edge e : list.getAdjacent(v)){
	           Node n = e.to;
	           if(n.index == -1){
	               tarjan(n, list);
	               v.lowlink = Math.min(v.lowlink, n.lowlink);
	           }else if(stack.contains(n)){
	               v.lowlink = Math.min(v.lowlink, n.index);
	           }
	       }
       }
       if(v.lowlink == v.index){
           Node n;
           ArrayList<Node> component = new ArrayList<Node>();
           do{
               n = stack.remove(0);
               component.add(n);
           }while(n != v);
           SCC.add(component);
       }
       return SCC;
   }
}

