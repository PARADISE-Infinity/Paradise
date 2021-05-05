/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.fha.wizard

import org.eclipse.jface.wizard.Wizard
import de.dlr.exchange.excel.fha.SyncModelGenerator

class MyFHAWizard extends Wizard {
	
	protected MyFHAPage page
	protected SyncModelGenerator exporter
	
	new (SyncModelGenerator exporter){
		super()
		this.exporter = exporter
	}
	
	override getWindowTitle(){
		return "Synchronize FHA"
	}
	
	override addPages(){
		page = new MyFHAPage(exporter.currentFilePath)
		addPage(page)
	}
	
	override performFinish() {
		exporter.saveWizardInput(page.update, page.direction, page.createBackup, page.dataPath)
		return true;
	}
	
}