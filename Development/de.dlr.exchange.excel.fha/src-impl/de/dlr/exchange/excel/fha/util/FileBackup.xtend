/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.excel.fha.util

import de.dlr.premise.util.PremiseHelper
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Date
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.IWorkspaceRoot
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.core.resources.IResource

class FileBackup {
	
	 private String wsPath
	
	 new() {
		var IWorkspace workspace = ResourcesPlugin.getWorkspace();
		var IWorkspaceRoot root = workspace.getRoot();
		wsPath = root.getLocation().toString();
	}
	
	/** creates a backup of the specified file. The backup location is 'PathToWorkspace'/backups/file.type */
	 def public void createBackup(String fileFullPath) {
	 	
        var source = new File(fileFullPath)
        
       	var String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

		// get the path to the backups folder
		var String filePath = fileFullPath.replace('\\','/')
		var String projectName = filePath.replace(wsPath,"")
		var projectPath = getProjectPath(projectName)
				
		var projectParts = projectName.split("/")
		if (projectParts == null || projectParts.size < 2){
			println("ERROR! Could not retrieve project, is the file you are trying to access closed?")
			return;
		}
		var String backupPath = wsPath + "/" + projectPath + "/backups"

		// create it if not already present
		var targetDir = new File(backupPath)
		targetDir.mkdir
		
		var target = new File(targetDir.toString+"/"+timestamp+"_"+source.name)
        
		try {
			
			Files.copy(source.toPath, target.toPath)
			
			// TODO: Refresh Navigator automatically
			ResourcesPlugin.getWorkspace().getRoot().getProjects().forEach[prj|prj.refreshLocal(IResource.DEPTH_INFINITE,null)]
			
		} catch (IOException e) {
			println("ERROR!\tCould not copy file\n\tFile Backup does not support linked workspaces or files")
			throw new RuntimeException(e);
		}

    }
	
	/** Creates a backups of the first resource in a ressource set */
	 def public void createBackup(ResourceSet resSet) {

		// get absolute path from resource set
		var res = resSet.resources.head
		var absFilePath = PremiseHelper.getAbsoluteFilePathFrom(res)

		// create the backup		
		createBackup(absFilePath)
	}
	
	/** Creates a Backup of a File, if it even exists */ 
	def public void createDefaultBackup(ResourceSet resSet, String fileName) {
	 	
	 	// check if a file to backup actually exists
	 	var res = resSet.resources.head
	 	var resPath = PremiseHelper.getAbsoluteFilePathFrom(res)
	 	var String projectName = resPath.replace(wsPath,"")
		var projectPath = getProjectPath(projectName)
		var absFilePath = wsPath + "/" + projectPath + "/export/" + fileName
	 	
	 	var newExportFile = new File(absFilePath)
	 	
	 	if (newExportFile.exists){
	 		createBackup(absFilePath)
	 	} else {
	 		println("The file you are trying to backup does not exist")
	 	}
	 }
	 
	 def private String getProjectPath(String projectName) {

		// get string parts	 	
	 	var parts = projectName.split("/")	 	
	 	
	 	var i = 0
	 	var path = ""
	 	// consider path without project and file name
		for (String part : parts) {
			if (i > 0 && i < parts.length - 1) {		
				path = String.join("/",path,part)
			}
			i++	
		}

		// remove the first /
		return path.replaceFirst("/","")	 
	 }
	 
//	 def private String getBackupPath() {
//	 	return wsPath + "/backups" 
//	 }
}