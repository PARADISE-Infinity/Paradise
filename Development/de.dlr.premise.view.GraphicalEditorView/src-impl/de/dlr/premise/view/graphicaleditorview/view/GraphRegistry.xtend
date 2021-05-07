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

import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.SelectionListener
import org.eclipse.swt.widgets.Display
import org.eclipse.ui.PlatformUI
import y.view.Graph2D
import y.view.Graph2DView
import y.view.hierarchy.HierarchyManager

/** 
 */
abstract class GraphRegistry {
	private static Graph2D graph
	private static boolean groupsEnabled
	private static boolean edgeGroupingEnabled
	private static int depth = 3
	private static boolean layoutEnabled = true
	private static boolean layoutTopToBottom = true
	private static boolean pullUpEdges = true
	private static LayoutExecutor layoutExecutor
	private static GraphListenerImpl graphListener

	def static void init(Graph2D graph) {
		setGraph(graph)
		layoutExecutor = new LayoutExecutor(graph.getCurrentView() as Graph2DView)
		var HierarchyManager hierarchy = new HierarchyManager(graph)
		GraphItemProvider.configureDefaultRealizers(graph)
		GraphItemProvider.configureDefaultGroupNodeRealizers(hierarchy)
		// register event listeners
		setGraphListener(new GraphListenerImpl())
		Display.^default.asyncExec [
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addSelectionListener(
				SelectionListener.getInstance())
		]
	}

	/**
	 * Handles the correct disposal of listeners and adapters.
	 */
	def static dispose() {
		SelectionListener.instance.dispose()
	}

	/** 
	 * @return the graph
	 */
	def static Graph2D getGraph() {
		return graph
	}

	/** 
	 * @param graph the graph to set
	 */
	def static void setGraph(Graph2D graph) {
		GraphRegistry.graph = graph
	}

	/** 
	 * @return the groupsEnabled
	 */
	def static boolean isGroupsEnabled() {
		return groupsEnabled
	}

	/** 
	 * @param groupsEnabled the groupsEnabled to set
	 */
	def static void setGroupsEnabled(boolean groupsEnabled) {
		GraphRegistry.groupsEnabled = groupsEnabled
	}

	/**
	 * @return if edge-grouping is enabled
	 */
	def static boolean isEdgeGroupingEnabled() {
		return edgeGroupingEnabled
	}

	/**
	 * @param edgeGroupingEnabled if the edge-grouping option should be enabled
	 */
	def static void setEdgeGroupingEnabled(boolean edgeGroupingEnabled) {
		GraphRegistry.edgeGroupingEnabled = edgeGroupingEnabled
	}

	/**
	 * @return the depth
	 */
	def static int getDepth() {
		return depth
	}

	/**
	 * @param depth the new depth
	 */
	def static int setDepth(int depth) {
		GraphRegistry.depth = if(depth == 0) Integer.MAX_VALUE else depth
	}

	/**
	 * @return if the automatic layout is enabled
	 */
	def static boolean isLayoutEnabled() {
		return layoutEnabled
	}

	/**
	 * @param layoutEnabled whether the automatic layout should be enabled
	 */
	def static void setLayoutEnabled(boolean layoutEnabled) {
		GraphRegistry.layoutEnabled = layoutEnabled
	}

	/**
	 * @return if the automatic layout is from top to bottom or left to right
	 */
	def static boolean isLayoutTopToBottom() {
		return layoutTopToBottom
	}

	/**
	 * @param layoutTopToBottom whether the automatic layout should be from top to bottom or left to right
	 */
	def static void setLayoutTopToBottom(boolean layoutTopToBottom) {
		GraphRegistry.layoutTopToBottom = layoutTopToBottom
	}

	/**
	 * @return if edges connecting children that are hidden due to depth should be pulled up to parents 
	 */
	def static boolean isPullUpEdges() {
		return pullUpEdges
	}

	/**
	 * @param layoutTopToBottom whether edges connecting children that are hidden due to depth should be pulled up to parents 
	 */
	def static void setPullUpEdges(boolean pullUpEdges) {
		GraphRegistry.pullUpEdges = pullUpEdges
	}


	/** 
	 * @return the layoutExecutor
	 */
	def static LayoutExecutor getLayoutExecutor() {
		return layoutExecutor
	}

	/** 
	 * @param layoutExecutor the layoutExecutor to set
	 */
	def static void setLayoutExecutor(LayoutExecutor layoutExecutor) {
		GraphRegistry.layoutExecutor = layoutExecutor
	}

	/** 
	 * @return the graphListener
	 */
	def static GraphListenerImpl getGraphListener() {
		return graphListener
	}

	/** 
	 * @param graphListener the graphListener to set
	 */
	def static void setGraphListener(GraphListenerImpl graphListener) {
		graph.removeGraphListener(GraphRegistry.graphListener)
		GraphRegistry.graphListener = graphListener
		graph.addGraphListener(graphListener)
	}
}
