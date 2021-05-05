/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.validation;

import static org.junit.Assert.assertEquals;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.junit.Before;
import org.junit.Test;

import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.element.Mode;
import de.dlr.premise.element.StateMachine;
import de.dlr.premise.element.Transition;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.util.TestHelper;

/**
 * @author steh_ti
 */
public class TransitionValidationTest {
	private ProjectRepository repository;
	private SystemComponent sc1;
	private SystemComponent sc2;
	private StateMachine sm1;
	private StateMachine sm2;
	private Transition mm;
	private Mode m1;
	private Mode m2;

	private Diagnostician diagnostician;
	private BasicDiagnostic diagnostic;

	@Before
	public void setUp() throws Exception {
	    ValdationTestHelper.registerOCL();

		// create some model classes
		repository = SystemFactory.eINSTANCE.createProjectRepository();
		sc1 = SystemFactory.eINSTANCE.createSystemComponent();
		sc2 = SystemFactory.eINSTANCE.createSystemComponent();
		sm1 = ElementFactory.eINSTANCE.createStateMachine();
		sm2 = ElementFactory.eINSTANCE.createStateMachine();
		m1 = ElementFactory.eINSTANCE.createMode();
		m2 = ElementFactory.eINSTANCE.createMode();
		mm = ElementFactory.eINSTANCE.createTransition();

		// set up modes
		m1.setName("Mode 1");
		m1.setEntryMode(true);
		m2.setName("Mode 2");

		// set up mode mapping
		mm.setSource(m1);
		mm.setTarget(m2);

		// set up system components
		sc1.setName("Component1");
		sc1.getStatemachines().add(sm1);
		sm1.getModes().add(m1);
		sm1.getModes().add(m2);
		sm1.getTransitions().add(mm);
		sc2.setName("Component2");
		sc2.getStatemachines().add(sm2);

		// set up repository:
		// remove adapters, as validation adapter would collide with test
		// validation
		repository.eAdapters().clear();
		// add projects
		repository.getProjects().add(sc1);
		repository.getProjects().add(sc2);
		
		TestHelper.createCommonResourceSet(repository);

		// create Diagnostician
		diagnostician = new Diagnostician();
		diagnostic = diagnostician.createDefaultDiagnostic(repository);
	}

	@Test
	public void testValidTransition() {
		assertEquals(true, diagnostician.validate(repository, diagnostic));
		assertEquals(0, diagnostic.getChildren().size());
	}

	@Test
	public void testTargetModeReachableInvariantInvalidTransition() {
	    sm1.getModes().remove(m2);
	    sm2.getModes().add(m2);

		assertEquals(false, diagnostician.validate(repository, diagnostic));
		assertEquals(3, diagnostic.getChildren().size());
	}

	@Test
	public void testMissingTargetTransition() {
		mm.setTarget(null);

		assertEquals(false, diagnostician.validate(repository, diagnostic));
		assertEquals(2, diagnostic.getChildren().size());
	}

	@Test
	public void testMultipleInvaliditiesTransition() {
	    sm1.getModes().remove(m1);
	    sm2.getModes().add(m1);
		mm.setTarget(null);

		assertEquals(false, diagnostician.validate(repository, diagnostic));
		assertEquals(4, diagnostic.getChildren().size());
	}
}
