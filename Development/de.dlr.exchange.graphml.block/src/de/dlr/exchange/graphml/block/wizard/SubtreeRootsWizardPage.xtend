/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.graphml.block.wizard

import de.dlr.exchange.graphml.block.settings.GraphMLBlockSettings
import de.dlr.premise.element.ElementPackage
import de.dlr.premise.functions.UseCasePackage
import java.util.Observable
import java.util.Observer
import org.eclipse.emf.common.notify.AdapterFactory
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider
import org.eclipse.jface.viewers.ArrayContentProvider
import org.eclipse.jface.viewers.CheckboxTableViewer
import org.eclipse.jface.viewers.TableViewer
import org.eclipse.jface.wizard.WizardPage
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Group
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Spinner
import de.dlr.premise.registry.MetaTypeDef
import de.dlr.premise.registry.RegistryPackage

import static de.dlr.exchange.graphml.block.wizard.BackgroundHelper.*

class SubtreeRootsWizardPage extends WizardPage implements Observer {

	AdapterFactory adapterFactory

	GraphMLBlockSettings settings

	Composite composite
	
	Button parameterCheckbox
	Button stateMachineCheckbox
	Button modeCheckbox
	
	CheckboxTableViewer metaTypesTableViewer
	TableViewer resultsTableViewer
	
	Button restrictDepthCheckbox
	Spinner depthSpinner
	

	new(AdapterFactory adapterFactory, GraphMLBlockSettings settings) {
		super("Select export roots")
		this.adapterFactory = adapterFactory
		this.settings = settings
		
		description = "Select roots of exported subtrees"
	}

	override createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE)
		composite.setLayout(new GridLayout)
		
		new Group(composite, SWT.SHADOW_ETCHED_IN) => [
			text = "Select additional node types"
			layout = new GridLayout
			layoutData = new GridData => [
				grabExcessHorizontalSpace = true
				horizontalAlignment = GridData.FILL
				verticalAlignment = GridData.FILL
			]
			
			parameterCheckbox = createTypeCheckbox(it, "Parameter", RegistryPackage.Literals.APARAMETER_DEF, UseCasePackage.Literals.REQUIRED_PARAMETER)
			stateMachineCheckbox = createTypeCheckbox(it, "StateMachine", ElementPackage.Literals.STATE_MACHINE)
			modeCheckbox = createTypeCheckbox(it, "Mode", ElementPackage.Literals.MODE)
			
			pack()
		] 

		new Label(composite, SWT.NONE).text = "Select MetaTypes"

		metaTypesTableViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER.bitwiseOr(SWT.H_SCROLL))
		metaTypesTableViewer.table.layoutData = new GridData => [
			grabExcessHorizontalSpace = true
			grabExcessVerticalSpace = true
			verticalAlignment = GridData.FILL
			horizontalAlignment = GridData.FILL
		]
		metaTypesTableViewer.contentProvider = ArrayContentProvider.getInstance()
		metaTypesTableViewer.labelProvider = new UnwrappingDelegatingLabelProvider(new AdapterFactoryLabelProvider(adapterFactory))

		metaTypesTableViewer.addCheckStateListener [ e |
			updateSelectedRootMetaTypes()
		]
		
		new Composite(composite, SWT.NONE) => [
			setLayout(new GridLayout(2, false) => [
				marginHeight = 0
				marginWidth = 0
			])
			
			restrictDepthCheckbox = new Button(it, SWT.CHECK) => [
				text = "Restrict export depth"
				selection = false
				addSelectionListener(new SelectionAdapter() {
					override widgetSelected(SelectionEvent e) {
						depthSpinner.enabled = restrictDepthCheckbox.selection
						updateDepth()
					}
				})
			]
			
			depthSpinner = new Spinner(it, SWT.BORDER) =>  [
				minimum = 1
				selection = 3
				increment = 1
				pageIncrement = 10
				enabled = false
				addSelectionListener(new SelectionAdapter() {
					override widgetSelected(SelectionEvent e) {
						updateDepth()
					}
				})
				pack()
			]
		
		]
		
		new Label(composite, SWT.HORIZONTAL.bitwiseOr(SWT.SEPARATOR)).layoutData = new GridData(GridData.FILL_HORIZONTAL)
		
		new Label(composite, SWT.NONE).text = "Selected roots"

		resultsTableViewer = new TableViewer(composite, SWT.BORDER.bitwiseOr(SWT.H_SCROLL))
		resultsTableViewer.table.layoutData = new GridData => [
			grabExcessHorizontalSpace = true
			grabExcessVerticalSpace = true
			verticalAlignment = GridData.FILL
			horizontalAlignment = GridData.FILL
		]
		resultsTableViewer.contentProvider = new ArrayContentProvider
		resultsTableViewer.labelProvider = new UnwrappingDelegatingLabelProvider(new AdapterFactoryLabelProvider(adapterFactory))

		control = composite
	}

	def public void updateSelectedRootMetaTypes() {
		val selectedRootMetaTypes = metaTypesTableViewer.checkedElements.filter(MetaTypeDef).toSet
		inBackgroundJob[
			settings.selectedRootMetaTypes = selectedRootMetaTypes
		]
	}
	
	def public updateDepth() {
		val restrictDepth = restrictDepthCheckbox.selection
		val depth = depthSpinner.selection
		inBackgroundJob[
			settings.setRestictedDepth(restrictDepth, depth)
		]
	}	
	
	def private createTypeCheckbox(Composite parent, String label, EClass... nodeTypes) {
		val checkbox = new Button(parent, SWT.CHECK)
		checkbox.text = label
		checkbox.selection = true
		checkbox.addSelectionListener(new SelectionAdapter() {
			override widgetSelected(SelectionEvent e) {
				val selection = checkbox.selection
				inBackgroundJob[
					if (selection) {
						settings.addSelectedAdditionalNodeType(nodeTypes)
					} else {
						settings.removeSelectedAdditionalNodeType(nodeTypes)
					}
				]
			}
		})
		checkbox
	}

	override update(Observable o, Object arg) {
		composite.display.asyncExec[
			metaTypesTableViewer.input = settings.getAllRootMetaTypes
			resultsTableViewer.input = settings.selectedRoots
			
			val mHeight = Math.min(120, metaTypesTableViewer.table.computeSize(SWT.DEFAULT, SWT.DEFAULT).y)
			(metaTypesTableViewer.table.layoutData as GridData).minimumHeight = mHeight
			
			modeCheckbox.enabled = settings.selectedAdditionalNodeTypes.contains(ElementPackage.Literals.STATE_MACHINE)
	
			composite.layout(true)
		]
	}

}
