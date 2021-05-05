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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.dlr.premise.migration.IPremiseMigration;
import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.ModelVersion;

public class MigrateUnversionedToV102 extends AMigration implements IPremiseMigration  {

	@Override
    public String getTargetVersion() {
        return ModelVersion.V102.toString();        
    }

	@Override
    protected void migrateRoot(final MigrationModel model, final Element root) {
		boolean needsNamespace = false;

		// add xsi:type="val:ModeMapping" to ModeMappings
		NodeList transitions = root.getElementsByTagName("transitions");
		for (int i = 0, l = transitions.getLength(); i < l; i++) {
			Element tr = (Element) transitions.item(i);

			if (!tr.getAttribute("xsi:type").equals("val:ModeMapping")) {
				tr.setAttribute("xsi:type", "val:ModeMapping");
				model.setChange();
				needsNamespace = true;
			}
		}

		// add xsi:type="val:TransitionBalancing" to TransitionBalancings
		NodeList balancings = root.getElementsByTagName("balancings");
		for (int i = 0, l = balancings.getLength(); i < l; i++) {
			Element bal = (Element) balancings.item(i);

			// make sure parent is a mode mappings, as balancing element is also called "balancings"
			if (bal.getParentNode().getNodeName().equals("transitions") && !bal.getAttribute("xsi:type").equals("val:TransitionBalancing")) {
				bal.setAttribute("xsi:type", "val:TransitionBalancing");
				model.setChange();
				needsNamespace = true;
			}
		}

		// if any changes were made, the validated namespace has to be added
		if (needsNamespace && !root.getAttribute("xmlns:val").equals("http://www.dlr.de/ft/premise/validated/2013/")) {
			root.setAttribute("xmlns:val", "http://www.dlr.de/ft/premise/validated/2013/");
		}
	}
}
