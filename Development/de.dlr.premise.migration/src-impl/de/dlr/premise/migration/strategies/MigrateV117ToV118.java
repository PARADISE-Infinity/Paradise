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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.ModelVersion;
import de.dlr.premise.migration.util.MigrationHelper;

/**
 * Connection was moved to package element, but for some reason older files explicitly encode connections xsi:type as prem:Connection. This
 * migration just removes the xsi:type attribute.
 */
public class MigrateV117ToV118 extends AMigration {

    @Override
    public String getTargetVersion() {
        return ModelVersion.V118.toString();
    }

    @Override
    protected void migrateRoot(MigrationModel model, Element modelRoot) {

        // get all connections
        List<Node> connections = new ArrayList<Node>(MigrationHelper.listFromNodeList(modelRoot.getElementsByTagName("connections")));

        // remove xsi:type
        for (Node connection : connections) {
            Element con = (Element) connection;
            if (con.hasAttribute("xsi:type")) {
                NamedNodeMap attributes = con.getAttributes();
                attributes.removeNamedItem("xsi:type");
                model.setChange();
            }
        }
    }
}