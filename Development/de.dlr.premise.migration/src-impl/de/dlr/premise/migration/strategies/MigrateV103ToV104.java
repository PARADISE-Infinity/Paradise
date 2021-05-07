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

public class MigrateV103ToV104 extends AMigration implements IPremiseMigration {
        
	@Override
    public String getTargetVersion() {
        return ModelVersion.V104.toString();
    }

	@Override
    protected void migrateRoot(MigrationModel model, Element root) {

		NodeList metaTypes = root.getElementsByTagName("metaTypes");
		for (int i = 0; i < metaTypes.getLength(); i++) {
			Element metaType = (Element) metaTypes.item(i);
			NodeList types = metaType.getElementsByTagName("type");

			for (int j = 0; j < types.getLength(); j++) {
				Element type = (Element) types.item(j);
				if (j == 0) {
					metaType.setAttribute("href", type.getAttribute("href"));
				}
				metaType.removeChild(type);

				model.setChange();
			}
		}
	}
}
