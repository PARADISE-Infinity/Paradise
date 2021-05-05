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
import de.dlr.premise.migration.util.MigrationHelper;

public class MigrateV108ToV109 extends AMigration implements IPremiseMigration {

    private static String PORTMAPPING = "val:PortMapping";
    private static String CONNECTION = "val:Connection";
    
	@Override
    public String getTargetVersion() {
        return ModelVersion.V109.toString();
    }

    protected void migrateRoot(MigrationModel model, Element root) {

		// change xsi:type "val:PortMapping" to "val:Connection"
		NodeList sources = root.getElementsByTagName("*");
		for (int i = 0, l = sources.getLength(); i < l; i++) {
			Element src = (Element) sources.item(i);
			if (src.getAttribute("xsi:type").equals(PORTMAPPING)) {
			    
	            // rename PortMapping > Connection			    
			    src.setAttribute("xsi:type", CONNECTION);
                
                // rename source > sourcePortName and target > targetPortName
                MigrationHelper.renameAttribute(src, "source", "sourcePortName");            
                MigrationHelper.renameAttribute(src, "target", "targetPortName");
                model.setChange();
            }			
		}
	}
}
