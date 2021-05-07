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

public class MigrateV106ToV107 extends AMigration implements IPremiseMigration {

	@Override
    public String getTargetVersion() {
        return ModelVersion.V107.toString();
    }

    protected void migrateRoot(MigrationModel model, Element root) {

		// change xsi:type to "val:Transition" where it was "val:ModeMapping"
		NodeList sources = root.getElementsByTagName("*");
		for (int i = 0, l = sources.getLength(); i < l; i++) {
			Element src = (Element) sources.item(i);

			if (src.getAttribute("xsi:type").equals("val:ModeMapping")) {
				src.setAttribute("xsi:type", "val:Transition");
				model.setChange();
			}
		}
	}
}
