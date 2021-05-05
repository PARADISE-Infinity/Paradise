/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.states

import com.google.common.collect.ImmutableSet
import de.dlr.premise.element.AGuardCondition
import de.dlr.premise.element.Mode
import de.dlr.premise.element.StateMachine
import de.dlr.premise.element.Transition
import de.dlr.premise.element.TransitionActivationBehavior
import de.dlr.premise.states.data.ModeReachabilityHolder
import de.dlr.premise.states.data.State
import de.dlr.premise.states.data.StateGraph
import de.dlr.premise.states.reachability.CacheUsingReachabilityCheckerDelegate
import de.dlr.premise.states.reachability.StateReachabilityChecker
import de.dlr.premise.states.reachability.mode.ModeReachabilityChecker
import de.dlr.premise.states.stability.StableSubstatesGenerator
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.util.scope.ScopedEObjectFactory
import java.util.ArrayDeque
import java.util.LinkedHashSet
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.emf.common.notify.Notification
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EContentAdapter
import org.eclipse.xtend.lib.annotations.Accessors

import static de.dlr.premise.component.ComponentPackage.Literals.*
import static de.dlr.premise.element.ElementPackage.Literals.*
import static de.dlr.premise.system.SystemPackage.Literals.*
import static de.dlr.premise.functions.UseCasePackage.Literals.*

import static extension com.google.common.collect.Maps.*
import static extension de.dlr.premise.states.util.StateCheckingHelper.*
import static extension de.dlr.premise.util.PremiseHelper.*
import static extension de.dlr.premise.util.scope.ScopedEObjectHelper.*

class StateCheckerAdapter extends EContentAdapter {
	val static relevantFeatures = #{
		AELEMENT__STATEMACHINES, 
		STATE_MACHINE__MODES, 
		STATE_MACHINE__TRANSITIONS,
		MODE__ENTRY_MODE,
		TRANSITION__SOURCE_POINTER,
		TRANSITION__TARGET_POINTER,
		ICONDITIONAL__CONDITION,
		TRANSITION__BEHAVIOR,
		GUARD_COMBINATION__CHILDREN,
		GUARD_COMBINATION__JUNCTION,
		MODE_GUARD__MODE_POINTER,
		
		PROJECT_REPOSITORY__PROJECTS,
		SYSTEM_COMPONENT__CHILDREN,
		COMPONENT_REFERENCE__ACTIVE_IMPLEMENTATION,
		COMPONENT_REFERENCE__COMPONENT_DEFINITION,
		
		USE_CASE_REPOSITORY__USECASES,
		USE_CASE__CHILDREN,
		
		COMPONENT_REPOSITORY__COMPONENTS
	}
	
	val ResourceSet root

	@Accessors val StateGraph graph = new StateGraph	
	val ModeReachabilityHolder modeReachabillityHolder = new ModeReachabilityHolder
	
	val ModeReachabilityChecker modeReachabilityChecker = new ModeReachabilityChecker(modeReachabillityHolder)
	
	val StateReachabilityChecker reachabilityChecker = new StateReachabilityChecker(new CacheUsingReachabilityCheckerDelegate(graph, modeReachabilityChecker))
	@Accessors val StableSubstatesGenerator stableSubstatesGenerator = new StableSubstatesGenerator()
	
	var isProcessingNotification = false
	val isProcessingNotificationMonitor = new Object
	
	new(ResourceSet root) {
		this.root = root
		
		// we want to get notifications first so we are already up to date when validation and calculation happens
		root.eAdapters.add(0, this)
	}
	
	def isValid(State state) {
		// Wait until we are done processing notifications
		synchronized(isProcessingNotificationMonitor) {
			while (isProcessingNotification) {
				isProcessingNotificationMonitor.wait()
			}
		}
		
		val stableSubstates = stableSubstatesGenerator.getStableSubstates(state)
		val result = stableSubstates.exists[reachabilityChecker.isReachable(it)]
	
		return result
	}
	
	def isModeReachable(Mode mode) {	
		// Wait until we are done processing notifications
		synchronized(isProcessingNotificationMonitor) {
			while (isProcessingNotification) {
				isProcessingNotificationMonitor.wait()
			}
		}
		
		return modeReachabilityChecker.isReachable(mode)
	}

	
	def getAllValidStates(IProgressMonitor monitor) {
		val subMonitor = SubMonitor.convert(monitor, 100)
		subMonitor.subTask("Preparing")
		
		val stateMachines = ScopedEObjectFactory.INSTANCE.createAllScopedEObjectsByType(root, StateMachine)
		val stateMachineModes = stateMachines.toMap[modes].filterValues[exists[entryMode]]
		val maxStateCount = stateMachineModes.values.map[size].reduce[s1, s2 | s1 * s2]
		val initialState = new State(stateMachineModes.values.map[findFirst[entryMode]].filterNull)
		
		subMonitor.worked(1)
		subMonitor.workRemaining = maxStateCount
		subMonitor.subTask('''Searching for valid states of «maxStateCount» total states''')
		
		val seenStates = new LinkedHashSet()
		val validStates = new LinkedHashSet()
		val stateQueue = new ArrayDeque()
		
		seenStates.add(initialState)
		stateQueue.add(initialState)
		
		while (!stateQueue.empty) {
			subMonitor.worked(1)
			if (subMonitor.canceled) {
				return null
			}
			val currentState = stateQueue.poll
			val transitions = currentState.modes.flatMap[outgoingTransitions]
			
			var stateStable = true
			for (transition : transitions) {
				if (
					transition.condition == null 
					|| transition.condition.possibleStates.exists[State.createNewStateByCombiningOrNull(currentState, it) != null]
				) {
					if (subMonitor.canceled) {
						return null
					}					
					val targetState = State.createNewStateByReplacing(currentState, new State(transition.target))
					// see de.dlr.premise.states.stability.StableSubstatesGenerator.getStableStubstatesForTransition(State, Transition)
					val transitionStable = targetState == currentState 
						|| transition.behavior == TransitionActivationBehavior.EXTERNAL 
						|| (transition.condition == null && transition.behavior != TransitionActivationBehavior.IMMEDIATE)
					graph.add(currentState, targetState, transition)
					if (!seenStates.contains(targetState)) {
						seenStates.add(targetState)
						stateQueue.add(targetState)
					}
					stateStable = stateStable && transitionStable
				}
			}
			
			if (stateStable) {
				validStates.add(currentState)
			}
		}
		
		graph.markStateAndSuccessorsReachable(initialState)

		return validStates
	}
	
	override notifyChanged(Notification notification) {	
		super.notifyChanged(notification);

		if (notification.touch || notification.eventType == Notification.MOVE || notification.eventType >= Notification.EVENT_TYPE_COUNT) {
			return
		}
				
		if (StateCheckerAdapter.relevantFeatures.contains(notification.feature)) {
			synchronized (isProcessingNotificationMonitor) {
				isProcessingNotification = true
				
				val updateJob = new Job("Updating system states") {
					override protected run(IProgressMonitor monitor) {
						try {
							notification.targetStateMachines.forEach[
								graph.removeStateMachine(it)	
								modeReachabillityHolder.removeStateMachine(it)
							]
						} finally {
							isProcessingNotification = false
							synchronized(isProcessingNotificationMonitor) {
								isProcessingNotificationMonitor.notifyAll
							}
						}
						return Status.OK_STATUS
					}
				}	
				updateJob.schedule()
				
			}		
		}
		
	}
	
	private def getTargetStateMachines(Notification notification) {
		val fromNotifier = notification.notifier.affectedStateMachines 
		if (!fromNotifier.empty) {
			return fromNotifier
		}
		
		val fromOldValue = notification.oldValue.asIterable.flatMap[affectedStateMachines]
		if (!fromOldValue.empty) {
			return fromOldValue
		}
		
		if (!(notification.notifier instanceof EObject && notification.oldValue instanceof EObject)) {
			return #[]
		}
		
		val notifierScoped = ScopedEObjectFactory.INSTANCE.createAllScopedEObjects(notification.notifier as EObject)
		val oldValueScoped = ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(notification.oldValue as EObject)
		
		return notifierScoped.iterator.flatMap[root | oldValueScoped.eAllContents.filter(StateMachine).map[sm |
			val scope = ImmutableSet.builder.addAll(root.childrenScope).addAll(sm.scope).build			
			ScopedEObjectFactory.INSTANCE.createScopedEObjectUnchecked(sm.wrappedElement, scope)
		]].toList
	}
	
	private def dispatch asIterable(Void nothing) {
		#[]
	}
	
	private def dispatch asIterable(Iterable<?> iter) {
		iter as Iterable<Object>
	}

	def private dispatch asIterable(Object obj) {
		#[obj]
	}
	
	def private dispatch Iterable<StateMachine> getAffectedStateMachines(Object obj) {
		#[]
	}
	
	def private dispatch Iterable<StateMachine> getAffectedStateMachines(Void v) {
		#[]
	}
	
	def private dispatch Iterable<StateMachine> getAffectedStateMachines(StateMachine sm) {
		#[sm]
	}
	
	def private dispatch Iterable<StateMachine> getAffectedStateMachines(Mode mode) {
		#[mode.stateMachine]
	}
	
	def private dispatch Iterable<StateMachine> getAffectedStateMachines(Transition trans) {
		#[trans.stateMachine]
	}
	
	def private dispatch Iterable<StateMachine> getAffectedStateMachines(AGuardCondition gc) {
		gc.eContainer.affectedStateMachines
	}
	
	def private static dispatch childrenScope(EObject obj) {
		obj.scope
	}
	
	def private static dispatch childrenScope(ComponentReference compRef) {
		ImmutableSet.builder.addAll(compRef.scope).add(compRef.wrappedElement).build
	}

}