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

public class MigrateV104ToV105 extends AMigration implements IPremiseMigration {

	@Override
    public String getTargetVersion() {
        return ModelVersion.V105.toString();
    }

	@Override
    protected void migrateRoot(MigrationModel model, Element root) {
		boolean needsPremiseValidatedNamespace = false;
		boolean needsRegistryValidatedNamespace = false;

		// add xsi:type="val:Balancing" to Balancings
		NodeList balancings = root.getElementsByTagName("balancings");
		for (int i = 0, l = balancings.getLength(); i < l; i++) {
			Element bal = (Element) balancings.item(i);

			// make sure this is not a transition balancing
			if (!bal.getAttribute("xsi:type").equals("val:TransitionBalancing") && !bal.getAttribute("xsi:type").equals("val:Balancing")) {
				bal.setAttribute("xsi:type", "val:Balancing");
				model.setChange();
				needsPremiseValidatedNamespace = true;
			}
		}

		// add xsi:type="regval:Value" to Values
		needsRegistryValidatedNamespace = addXsiType(root, "value", "regval:Value") || needsRegistryValidatedNamespace;

		// add xsi:type="val:PortMapping" to PortMappings
		needsPremiseValidatedNamespace = addXsiType(root, "connections", "val:PortMapping") || needsPremiseValidatedNamespace;

		// add xsi:type="val:Mode" to Modes
		needsPremiseValidatedNamespace = addXsiType(root, "modes", "val:Mode") || needsPremiseValidatedNamespace;

		// add xsi:type="val:Mode" to Modes
		needsPremiseValidatedNamespace = addXsiType(root, "modeValues", "val:ModeValueRef") || needsPremiseValidatedNamespace;

		if (needsPremiseValidatedNamespace || needsRegistryValidatedNamespace) {
			model.setChange();

			if (!root.getAttribute("xmlns:xsi").equals("http://www.w3.org/2001/XMLSchema-instance")) {
				root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			}
		}
		if (needsPremiseValidatedNamespace && !root.getAttribute("xmlns:val").equals("http://www.dlr.de/ft/premise/validated/2013/")) {
			root.setAttribute("xmlns:val", "http://www.dlr.de/ft/premise/validated/2013/");
		}
		if (needsRegistryValidatedNamespace && !root.getAttribute("xmlns:regval").equals("http://www.dlr.de/ft/premise/registry/validated/2013/")) {
			root.setAttribute("xmlns:regval", "http://www.dlr.de/ft/premise/registry/validated/2013/");
		}
	}

	private boolean addXsiType(Element root, String tagName, String type) {
		boolean change = false;

		NodeList elements = root.getElementsByTagName(tagName);
		for (int i = 0, l = elements.getLength(); i < l; i++) {
			Element elem = (Element) elements.item(i);

			if (!elem.getAttribute("xsi:type").equals(type)) {
				elem.setAttribute("xsi:type", type);
				change = true;
			}
		}

		return change;
	}
}
