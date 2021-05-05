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

import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.ModelVersion;


public class MigrateV113ToV114 extends AMigration {

    @Override
    public String getTargetVersion() {
        return ModelVersion.V114.toString();
    }

    @Override
    protected void migrateRoot(MigrationModel model, Element modelRoot) {

//    	String targetVersion = ModelVersion.V114.toString();
    	
    	// set meta model version for all model files
//    	modelRoot.setAttribute("metaModel", targetVersion);
//    	model.setVersion(targetVersion);
    	model.setChange();
    }
}
