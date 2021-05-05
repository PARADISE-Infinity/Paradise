/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.util.cyclecheck;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.dlr.premise.graph.DirectPointer;
import de.dlr.premise.graph.GraphFactory;
import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.system.Balancing;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.ParameterNameMapping;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.util.scope.ScopedEObjectFactory;
import de.dlr.premise.util.cyclecheck.tarjan.Node;

public class BalancingCycleCheckerTest {
	private ProjectRepository repo;
	private SystemComponent sysco;

	private Parameter para1;
	private Parameter para2;
	private Parameter para3;
	private Parameter para_out1;
	private Parameter para_out2;

	private Balancing bal1;
	private Balancing bal2;

	@Before
	public void buildCleanInstance(){
		/* create a ProjectRepository with following structure:
		 * repository
		 * 		sysco
		 * 			para1
		 * 			para2
		 * 			para3
		 * 			para_out1
		 * 			para_out2
		 * 			para_out1 = bal1(para1, para2)
		 * 			para_out2 = bal2(para_ou1, para3)
		 */
		repo = SystemFactory.eINSTANCE.createProjectRepository();
		sysco = SystemFactory.eINSTANCE.createSystemComponent();
		sysco.setName("sysco");
		repo.getProjects().add(sysco);

		para1 = SystemFactory.eINSTANCE.createParameter();
		sysco.getParameters().add(para1);
		para1.setName("para1");

		para2 = SystemFactory.eINSTANCE.createParameter();
		sysco.getParameters().add(para2);
		para2.setName("para2");

		para3 = SystemFactory.eINSTANCE.createParameter();
		sysco.getParameters().add(para3);
		para3.setName("para3");

		para_out1 = SystemFactory.eINSTANCE.createParameter();
		sysco.getParameters().add(para_out1);
		para_out1.setName("para_out1");

		para_out2 = SystemFactory.eINSTANCE.createParameter();
		sysco.getParameters().add(para_out2);
		para_out2.setName("para_out2");

		bal1 = SystemFactory.eINSTANCE.createBalancing();
		sysco.getBalancings().add(bal1);
		bal1.setName("bal1");
        bal1.getActualSources().add(createParameterNameMapping("para1", para1));
        bal1.getActualSources().add(createParameterNameMapping("para2", para2));
		bal1.setTarget(para_out1);

		bal2 = SystemFactory.eINSTANCE.createBalancing();
		sysco.getBalancings().add(bal2);
		bal2.setName("bal2");
        bal2.getActualSources().add(createParameterNameMapping("para_out1", para_out1));
        bal2.getActualSources().add(createParameterNameMapping("para3", para3));
		bal2.setTarget(para_out2);
	}


	@Test
	public void testSystemComponentWithNoCycle() {

		IBalancingCycleChecker balList = createCycleChecker();
		Assert.assertEquals(false, balList.hasCycle());
	}

	@Test
	public void testSystemComponentWithOneCycle() {

        bal1.getActualSources().add(createParameterNameMapping("para_out2", para_out2));

		IBalancingCycleChecker balList = createCycleChecker();
		Assert.assertEquals(true, balList.hasCycle());
	}

	@Test
	public void testSystemComponentWithReflectiveCycle() {

        bal1.getActualSources().add(createParameterNameMapping("target", bal1.getTarget()));

		IBalancingCycleChecker balList = createCycleChecker();
		Assert.assertEquals(true, balList.hasCycle());
	}

	/**
	 * Graphic output of the Tarjan result.
	 */
	public static void display(ArrayList<ArrayList<Node>> nodeList){
		System.out.println("---------------------");
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
	
    private ParameterNameMapping createParameterNameMapping(String name, AParameterDef parameter) {
        DirectPointer<AParameterDef> pointer = GraphFactory.eINSTANCE.createDirectPointer();
        pointer.setTarget(parameter);
        ParameterNameMapping pm = SystemFactory.eINSTANCE.createParameterNameMapping();
        pm.setKey(name);
        pm.setValue(pointer);
        return pm;
    }

    private IBalancingCycleChecker createCycleChecker() {
        return new BalancingCycleChecker(sysco, ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(bal1));
    }

}
