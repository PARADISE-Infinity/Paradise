/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.migration.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import de.dlr.premise.migration.MigrationMissingException;
import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.MigrationStrategy;

public class StandaloneMigration {
	public void startInteractive() {
		Scanner s = new Scanner(System.in);

		try {
			while (true) {
				System.out.println("Enter target version: ");
				String targetVersion = s.nextLine();

				System.out.println("Enter file path: ");
				String path = s.nextLine();

				migratePath(path, targetVersion);

				System.out.println("----------------");
			}
		} finally {
			s.close();
		}
	}

    public void migratePath(String path, String targetVersion) {
        migratePath(new File(path), targetVersion);
    }
	
	public void migratePath(File f, String targetVersion) {
		if (f.isDirectory()) {
			List<File> children = new ArrayList<File>(Arrays.asList(f.listFiles()));
			for (File child : children) {
				migratePath(child, targetVersion);
			}
		} else {
			migrateSingleFile(f, targetVersion);
		}
	}

	private void migrateSingleFile(File f, String targetVersion) {
		migrateSingleFile(f.getAbsolutePath(), targetVersion);
	}

	private void migrateSingleFile(String path, String targetVersion) {
		MigrationModel model;

		try {
			if (!path.matches("^.*\\.(premise|system|usecase|function|xmi)$")) {
				return;
			}

			System.out.println("Migrating file \"" +path + "\"");

			model = new MigrationModel(path);

			doMigrate(model, targetVersion);

			if (model.wasChanged()) {
				model.save();
				System.out.println("File was migrated!\n");
			} else {
				System.out.println("No migration needed!\n");
			}
		} catch (FileNotFoundException e) {
			System.out.println("FILE NOT FOUND!");
		}  catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doMigrate(MigrationModel model, String targetVersion) throws MigrationMissingException {
		MigrationStrategy.migrate(model, targetVersion);
	}


	public static void main(String[] args) {
		StandaloneMigration f = new StandaloneMigration();
		f.startInteractive();
	}
}
