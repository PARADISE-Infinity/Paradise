/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.requirements

import com.smartxls.WorkBook
import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.premise.component.Satisfies
import de.dlr.premise.element.AElement
import de.dlr.premise.element.Connection
import de.dlr.premise.element.Relation
import de.dlr.premise.functions.RangeConstraint
import de.dlr.premise.functions.RequiredParameter
import de.dlr.premise.functions.UseCase
import de.dlr.premise.functions.UseCaseRepository
import de.dlr.premise.graph.INode
import de.dlr.premise.registry.ADataItem
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.registry.IMetaTypable
import de.dlr.premise.registry.MetaData
import java.io.File
import java.io.FileNotFoundException
import java.io.OutputStream
import java.util.List
import java.util.Map
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Stream
import org.eclipse.core.resources.IFolder
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Platform
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet

import static de.dlr.premise.util.PremiseHelper.getGuardConditionString

import static extension de.dlr.premise.util.PremiseHelper.closure

class RequirementsTableGenerator {
	val static level = "Level"
	val static depth = "Depth"
	val static owner = "Owner"
	val static type = "Type"
	val static uuid = "UUID"
	val static name = "Name"
	val static valMin = "Value min"
	val static valMax = "Value max"
	val static unit = "Unit"
	val static conditions = "Conditions"
	val static parent = "Parent"
	val static parUUID = "Parent UUID"
	val static allocation = "Allocation"
	val static allocUUID = "Allocation UUID"
	val static inputs = "Inputs"
	val static inUUIDs = "Inputs UUIDs"
	val static outputs = "Outputs"
	val static outUUIDs = "Outputs UUIDs"
	val static description = "Description"
	val static hyperlinks = "Hyperlinks"
	val static rationale = "Rationale"
	val static verification = "Verification"
	val static modified = "Modified"
	val static header = #[
		#["Category", "", "", "", "", "", "", "", "", "", "Associations/Edges", "", "", "", "", "", "", "", "Text", "",
			"", "", "Change Management"],
		#[level, depth, owner, type, uuid, name, valMin, valMax, unit, conditions, parent, parUUID, allocation,
			allocUUID, inputs, inUUIDs, outputs, outUUIDs, description, hyperlinks, rationale, verification, modified]
	]

	public static val String PLUGIN_NAME = "de.dlr.exchange.excel.requirements"
	public static val String OPT_TEMPLATE_PATH = "Template Path (leave empty for default template)"
	static val String defaultTemplatePath = "template/requirements_template.xlsx"
	var WorkBook wb
	var int activeRow
	var Map<INode, List<INode>> solutions
	var Map<INode, List<INode>> inputRelations
	var Map<INode, List<INode>> outputRelations
	var Map<AElement, List<AElement>> inputConnections
	var Map<AElement, List<AElement>> outputConnections
	var Predicate<EObject> isVisible

	MetaData optRoot

	new(MetaData optRoot) {
		this.optRoot = optRoot
	}

	def void generateRequirementsTable(IFolder srcGenFolder, ResourceSet input, Predicate<EObject> isVisible,
		OutputStream out) {
		this.isVisible = isVisible
		initWorkbook(srcGenFolder)
		input.exportRequirements()
		wb.writeXLSX(out)
	}

	private def void initWorkbook(IFolder srcGenFolder) {
		wb = new WorkBook()
		readTemplate(srcGenFolder)
		setupHeader()
	}

	private def void readTemplate(IFolder srcGenFolder) {
		if (OPT_TEMPLATE_PATH.option.nullOrEmpty) {
			val templateURL = Platform.getBundle(PLUGIN_NAME).getEntry(defaultTemplatePath)
			val teplateFile = new File(FileLocator.toFileURL(templateURL).toURI)
			wb.readXLSX(teplateFile.absolutePath)
		} else {
			val templatePath = srcGenFolder.parent.fullPath.append(OPT_TEMPLATE_PATH.option).toOSString
			try {
				wb.readXLSX(templatePath)
			} catch (FileNotFoundException e) {
				println("Could not find template file: " + e.message)
			}
		}

	}

	private def void setupHeader() {
		for (activeRow = 0; activeRow < header.length; activeRow++) {
			for (var col = 0; col < header.get(activeRow).length; col++) {
				val text = header.get(activeRow).get(col)
				if (!text.nullOrEmpty)
					wb.setEntry(activeRow, col, text)
			}
		}
	}

	private def void exportRequirements(ResourceSet input) {
		solutions = merge(
			input.allContents.filter(Satisfies as Class<?> as Class<Satisfies<?, ?>>).filter [
				target instanceof UseCase || target instanceof RequiredParameter
			].groupBy[target as INode].mapValues[map[source]],
			input.allContents.filter(Relation).filter [
				(target instanceof UseCase || target instanceof RequiredParameter) && source instanceof EObject &&
					(source as EObject).closure[#[eContainer]].filter(IMetaTypable).map[metaTypes].flatten.exists [
						it.name == "ComponentSpec"
					]
			].groupBy[target].mapValues[map[source]]
		)
		val connections = input.allContents.filter(Connection as Class<?> as Class<Connection<?>>).filter [
			target instanceof UseCase
		].toList
		inputRelations = input.allContents.filter(Relation).filter [
			target instanceof RequiredParameter
		].groupBy[target].mapValues[it.map[source]]
		outputRelations = input.allContents.filter(Relation).filter [
			source instanceof RequiredParameter
		].groupBy[source].mapValues[it.map[target]]
		inputConnections = connections.groupBy[target].mapValues[it.map[source]]
		outputConnections = connections.groupBy[source].mapValues[it.map[target]]
		input.resources.map[contents.filter(UseCaseRepository)].flatten.map[usecases].flatten.forEach [
			iterateTree(0, 0, null, "")
		]
	}

	private def void iterateTree(ADataItem it, int inheritedLevel, int inheritedDepth, String inheritedOwner,
		String inheritedType) {
		val actualOwner = metaData.filter[it.name.equalsIgnoreCase(owner)].map[value].head ?: inheritedOwner
		val defaultType = if(it instanceof RequiredParameter) "Quantifier" else inheritedType
		val actualType = if(it instanceof IMetaTypable) metaTypes.head?.name ?: defaultType else defaultType
		val referencedSolutions = solutions.getOrDefault(it, #[]).filter(ANameItem).map[it.name].toList
		referencedSolutions.addAll(metaData.filter[it.name.equalsIgnoreCase("solution")].map[value])
		if (isVisible.test(it)) {
			it.modifyRow(inheritedDepth, actualOwner, actualType, inheritedLevel, referencedSolutions)
		}

		if (it instanceof UseCase) {
			val newLevel = if(referencedSolutions.nullOrEmpty) inheritedLevel else inheritedLevel + 1
			requiredParameters.forEach[iterateTree(inheritedLevel, inheritedDepth + 1, actualOwner, actualType)]
			children.forEach[iterateTree(newLevel, inheritedDepth + 1, actualOwner, actualType)]
		}
	}

	private def modifyRow(ADataItem it, int inheritedDepth, String actualOwner, String actualType, int inheritedLevel,
		List<String> referencedSolutions) {
		("" + inheritedDepth).writeTo(depth)
		actualOwner.writeTo(owner)
		actualType.writeTo(type)
		id.writeTo(uuid)
		if(!#{"", "Actor", "Activity"}.contains(actualType)) ("L" + inheritedLevel).writeTo(level)
		(inheritedDepth.indentation + it.name).writeTo(name)
		metaData.filter[it.name.equalsIgnoreCase("url")].map[value].writeTo(hyperlinks)
		val parentElem = eContainer
		if (parentElem instanceof UseCase) {
			parentElem.name?.writeTo(parent)
			parentElem.id?.writeTo(parUUID)
		}
		referencedSolutions.writeTo(allocation)
		solutions.getOrDefault(it, #[]).filter(ADataItem).map[id].writeTo(allocUUID)
		it.description?.writeTo(description)
		metaData.filter[it.name.equalsIgnoreCase(rationale)].map[value].writeTo(rationale)
		metaData.filter[it.name.equalsIgnoreCase(verification)].map[value].writeTo(verification)
		"no".writeTo(modified)
		if (it instanceof UseCase) {
			getGuardConditionString(condition)?.writeTo(conditions)
			inputConnections.getOrDefault(it, #[]).filter(ANameItem).map[it.name].writeTo(inputs)
			inputConnections.getOrDefault(it, #[]).filter(ADataItem).map[id].writeTo(inUUIDs)
			outputConnections.getOrDefault(it, #[]).filter(ANameItem).map[it.name].writeTo(outputs)
			outputConnections.getOrDefault(it, #[]).filter(ADataItem).map[id].writeTo(outUUIDs)
			colorBlue()
		}
		if (it instanceof RequiredParameter) {
			val constraint = valueConstraint
			if (constraint instanceof RangeConstraint) {
				constraint.lower?.writeTo(valMin)
				constraint.upper?.writeTo(valMax)
			}
			it.unit?.symbol?.writeTo(unit)
			inputRelations.getOrDefault(it, #[]).filter(ANameItem).map[it.name].writeTo(inputs)
			inputRelations.getOrDefault(it, #[]).filter(ADataItem).map[id].writeTo(inUUIDs)
			outputRelations.getOrDefault(it, #[]).filter(ANameItem).map[it.name].writeTo(outputs)
			outputRelations.getOrDefault(it, #[]).filter(ADataItem).map[id].writeTo(outUUIDs)
		}
		activeRow++
	}

	private def String indentation(int depth) {
		Stream.generate["   "].limit(depth).collect(Collectors.joining)
	}

	private def void writeTo(String value, String columnName) {
		if(value.nullOrEmpty) return;
		wb.setRowHeight(activeRow, Math.max(value.split("\n").length * 300, wb.getRowHeight(activeRow)))
		wb.setEntry(activeRow, columnName.col, value)
	}

	private def void writeTo(String[] values, String columnName) {
		values.join("\n").writeTo(columnName)
	}

	private def int col(String colName) {
		return header.get(1).indexOf(colName)
	}

	private def colorBlue() {
		val rs = wb.getRangeStyle(activeRow, 0, activeRow, modified.col)
		rs.fontColor = 0x000070c0
		wb.setRangeStyle(rs, activeRow, 0, activeRow, modified.col)
	}

	private def <T, R> Map<T, List<R>> merge(Map<T, List<R>> first, Map<T, List<R>> second) {
		val result = newHashMap()
		result.putAll(first)
		second.forEach[k, l|result.merge(k, l, [f, s|val res = newArrayList(); res.addAll(f); res.addAll(s); res])]
		return result
	}

	private def getOption(String optName) {
		return GeneratorHelper.getMetaDataValue(optRoot, optName)
	}
}
