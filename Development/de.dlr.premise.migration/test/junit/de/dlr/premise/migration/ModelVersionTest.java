/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.dlr.premise.migration.ModelVersion;
import de.dlr.premise.registry.RegistryPackage;

public class ModelVersionTest {

    final String latest = ModelVersion.V133.toString();

    @Test
    public void testToString() {
        assertEquals("1.02", ModelVersion.V102.toString());
        assertEquals("1.03", ModelVersion.V103.toString());
        assertEquals("1.04", ModelVersion.V104.toString());
        assertEquals("1.05", ModelVersion.V105.toString());
        assertEquals("1.06", ModelVersion.V106.toString());
        assertEquals("1.07", ModelVersion.V107.toString());
        assertEquals("1.08", ModelVersion.V108.toString());
        assertEquals("1.09", ModelVersion.V109.toString());
        assertEquals("1.10", ModelVersion.V110.toString());
        assertEquals("1.11", ModelVersion.V111.toString());
        assertEquals("1.12", ModelVersion.V112.toString());
        assertEquals("1.13", ModelVersion.V113.toString());
        assertEquals("1.14", ModelVersion.V114.toString());
        assertEquals("1.15", ModelVersion.V115.toString());
        assertEquals("1.16", ModelVersion.V116.toString());
        assertEquals("1.16", ModelVersion.V116.toString());
        assertEquals("1.17", ModelVersion.V117.toString());
        assertEquals("1.18", ModelVersion.V118.toString());
        assertEquals("1.19", ModelVersion.V119.toString());
        assertEquals("1.20", ModelVersion.V120.toString());
        assertEquals("1.21", ModelVersion.V121.toString());
        assertEquals("1.22", ModelVersion.V122.toString());
        assertEquals("1.23", ModelVersion.V123.toString());
        assertEquals("1.24", ModelVersion.V124.toString());
        assertEquals("1.25", ModelVersion.V125.toString());
        assertEquals("1.26", ModelVersion.V126.toString());
        assertEquals("1.27", ModelVersion.V127.toString());
    }

    @Test
    public void testGetLatestVersion() {
        assertEquals(latest, ModelVersion.getLatestVersion());
    }

    @Test
    public void testIsVersion() {
        assertTrue(ModelVersion.isVersion("1.02"));
        assertTrue(ModelVersion.isVersion("1.03"));
        assertTrue(ModelVersion.isVersion("1.04"));
        assertTrue(ModelVersion.isVersion("1.05"));
        assertTrue(ModelVersion.isVersion("1.06"));
        assertTrue(ModelVersion.isVersion("1.07"));
        assertTrue(ModelVersion.isVersion("1.08"));
        assertTrue(ModelVersion.isVersion("1.09"));
        assertTrue(ModelVersion.isVersion("1.10"));
        assertTrue(ModelVersion.isVersion("1.11"));
        assertTrue(ModelVersion.isVersion("1.12"));
        assertTrue(ModelVersion.isVersion("1.13"));
        assertTrue(ModelVersion.isVersion("1.14"));
        assertTrue(ModelVersion.isVersion("1.15"));
        assertTrue(ModelVersion.isVersion("1.16"));
        assertTrue(ModelVersion.isVersion("1.17"));
        assertTrue(ModelVersion.isVersion("1.18"));
        assertTrue(ModelVersion.isVersion("1.19"));
        assertTrue(ModelVersion.isVersion("1.20"));
        assertTrue(ModelVersion.isVersion("1.21"));
        assertTrue(ModelVersion.isVersion("1.22"));
        assertTrue(ModelVersion.isVersion("1.23"));
        assertTrue(ModelVersion.isVersion("1.24"));
        assertTrue(ModelVersion.isVersion("1.25"));
        assertTrue(ModelVersion.isVersion("1.26"));
        assertTrue(ModelVersion.isVersion("1.27"));
        assertTrue(ModelVersion.isVersion("1.28"));
        assertTrue(ModelVersion.isVersion("1.29"));
        assertTrue(ModelVersion.isVersion("1.30"));
        assertFalse(ModelVersion.isVersion("Unknown"));
    }

    @Test
    public void testGetModelVersion() throws ParserConfigurationException {
        Element root = getRootElement();
        assertEquals("", ModelVersion.getModelVersion(root));

        root.setAttribute("metaModel", "1.07");
        assertEquals("1.07", ModelVersion.getModelVersion(root));
    }


    @Test
    public void testMetamodelVersion() {
        assertEquals(ModelVersion.getLatestVersion(), RegistryPackage.Literals.AVERSIONED_MODEL_ROOT__META_MODEL.getDefaultValueLiteral());
    }
    
    /**
     * @return
     * @throws ParserConfigurationException Returns root element.
     */
    private Element getRootElement() throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder loader = factory.newDocumentBuilder();
        Document document = loader.newDocument();
        return document.createElement("root");
    }
}
