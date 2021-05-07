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
import de.dlr.premise.component.ComponentPackage
import de.dlr.premise.element.ElementPackage
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
import de.dlr.premise.system.SystemPackage
import de.dlr.premise.registry.MetaTypeDef

import static de.dlr.exchange.graphml.block.wizard.BackgroundHelper.*

class EdgesWizardPage extends WizardPage implements Observer {

	AdapterFactory adapterFactory
	GraphMLBlockSettings settings
	
	Button connectionCheckbox
	Button relationCheckbox
	Button satisfiesCheckbox
	Button balancingCheckbox
	Button transitionCheckbox
	
	Composite composite
	CheckboxTableViewer metaTypesTableViewer
	TableViewer resultsTableViewer
	
	Button parentEdgesCheckbox
	Button externalElementsCheckbox
	Button selfReferencesCheckbox
	Button edgeCountCheckbox
	Button showHierarchyAsGroupsCheckbox
	
	

	new(AdapterFactory adapterFactory, GraphMLBlockSettings settings) {
		super("edges")
		this.adapterFactory = adapterFactory
		this.settings = settings
		
		description = "Select exported edges"
	}

	override createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE)
		composite.layout = new GridLayout(2, true)
		
		new Group(composite, SWT.SHADOW_ETCHED_IN) => [
			text = "Select edge types"
			layout = new GridLayout
			layoutData = new GridData => [
				grabExcessHorizontalSpace = true
				horizontalAlignment = GridData.FILL
				verticalAlignment = GridData.FILL
			]
			
			connectionCheckbox = createTypeCheckbox(it, "Connection", ElementPackage.Literals.CONNECTION)
			relationCheckbox = createTypeCheckbox(it, "Relation", ElementPackage.Literals.RELATION)
			satisfiesCheckbox = createTypeCheckbox(it, "Satisfies", ComponentPackage.Literals.SATISFIES)
			balancingCheckbox = createTypeCheckbox(it, "Balancing", SystemPackage.Literals.ABALANCING)
			transitionCheckbox = createTypeCheckbox(it, "Transition", ElementPackage.Literals.TRANSITION)
			
			pack()
		] 
		
		new Group(composite, SWT.SHADOW_ETCHED_IN) => [
			text = "Edge handling"
			layout = new GridLayout
			layoutData = new GridData => [
				grabExcessHorizontalSpace = true
				horizontalAlignment = GridData.FILL
				verticalAlignment = GridData.FILL
			]
			
		
			parentEdgesCheckbox = new Button(it, SWT.CHECK) => [
				text = "Move edges of hidden children to parents"
				selection = true
				addSelectionListener(new SelectionAdapter() {
					override widgetSelected(SelectionEvent e) {
						val moveEdgesToParents = selection
						inBackgroundJob[settings.moveEdgesToParents = moveEdgesToParents]
					}
				})
			]		
			
			edgeCountCheckbox = new Button(it, SWT.CHECK) => [
				text = "Show count on grouped edges"
				selection = true
				addSelectionListener(new SelectionAdapter() {
					override widgetSelected(SelectionEvent e) {
						val showEdgeCount = selection
						inBackgroundJob[settings.showEdgeCount = showEdgeCount]
					}
				})
			]
			
			showHierarchyAsGroupsCheckbox = new Button(it, SWT.CHECK) => [
				text = "Show hierarchy as groups"
				selection = true
				addSelectionListener(new SelectionAdapter() {
					override widgetSelected(SelectionEvent e) {
						val showHierarchyAsGroups = selection
						inBackgroundJob[settings.showHierarchyAsGroups = showHierarchyAsGroups]
					}
				})
			]
			
			externalElementsCheckbox = new Button(it, SWT.CHECK) => [
				text = "Show external dependencies"
				selection = true
				addSelectionListener(new SelectionAdapter() {
					override widgetSelected(SelectionEvent e) {
						val drawExternalDependencies = selection
						inBackgroundJob[settings.drawExternalDependencies = drawExternalDependencies]
					}
				})
			]
			
			selfReferencesCheckbox = new Button(it, SWT.CHECK) => [
				text = "Show self references"
				selection = false
				addSelectionListener(new SelectionAdapter() {
					override widgetSelected(SelectionEvent e) {
						val drawSelfReferences = selection
						inBackgroundJob[settings.drawSelfReferences = drawSelfReferences]
					}
				})
			]
		]
		
		new Label(composite, SWT.NONE) => [
			text = "Select MetaTypes"
			layoutData = new GridData => [
				horizontalSpan = 2
			]
		]
		metaTypesTableViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER.bitwiseOr(SWT.H_SCROLL))
		metaTypesTableViewer.table.layoutData = new GridData => [
			grabExcessHorizontalSpace = true
			grabExcessVerticalSpace = true
			horizontalAlignment = GridData.FILL
			verticalAlignment = GridData.FILL
			horizontalSpan = 2
		]
		metaTypesTableViewer.contentProvider = ArrayContentProvider.getInstance()
		metaTypesTableViewer.labelProvider = new UnwrappingDelegatingLabelProvider(new AdapterFactoryLabelProvider(adapterFactory))

		metaTypesTableViewer.addCheckStateListener [ e |
			updateSelectedEdgeMetaTypes()
		]
		
		new Label(composite, SWT.HORIZONTAL.bitwiseOr(SWT.SEPARATOR)) => [
			layoutData = new GridData => [
				horizontalAlignment = SWT.FILL
				horizontalSpan = 2
			]
		]
		
		new Label(composite, SWT.NONE) => [
			text = "Selected edges"
			layoutData = new GridData => [
				horizontalSpan = 2
			]
		]
		
		resultsTableViewer = new TableViewer(composite, SWT.BORDER.bitwiseOr(SWT.H_SCROLL))
		resultsTableViewer.table.layoutData = new GridData => [
			grabExcessHorizontalSpace = true
			grabExcessVerticalSpace = true
			horizontalAlignment = GridData.FILL
			verticalAlignment = GridData.FILL
			horizontalSpan = 2
		]
		resultsTableViewer.contentProvider = new ArrayContentProvider
		resultsTableViewer.labelProvider = new UnwrappingDelegatingLabelProvider(new AdapterFactoryLabelProvider(adapterFactory))
			

		control = composite
	}
	
	def updateSelectedEdgeMetaTypes() {
		val selectedEdgeMetaTypes = metaTypesTableViewer.checkedElements.filter(MetaTypeDef).toSet
		inBackgroundJob[
			settings.selectedEdgeMetaTypes = selectedEdgeMetaTypes
		]
	}

	def private createTypeCheckbox(Composite parent, String label, EClass edgeType) {
		val checkbox = new Button(parent, SWT.CHECK)
		checkbox.text = label
		checkbox.selection = true
		checkbox.addSelectionListener(new SelectionAdapter() {
			override widgetSelected(SelectionEvent e) {
				val selection = checkbox.selection
				inBackgroundJob[
					if (selection) {
						settings.addSelectedEdgeType(edgeType)
					} else {
						settings.removeSelectedEdgeType(edgeType)
					}
				]
			}
		})
		checkbox
	}
	
	override update(Observable o, Object arg) {
		composite.display.syncExec[			
			metaTypesTableViewer.input = settings.allEdgeMetaTypes
			resultsTableViewer.input = settings.selectedEdges
			
			val mHeight = Math.min(120, metaTypesTableViewer.table.computeSize(SWT.DEFAULT, SWT.DEFAULT).y)
			(metaTypesTableViewer.table.layoutData as GridData).minimumHeight = mHeight
			
			transitionCheckbox.enabled = settings.selectedAdditionalNodeTypes.contains(ElementPackage.Literals.STATE_MACHINE)
			
			edgeCountCheckbox.enabled = settings.moveEdgesToParents
			
			composite.layout(true, true)
		]
	}
}
