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

public class MigrateV105ToV106 extends AMigration implements IPremiseMigration {

	@Override
    public String getTargetVersion() {
        return ModelVersion.V106.toString();
    }

    @Override
    protected void migrateRoot(MigrationModel model, Element root) {
		boolean needsPremiseValidatedNamespace = false;

		// add xsi:type="val:Parameter" to Parameters
		NodeList parameters = root.getElementsByTagName("parameters");
		for (int i = 0, l = parameters.getLength(); i < l; i++) {
			Element param = (Element) parameters.item(i);

			if (!param.getAttribute("xsi:type").equals("val:Parameter") &&
				(param.getParentNode().getNodeName().equals("projects") || 
			     param.getParentNode().getNodeName().equals("children"))) {
			    
				param.setAttribute("xsi:type", "val:Parameter");
				model.setChange();
				needsPremiseValidatedNamespace = true;
			}
		}

		// change xsi:type to "val:Parameter" where it was "prem:parameter"
		NodeList sources = root.getElementsByTagName("*");
		for (int i = 0, l = sources.getLength(); i < l; i++) {
			Element src = (Element) sources.item(i);

			if (src.getAttribute("xsi:type").equals("prem:Parameter")) {
				src.setAttribute("xsi:type", "val:Parameter");
				model.setChange();
				needsPremiseValidatedNamespace = true;
			}
		}

		if (needsPremiseValidatedNamespace) {
			if (!root.getAttribute("xmlns:xsi").equals("http://www.w3.org/2001/XMLSchema-instance")) {
				root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			}
		}
		if (needsPremiseValidatedNamespace && !root.getAttribute("xmlns:val").equals("http://www.dlr.de/ft/premise/validated/2013/")) {
			root.setAttribute("xmlns:val", "http://www.dlr.de/ft/premise/validated/2013/");
		}
	}
}
