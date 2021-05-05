/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.migration.ui;

import java.io.FileNotFoundException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.MigrationStrategy;
import de.dlr.premise.migration.ModelVersion;
import de.dlr.premise.system.extensionpoints.IBeforeModelLoad;

public class MigrateBeforeModelLoad implements IBeforeModelLoad {

    @Override
    public void execute(IFile file) {
        try {
            MigrationModel model = new MigrationModel(file.getRawLocation().toString());
            MigrationStrategy.migrate(model, ModelVersion.getLatestVersion());
            if (model.wasChanged()) {
                boolean doMigration =
                        MessageDialog
                                .openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Migration needed!",
                                        "The premise file format has changed. Your model has to be migrated to conform to the new format. \nClick \"OK\" to perform migration now.");
                if (doMigration) {
                    model.createBackup();
                    model.save();
                }
            }
        } catch (FileNotFoundException e) {
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "File not found!",
                    "The file you were trying to open was not found!");
        } catch (Exception e) {
            System.err.println("Migration of " + file + " failed.");
            e.printStackTrace();
        }
    }

}
