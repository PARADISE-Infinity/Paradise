/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.excel.fha.wizard

import org.eclipse.jface.wizard.WizardPage
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.layout.RowLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.FileDialog
import org.eclipse.swt.widgets.Group
import org.eclipse.swt.widgets.Text

class MyFHAPage extends WizardPage {
	
	private boolean isExport = true;
	private boolean isUpdate = false;
	private boolean createBackup = true;
	private String dataPath = "";
	private Text fileBrowser;
	private String startPath = "/";
	
	protected new(String startPath) {
		super("FHA Synchronization Wizard")
		setTitle("FHA Synchronization Wizard");
        setDescription("FHA Synchronization Wizard");
       
		if (startPath != null && startPath != ""){
			this.startPath = startPath
		}
	}
	
	override createControl(Composite parent) {
		val container = new Composite(parent, SWT.NONE)
		var layout = new GridLayout(1, false)
		container.layout = layout
		var fillData = new GridData(SWT.FILL, SWT.FILL, true, true);
		container.data = fillData
		fillData.widthHint = 500
		
		// sync direction import/export
		var Group directionGroup = new Group(container, SWT.NONE)
		var groupLayout = new RowLayout(SWT.HORIZONTAL)
		directionGroup.layout = groupLayout
		
		// the buttons
		var Button buttonEx = new Button(directionGroup, SWT.RADIO)
		buttonEx.text = "Export"
		
		var Button buttonIm = new Button(directionGroup, SWT.RADIO)
		buttonIm.text = "Import"
		

                
        // update or create?
        val updateB = new Button(container, SWT.CHECK)
        updateB.text = "Update existing File?"
        
        // update or create?
        val backupB = new Button(container, SWT.CHECK.bitwiseOr(2))
        backupB.text = "Create Backups?"
        backupB.selection = true
                

         fileBrowser = new Text(container, SWT.BORDER)
		 fileBrowser.text = "FILEPATH"
		 fileBrowser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		 val chooseB = new Button(container, SWT.NONE)
		 chooseB.text = "Choose File"
		 chooseB.addSelectionListener(new SelectionAdapter() {
			override widgetSelected(SelectionEvent event) {
				var fileDialog = new FileDialog(Display.current.activeShell, SWT.OPEN)
				fileDialog.setText("Choose a File to Import")
				fileDialog.setFilterPath(startPath)
				var filterExtensions = #["*.xlsx"]
				fileDialog.filterExtensions = filterExtensions
				dataPath = fileDialog.open
				if (dataPath != null){
					fileBrowser.text = dataPath
				}
			}
		})
		 
		 
		 // Button Selection listener now, to control other UI Elements
		buttonEx.addSelectionListener(new SelectionAdapter() {
			override widgetSelected(SelectionEvent e) {
				var Button source = e.getSource() as Button;
				if (source.getSelection()) {
					isExport = true
					dataPath = "Export.xlsx"
					fileBrowser.text = dataPath
					updateB.enabled = true
					chooseB.enabled = false
					if (isUpdate) {
						chooseB.enabled = true
					}
				}
			}
		})

		buttonIm.addSelectionListener(new SelectionAdapter() {
			override widgetSelected(SelectionEvent e) {
				var Button source = e.getSource() as Button;
				if (source.getSelection()) {
					isExport = false
					isUpdate = true
					updateB.enabled = false
					updateB.selection = true
					chooseB.enabled = true
				}
			}
		})

		updateB.addSelectionListener(new SelectionAdapter() {
			override widgetSelected(SelectionEvent e) {
				var Button source = e.getSource() as Button;
				if (source.getSelection()) {
					isUpdate = true
					chooseB.enabled = isUpdate
				} else {
					isUpdate = false
					if (!isExport) {
						chooseB.enabled = true
					} else {
						chooseB.enabled = false
					}
				}
			}
		})
		
		backupB.addSelectionListener(new SelectionAdapter() {
			override widgetSelected(SelectionEvent e) {
				var Button source = e.getSource() as Button;
				if (source.getSelection()) {
					createBackup = true			
				} else {
					createBackup = false
				}
			}
		})

		setControl(parent)
	}
	
	public def getDirection(){
		isExport
	}
	
	public def getUpdate(){
		isUpdate
	}
	
	public def getDataPath(){
		if (dataPath != fileBrowser.text){
			return fileBrowser.text
		} else {
			return dataPath
		}
	}
	
	public def getCreateBackup(){
		createBackup
	}
}