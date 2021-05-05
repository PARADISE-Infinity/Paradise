/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.calculation.legacy;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.common.command.AssociatedCommandsCommandStack;
import de.dlr.premise.element.Mode;
import de.dlr.premise.element.ModeGuard;
import de.dlr.premise.element.Transition;
import de.dlr.premise.element.impl.ModeGuardImpl;
import de.dlr.premise.element.impl.ModeImpl;
import de.dlr.premise.element.impl.StateMachineImpl;
import de.dlr.premise.element.impl.TransitionImpl;
import de.dlr.premise.provider.util.PremiseAdapterFactoryHelper;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.TransitionBalancing;
import de.dlr.premise.system.TransitionParameter;
import de.dlr.premise.system.impl.TransitionBalancingImpl;


/**
 * @author hschum
 *
 */
public class ABalancingNotificationTest {

	private static String PATH_TESTDATA = "test/data/ABalancingNotificationTest/";
    private static String PATH_INPUT_FILE = de.dlr.premise.util.TestHelper.locateFile("de.dlr.premise.calc", PATH_TESTDATA + "ABalancingNotificationTest.premise").getPath();
  
	private static ProjectRepository rep = null;

	// use a shorter name for class ValueChangedContentAdapterTestHelper:
	private static final class NotifTstHlp extends ValueChangedContentAdapterTestHelper {};
    // list of notification log messages, see for details of msg format: ValueChangedContentAdapter#notifyChanged()
	private static EList<String> notifications = NotifTstHlp.getNotifications();

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
        ValueChangedContentAdapterTestHelper valueChangedContentAdapter = new ValueChangedContentAdapterTestHelper();

        // load resources
        ComposedAdapterFactory adapterFactory = PremiseAdapterFactoryHelper.createComposedAdapterFactory(); 
        BasicCommandStack commandStack = new AssociatedCommandsCommandStack();
        EditingDomain editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<Resource, Boolean>());
		Resource resource = editingDomain.getResourceSet().getResource(URI.createFileURI(PATH_INPUT_FILE), true);
		rep = (ProjectRepository) resource.getContents().get(0);
        rep.eAdapters().add(valueChangedContentAdapter);
		
		// We need to precalculate all the balancings. This used to be done by the ValueChangedAdapter, as it was 
		// present during file load. So the test expects all values to be already set correctly. But in the new design, the
		// ValueChangedContentAdapter is added only *after* the file load.
        valueChangedContentAdapter.recalculate(rep.eResource().getResourceSet(), new NullProgressMonitor());
		
		NotifTstHlp.setVerbose(true);		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		System.out.println("--- set up test '" + this.getClass().getSimpleName() + "' ---");
		notifications.clear();		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		System.out.println("--- end of test ---");		
	}

	@Test
	public void testStructuralChangeParameter() {
		// change structure:
		// change Parameter of TransitionBal/ModeMapping

		SystemComponent sc2 = rep.getProjects().get(1);
		
		// cut, paste
		TransitionBalancing transBal = sc2.getStatemachines().get(0).getTransitions().get(0).getBalancings().get(0); // ModeTrigger
		TransitionParameter tParam = transBal.getTarget();
		transBal.setTarget(null);
		transBal.setTarget(tParam);
		assertEquals(2, notifications.size());
        NotifTstHlp.assertLog(0, TransitionBalancingImpl.class, "SET", "target", NotifTstHlp.LOGMSG_IDENTIFIED);
        NotifTstHlp.assertLog(1, TransitionBalancingImpl.class, "SET", "target", NotifTstHlp.LOGMSG_IDENTIFIED);
        // notifTstHlp.assertLog(2, ValueImpl.class, "SET", "value", notifTstHlp.LOGMSG_IDENTIFIED); // TP>2 (target)
		notifications.clear();
		transBal = sc2.getStatemachines().get(0).getTransitions().get(1).getBalancings().get(0); // ModeMappingTrigger
		tParam = transBal.getTarget();
		transBal.setTarget(null);
		transBal.setTarget(tParam);
		assertEquals(2, notifications.size());
        NotifTstHlp.assertLog(0, TransitionBalancingImpl.class, "SET", "target", NotifTstHlp.LOGMSG_IDENTIFIED);
        NotifTstHlp.assertLog(1, TransitionBalancingImpl.class, "SET", "target", NotifTstHlp.LOGMSG_IDENTIFIED);
        // notifTstHlp.assertLog(2, ValueImpl.class, "SET", "value", notifTstHlp.LOGMSG_IDENTIFIED); // TP>2 (target)
		notifications.clear();
		// cut, paste
		Transition trans = sc2.getStatemachines().get(0).getTransitions().get(0);
		tParam = trans.getParameters().get(0);
		trans.getParameters().remove(0);
		trans.getParameters().add(0, tParam);
        assertEquals(2, notifications.size());
        NotifTstHlp.assertLog(0, TransitionImpl.class, "REMOVE", "parameters", NotifTstHlp.LOGMSG_IDENTIFIED);
        NotifTstHlp.assertLog(1, TransitionImpl.class, "ADD", "parameters", NotifTstHlp.LOGMSG_IDENTIFIED);
		// no further notification or re-calculation, because TransBal has lost its target
		notifications.clear();
	}

	@Test
	public void testStructuralChangeMode() {
		// change structure:
		// change Modes of SystemComponent/ModeValueRef/ModeMapping/ModeTrigger/Mode EntryMode

		SystemComponent sc1 = rep.getProjects().get(0);
		// change
		Mode mode = sc1.getStatemachines().get(0).getModes().get(0);
		mode.setEntryMode(false);
		mode.setEntryMode(true);
		assertEquals(2, notifications.size());
		NotifTstHlp.assertLog(0, ModeImpl.class, "SET", "entryMode", NotifTstHlp.LOGMSG_IDENTIFIED); // P3
		NotifTstHlp.assertLog(1, ModeImpl.class, "SET", "entryMode", NotifTstHlp.LOGMSG_IDENTIFIED); // P3
		notifications.clear();
		// cut, paste
		// sc1
		
		sc1.getStatemachines().get(0).getModes().remove(0);
		System.out.println("Mode: "+sc1.getStatemachines().get(0).getModes());
		sc1.getStatemachines().get(0).getModes().add(mode);
        assertEquals(2, notifications.size());
        NotifTstHlp.assertLog(0, StateMachineImpl.class, "REMOVE", "modes", NotifTstHlp.LOGMSG_IDENTIFIED);
		// TODO no calculations? Any modeValueRefs to modes are invalid!
        NotifTstHlp.assertLog(1, StateMachineImpl.class, "ADD", "modes", NotifTstHlp.LOGMSG_IDENTIFIED);
		notifications.clear();
		// cut, paste
		Transition trans = sc1.getStatemachines().get(0).getTransitions().get(0);
		trans.setSource(mode);
		trans.setSource(null);
		assertEquals(2, notifications.size());
        NotifTstHlp.assertLog(0, TransitionImpl.class, "SET", "sourcePointer", NotifTstHlp.LOGMSG_IDENTIFIED);
        NotifTstHlp.assertLog(1, TransitionImpl.class, "SET", "sourcePointer", NotifTstHlp.LOGMSG_IDENTIFIED);
		notifications.clear();
		// cut, paste
		trans.setTarget(null);
		trans.setTarget(mode);
		assertEquals(2, notifications.size());
        NotifTstHlp.assertLog(0, TransitionImpl.class, "SET", "targetPointer", NotifTstHlp.LOGMSG_IDENTIFIED);
        NotifTstHlp.assertLog(1, TransitionImpl.class, "SET", "targetPointer", NotifTstHlp.LOGMSG_IDENTIFIED);
		notifications.clear();
		// cut, paste
		ModeGuard condition = (ModeGuard) sc1.getStatemachines().get(0).getTransitions().get(0).getCondition();
		condition.setMode(null);
		condition.setMode(mode);
		assertEquals(2, notifications.size());
		NotifTstHlp.assertLog(0, ModeGuardImpl.class, "SET", "modePointer", NotifTstHlp.LOGMSG_IDENTIFIED);
		NotifTstHlp.assertLog(1, ModeGuardImpl.class, "SET", "modePointer", NotifTstHlp.LOGMSG_IDENTIFIED);
		notifications.clear();
	}
}
