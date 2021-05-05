/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.aspect.graphml.util

import com.yworks.ygraphml.Alignment
import com.yworks.ygraphml.ArrowType
import com.yworks.ygraphml.AutoSizePolicy
import com.yworks.ygraphml.Edge
import com.yworks.ygraphml.GenericNode
import com.yworks.ygraphml.HorizontalTextPosition
import com.yworks.ygraphml.Label
import com.yworks.ygraphml.LineType
import com.yworks.ygraphml.Node
import com.yworks.ygraphml.PolyLineEdge
import com.yworks.ygraphml.VerticalTextPosition
import com.yworks.ygraphml.YGraphMLFactory
import com.yworks.ygraphml.YGraphMLPackage
import java.util.List
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EFactory
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.FeatureMap
import org.eclipse.emf.ecore.xml.type.XMLTypePackage
import org.graphdrawing.graphml.Data
import org.graphdrawing.graphml.Key

import static extension de.dlr.aspect.graphml.util.GraphMLHelper.*
import static extension org.eclipse.emf.ecore.util.FeatureMapUtil.*
import com.yworks.ygraphml.NodeLabelModel
import com.yworks.ygraphml.NodeLabelPosition

class YGraphMLHelper {	
	val static final XML_TEXT = XMLTypePackage.eINSTANCE.getXMLTypeDocumentRoot_Text()
	val static final YGraphMLFactory yf = YGraphMLFactory.eINSTANCE
	
	def static findYNodes(Data d) {
		d.eAllContents.filter(Node).toIterable
	}
	
	def static findYEdge(Data d) {
		d.findType(Edge)
	}

	def static createYPolyLineEdge(Data d) {
		(d.createType(YGraphMLPackage.Literals.POLY_LINE_EDGE) as PolyLineEdge).init
	}
	
	def static createYGenericNode(Data d){
		(d.createType(YGraphMLPackage.Literals.GENERIC_NODE) as GenericNode).init
	}
	
	def static findOrCreateYNodes(Data d) {
		d.findYNodes() ?: #{d.createYGenericNode()}
	}
	
	def static findOrCreateYEdge(Data d) {
		d.findYEdge() ?: d.createYPolyLineEdge()
	}
	
	def static init(Node n) {
		n => [
			borderStyle = yf.createLineStyle => [
				type = LineType.LINE
				color = "#000000"
				width = 1
			]
			fill = yf.createFill => [
				transparent = false
				color = "#ffffff"
			]
			geometry = yf.createGeometry => [
				x = 0
				y = 0
				width = 100
				height = 100
			]
			nodeLabel.add(yf.createNodeLabel => [
				fontSize = 12 as short
				horizontalTextPosition = HorizontalTextPosition.RIGHT
				verticalTextPosition = VerticalTextPosition.CENTER
				alignment = Alignment.LEFT
				autoSizePolicy = AutoSizePolicy.NODE_WIDTH
				iconTextGap = 5 as byte
				visible = true
				topInset = 5
				bottomInset = 5
				leftInset = 10
				rightInset = 10
				modelName = NodeLabelModel.INTERNAL
				modelPosition = NodeLabelPosition.C
			])
		]
	}
	
	def static init(GenericNode n) {
		(n as Node).init
		n => [
			configuration = "DEFAULT"
		]
	}
		
	def static init(Edge e) {
		e => [
			path = yf.createPath => [
				sx = 0
				sy = 0
				tx = 0
				ty = 0
			]
			lineStyle = yf.createLineStyle => [
				type = LineType.LINE
				color = "#000000"
				width = 1
			]
			arrows = yf.createArrows => [
				source = ArrowType.NONE
				target = ArrowType.STANDARD
			]
			edgeLabel.add(yf.createEdgeLabel => [
				visible = true
			])
		]
	}
	
	def static init(PolyLineEdge e) {
		(e as Edge).init
		e => [
			bendStyle = yf.createBendStyle => [smoothed = false]
		]
	}
			
	def static getText(Label label) {
		if (label.hasText) {
			(label.mixed.get(XML_TEXT, true) as List<String>)?.head
		}
	}
	
	def static setText(Label label, String text) {
		label.mixed.addText(0, text ?: "");
	}
	
	def static <T> findType(Data d, Class<T> ^class) {
		d.any.map[value].filter(^class).head
	}
	
	def static findType(Data d, EClass eClass) {
		d.findType(eClass.instanceClass)
	}
	
	def static createType(Data d, EClass eClass) {
		eClass.newInstance => [
			val reference = eClass.EPackage.EClassifiers.filter(EClass).findFirst[name == "DocumentRoot"].EReferences.findFirst[EType == eClass]
			d.any.add(reference, it)
		]
	}
	
	def static <T extends EObject> addOne(FeatureMap any, T obj) {
		val reference = obj.eClass.EPackage.EClassifiers.filter(EClass).findFirst[name == "DocumentRoot"].EReferences.findFirst[EType == obj.eClass]
		any.add(reference, obj)
	}
	
	def static <T extends Label> getFirstVisible(List<T> label) {
		label.findFirst[visible]
	}
	
	def static getPremiseData(org.graphdrawing.graphml.Node n, Key k, Key nodegraphicsKey) {
		var value = n.data.findByKey(k)?.any?.CDATA?.head
		
		if (value == null) {
			val keyName = k?.attrName
			if (keyName != null) {
				val yNodes = n.data.findByKey(nodegraphicsKey)?.findYNodes
				
				val hiddenLabel = yNodes
					.map[nodeLabel?.findFirst[!visible && mixed.getText?.head.safeStartsWith(keyName + "=")]]
					.findFirst[it != null]
				
				if (hiddenLabel != null)  {
					value = hiddenLabel.mixed.getText.head?.replaceFirst(keyName + "=", "")
				}
			}
		}
		
		value
	}
	
	def static getPremiseData(org.graphdrawing.graphml.Edge e, Key k, Key edgegraphicsKey) {
		var value = e.data.findByKey(k)?.any?.CDATA.head
		
		if (value == null) {
			val yNode = e.data.findOrCreateByKey(edgegraphicsKey)?.findYEdge
			var hiddenLabel = yNode?.edgeLabel.findFirst[!visible && mixed.getText?.head.safeStartsWith(k.attrName + "=")]
			
			if (hiddenLabel != null) {
				value = hiddenLabel.mixed.getText.head.replaceFirst(k.attrName + "=", "")
			}
		}
		
		value
	}
	
	def static setPremiseData(org.graphdrawing.graphml.Node n, Key k, String value) {
		n.data.findOrCreateByKey(k).mixed.clear
		n.data.findOrCreateByKey(k).mixed.addCDATA(0, value)
	}
	
	def static setPremiseData(org.graphdrawing.graphml.Edge e, Key k, String value) {
		e.data.findOrCreateByKey(k).any.addCDATA(0, value)
	}
	
	def static setPremiseDataWithHiddenLabel(org.graphdrawing.graphml.Node n, Key k, Key nodegraphicsKey, String value) {
		n.setPremiseData(k, value)
		
		val nodegraphics = n.data.findOrCreateByKey(nodegraphicsKey)
		val yNodes = nodegraphics.findOrCreateYNodes
		
		yNodes.forEach[yNode | 
			var hiddenLabel = yNode.nodeLabel.findFirst[!visible && mixed.getText?.head.safeStartsWith(k.attrName + "=")]
			
			if (hiddenLabel == null) {
				hiddenLabel = yf.createNodeLabel => [
					fontSize = 0 as short
					visible = false
					yNode.nodeLabel.add(it)
				]
				yNode.nodeLabel.add(hiddenLabel)
			}
			
			hiddenLabel.mixed.addText(0, '''«k.attrName»=«value»''')
		]
	}
	
	def static setPremiseDataWithHiddenLabel(org.graphdrawing.graphml.Edge e, Key k, Key edgegraphicsKey, String value) {
		e.setPremiseData(k, value)
		
		val edgegraphics = e.data.findOrCreateByKey(edgegraphicsKey)
		val yEdge = edgegraphics.findOrCreateYEdge
		
		var hiddenLabel = yEdge.edgeLabel.findFirst[!visible && mixed.getText?.head.safeStartsWith(k.attrName + "=")]
		
		if (hiddenLabel == null) {
			hiddenLabel = yf.createEdgeLabel => [
				fontSize = 0 as short
				visible = false
			]
			yEdge.edgeLabel.add(hiddenLabel)
		}
		
		hiddenLabel.mixed.addText(0, '''«k.attrName»=«value»''');
	}
	
	/**
	 * Creates a new instance of an {@link EClass} via its {@link EFactory}
	 */
	def private static newInstance(EClass eClass) {
		eClass.EPackage.EFactoryInstance.create(eClass)
	}
	
	/**
	 * Null safe implementation of {@link String#startsWith}
	 */
	def private static safeStartsWith(String str, String start) {
		if (str == null) false else str.startsWith(start)
	}
}
	
	