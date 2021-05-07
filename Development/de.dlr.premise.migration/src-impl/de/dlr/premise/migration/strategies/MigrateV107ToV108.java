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

public class MigrateV107ToV108 extends AMigration implements IPremiseMigration {

    private boolean changed = false;

	@Override
    public String getTargetVersion() {
        return ModelVersion.V108.toString();
    }

    protected void migrateRoot(MigrationModel model, Element root) {

		// change xsi:type to "val:Parameter" where it was "prem:parameter"
		NodeList sources = root.getElementsByTagName("*");
		for (int i = 0, l = sources.getLength(); i < l; i++) {
			Element src = (Element) sources.item(i);

			// get value
			if (src.getAttribute("xsi:type").equals("regval:Value")) {			    
			    Element parent = (Element) src.getParentNode();
			    
                // move unit
			    String unit = src.getAttribute("unit");
			    if (unit != null && unit.length() > 0) {
			        parent.setAttribute("unit", unit);
			        src.removeAttribute("unit");
			        changed = true;
		        }
			    
			    NodeList units = src.getElementsByTagName("unit");
			    for(int j = 0; j < units.getLength(); j++ ) {
			        Element cUnit = (Element) units.item(j);
			        if (cUnit != null) {
			            parent.appendChild(cUnit);
		                 changed = true;
			        }
			    }
			    
			    // move shared
			    if (parent.getAttribute("xsi:type").equals("val:Parameter")) {
			        String shared = src.getAttribute("shared");
			        if (shared != null && shared.length() > 0) {
			            parent.setAttribute("shared", shared);
			            src.removeAttribute("shared");
		                 changed = true;
			        }
			    }
			}
		}
		
		if (changed) {
		    model.setChange();
		}
		
		
	}
}
