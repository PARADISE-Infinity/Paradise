/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.migration.strategies;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.dlr.premise.migration.IPremiseMigration;
import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.ModelVersion;

public class MigrateV110ToV111 extends AMigration implements IPremiseMigration {
    @Override
    public String getTargetVersion() {
        return ModelVersion.V111.toString();
    }

    @Override
    protected void migrateRoot(final MigrationModel model, final Element root) {
        boolean premiseNamespaceNeeded = false;
        boolean registryNamespaceNeeded = false;

        NodeList allElements = root.getElementsByTagName("*");

        // remove validated namspaces
        if (root.hasAttribute("xmlns:val")) {
            root.removeAttribute("xmlns:val");
            model.setChange();
        }

        if (root.hasAttribute("xmlns:regval")) {
            root.removeAttribute("xmlns:regval");
            model.setChange();
        }

        // change validated xmi:type to the non-validated equivalent
        for (int i = 0, l = allElements.getLength(); i < l; i++) {
            Element src = (Element) allElements.item(i);

            if (src.getAttribute("xsi:type").startsWith("val:")) {
                // split off "val:" (e.g. val:Parameter -> Parameter);
                String newType = "prem:" + src.getAttribute("xsi:type").substring(4);
                src.setAttribute("xsi:type", newType);
                premiseNamespaceNeeded = true;
            } else if (src.getAttribute("xsi:type").startsWith("regval:")) {
                // split off "regval:" (e.g. regval:Value -> Value);
                String newType = "reg:" + src.getAttribute("xsi:type").substring(7);
                src.setAttribute("xsi:type", newType);
                registryNamespaceNeeded = true;
            }
        }

        if (premiseNamespaceNeeded) {
            root.setAttribute("xmlns:prem", "http://www.dlr.de/ft/premise/2010/");
            model.setChange();
        }

        if (registryNamespaceNeeded) {
            root.setAttribute("xmlns:reg", "http://www.dlr.de/ft/premise/registry/2010/");
            model.setChange();
        }
    }
}