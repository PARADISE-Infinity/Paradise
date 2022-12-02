/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.excel.trace.xtend

import com.smartxls.RangeStyle
import com.smartxls.WorkBook
import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.base.xtend.IGeneratorMyWithProgress
import de.dlr.exchange.base.xtend.RepresentationHelper
import de.dlr.premise.component.Satisfies
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ARepository
import de.dlr.premise.element.Connection
import de.dlr.premise.element.Relation
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.util.scope.ScopedEObjectFactory
import java.awt.Color
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.List
import java.util.Set
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.InMemoryFileSystemAccess
import org.eclipse.xtext.util.RuntimeIOException

import static extension de.dlr.premise.util.PremiseHelper.*
import de.dlr.premise.util.PremiseHelper
import de.dlr.premise.util.LabelHelper

abstract class AbstractGenerator implements IGeneratorMyWithProgress {

	private RepresentationHelper repHelper
	private EObject optRoot

	private static String indentationString = "   "
	private static String pathSeperator = "/"

	public static String OPT_DEPENDENCY_COUNT = "Calculate number of dependencies (on/off)"
	public static String OPT_DEPENDENCY_TYPES = "Types of dependencies to consider"
	public static String OPT_DEPENDENCY_TYPES_RELATIONS = "Relations (on/off)"
	public static String OPT_DEPENDENCY_TYPES_CONNECTIONS = "Connections (on/off)"
	public static String OPT_DEPENDENCY_TYPES_BALANCINGS = "Balancings (on/off)"
	public static String OPT_DEPENDENCY_TYPES_SATISFIES = "Satisfies (on/off)"
	public static String OPT_PARENT_NAMES = "Print parent names (all/none/one)"
	public static String OPT_INCLUDE_PARENTS = "Include parents in export (on/off)"
	public static String OPT_INCLUDE_COLOR = "Include colors in export (on/off)"
	public static String OPT_DEPTH_INFERENCE = "Infer depth of satisfying components (on/off)"

	public static String EXTENSION = ".xlsx"

	def protected abstract String getTitle();

	def void compile(IFileSystemAccess fsa, ResourceSet resSet, List<AElement> toprow, List<AElement> leftcol,
		String fileName, IProgressMonitor monitor) {
			
		monitor.beginTask("Exporting " + title, toprow.length)

		// get content
		val resSetRoots = resSet.resources.flatMap[contents].map [
			ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(it)
		].toList

		val relations = resSetRoots.iterator.flatMap[it.eAllContentsIncludingRoot].filter(Relation).
			toSet as Set<?> as Set<Relation>
		val connections = resSetRoots.iterator.flatMap[it.eAllContentsIncludingRoot].filter(Connection).
			toSet as Set<?> as Set<Connection<?>>
		val satisfies = resSetRoots.iterator.flatMap[it.eAllContentsIncludingRoot].filter(Satisfies).
			toSet as Set<?> as Set<Satisfies<?, ?>>
		val balancings = resSetRoots.iterator.flatMap[it.eAllContentsIncludingRoot].filter(Balancing).toSet

		// start writing
		val workbook = new WorkBook
		workbook.insertSheets(0, 2)

		workbook.sheet = 0
		workbook.setSheetName(0, 'Function')

		workbook.sheet = 1
		workbook.setSheetName(1, 'DSM')

				
		// print the title of the sheet
		workbook.setText(0, 0, title)
		workbook.setText(1, 0, fileName)
				
		var rangeStyle = workbook.rangeStyle
		workbook.setSelection(0, 0, 0, toprow.length + 1)
		rangeStyle.mergeCells = true
		rangeStyle.pattern = 1 as short
		rangeStyle.patternFG = Color.lightGray.RGB
		rangeStyle.fontSize = 20 * 20 // some weird SX thing. It's actualFontSize * 20
		rangeStyle.fontBold = true
		rangeStyle.horizontalAlignment = RangeStyle.HorizontalAlignmentCenter
		workbook.rangeStyle = rangeStyle
		workbook.setSelection(1, 0, 1, toprow.length + 1)
		rangeStyle.fontSize = 12 * 20
		workbook.rangeStyle = rangeStyle

		// print the top row and relations
		val minHierarchyLevel = toprow.minHierarchyLevel
		
		
		toprow.forEach [ top, i |
			if(monitor.isCanceled) return;
			setupCell(workbook, i, top, minHierarchyLevel)

			printRelations(leftcol, satisfies, top, workbook, i, relations, connections, balancings)

			grayOutUnneededCol(leftcol, workbook, i)

			monitor.worked(1)
		]

		if(monitor.isCanceled) return;
		
		// print the left column
		leftcol.forEach [ it, i |

			// set color
			if (OPT_INCLUDE_COLOR.option == "on") {		
				if (repHelper.getColor(it) != null) {
					workbook.setSelection(5 + i, 0, 5 + i, 0)
					workbook.setElementsColor(it)
				}				
			}

			var String prefix = ""
			if (OPT_INCLUDE_PARENTS.option == "on") {
				for (var j = 0; j < hierarchyLevel; j++) {
					prefix += indentationString
				}
			} else {
				switch (OPT_PARENT_NAMES.option) {
					case "all":
						prefix = allParents.reverseView.join("", pathSeperator, pathSeperator, [name])
					case "none":
						prefix = ""
					default:
						prefix = (allParents.head?.name ?: "") + pathSeperator
				}
			}
			
			// clean prefix
			var clean_prefix = LabelHelper.cleanName(prefix)
			clean_prefix = clean_prefix.replace(pathSeperator,'.').trim()

			// clean name
			var clean_name = name
			clean_name = LabelHelper.cleanName(name)
			clean_name = clean_name.trim()
			clean_name = LabelHelper.cleanQualifiedName(clean_name)

			workbook.setText(5 + i, 0, clean_prefix + clean_name)

			grayOutUnneededRow(toprow, workbook, i)
		]

		// final theming and sizing
		var rs = workbook.getRangeStyle(5, 2, leftcol.length + 4, toprow.length + 1)
		rs.bottomBorder = RangeStyle.BorderThick
		rs.leftBorder = RangeStyle.BorderThick
		rs.topBorder = RangeStyle.BorderThick
		rs.rightBorder = RangeStyle.BorderThick
		rs.horizontalAlignment = RangeStyle.HorizontalAlignmentCenter
		rs.fontBold = true
		workbook.setRangeStyle(rs, 5, 2, leftcol.length + 4, toprow.length + 1)
		workbook.setColWidthAutoSize(0, true)
		workbook.setSelection(1, 0, 1, 0)

		// Add the Sum function for each col and row
		var prefix = "COUNTA("
		if (OPT_DEPENDENCY_COUNT.option == "on") {
			prefix = "SUM("
		}
		for (var i = 2; i <= toprow.length + 1; i++) {
			workbook.setFormula(4, i, prefix + coordsToString(5, i) + ":" + coordsToString(leftcol.length + 4, i) + ")")
		}
		for (var i = 5; i <= leftcol.length + 4; i++) {
			workbook.setFormula(i, 1, prefix + coordsToString(i, 2) + ":" + coordsToString(i, toprow.length + 1) + ")")
		}

		if(monitor.isCanceled) return;
		
		// connection functions etc ...
		workbook.sheet = 0
		workbook.setText(0, 0, "Name")
		workbook.setText(0, 1, "Description")

		leftcol.forEach [ it, i |

			val offset = 1
	
			// set color
			if (OPT_INCLUDE_COLOR.option == "on") {		
				if (repHelper.getColor(it) != null) {
					workbook.setSelection(offset + i, 0, offset + i, 0)
					workbook.setElementsColor(it)
				}				
			}
		
			var String pre = ""
			if (OPT_INCLUDE_PARENTS.option == "on") {
				for (var j = 0; j < hierarchyLevel; j++) {
					pre += indentationString
				}
			} else {
				switch (OPT_PARENT_NAMES.option) {
					case "all":
						pre = allParents.reverseView.join("", pathSeperator, pathSeperator, [name])
					case "none":
						pre = ""
					default:
						pre = (allParents.head?.name ?: "") + pathSeperator
				}
			}
			
			// clean prefix
			var clean_prefix = LabelHelper.cleanName(pre)
			clean_prefix = clean_prefix.replace(pathSeperator,'.').trim()

			// clean name
			var clean_name = name
			clean_name = LabelHelper.cleanName(name)
			clean_name = clean_name.trim()
			clean_name = LabelHelper.cleanQualifiedName(clean_name)

			workbook.setText(offset + i, 0, clean_prefix + clean_name)
			workbook.setText(offset + i, 1, description)

			grayOutUnneededRow(toprow, workbook, i)
		]


		// connections
		workbook.sheet = 2
		workbook.setSheetName(2, 'Connections')
		
		workbook.setText(0, 0, 'Name')
		workbook.setText(0, 1, 'Source Name')
		workbook.setText(0, 2, 'Target Name')

		connections.forEach[ it, k |
			workbook.setText(1 + k, 0, name)

			var String sourceName = null
			if (sourcePointer != null) {
				if (sourcePointer.target != null) {
					sourceName = sourcePointer.target.name
				}
			}

			var String targetName = null
			if (targetPointer != null) {
				if (targetPointer.target != null) {
					targetName = targetPointer.target.name
				}
			}
			
			if (sourceName != null) { 			
				workbook.setText(1 + k, 1, LabelHelper.cleanName(sourceName))
			}
			if (targetName != null) {
				workbook.setText(1 + k, 2, LabelHelper.cleanName(targetName))
			}
		]
		
		// save the file
		try {
			var out = new ByteArrayOutputStream
			workbook.writeXLSX(out)
			
			var encoded_file = GeneratorHelper::encodeFileName(fileName)
			var inStream = new ByteArrayInputStream(out.toByteArray)
					
			if (fsa instanceof InMemoryFileSystemAccess) { // still needed, the GenerationHandler uses it
				fsa.generateFile(encoded_file + EXTENSION, inStream)
			} else if (fsa instanceof EclipseResourceFileSystemAccess2) {
				fsa.generateFile(encoded_file + EXTENSION, inStream)
			}
		} catch (RuntimeIOException e) {
			println("Could not write file: " + (e.cause as CoreException).status.exception.message)
		} catch (FileNotFoundException e) {
			println(e.message)
		}
	}

	def protected abstract void grayOutUnneededRow(List<AElement> toprow, WorkBook workbook, Integer i)

	def protected abstract void grayOutUnneededCol(List<AElement> leftcol, WorkBook workbook, Integer i)

	def protected abstract void printRelations(List<AElement> leftcol, Set<Satisfies<?, ?>> satisfies, AElement top,
		WorkBook workbook, Integer i, Set<Relation> relations, Set<Connection<?>> connections,
		Set<Balancing> balancings)

	private def void setupCell(WorkBook workbook, Integer i, AElement top, int minHierarchyLevel) {
		
		// set orientation vertical
		workbook.setSelection(3, i + 2, 3, i + 2)
		workbook.setRotation(90)

		// set color
		if (OPT_INCLUDE_COLOR.option == "on") {	
			if (repHelper.getColor(top) != null) {
				workbook.setElementsColor(top)
			}			
		}

		// set indentation/path postfix
		if (OPT_INCLUDE_PARENTS.option == "on") {
			var prefix = ""
			var indentation = top.hierarchyLevel - minHierarchyLevel
			for (var j = 0; j < indentation; j++) {
				prefix += indentationString
			}
			workbook.setText(3, i + 2, prefix + top.name)
		} else {

			var prefix =""
			switch (OPT_PARENT_NAMES.option) {
				case "all":
					prefix = top.allParents.reverseView.join("", pathSeperator, pathSeperator, [name])
				case "none":
					prefix = ""
				default:
					prefix = (top.allParents.head?.name ?: "") + pathSeperator
			}
			
			/*
			var String postfix = ""
			switch (OPT_PARENT_NAMES.option) {
				case "all":
					postfix = top.allParents.join(pathSeperator, pathSeperator, "", [name])
				case "none":
					postfix = ""
				default:
					postfix = pathSeperator + (top.allParents.head?.name ?: "")
			}*/
			
			var clean_name = LabelHelper.cleanName(top.name).trim()
			
			var clean_prefix = LabelHelper.cleanName(prefix).trim()
			clean_prefix = clean_prefix.replace(pathSeperator, '.')
			
			workbook.setText(3, i + 2, clean_prefix + clean_name)
		}
	}

	/**
	 * set the RepresentationHelper. Used by the TraceGenerator.
	 */
	def setRepHelper(RepresentationHelper repHelper) {
		this.repHelper = repHelper
	}

	/**
	 * get the RepresentationHelper. Used by the TraceGenerator.
	 */
	def getRepHelper() {
		return repHelper
	}

	/**
	 * marks the given range gray in the workbook
	 */
	protected def markUnneeded(WorkBook workbook, int r1, int c1, int r2, int c2) {
		workbook.setSelection(r1, c1, Math.max(r1, r2), Math.max(c1, c2))
		var rs = workbook.rangeStyle
		rs.pattern = RangeStyle.PatternSolid
		rs.patternFG = Color.decode("#dddddd").RGB
		workbook.rangeStyle = rs
	}

	/**
	 * set the color of the active cell to the color that represents the given AElement
	 */
	private def void setElementsColor(WorkBook workbook, AElement element) {
		try {
			var rs = workbook.rangeStyle
			rs.pattern = 1 as short
			rs.patternFG = Color.decode(repHelper.getColor(element).value).RGB
			workbook.rangeStyle = rs
		} catch (NumberFormatException e) {
			println('''Could not interpret as Color: «repHelper.getColor(element).value»''')
		}

	}

	/**
	 * set the rotation of the text in the active cell
	 */
	private def void setRotation(WorkBook workbook, int degrees) {
		var rs = workbook.rangeStyle
		rs.orientation = degrees as short
		workbook.setColWidth(workbook.selection.col1, 4 * 256)
		workbook.rangeStyle = rs

	}

	/**
	 * use this ARepository for {@link #getOption getOption()} calls
	 */
	def initOption(ARepository optRepository, String optRoot) {
		optRoot = PremiseHelper.getMetaData(optRepository, optRoot)
	}

	/**
	 * @return the value of the given option
	 */
	def getOption(String optName) {
		return GeneratorHelper.getMetaDataValue(optRoot, optName)
	}

	/**
	 * @return whether the given option is enabled
	 */
	def isEnabled(String optName) {
		return optName.option.matches("\\s*(yes|on|true)\\s*")
	}

	/**
	 * @return the level of the element in the hierarchy as in how many parents it has.
	 */
	private def getHierarchyLevel(AElement element) {
		element.allParents.length
	}

	/**
	 * @return the minimum level as returned by {@link #getHierarchyLevel getHierarchyLevel()}
	 */
	private def getMinHierarchyLevel(List<AElement> elements) {
		elements.map[hierarchyLevel].min
	}

	/**
	 * @return all parents of type AElement of the element in a sorted list
	 */
	private def getAllParents(AElement element) {
		return element.<EObject>closure[#[parent]].drop(1).filter(AElement).toList
	}

	protected def EObject getParent(EObject obj) {
		val directParent = obj.eContainer
		if (directParent instanceof ComponentReference) {
			return directParent.eContainer
		}
		return directParent
	}

	def private String coordsToString(int row, int col) {
		val char base = 'A'
		var c0 = (base + (((col / 26 - 1) / 26) - 1) % 26) as char
		var c1 = (base + (col / 26 - 1) % 26) as char
		var c2 = (base + col % 26) as char
		'''«IF c0 != '@'.charAt(0)»«c0»«ENDIF»«IF c1 != '@'.charAt(0)»«c1»«ENDIF»«c2»«row + 1»'''
	}
}
