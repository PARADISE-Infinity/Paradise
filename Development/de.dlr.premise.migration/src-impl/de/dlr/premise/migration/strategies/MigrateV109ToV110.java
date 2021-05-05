/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.migration.strategies;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.dlr.premise.migration.IPremiseMigration;
import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.ModelVersion;
import de.dlr.premise.migration.util.MigrationHelper;

public class MigrateV109ToV110 extends AMigration implements IPremiseMigration {

    private Map<String, Element> inputs = new HashMap<String, Element>();
    private Map<String, Element> outputs = new HashMap<String, Element>();
    private Map<String, Element> parents = new HashMap<String, Element>();

    @Override
    public String getTargetVersion() {
        return ModelVersion.V110.toString();
    }

    @Override
    protected void migrateRoot(MigrationModel model, Element root) {

        // migration is only for premise files
        if (root.getTagName().equals("prem:ProjectRepository") == false) {
            return;
        }

        // get inputs and outputs to access them directly for the migration
        NodeList sources = root.getElementsByTagName("*");
        getMaps(sources);        
        if (inputs.size() == 0 && outputs.size() == 0 && parents.size() == 0) {
            return;
        }

        for (int i = 0, l = sources.getLength(); i < l; i++) {
            Element src = (Element) sources.item(i);

            if (src.getAttribute("xsi:type").equals("val:Connection")) {

                // add sourcePortName (Output.name) and replace source (Output) with
                // (Output parent SystemComponent)
                String compID = src.getAttribute("sourcePortName");
                Element comp = outputs.get(compID);
                src.setAttribute("sourcePortName", comp.getAttribute("name"));

                Element parent = parents.get(compID);
                src.setAttribute("source", parent.getAttribute("id"));

                // add targetPortName (Input.name) and replace target (Input)
                // with (Input parent SystemComponent)
                compID = src.getAttribute("targetPortName");
                comp = inputs.get(compID);
                src.setAttribute("targetPortName", comp.getAttribute("name"));

                parent = parents.get(compID);
                src.setAttribute("target", parent.getAttribute("id"));

                model.setChange();
            }
        }

        // delete obsolete ports
        if (MigrationHelper.deleteElements(root, "inputs") > 0) {
            root.normalize();
            model.setChange();                 
        }
        if (MigrationHelper.deleteElements(root, "outputs") > 0) {
            root.normalize();
            model.setChange();
        }
    }

    private void getMaps(final NodeList sources) {
        for (int i = 0, l = sources.getLength(); i < l; i++) {
            Element src = (Element) sources.item(i);

            if (src.getNodeName().equals("inputs")) {
                String id = src.getAttribute("id");
                inputs.put(id, src);
                parents.put(id, (Element) src.getParentNode());
            }

            if (src.getNodeName().equals("outputs")) {
                String id = src.getAttribute("id");
                outputs.put(id, src);
                parents.put(id, (Element) src.getParentNode());
            }
        }
    }
}
