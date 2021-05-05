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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import de.dlr.premise.migration.ModelVersion;

/**
 * Migrate all test files to the latest model version
 */
public class TestFilesMigration extends StandaloneMigration {
	@Override
    public void startInteractive() {
		Scanner s = new Scanner(System.in);

		try {
            String workspaceFolder = new File(System.getProperty("user.dir")).getParent();
		    
            if (workspaceFolder == null) {
                System.out.println("Enter path to workspace folder containing all projects: ");
                workspaceFolder = s.nextLine();
            }

			String targetVersion = ModelVersion.getLatestVersion();
            migrateProjects(workspaceFolder, targetVersion);
		} finally {
			s.close();
		}
	}

	public void migrateProjects(String path, String targetVersion) {
		migrateProjects(new File(path), targetVersion);
	}

	private void migrateProjects(File workspaceFolder, String targetVersion) {
		if (workspaceFolder.isDirectory()) {
			List<File> children = new ArrayList<File>(Arrays.asList(workspaceFolder.listFiles()));

			for (File child : children) {
				String name = child.getName();

				// don't migrate the migration project, as it must test with old version
                if ((name.startsWith("de.dlr") && !name.equals("de.dlr.premise.migration")) || name.startsWith("de.systemsdesign")) {
                    migrateProject(child, targetVersion);
                }
			}
		} else {
			System.err.println("Not a directory");
		}

	}
	
	public void migrateProject(File f, String targetVersion) {
		System.out.println("Migrating " + f.getName());
		migratePath(f, targetVersion);
	}

	public static void main(String[] args) {
		TestFilesMigration f = new TestFilesMigration();
		f.startInteractive();
	}
}
