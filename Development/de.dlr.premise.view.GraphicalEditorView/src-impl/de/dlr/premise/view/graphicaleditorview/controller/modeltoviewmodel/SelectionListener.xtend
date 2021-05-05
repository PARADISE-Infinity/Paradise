/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel

import de.dlr.premise.element.ARepository
import de.dlr.premise.query.persistent.PersistableQuery
import de.dlr.premise.system.presentation.my.query.QueryableTree
import de.dlr.premise.util.PremiseHelper
import de.dlr.premise.view.graphicaleditorview.Options
import de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry
import de.dlr.premise.view.graphicaleditorview.view.GraphRegistry
import java.util.Collection
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.emf.common.util.BasicEList
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain
import org.eclipse.emf.edit.provider.DelegatingWrapperItemProvider
import org.eclipse.jface.viewers.ISelection
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.swt.widgets.Display
import org.eclipse.ui.ISelectionListener
import org.eclipse.ui.IWorkbenchPart
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.part.MultiPageEditorPart

import static de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry.*
import static de.dlr.premise.view.graphicaleditorview.view.GraphRegistry.*

/**
 * Listens for selection-changes in the model and schedules graphing.
 */
class SelectionListener implements ISelectionListener {
	var graphJob = new GraphJob
	var graphLock = new Object
	val long GRAPHING_DELAY = 200
	var ARepository repo

	static var SelectionListener instance = null

	int enabled = 0

	static synchronized def getInstance() {
		if (instance == null) {
			instance = new SelectionListener()
		}
		return instance
	}

	private new() {
		super()
	}

	/* (non-Javadoc)
	 * @see ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	override void selectionChanged(IWorkbenchPart part, ISelection selection) {
		var selectedElements = checkSeleciton(selection)
		val activeQuery = checkPredicateAndGetActiveQuery()
		if (selectedElements.nullOrEmpty ||
			selectedElements == SelectionHolder.instance.selection && activeQuery == SelectionHolder.instance.query) {
			return
		}
		selectedElements.prepare()
		SelectionHolder.instance.selection = selectedElements
		SelectionHolder.instance.query = activeQuery
		MapperRegistry.nodeCreationAllowed = MapperRegistry.getMapper.isNodeCreationAllowed(selectedElements)
		scheduleGraphing(GRAPHING_DELAY, false)
	}

	def prepare(Collection<EObject> selectedElements) {
		if (!selectedElements.isNullOrEmpty) {
			var firstSelected = selectedElements.get(0)
			if (firstSelected instanceof EObject) {
				// set up "options"
				var root = PremiseHelper.getRoot(firstSelected)
				if (root != repo && root instanceof ARepository) {
					val oldAdapters = repo?.eAdapters ?: new BasicEList
					oldAdapters.removeIf[it instanceof ModelContentAdadpter]
					repo = root as ARepository
				}
				if (repo instanceof ARepository) {
					var options = new Options()
					options.setOptions(repo)

					GraphRegistry.groupsEnabled = isGroupsEnabled(repo)
					GraphRegistry.edgeGroupingEnabled = isEdgeGroupingEnabled(repo)
				}

				// add ContentAdapter
				if (!repo.eAdapters.exists[it instanceof ModelContentAdadpter]) {

					var adapter = new ModelContentAdadpter()
					repo.eAdapters.add(adapter)
				}

				// set editingDomain
				MapperRegistry.editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(firstSelected)
			}
		}
	}

	def PersistableQuery checkPredicateAndGetActiveQuery() {
		val editor = PlatformUI.workbench.activeWorkbenchWindow.activePage.activeEditor
		if (editor instanceof MultiPageEditorPart) {
			val page = editor.selectedPage
			if (page instanceof QueryableTree) {
				SelectionHolder.instance.predicate = page.predicate
				return page.activeQuery
			}
		}
		return null
	}

	/**
	 * Validates if the selection is usable.
	 */
	def checkSeleciton(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			val selectedElements = (selection as IStructuredSelection).toList.map[unwrap]
			if (!selectedElements.isNullOrEmpty && !MapperRegistry.mapper.isSelectionEmptyForMapper(selectedElements)) {
				return selectedElements.filter[it instanceof EObject].map[it as EObject].toList
			}
		}
	}

	def void dispose() {
		val oldAdapters = repo?.eAdapters ?: new BasicEList
		oldAdapters.removeIf[it instanceof ModelContentAdadpter]
		Display.^default.asyncExec [
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()?.getActivePage()?.removeSelectionListener(this)
		]
	}

	def scheduleGraphing(long delay, boolean keepZoomFactor) {
		synchronized (graphLock) {
			if(enabled != 0) return;
			if (graphJob != null) {
				graphJob.cancel
			}
			GraphRegistry.groupsEnabled = isGroupsEnabled(repo)
			GraphRegistry.edgeGroupingEnabled = isEdgeGroupingEnabled(repo)
			graphJob.keepZoomFactor = keepZoomFactor
			graphJob.schedule(delay)
		}
	}

	def void setEnableSchedule(boolean enabled) {
		if(enabled) this.enabled++ else this.enabled--
	}

	private static class GraphJob extends Job {

		boolean keepZoomFactor = false

		new() {
			super("Graphing Model")
		}

		override protected run(IProgressMonitor monitor) {
			MapperRegistry.mapper.selectionChanged(SelectionHolder.instance.selection)
			return Status.OK_STATUS
		}
	}

	def dispatch unwrap(Object object) {
		return object
	}

	def dispatch unwrap(DelegatingWrapperItemProvider object) {
		return object.value
	}
}
