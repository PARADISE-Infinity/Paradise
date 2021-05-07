/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.excel.tradeoff.xtend

import com.google.common.primitives.Doubles
import com.smartxls.WorkBook
import de.dlr.premise.functions.Optimum
import de.dlr.premise.functions.RequiredParameter
import de.dlr.premise.functions.UseCaseRepository
import de.dlr.premise.util.PremiseHelper
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.ArrayList
import java.util.List
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject
import de.dlr.premise.system.Parameter
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.registry.ANameItem
import de.dlr.premise.registry.Value

/**
 * Simplifies the process of writing the Data to the Template for this specific use case
 * @author enge_do
 */
class ExcelTradeOffEngine {

	val COORDS_USECASE = new ExcelCoordinates(1, 4, 0)
	val COORDS_USECASE_WEIGHT = new ExcelCoordinates(1, 4, 1)
	val COORDS_PREMISE = new ExcelCoordinates(1, 4, 2)
	val COORDS_SOLUTION_DATA = new ExcelCoordinates(1, 0, 2)
	val COORDS_SOLUTION_OVERVIEW = new ExcelCoordinates(0, 4, 1)
	val COORDS_RATING_DATA = new ExcelCoordinates(1, 1, 2)
	val COORDS_RATING_OVERVIEW = new ExcelCoordinates(0, 5, 1)

	val CALCULATION_SHEET = 2
	val EXCEL_MAX_ROW_COUNT = 65535

	var initialized = false
	var relative = true
	var WorkBook workbook
	var ProjectRepository reference
	var List<Pair<String, ProjectRepository>> solutions = new ArrayList<Pair<String, ProjectRepository>>
	var List<RequiredParameter> requiredParameters
	var List<Parameter> referenceParameters = new ArrayList<Parameter>
	var List<Pair<Parameter, RequiredParameter>> paramsWithRequiredParamaters = new ArrayList<Pair<Parameter, RequiredParameter>>

	/**
	 * The constructor
	 * 
	 * @param workbook the instance of the WorkBook to write to
	 */
	new(String templatePath, boolean relative) {
		this.relative = relative
		workbook = new WorkBook
		try {
			workbook.readXLSX(templatePath)
		} catch (RuntimeException e) {
			throw new FileNotFoundException(e.message)
		}
	}

	/**
	 * Initializes the engine with the required data
	 * 
	 * @param solutions an ArrayList containing the ProjectRepositorys of the solutions
	 * @param solutionNames the names of the solutions in the same order as the solutions
	 * @param reference the Reference-System needed for the absolute calculation
	 */
	def void init(List<ProjectRepository> solutions, List<String> solutionNames, ProjectRepository reference) {
		this.reference = reference
		PremiseHelper::getAll(this.reference, Parameter).forEach[referenceParameters.add(it as Parameter)]

		init(solutions, solutionNames)
	}

	/**
	 * Initializes the engine with the required data
	 * 
	 * @param solutions an ArrayList containing the ProjectRepositorys of the solutions
	 * @param solutionNames the names of the solutions in the same order as the solutions
	 */
	def void init(List<ProjectRepository> solutions, List<String> solutionNames) {
		if (solutions.length == solutionNames.length) {
			solutions.forEach [ s, i |
				this.solutions.add(solutionNames.get(i) -> s)
			]

			initialized = true
		}
	}

	/**
	 * fills the calculation-sheet to determine the ratings. Should be called after {@link #init init()} only
	 */
	def calculate() {
		if (!initialized) {
			return
		}

		//fill paramsWithRequiredParameters and requiredParameters
		solutions.forEach [
			PremiseHelper::getAll(value, Parameter).forEach [
				val param = it as Parameter
				param.satisfiesRequiredParameters.forEach [
					if (safeGetEvaluationWeightOrThrow() != 0d) {
						paramsWithRequiredParamaters.add(param -> it)
					}
				]
			]
		]

		requiredParameters = PremiseHelper::getAll(useCaseRepository, RequiredParameter).toArrayList

		//remove the ones that have evaluationWeight "0" or optimum NONE
		for (var iterator = requiredParameters.iterator; iterator.hasNext;) {
			var reqParam = iterator.next
			if (reqParam.safeGetEvaluationWeightOrThrow() == 0d || reqParam.optimum == Optimum.NONE ) {
				iterator.remove()
			}
		}
		paramsWithRequiredParamaters = paramsWithRequiredParamaters.filter[requiredParameters.contains(value)].toList
		if (paramsWithRequiredParamaters.empty) {
			return
		}

		//write the parameters into the calculation sheet
		paramsWithRequiredParamaters.forEach [ it, i |
			//solution name
			workbook.setText(CALCULATION_SHEET, i + 1, 0, key.solutionName)
			//parameter name
			workbook.setText(CALCULATION_SHEET, i + 1, 1, value.nameWithQualification)
			//evaluation weight
			var row = requiredParameters.indexOf(value) + COORDS_USECASE_WEIGHT.row
			var col = COORDS_USECASE_WEIGHT.col
			workbook.setFormula(CALCULATION_SHEET, i + 1, 2,
				'''«workbook.getSheetName(COORDS_USECASE_WEIGHT.sheet)»!«coordsToString(row, col)»''')
			//optimum
			workbook.setNumber(CALCULATION_SHEET, i + 1, 3, value.optimum.value)
			//reference
			if (relative) {
				switch (value.optimum) {
					case Optimum.HIGH:
						workbook.setFormula(CALCULATION_SHEET, i + 1, 4, '''G«i + 2»''')
					default:
						workbook.setFormula(CALCULATION_SHEET, i + 1, 4, '''F«i + 2»''')
				}
			} else {
				try {
					var s = referenceParameters.findFirst[param|param.satisfiesRequiredParameters.exists[rp|rp.id == value.id]].value.
						value
					var reference = Double.valueOf(s)
					workbook.setNumber(CALCULATION_SHEET, i + 1, 4, reference)
				} catch (NumberFormatException e) {
					println(
						'''Could not interpret the value of the reference-parameter for the RequiredParameter «value.
							name» as number''')
				} catch (NullPointerException e) {
					println('''No reference-parameter found for the RequiredParameter "«value.name»"''')
				}
			}
			//calculate lower
			if (relative) {
				var min = Doubles.tryParse(
					paramsWithRequiredParamaters.filter[value == paramsWithRequiredParamaters.get(i).value].
						minBy[Doubles.tryParse(key.value.value)].key.value.value
				)
				workbook.setNumber(CALCULATION_SHEET, i + 1, 5, min)
			} else {

				//reference - uncertainty
				try {
					var referenceParam = referenceParameters.findFirst[param|
						param.satisfiesRequiredParameters.exists[rp|rp.id == value.id]]
					var lower =  Double.parseDouble(referenceParam.value.value) as double
					try {						
						lower -= referenceParam.value.absoluteUncertainty
						workbook.setNumber(CALCULATION_SHEET, i + 1, 5, lower)
					} catch (NumberFormatException e) {
						println('''No uncertainty found for RequiredParameter "«value.name»"''')
					}
				} catch (NumberFormatException e) {
					println(
						'''Could not interpret as Number: «e.message» at reference-parameter "«referenceParameters.
							findFirst[param|param.satisfiesRequiredParameters.exists[rp|rp.id == value.id]].name»"''')
				}
			}
			//calculate upper
			if (relative) {
				var max = Doubles.tryParse(
					paramsWithRequiredParamaters.filter[value == paramsWithRequiredParamaters.get(i).value].
						maxBy[Doubles.tryParse(key.value.value)].key.value.value)
				workbook.setNumber(CALCULATION_SHEET, i + 1, 6, max)
			} else {

				//reference + uncertainty
				try {
					var referenceParam = referenceParameters.findFirst[param|
						param.satisfiesRequiredParameters.exists[rp|rp.id == value.id]]
					var upper = Double.parseDouble(referenceParam.value.value) as double
					upper += referenceParam.value.absoluteUncertainty
					workbook.setNumber(CALCULATION_SHEET, i + 1, 6, upper)
				} catch (NumberFormatException e) {
					//nothing to do here, message has been printed at lower already
				}
			}
			//value
			//TODO mode values
			try {
				workbook.setNumber(CALCULATION_SHEET, i + 1, 7, Double.valueOf(key.value.getValue))
			} catch (NullPointerException e) {
				//nothing to do here. The value of the Parameter simply is not set
			} catch (NumberFormatException e) {
				if (key.value.value != "") {
					throw new NumberFormatException(
						'''
						Could not interpret as number: Value = "«key.value.value»"
						At Parameter «key.name»''')
				}
			}
		]

		//copy the formula down (for rating)
		workbook.selection = '''«workbook.getSheetName(CALCULATION_SHEET)»!I2:«workbook.getSheetName(CALCULATION_SHEET)»!I«paramsWithRequiredParamaters.
			length + 1»'''
		workbook.editCopyDown
		workbook.selection = '''«workbook.getSheetName(CALCULATION_SHEET)»!A2'''
	}

	def private String coordsToString(int row, int col) {
		val char base = 'A'
		var c0 = (base + (((col / 26 - 1) / 26) - 1) % 26) as char
		var c1 = (base + (col / 26 - 1) % 26) as char
		var c2 = (base + col % 26) as char
		'''«IF c0 != '@'.charAt(0)»«c0»«ENDIF»«IF c1 != '@'.charAt(0)»«c1»«ENDIF»«c2»«row + 1»'''
	}

	def private String coordsToString(int row1, int col1, int row2, int col2) '''
	«coordsToString(row1, col1)»«IF row1 != row2 || col1 != col2»:«coordsToString(row2, col2)»«ENDIF»'''

	def private ArrayList<RequiredParameter> toArrayList(EList<EObject> in) {
		var out = new ArrayList<RequiredParameter>
		for (element : in) {
			out.add(element as RequiredParameter)
		}
		return out
	}

	/**
	 * fills the Data-Sheet and the Overview. Should be called after {@link #calculate calculate()}
	 */
	def fill() {
		for (constraint : requiredParameters) {

			//print the constraints and weights
			var row = requiredParameters.indexOf(constraint)
			var name = constraint.nameWithQualification
			var weight = constraint.safeGetEvaluationWeightOrThrow()
			workbook.setText(COORDS_USECASE.sheet, COORDS_USECASE.row + row, COORDS_USECASE.col, name)
			try {
				workbook.setNumber(COORDS_USECASE_WEIGHT.sheet, COORDS_USECASE_WEIGHT.row + row,
					COORDS_USECASE_WEIGHT.col, Double.valueOf(weight))
			} catch (Exception e) {
				workbook.setNumber(COORDS_USECASE_WEIGHT.sheet, COORDS_USECASE_WEIGHT.row + row,
					COORDS_USECASE_WEIGHT.col, 1)
			}

			//print the values of the solutions
			for (solution : solutions) {
				var indices = paramsWithRequiredParamaters.getAllIndicesWhere(constraint as RequiredParameter, solution)

				if (indices.toArray.length > 0) {
					workbook.setFormula(COORDS_PREMISE.sheet, COORDS_PREMISE.row + row,
						COORDS_PREMISE.col + solutions.indexOf(solution),
						'''
							«FOR i : indices BEFORE 'ROUND(SUM(' SEPARATOR ',' AFTER '), 2)'»«workbook.getSheetName(CALCULATION_SHEET)»!I«i + 2»«ENDFOR»
						''')
				}
			}
		}

		//write the solutionNames and rating to the top of the data sheet and the overview
		solutions.forEach [
			workbook.setText(COORDS_SOLUTION_DATA.sheet, COORDS_SOLUTION_DATA.row,
				COORDS_SOLUTION_DATA.col + solutions.indexOf(it), key)
			workbook.setFormula(COORDS_RATING_DATA.sheet, COORDS_RATING_DATA.row,
				COORDS_RATING_DATA.col + solutions.indexOf(it),
				'''
					ROUND(SUM(«coordsToString(COORDS_PREMISE.row, COORDS_RATING_DATA.col + solutions.indexOf(it), EXCEL_MAX_ROW_COUNT,
						COORDS_RATING_DATA.col + solutions.indexOf(it))»), 2)
				''')
			//overview
			workbook.setText(COORDS_SOLUTION_OVERVIEW.sheet, COORDS_SOLUTION_OVERVIEW.row,
				COORDS_SOLUTION_OVERVIEW.col + solutions.indexOf(it), key)
		]
		workbook.setFormula(COORDS_RATING_OVERVIEW.sheet, COORDS_RATING_OVERVIEW.row, COORDS_RATING_OVERVIEW.col,
			'''
				«workbook.getSheetName(COORDS_RATING_DATA.sheet)»!«coordsToString(COORDS_RATING_DATA.row,
					COORDS_RATING_DATA.col, COORDS_RATING_DATA.row, COORDS_RATING_DATA.col).toString»
			''')
		workbook.sheet = COORDS_RATING_OVERVIEW.sheet
		workbook.setSelection(COORDS_RATING_OVERVIEW.row, COORDS_RATING_OVERVIEW.col, COORDS_RATING_OVERVIEW.row,
			COORDS_RATING_OVERVIEW.col + solutions.length - 1)
		workbook.editCopyRight
		workbook.setActiveCell(0, 0)
	}

	/**
	 * @param params
	 * @param constraint the RequiredParameter
	 * @param solution the solution
	 * @return the indices of parameters satisfying the given constraint and being part of the given solution
	 */
	def getAllIndicesWhere(List<Pair<Parameter, RequiredParameter>> params, RequiredParameter constraint,
		Pair<String, ProjectRepository> solution) {
		var List<Integer> parameters = new ArrayList<Integer>
		for (i : 0 ..< params.length) {
			var param = params.get(i)
			try {
				if (param.key.solutionName == solution.key && (param.value == constraint || constraint == null)) {
					parameters.add(i)
				}
			} catch (NullPointerException e) {
				if (param.value == constraint) {
					parameters.add(i)
				}
			}
		}
		return parameters
	}

	/**
	 * @return the UseCaseRepository of the first RequiredParameter
	 */
	def getUseCaseRepository() throws NoUseCaseFoundException{
		try {
			var root = PremiseHelper::getRoot(paramsWithRequiredParamaters.get(0).value)
			if (root instanceof UseCaseRepository) {
				return root as UseCaseRepository
			} else {
				throw new NoUseCaseFoundException("The referenced .usecase file could not be found")
			}
		} catch (IndexOutOfBoundsException e) {
			var ex = new NoUseCaseFoundException("There are no RequiredParameter's to evaluate")
			ex.initCause(e)
			throw ex
		}
	}

	/**
	 * @param param a Parameter
	 * @return the name of the solution the Parameter is part of
	 */
	def getSolutionName(Parameter param) {
		var repo = PremiseHelper::getRoot(param) as ProjectRepository
		for (it : solutions) {
			if (value == repo) {
				return key
			}
		}
		return ""
	}

	def getAbsoluteUncertainty(Value v) throws NumberFormatException {
		var u = v.uncertainty ?: ""
		var double r
		if (u.endsWith("%")) {
			u = u.replace('%', '')
			r = Double.parseDouble(u) * Double.parseDouble(v.value) / 100
		} else {
			r = Double.parseDouble(u)
		}
		return r
	}

	def write() {
		var out = new ByteArrayOutputStream
		workbook.writeXLSX(out)
		return out
	}
	
	def private getNameWithQualification(ANameItem item) {
		PremiseHelper.getQualifyingNamePrefix(item) + item.name
	}
	
	def private safeGetEvaluationWeightOrThrow(RequiredParameter it) {
		if (evaluationWeight?.trim().nullOrEmpty) {
			return 1.0
		}
		try {
				Double.valueOf(evaluationWeight)
		} catch (NumberFormatException e) {
			throw new NumberFormatException(
				'''
					Could not interpret as number: Evaluation Weight = "«evaluationWeight»"
					At RequiredParameter «name»
				''')
		}
	}

}

/**
 * Thrown to indicate that no (valid) UseCase file could be found
 */
class NoUseCaseFoundException extends RuntimeException {

	new(String message) {
		super(message)
	}

}
