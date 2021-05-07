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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
import de.dlr.premise.util.TestHelper;

/**
 * @author steh_ti
 */
public class BalancingValidationTest {

    private static String PATH_INPUT_FILE = de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_VALIDATOR, "test/data/BalancingValidationTest.premise").getPath();
    private static String PATH_FUNCPOOL_FILE = de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_VALIDATOR, "test/data/_GLOBAL/GlobalFuncDefs.premise.functionpool").getPath();
    private static String PATH_EXCEL_FILE = de.dlr.premise.util.TestHelper.locateFile(TestHelper.DE_DLR_PREMISE_VALIDATOR, "test/data/_GLOBAL/GlobalCalcFunctions.xls").getPath();
   
    private static ProjectRepository repository = null;

    private Diagnostician diagnostician;
    private BasicDiagnostic diagnostic;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestHelper.replaceFileString(PATH_FUNCPOOL_FILE, PATH_FUNCPOOL_FILE, "fileURI=\"test/data/_GLOBAL/GlobalCalcFunctions.xls\"", "fileURI=\"" + PATH_EXCEL_FILE.substring(1) + "\"");
        ValdationTestHelper.registerOCL();
    }
    
    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestHelper.replaceFileString(PATH_FUNCPOOL_FILE, PATH_FUNCPOOL_FILE, "fileURI=\"" + PATH_EXCEL_FILE.substring(1)  + "\"", "fileURI=\"test/data/_GLOBAL/GlobalCalcFunctions.xls\"");
    }

    @Before
    public void setUp() throws Exception {
        Resource resource = TestHelper.loadResource(PATH_INPUT_FILE);
        EcoreUtil.resolveAll(resource.getResourceSet());
        repository = (ProjectRepository) resource.getContents().get(0);

        diagnostician = new Diagnostician();
        diagnostic = diagnostician.createDefaultDiagnostic(repository);
    }

    @Test
    public void testValid() {
        assertTrue(diagnostician.validate(repository, diagnostic));
        assertEquals(0, diagnostic.getChildren().size());
    }

    @Test
    public void testSiblingConnection() {   
        // Add parameter P2a to "Calculation with child parameters"
        Parameter p = repository.getProjects().get(0).getReferencedChildren().get(1).getParameters().get(0);
        repository.getProjects().get(0).getReferencedChildren().get(0).getBalancings().get(0).getActualSources().add(createParameterNameToParameterMapping("P2a", p));
       
        assertFalse(diagnostician.validate(repository, diagnostic));
        assertEquals(1, diagnostic.getChildren().size());
        assertEquals(Diagnostic.WARNING, diagnostic.getChildren().get(0).getSeverity());
    }

    @Test
    public void testUncleConnection() {
        // Add parameter P1b to "Calculation with own parameters"
        Parameter p = repository.getProjects().get(0).getReferencedChildren().get(0).getParameters().get(1);
        repository.getProjects().get(0).getReferencedChildren().get(1).getReferencedChildren().get(0).getBalancings().get(0).getActualSources().add(createParameterNameToParameterMapping("P1b", p));

        assertFalse(diagnostician.validate(repository, diagnostic));
        assertEquals(1, diagnostic.getChildren().size());
        assertEquals(Diagnostic.WARNING, diagnostic.getChildren().get(0).getSeverity());
    }

    @Test
    public void testMultipleTarget() {
        SystemComponent compSC1 = repository.getProjects().get(0).getReferencedChildren().get(0);
        compSC1.getBalancings().add(EcoreUtil.copy(compSC1.getBalancings().get(0)));
        
        assertFalse(diagnostician.validate(repository, diagnostic));
        // one diagnostic on affected parameter
        assertEquals(1, diagnostic.getChildren().size());
        assertEquals(Diagnostic.WARNING, diagnostic.getChildren().get(0).getSeverity());
    }
    
    @Test
    public void testCycle() {
        SystemComponent compSC4 = repository.getProjects().get(0).getReferencedChildren().get(3);
        Balancing bal1 = compSC4.getBalancings().get(0);

        Balancing balForCycle = SystemFactory.eINSTANCE.createBalancing();
        balForCycle.setActualTarget(EcoreUtil.copy(bal1.getActualSources().get(0)));
        balForCycle.getActualSources().add(EcoreUtil.copy(bal1.getActualTarget()));
        compSC4.getBalancings().add(balForCycle);

        assertFalse(diagnostician.validate(repository, diagnostic));
        assertEquals(2, diagnostic.getChildren().size());
        assertEquals(Diagnostic.WARNING, diagnostic.getChildren().get(0).getSeverity());
        assertEquals(Diagnostic.WARNING, diagnostic.getChildren().get(1).getSeverity());
    }

    private ParameterNameMapping createParameterNameToParameterMapping(String name, AParameterDef parameter) {
        DirectPointer<AParameterDef> pointer = GraphFactory.eINSTANCE.createDirectPointer();
        pointer.setTarget(parameter);

        ParameterNameMapping pm = SystemFactory.eINSTANCE.createParameterNameMapping();
        pm.setKey(name);
        pm.setValue(pointer);

        return pm;
    }
}
