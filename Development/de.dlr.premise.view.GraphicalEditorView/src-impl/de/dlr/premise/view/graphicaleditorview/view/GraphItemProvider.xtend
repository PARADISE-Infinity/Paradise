/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.view

import de.dlr.premise.component.Satisfies
import de.dlr.premise.element.Transition
import de.dlr.premise.element.Connection
import de.dlr.premise.element.Relation
import de.dlr.premise.view.graphicaleditorview.viewmodel.Icon
import java.awt.Color
import java.net.URL
import org.eclipse.emf.ecore.EObject
import de.dlr.premise.system.Balancing
import y.view.Arrow
import y.view.EdgeRealizer
import y.view.Graph2D
import y.view.LineType
import y.view.PolyLineEdgeRealizer
import y.view.ShapeNodeRealizer
import y.view.hierarchy.DefaultHierarchyGraphFactory
import y.view.hierarchy.GroupNodeRealizer
import y.view.hierarchy.HierarchyManager
import yext.svg.view.SVGNodeRealizer

/**
 * Helper class that provides easy access to various graph-elements.
 */
class GraphItemProvider {

	def static getRelationColor(Class<? extends EObject> type) {
		var Color color
		switch (type) {
			case Relation: color = Color.decode("#009966")
			case Connection: color = Color.decode("#AD2C2C")
			case Balancing: color = Color.decode("#8ABDFF")
			case Satisfies: color = Color.blue
			case Transition: color = Color.black
			default: color = Color.black
		}
		return color
	}

	def static getRelationRealizer(Class<? extends EObject> type) {
		return getRelationRealizer(getRelationColor(type))
	}

	def static getRelationRealizer(Class<? extends EObject> type, String label) {
		var realizer = getRelationRealizer(type)
		realizer.labelText = label
		return realizer
	}

	def static EdgeRealizer getRelationRealizer(Color color) {
		return getRelationRealizer(color, false)
	}

	def static EdgeRealizer getRelationRealizer(Color color, boolean inverted) {
		var realizer = new PolyLineEdgeRealizer()
		if (inverted) {
			realizer.targetArrow = Arrow.NONE
			realizer.sourceArrow = Arrow.STANDARD
		} else {
			realizer.targetArrow = Arrow.STANDARD
			realizer.sourceArrow = Arrow.NONE
		}
		realizer.lineColor = color
		return realizer
	}

	def static getRelationRealizer() {
		return getRelationRealizer(Color.black)
	}

	def static configureDefaultRealizers(Graph2D graph) {
		// add an arrow decoration to edges
		graph.defaultEdgeRealizer = getRelationRealizer(Relation)

		graph.defaultNodeRealizer = nodeRealizer
	}

	def static configureDefaultGroupNodeRealizers(HierarchyManager manager) {
		// Get the graph factory that maintains the default group node/folder node realizer. 
		var hgf = manager.graphFactory as DefaultHierarchyGraphFactory

		// Create the default GroupNodeRealizer
		var gnr = groupNodeRealizer

		// Set the default group node/folder node realizer
		hgf.defaultGroupNodeRealizer = gnr.createCopy
		hgf.defaultFolderNodeRealizer = gnr.createCopy
	}

	def static getNodeRealizer(Icon icon) {
		
		switch icon {
			case ROUND_RECT: {
				return getRoundRectRealizer(LineType.LINE_1)
			}
			case ROUND_RECT_DASH: {
				return getRoundRectRealizer(LineType.DASHED_1)
			}			
			case PAPER:
				return new SVGNodeRealizer(
					new URL("platform:/plugin/de.dlr.premise.view.GraphicalEditorView/icons/Parameter.svg"))
			case ROUND: {
				return new SVGNodeRealizer(
					new URL("platform:/plugin/de.dlr.premise.view.GraphicalEditorView/icons/Round.svg"))
			}
			case ROUND_DOUBLE_BORDER: {
				return new SVGNodeRealizer(
					new URL("platform:/plugin/de.dlr.premise.view.GraphicalEditorView/icons/RoundDoubleBorder.svg"))
			}
		}
	}

	def static getNodeRealizer() {
		return getNodeRealizer(Icon.ROUND_RECT)
	}

	def static getGroupNodeRealizer() {
		var gnr = new GroupNodeRealizer()
		gnr.groupDepthFillColorEnabled = false
		gnr.shapeType = ShapeNodeRealizer.ROUND_RECT
		gnr.fillColor = Color.decode("#FFFFFF")
		gnr.fillColor2 = Color.decode("#D4D4D4")
		gnr.lineType = LineType.LINE_1
		gnr.lineColor = Color.decode("#123EA2")
		return gnr
	}
	
	def static getRoundRectRealizer(LineType type) {
		var nodeRealizer = new AutoSizeShapeNodeRealizer(ShapeNodeRealizer.ROUND_RECT)
			nodeRealizer.fillColor = Color.decode("#FFFFFF")
			nodeRealizer.fillColor2 = Color.decode("#D4D4D4")
			nodeRealizer.lineType = type
			nodeRealizer.lineColor = Color.decode("#123EA2")
			return nodeRealizer
	}	
}
