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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdjacencyList {

   private Map<Node, ArrayList<Edge>> adjacencies = new HashMap<Node, ArrayList<Edge>>();

   public void addEdge(Node source, Node target){
       ArrayList<Edge> list;
       if(!adjacencies.containsKey(source)){
           list = new ArrayList<Edge>();
           adjacencies.put(source, list);
       }else{
           list = adjacencies.get(source);
       }
       list.add(new Edge(source, target));
   }

   public ArrayList<Edge> getAdjacent(Node source){
       return adjacencies.get(source);
   }
}

