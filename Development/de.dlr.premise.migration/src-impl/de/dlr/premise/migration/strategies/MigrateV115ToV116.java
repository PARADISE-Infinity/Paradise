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
import org.w3c.dom.Node;

import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.ModelVersion;
import de.dlr.premise.migration.util.MigrationHelper;

/**
 *
 */
public class MigrateV115ToV116 extends AMigration {

    @Override
    public String getTargetVersion() {
        return ModelVersion.V116.toString();
    }

    @Override
    protected void migrateRoot(MigrationModel model, Element modelRoot) {

        // get all relations and connections
        List<Node> relations = new ArrayList<Node>(MigrationHelper.listFromNodeList(modelRoot.getElementsByTagName("connections")));
        relations.addAll(MigrationHelper.listFromNodeList(modelRoot.getElementsByTagName("relations")));

        // set source parameter of all relations and connections to the parent if it isn't set
        for (Node relation : relations) {
            Element rel = (Element) relation;
            if (rel.getAttribute("source").equals("")) {
                String parentId = ((Element) rel.getParentNode()).getAttribute("id");
                rel.setAttribute("source", parentId);
                model.setChange();
            }
        }
    }
}