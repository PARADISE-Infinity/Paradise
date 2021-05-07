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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.dlr.premise.migration.IPremiseMigration;
import de.dlr.premise.migration.MigrationModel;

public abstract class AMigration implements IPremiseMigration {
    
    @Override
    public void migrate(final MigrationModel model) {
        
        // migrate file itself
        migrateRoot(model, model.getModelRoot());

        // migrate related files
        for (Document document :  model.getReferencedFiles().values()) {
            Element root = document.getDocumentElement();
            root.normalize();
            migrateRoot(model, root);
        }
    }
    
    protected abstract void migrateRoot(final MigrationModel model, final Element modelRoot);
}
