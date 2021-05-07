/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.calculation

import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import de.dlr.calc.engine.dsl.BalancingInterpreter
import de.dlr.calc.engine.dsl.BalancingScopeFactory
import de.dlr.premise.common.command.AssociatedCommandsCommandStack
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.states.data.State
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.ModeValueRef
import de.dlr.premise.system.Parameter
import de.dlr.premise.util.PremiseHelper
import java.util.Comparator
import java.util.Optional
import java.util.Set
import java.util.function.Predicate
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.emf.common.util.ECollections
import org.eclipse.emf.edit.command.ChangeCommand
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain
import org.eclipse.swt.widgets.Display

import static extension com.google.common.collect.Maps.toMap
import static extension de.dlr.premise.states.StateHelper.*
import static extension de.dlr.premise.util.PremiseHelper.flatMap

class BalancingCalculator implements IBalancingCalculator {
	
	val static final MODE_VALUE_REF_COMPARATOR = Comparator.<ModeValueRef, Integer>comparing[modes.size].thenComparing[mv1, mv2 | 
		val s1 = mv1.state
		val s2 = mv2.state
		val stateMachines = Sets.union(s1.modeByStateMachine.keySet, s2.modeByStateMachine.keySet).toList.sortBy[name]
		for (sm : stateMachines) {
			val m1 = s1.modeByStateMachine.get(sm)
			val m2 = s2.modeByStateMachine.get(sm)
			if (m1 == null) {
				return 1
			}
			if (m2 == null) {
				return -1
			}
			val modeIndexComparison = sm.modes.indexOf(m1) - sm.modes.indexOf(m2) 
			if (modeIndexComparison != 0) {
				return modeIndexComparison
			}
		}
		return 0
	]
	
	
	val private static MUTEX_RULE_CALCULATION = new MutexRule()
	
	val private interpreter = BalancingInterpreter.getNewInstance();
	val private scopeFactory = new BalancingScopeFactory();
	
	
	
	/**
	 * @param filterStatesBy A set of states which restricts which outputs are calculated
	 */
	override calculate(Balancing balancing, Predicate<State> targetStatePredicate, boolean sync) {
		
		val commandStack = AdapterFactoryEditingDomain.getEditingDomainFor(balancing).commandStack as AssociatedCommandsCommandStack		
		if (commandStack.undoing || commandStack.redoing) {
			return
		}
		
		val command = commandStack.commandInExecution
		
		if (command == null) {
			System.err.println('''No causing command while calculating «balancing»''')
		}
		
		run(balancing, sync)[
			val calculationCommand = createCalculationCommand(balancing, targetStatePredicate)
			Display.^default.syncExec[
				if (command == null) {
					commandStack.execute(calculationCommand)
				} else {
					commandStack.executeAssociatedCommand(command, calculationCommand)
				}
			]
		]

	}
	
	def createCalculationCommand(Balancing balancing, Predicate<State> targetStatePredicate) {

			
		if (balancing.function == null || balancing.function.length == 0) {
			return null
		}
		
		val scope = scopeFactory.createCalculationScope(balancing)
		
		if (scope.needsPrepare()) {
			Display.getDefault().syncExec[
				scope.prepareModel()
			]
		}
					
		if (scope.needsCalculation()) {
			val targetParameter = scope.targetParameter
			if (targetParameter == null) {
				System.err.println('''Calculation omitted: Target of Balancing '«balancing.name»' is not valid!''')
				return null
			}
			
			val sourceParameters = scope.sourceNames.map[scope.getSourceParameter(it)].filterNull.toSet


			val Set<State> states = newHashSet
			if (targetStatePredicate == null) {
				states.addAll(sourceParameters.validTargetStates)
				states.add(State.EMPTY_STATE)
			} else {
				states.addAll(targetParameter.statesFromModeValues)
				states.add(State.EMPTY_STATE) 
				states.removeIf[state | !targetStatePredicate.test(state)]
			}
			
			val results = states
				.toMap[state | 
					Optional.ofNullable(interpreter.calculate(balancing, scope, state))
				]
				.filter[k, v | v.present]
				.mapValues[get]

			return new ChangeCommand(targetParameter.eResource.resourceSet) {
				override protected doExecute() {
					results.forEach[state, calcResult |
						targetParameter.setValue(state, calcResult)
					]
	
					if (targetParameter instanceof Parameter) {
						if (targetStatePredicate == null) {
							targetParameter.modeValues.removeIf[!states.contains(state)]
						}
						ECollections.sort(targetParameter.modeValues, MODE_VALUE_REF_COMPARATOR)
					}
				}
			}
		}
	}
	
	def dispatch getStatesFromModeValues(Parameter parameter) {
		parameter.modeValues.map[state].toSet
	}
	
	def dispatch getStatesFromModeValues(AParameterDef notParameter) {
		ImmutableSet.of()
	}
	
	/**
	 * Gets all states a target needs to have for a set of source parameters
	 */
	def getValidTargetStates(Set<AParameterDef> parameterDefs) {
		if (parameterDefs.empty) {
			return #{State.EMPTY_STATE}
		}
		val possibleTargetStates = parameterDefs.possibleTargetStates
		val sca = parameterDefs.iterator.next.eResource.resourceSet.findOrCreateStateCheckerAdapter
		
		return possibleTargetStates.filter[sca.isValid(it)]
	}
	
	/**
	 * Gets all states a target might have for a set of source parameters
	 */
	private def getPossibleTargetStates(Set<AParameterDef> parameterDefs) {
		val parameterStateSets = parameterDefs.map[states].toList
		return Sets.cartesianProduct(parameterStateSets).map[State.createNewStateByCombiningOrNull(it)].filterNull.toSet
	}
	
	private def dispatch getStates(Parameter parameter) {
		val parameterStates = parameter.modeValues.map[state].filterNull.toSet
		if (parameterStates.empty) {
			return #{State.EMPTY_STATE}
		}
		if (!parameterStates.complete) {
			parameterStates.add(State.EMPTY_STATE)
		}
		return parameterStates
		
	}
	
	private def dispatch getStates(AParameterDef notParameter) {
		return #{State.EMPTY_STATE}
	}
	
	/**
	 * Checks whether a state set is complete, that is it contains a matching state for all possible complete states of the model 
	 */
	private def isComplete(Set<State> states) {
		val stateMachines = states.flatMap[modeByStateMachine.keySet].toSet
		val allStateMachineStates = Sets.cartesianProduct(stateMachines.map[modes.toSet].toList).map[new State(it)].toSet
		return allStateMachineStates.forall[state | states.exists[isCompatibleSubstateOf(state)]]
	}
	
	private def run(Balancing balancing, boolean sync, () => void fn)  {
		if (sync) {
			fn.apply()
		} else {
			val calculationJob = new Job("Calculating: " + PremiseHelper.getMeaningfulName(balancing)) {
				override protected run(IProgressMonitor monitor) {
					fn.apply()
					return Status.OK_STATUS
				}
			}
			calculationJob.rule = MUTEX_RULE_CALCULATION
			calculationJob.schedule()
		}
	}
}