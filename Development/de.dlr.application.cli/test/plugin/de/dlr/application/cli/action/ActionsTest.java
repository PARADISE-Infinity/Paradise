/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.application.cli.action;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.junit.Test;

import de.dlr.premise.common.command.AssociatedCommandsCommandStack;
import de.dlr.premise.registry.Value;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.TestHelper;


public class ActionsTest {

    public static final String PLUGIN_ID = "de.dlr.application.cli";
    public static final String TEST_FOLDER = "test/data/";
    
    public static final String TEST_CALCULATION_FILE_PATH = TestHelper.locateFile(PLUGIN_ID, TEST_FOLDER + "TestCalculation.system").getPath().substring(1);
    public static final String TEST_CONSTRAINTS_FILE_PATH = TestHelper.locateFile(PLUGIN_ID, TEST_FOLDER + "TestConstraints.system").getPath().substring(1);
    public static final String TEST_VALIDATION_FILE_PATH = TestHelper.locateFile(PLUGIN_ID, TEST_FOLDER + "TestValidation.system").getPath().substring(1);
    
    @Test
    public void testCalculation() throws Exception {
        
        ResourceSet resourceSet = getResource(TEST_CALCULATION_FILE_PATH);        
        ProjectRepository repository = getProjectRepository(resourceSet);

        Parameter sysMass = getParameter(repository, "e91814e8-b12d-4414-b8cc-f9821e52119d");
        double value = getParameterValue(sysMass);
        assertEquals(300, value, 0.01);

        sysMass = setParameterValue(sysMass, 150);
        
        Parameter tmpMass = getParameter(repository, "e91814e8-b12d-4414-b8cc-f9821e52119d");
        value = getParameterValue(tmpMass);
        assertEquals(150, value, 0.01);

        Actions actions = new Actions(resourceSet);
        actions.calculate();

        value = getParameterValue(sysMass);
        assertEquals(300, value, 0.01);
    }
    
    @Test
    public void testCheckConstraints() throws Exception {
        ResourceSet resourceSet = getResource(TEST_CONSTRAINTS_FILE_PATH);
        Actions actions = new Actions(resourceSet);
        List<String> results = actions.checkConstraints();
        assertEquals(3, results.size());
        assertListContains(results, "MultiplySatisfied : MULTIPLY_SATISFIED");
        assertListContains(results, "Unsatisfied : NOT_SATISFIED");
        assertListContains(results, "Mass : VIOLATED");
    }

    @Test
    public void testValidation() throws Exception {        
        ResourceSet resourceSet = getResource(TEST_VALIDATION_FILE_PATH);        
        Actions actions = new Actions(resourceSet);
        List<String> results = actions.validate();
        assertEquals(2, results.size());
        assertEquals("Mass : Parameter is target of multiple balancings: getSystemMass > Mass, System_1.Mass = -1. [warning]", results.get(0));
        assertEquals("//@projects.0/@children.0/@connections.0 : Connection: Target must be the parent element or any of its children. [warning]", results.get(1));
    }
    
    private ProjectRepository getProjectRepository(ResourceSet resourceSet) {
        
        ProjectRepository repository = (ProjectRepository) resourceSet.getResources().get(0).getContents().get(0);
        
        return repository;
    }

    private ResourceSet getResource(final String fileName) throws Exception {
        
        ResourceSet resourceSet = initializeEditingDomain().getResourceSet();
        PremiseHelper.loadResource(resourceSet, fileName);
        EcoreUtil.resolveAll(resourceSet);
        
        return resourceSet;
    }
    
    private EditingDomain initializeEditingDomain() {
        
        AdapterFactory adapterFactory = new AdapterFactoryImpl();
        BasicCommandStack commandStack = new AssociatedCommandsCommandStack();
        
        return new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<Resource, Boolean>());
    }
    
    private Double getParameterValue(final Parameter param) {
     
        if (param == null) {
            return Double.NaN;
        }
        
        String strValue = param.getValue().getValue();
        
        return Double.parseDouble(strValue);
    }
    
    private Parameter setParameterValue(Parameter param, final double value) {

        String strValue = new DecimalFormat("#.0#").format(value);
        
        Value val = param.getValue();
        val.setValue(strValue);
        param.setValue(val);
        
        return param;
    }

    /**
     * Returns the parameter with the given uuid.
     * @param uuid
     * @return
     */
    private Parameter getParameter(final ProjectRepository repository,final String uuid) {
    
        // initialize return result
        Parameter result = null;

        if (repository != null) {
            EObject eObject = repository.eResource().getEObject(uuid);
            if (eObject instanceof Parameter) {
                result = (Parameter) eObject;
            }
        }
        
        return result;
    }   
    
    private void assertListContains(List<String> results, String x) {
        assertTrue(results.stream().anyMatch(s -> s.equals(x)));
    }
}
