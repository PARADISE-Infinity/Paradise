/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.view

import java.util.ConcurrentModificationException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import y.algo.AlgorithmAbortedException
import y.layout.LayoutOrientation
import y.layout.hierarchic.IncrementalHierarchicLayouter
import y.view.Graph2DLayoutExecutor
import y.view.Graph2DView

/**
 * Convenience implementation for handling smooth layouting.
 */
class LayoutExecutor {

	var Graph2DView graphView
	var IncrementalHierarchicLayouter layouter
	var Graph2DLayoutExecutor executor
	var volatile busy = false

	var LayoutJob layoutJob

	new(Graph2DView graphView) {
		this.graphView = graphView

		initLayouter()
		initExecutor()
	}

	private def initLayouter() {
		layouter = new IncrementalHierarchicLayouter()

		layouter.automaticEdgeGroupingEnabled = true
		layouter.considerNodeLabelsEnabled = true
		layouter.groupCompactionEnabled = true
		layouter.gridSpacing = 15
		layouter.minimumLayerDistance = 30
		layouter.nodeToNodeDistance = 30
		layouter.recursiveGroupLayeringEnabled = true
		layouter.orthogonallyRouted = true
		layouter.maximalDuration = 5000
		layouter.layoutOrientation = LayoutOrientation.LEFT_TO_RIGHT
	}

	private def initExecutor() {
		executor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.ANIMATED)
	}

	def synchronized scheduleLayout(boolean keepZoomFactor) {
		if(keepZoomFactor && !GraphRegistry.layoutEnabled) return;
		if (layoutJob == null) {
			layoutJob = new LayoutJob()
		}
		layoutJob.cancel
		layoutJob.keepZoomFactor = keepZoomFactor
		layoutJob.schedule(LayoutJob::layoutDelay)
	}

	private static class LayoutJob extends Job {

		public static val layoutDelay = 200L

		var boolean keepZoomFactor

		new() {
			super("LayoutJob")
		}

		override protected run(IProgressMonitor monitor) {
			if (GraphRegistry.layoutExecutor.isBusy)
				GraphRegistry.layoutExecutor.abortLayout()
			GraphRegistry.layoutExecutor.doLayout(keepZoomFactor)
			return Status.OK_STATUS
		}

	}

	def doLayout(boolean keepZoomFactor) {
		busy = true
		executor.layoutMorpher.keepZoomFactor = keepZoomFactor
		layouter.automaticEdgeGroupingEnabled = GraphRegistry.edgeGroupingEnabled
		layouter.layoutOrientation = if (GraphRegistry.layoutTopToBottom)
			LayoutOrientation.TOP_TO_BOTTOM
		else
			LayoutOrientation.LEFT_TO_RIGHT

		try {
			executor.doLayout(graphView, layouter, [busy = false], null)
		} catch (ArrayIndexOutOfBoundsException e) {
		} catch (ConcurrentModificationException e) {
			// ignore the annoying yFiles bugs...
		}
	}

	def abortLayout() {
		try {
			busy = false
			executor.defaultAbortHandler.cancel
			executor.defaultAbortHandler.check
			executor.defaultAbortHandler.reset
		} catch (AlgorithmAbortedException e) {
			// the algorithm as been aborted
		}
	}

	def isBusy() {
		return busy
	}

	def setBusy(boolean busy) {
		this.busy = busy
	}
}
