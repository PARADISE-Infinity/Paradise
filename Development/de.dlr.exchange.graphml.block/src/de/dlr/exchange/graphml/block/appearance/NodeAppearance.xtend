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
import de.dlr.premise.graph.INode
import de.dlr.premise.representation.FontStyle
import de.dlr.premise.representation.StyleTypes
import java.util.Optional
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.EqualsHashCode
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

@Accessors
@EqualsHashCode
@FinalFieldsConstructor
class NodeAppearance {
	def static create(extension RepresentationHelper repHelper, NodeAppearance defaultAppearance, INode node) {
		val color = node.color
		val lineStyle = node.style
		val coordinate = node.coordinate
		val shape = node.shape
		val labelStyle = node.labelStyle
		
		new NodeAppearance(
			color?.value ?: defaultAppearance.color,
			Optional.ofNullable(lineStyle).map[it.width].orElse(defaultAppearance.lineWidth),
			lineStyle?.type ?: defaultAppearance.styleType,
			Optional.ofNullable(coordinate).map[it.x * 150].orElse(defaultAppearance.x),
			Optional.ofNullable(coordinate).map[it.y * 80].orElse(defaultAppearance.y),
			Optional.ofNullable(shape).map[it.width].filter[it > 0].orElse(defaultAppearance.width),
			Optional.ofNullable(shape).map[it.height].filter[it > 0].orElse(defaultAppearance.height),
			Optional.ofNullable(labelStyle).map[visible].orElse(true),
			labelStyle?.fontStyle ?: defaultAppearance.fontStyle,
			Optional.ofNullable(labelStyle).map[underlined].orElse(defaultAppearance.labelUnderlined)
		)
	}
	
	
	val String color
	val double lineWidth
	val StyleTypes styleType
	
	val double x
	val double y
	val double width
	val double height
	
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