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

import org.junit.Assert;
import org.junit.Test;

import de.dlr.premise.util.cyclecheck.tarjan.AdjacencyList;
import de.dlr.premise.util.cyclecheck.tarjan.Node;
import de.dlr.premise.util.cyclecheck.tarjan.Tarjan;

public class TarjanTest {
	
	Node zero = new Node(0);
	Node one = new Node(1);
	Node two = new Node(2);
	Node three = new Node(3);
	Node four = new Node(4);
	Node five = new Node(5);
	Node six =  new Node(6);
	
	Tarjan algo = new Tarjan();
	
	@Test
	public void testThreeNodesChainNoCircle(){
		
		AdjacencyList list = new AdjacencyList();
		
		list.addEdge(zero, one);
		list.addEdge(one, two);
		
		ArrayList<ArrayList<Node>> nodeListList = new ArrayList<ArrayList<Node>>();
		ArrayList<Node> nodeList1 = new ArrayList<Node>();
		ArrayList<Node> nodeList2 = new ArrayList<Node>();
		ArrayList<Node> nodeList3 = new ArrayList<Node>();
		
		nodeList1.add(two);
		nodeList2.add(one);
		nodeList3.add(zero);
		
		nodeListList.add(nodeList1);
		nodeListList.add(nodeList2);
		nodeListList.add(nodeList3);
		
		Assert.assertEquals(algo.tarjan(zero, list), nodeListList);
		
	}
	
	@Test
	public void testOnlyOneElement(){
		
		AdjacencyList list = new AdjacencyList();
			
		ArrayList<ArrayList<Node>> nodeListList = new ArrayList<ArrayList<Node>>();
		ArrayList<Node> nodeList = new ArrayList<Node>();
		
		nodeList.add(zero);
		nodeListList.add(nodeList);
		
		Assert.assertEquals(algo.tarjan(zero, list), nodeListList);
		
	}
	
	@Test
	public void testFiveElementsNoCircle(){
		
		AdjacencyList list = new AdjacencyList();
		
		list.addEdge(zero, one);
		list.addEdge(zero, two);
		list.addEdge(four, one);
		list.addEdge(three, two);
		
		ArrayList<ArrayList<Node>> nodeListList = new ArrayList<ArrayList<Node>>();
		ArrayList<Node> nodeList1 = new ArrayList<Node>();
		ArrayList<Node> nodeList2 = new ArrayList<Node>();
		ArrayList<Node> nodeList3 = new ArrayList<Node>();
		
		nodeList1.add(one);
		nodeList2.add(two);
		nodeList3.add(zero);
		
		nodeListList.add(nodeList1);
		nodeListList.add(nodeList2);
		nodeListList.add(nodeList3);
		
		Assert.assertEquals(algo.tarjan(zero, list), nodeListList);
		
	}
	
	@Test
	public void testTwoElementsWithOneReflective(){
		AdjacencyList list = new AdjacencyList();
		
		list.addEdge(zero, one);
		list.addEdge(one, one);
		
		ArrayList<ArrayList<Node>> nodeListList = new ArrayList<ArrayList<Node>>();
		ArrayList<Node> nodeList1 = new ArrayList<Node>();
		ArrayList<Node> nodeList2 = new ArrayList<Node>();
		
		nodeList1.add(one);
		nodeList2.add(zero);
		
		nodeListList.add(nodeList1);
		nodeListList.add(nodeList2);
		
		Assert.assertEquals(algo.tarjan(zero, list), nodeListList);
	}
	
	/*
	 * Circle: 5-3-1
	 */
	@Test
	public void testFiveElementsWithCircle(){

		AdjacencyList list = new AdjacencyList();
		
		list.addEdge(zero, one);
		list.addEdge(one, three);
		list.addEdge(four, three);
		list.addEdge(three, five);
		list.addEdge(six, five);
		list.addEdge(five, one);
		
		
		ArrayList<ArrayList<Node>> nodeListList = new ArrayList<ArrayList<Node>>();
		ArrayList<Node> nodeList1 = new ArrayList<Node>();
		ArrayList<Node> nodeList2 = new ArrayList<Node>();
		
		nodeList1.add(five);
		nodeList1.add(three);
		nodeList1.add(one);
		
		nodeList2.add(zero);
		
		nodeListList.add(nodeList1);
		nodeListList.add(nodeList2);

		Assert.assertEquals(algo.tarjan(zero, list), nodeListList);
		
	}
	
	@Test
	public void testSixElementsNoCircle(){
		AdjacencyList list = new AdjacencyList();
		
		list.addEdge(zero, one);
		list.addEdge(zero, two);
		list.addEdge(five, one);
		list.addEdge(four, two);
		list.addEdge(one, three);
		list.addEdge(two, three);
		
		ArrayList<ArrayList<Node>> nodeListList = new ArrayList<ArrayList<Node>>();
		ArrayList<Node> nodeList1 = new ArrayList<Node>();
		ArrayList<Node> nodeList2 = new ArrayList<Node>();
		ArrayList<Node> nodeList3 = new ArrayList<Node>();
		ArrayList<Node> nodeList4 = new ArrayList<Node>();

		nodeList1.add(three);
		nodeList2.add(one);
		nodeList3.add(two);
		nodeList4.add(zero);
		
		nodeListList.add(nodeList1);
		nodeListList.add(nodeList2);
		nodeListList.add(nodeList3);
		nodeListList.add(nodeList4);
		
		Assert.assertEquals(algo.tarjan(zero, list), nodeListList);
		
	}
	
	/*
	 * Graphic output of the Tarjan result.
	 */
	public static void display(ArrayList<ArrayList<Node>> nodeList){
		System.out.println("----------------------");
		for(ArrayList<Node> nodeListList : nodeList){
			System.out.print("(");
			for(Node node : nodeListList){
				System.out.print(node.getName());
			}
			System.out.print(")");
		}
		System.out.println();
		System.out.println("----------------------");
	}
}
