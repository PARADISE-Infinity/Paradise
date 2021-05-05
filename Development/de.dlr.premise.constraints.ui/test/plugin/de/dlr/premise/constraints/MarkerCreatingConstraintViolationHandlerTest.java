/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import de.dlr.premise.constraints.handlers.MarkerCreatingConstraintViolationHandler;
import de.dlr.premise.functions.AModeValueConstraint;
import de.dlr.premise.functions.AValueConstraint;
import de.dlr.premise.functions.RequiredParameter;
import de.dlr.premise.functions.UseCaseRepository;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.ProjectRepository;

public class MarkerCreatingConstraintViolationHandlerTest {
    
    private class SpiedMarkerCreatingConstraintViolationHandler extends MarkerCreatingConstraintViolationHandler {
        public SpiedMarkerCreatingConstraintViolationHandler(IResource file, ResourceSet resourceSet) {
            super(file, resourceSet);
        }

        public void waitForMarkers() throws InterruptedException {
            markersJob.join();
        }
    }

    private static String PATH_TESTDATA = "test/data/";
    private static final java.net.URI URI_TEST_FILE = de.dlr.premise.util.TestHelper.locateFile("de.dlr.premise.constraints.ui",
            PATH_TESTDATA + "MarkerCreatingConstraintViolationHandlerTest.premise");
    private static final java.net.URI URI_TEST_FILE2 = de.dlr.premise.util.TestHelper.locateFile("de.dlr.premise.constraints.ui",
            PATH_TESTDATA + "MarkerCreatingConstraintViolationHandlerTest.usecase");
    
    private IProject proj;
    private IFile prRepoPlatformResource;

    private ResourceSetImpl resSet;
    private Parameter param1;
    private Parameter param2;
    private RequiredParameter reqParam;
    private AValueConstraint valueConstr;
    private AModeValueConstraint modeValueConstr;

    private SpiedMarkerCreatingConstraintViolationHandler violations;

    @Before
    public void setUp() throws Exception {
        // get the eclipse workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // create project in junit workspace
        IProject proj = workspace.getRoot().getProject("abc");
        proj.create(null);
        proj.open(null);


        // create resource set for emf resouces to live in
        resSet = new ResourceSetImpl();

        // copy premise file into junit workspace with different name
        IFile prRepoPlatformResource = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("abc/test.premise"));
        prRepoPlatformResource.create(new FileInputStream(new File(URI_TEST_FILE)), IResource.NONE, null);
        // load model from resource
        Resource prRepoEMFResource = resSet.createResource(URI.createURI("platform:/resource/abc/test.premise"));
        prRepoEMFResource.load(null);
        // init premise variables
        ProjectRepository rep = (ProjectRepository) prRepoEMFResource.getContents().get(0);
        Parameter param1 = rep.getProjects().get(0).getParameters().get(0);
        Parameter param2 = rep.getProjects().get(0).getParameters().get(1);

        // copy usecase file into junit workspace with different name
        IFile ucRepoPlatformResource = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("abc/test.usecase"));
        ucRepoPlatformResource.create(new FileInputStream(new File(URI_TEST_FILE2)), IResource.NONE, null);
        // load model from resource
        Resource ucRepoEMFResource = resSet.createResource(URI.createURI("platform:/resource/abc/test.usecase"));
        ucRepoEMFResource.load(null);

        // init usecase variables
        UseCaseRepository ucRep = (UseCaseRepository) ucRepoEMFResource.getContents().get(0);
        RequiredParameter reqParam = ucRep.getUsecases().get(0).getChildren().get(0).getRequiredParameters().get(0);
        AValueConstraint valueConstr = reqParam.getValueConstraint();
        AModeValueConstraint modeValueConstr = reqParam.getModeValueConstraints().get(0);

        // make model elements that are used in test available
        this.proj = proj;
        this.prRepoPlatformResource = prRepoPlatformResource;
        this.param1 = param1;
        this.param2 = param2;
        this.reqParam = reqParam;
        this.valueConstr = valueConstr;
        this.modeValueConstr = modeValueConstr;

        // init helper
        violations = new SpiedMarkerCreatingConstraintViolationHandler(prRepoPlatformResource, resSet);
    }

    @After
    public void tearDown() throws Exception {
        proj.delete(true, null);
    }

    @Test
    public void testNotSatisfiedMarkerCreation() throws CoreException {
        IMarker[] markers;

        // add a standard "not satisfied" violation
        violations.addViolation(reqParam, ConstraintViolationKind.NOT_SATISFIED, null);

        markers = getMarkers();
        assertEquals(1, markers.length);
        assertEquals("RequiredParameter TheParameterConstraint = [4, 20] is not satisfied by any parameter",
                markers[0].getAttribute(MarkerCreatingConstraintViolationHandler.MARKER_ATTRIBUTE_MESSAGE));

        // remove violations
        violations.removeViolations(reqParam);
        markers = getMarkers();
        assertEquals(0, markers.length);
    }

    @Test
    public void testMultiplySatisfiedMarkerCreation() throws CoreException {
        IMarker[] markers;

        // add the multiply satisfied marker
        violations.addViolation(reqParam, ConstraintViolationKind.MULTIPLY_SATISFIED, Lists.newArrayList(param1, param2));

        markers = getMarkers();
        assertEquals(1, markers.length);
        assertEquals(
                "RequiredParameter TheParameterConstraint = [4, 20] is satisfied by multiple parameters (Parameter TheParameter = 123, Parameter TheOtherParameter = 0)",
                markers[0].getAttribute(MarkerCreatingConstraintViolationHandler.MARKER_ATTRIBUTE_MESSAGE));

        // remove violations
        violations.removeViolations(reqParam);
        markers = getMarkers();
        assertEquals(0, markers.length);
    }

    @Test
    public void testConstraintViolatedMarkerCreation() throws CoreException {
        IMarker[] markers;

        // add the constraint violatated marker
        violations.addViolation(param1, ConstraintViolationKind.VIOLATED, valueConstr);

        markers = getMarkers();
        assertEquals(1, markers.length);
        assertEquals(
                "Parameter TheParameter = 123 violates RangeConstraint [4, 20] of \"RequiredParameter TheParameterConstraint = [4, 20]\" of \"UseCase TheRequirement\"",
                markers[0].getAttribute(MarkerCreatingConstraintViolationHandler.MARKER_ATTRIBUTE_MESSAGE));

        // remove violations
        violations.removeViolations(param1);
        markers = getMarkers();
        assertEquals(0, markers.length);
    }

    @Test
    public void testModeValueConstraintViolatedMarkerCreation() throws CoreException {
        IMarker[] markers;

        // add mode value constraint violations
        violations.addViolation(param1, ConstraintViolationKind.VIOLATED, modeValueConstr);

        markers = getMarkers();
        assertEquals(1, markers.length);
        assertEquals(
                "Parameter TheParameter = 123 violates ModeRangeConstraint [-Inf, 200] of \"RequiredParameter TheParameterConstraint = [4, 20]\" of \"UseCase TheRequirement\"",
                markers[0].getAttribute(MarkerCreatingConstraintViolationHandler.MARKER_ATTRIBUTE_MESSAGE));

        // remove violations
        violations.removeViolations(param1);
        markers = getMarkers();
        assertEquals(0, markers.length);
    }

    @Test
    public void testMultipleMarkersCreation() throws CoreException {
        IMarker[] markers;

        // add some violations
        violations.addViolation(reqParam, ConstraintViolationKind.NOT_SATISFIED, null);
        violations.addViolation(reqParam, ConstraintViolationKind.MULTIPLY_SATISFIED, Lists.newArrayList(param1, param2));
        violations.addViolation(param1, ConstraintViolationKind.VIOLATED, valueConstr);
        violations.addViolation(param1, ConstraintViolationKind.VIOLATED, modeValueConstr);

        markers = getMarkers();
        assertEquals(4, markers.length);

        // remove one
        violations.removeViolations(param1);

        markers = getMarkers();
        assertEquals(2, markers.length);

        // remove the rest
        violations.removeViolations();
        markers = getMarkers();
        assertEquals(0, markers.length);
    }

    @Test
    public void testDiscoveringPreviouslyExistingMarkers() throws CoreException {
        IMarker[] markers;

        // add a standard "not satisfied" violation
        violations.addViolation(reqParam, ConstraintViolationKind.NOT_SATISFIED, null);

        markers = getMarkers();
        assertEquals(1, markers.length);
        assertEquals("RequiredParameter TheParameterConstraint = [4, 20] is not satisfied by any parameter",
                     markers[0].getAttribute(MarkerCreatingConstraintViolationHandler.MARKER_ATTRIBUTE_MESSAGE));

        // use a new MarkerCreatingConstraintViolationHandler, which has to discover the existing marker by searching the workspace

        violations = new SpiedMarkerCreatingConstraintViolationHandler(prRepoPlatformResource, resSet);

        // remove violations
        violations.removeViolations(reqParam);
        markers = getMarkers();
        assertEquals(0, markers.length);
    }

    /**
     * Gets all the markers. It waits for until jobs in constraint violation handler have finished and only returns after this has happened
     */
    private IMarker[] getMarkers() throws CoreException {
        try {
            violations.waitForMarkers();
        } catch (InterruptedException e) {
            fail();
        }

        // get the markers
        IMarker[] markers = prRepoPlatformResource.findMarkers(MarkerCreatingConstraintViolationHandler.MARKER, true, IResource.DEPTH_INFINITE);
        return markers;
    }

}
