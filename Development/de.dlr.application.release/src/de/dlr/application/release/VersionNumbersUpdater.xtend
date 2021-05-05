/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.application.release

import de.dlr.application.WindowTitle
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.Scanner
import java.util.regex.Pattern

class VersionNumbersUpdater {

	val static PLUGIN_PATTERN = Pattern.compile('''^([^<]*)<plugin id="([^"]+)"( version="[^"]+")?/>$''')

	val Path path
	val String version
	
	val String fullVersion
	
	
	new(Path path, String version) {
		this.path = path
		this.version = version	
		this.fullVersion = version + ".qualifier"
	}
	
	def update() {
		val ds = Files.newDirectoryStream(path)
		
		for (child : ds) {
			val folderName = child.last.toString
			if (Files.isDirectory(child) && (isParadisePluginName(folderName))) {
				println("Updating " + folderName)
								
				val manifest = child.resolve("META-INF/MANIFEST.MF")
				if (Files.exists(manifest)) {
					updateManifest(manifest)
				}
			
				
				if (folderName == "de.dlr.application.repository") {
					val productFile = child.resolve("paradise.product")
					updateProduct(productFile)
					val headlessFile = child.resolve("headless.product")
					updateProduct(headlessFile)
				}
			}
		}
		println("Done.")
	}
	
	def updateManifest(Path manifest) {
		val fileContent = Files.readAllLines(manifest, StandardCharsets.UTF_8)
		val newFileContent = fileContent.map[line |
			if (line.startsWith("Bundle-Version:")) {
				'''Bundle-Version: «fullVersion»'''
			} else {
				line
			}
		]
		Files.write(manifest, newFileContent, StandardCharsets.UTF_8);
	}
	
	def updateProduct(Path productFile) {
		val fileContent = Files.readAllLines(productFile, StandardCharsets.UTF_8)
		val newFileContent = fileContent.map[line |
			val pluginMatcher = PLUGIN_PATTERN.matcher(line)
			
			if (line.matches("<product.*")) {
				line.replaceAll('''version="[^"]+"''', '''version="«fullVersion»"''')
			} else if (pluginMatcher.matches && pluginMatcher.group(2).isParadisePluginName) {
				'''«pluginMatcher.group(1)»<plugin id="«pluginMatcher.group(2)»" version="«fullVersion»"/>'''
			} else {
				line
			}
		]
		Files.write(productFile, newFileContent, StandardCharsets.UTF_8);
	}
	
	def isParadisePluginName(String pluginName) {
		pluginName.startsWith("de.dlr") || pluginName.startsWith("de.systemsdesign")
	}
	
	def static void main(String[] args) {
		new VersionNumbersUpdater(getPathWorkspacePath(), WindowTitle.VERSION).update()
	}
	
	def static getPathWorkspacePath() {
		val thisPluginPath = FileSystems.getDefault().getPath(System.getProperty("user.dir"))
		
		if (Files.exists(thisPluginPath)) {
			return thisPluginPath.parent
		} else {
			val s = new Scanner(System.in)
			println("Enter path to workspace folder containing all projects: ")
			return FileSystems.getDefault().getPath(s.nextLine())
		}
	}
}