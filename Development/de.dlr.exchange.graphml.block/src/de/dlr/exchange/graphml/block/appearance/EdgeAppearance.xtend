/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.graphml.block.appearance

import de.dlr.exchange.base.xtend.RepresentationHelper
import de.dlr.premise.graph.IEdge
import de.dlr.premise.representation.FontStyle
import de.dlr.premise.representation.StyleTypes
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.EqualsHashCode
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static extension de.dlr.exchange.graphml.block.PremiseExtensions.*

@Accessors
@EqualsHashCode
@FinalFieldsConstructor
class EdgeAppearance {
	def static create(extension RepresentationHelper repHelper, EdgeAppearance defaultAppearance, IEdge<?, ?> edge,
		EObject source) {
		val edgeStyle = edge.getStyle(source)
		val edgeColor = edge.getColor(source)
		val labelStyle = edge.getLabelStyle(source)

		new EdgeAppearance(
			edgeColor?.value ?: defaultAppearance.color,
			if(edgeStyle != null && edgeStyle.width != 0) edgeStyle.width else defaultAppearance.width,
			edgeStyle?.type ?: defaultAppearance.styleType,
			edge.bidirectional,
			if(labelStyle != null) labelStyle.visible else defaultAppearance.labelVisible,
			labelStyle?.fontStyle ?: defaultAppearance.fontStyle,
			if(labelStyle != null) labelStyle.underlined else defaultAppearance.labelUnderlined
		)
	}
	
	
	val String color
	val double width
	val StyleTypes styleType
	
	val boolean bidirectional
	
	val boolean labelVisible
	val FontStyle fontStyle
	val boolean labelUnderlined
	
	def getStyleTypeString() {
		return styleType.literal.toLowerCase
	}
	
	def getFontStyleString() {
		return fontStyle.literal.toLowerCase
	}
}
