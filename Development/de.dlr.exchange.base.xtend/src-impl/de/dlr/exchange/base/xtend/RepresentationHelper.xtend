/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.base.xtend

import de.dlr.premise.graph.IEdge
import de.dlr.premise.query.QueryInterpreter
import de.dlr.premise.query.QueryInterpreterFactory
import de.dlr.premise.query.exceptions.ParserException
import de.dlr.premise.representation.ADecorator
import de.dlr.premise.representation.Color
import de.dlr.premise.representation.Coordinate
import de.dlr.premise.representation.LabelStyle
import de.dlr.premise.representation.LineStyle
import de.dlr.premise.representation.NodeShape
import de.dlr.premise.representation.Representation
import de.dlr.premise.representation.Selector
import de.dlr.premise.system.presentation.my.EditorHelper
import de.dlr.premise.system.util.ConditionLock
import java.util.Collection
import java.util.Map
import java.util.Set
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.function.Consumer
import org.eclipse.emf.common.notify.Notification
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EContentAdapter
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.ui.IEditorPart
import org.eclipse.ui.IPartListener2
import org.eclipse.ui.IWorkbenchPartReference

import static extension de.dlr.exchange.base.xtend.ui.EcoreUtilMy.copyAll
import static extension de.dlr.premise.util.PremiseHelper.*

class RepresentationHelper extends EContentAdapter {

	private static val Map<ResourceSet, RepresentationHelper> instances = newHashMap()

	val ResourceSet resSet
	val extension QueryInterpreter queryInterpreter
	val Thread queryJob
	val BlockingQueue<Object> queue = new ArrayBlockingQueue(1)
	val Set<Consumer<EObject>> observers = newHashSet()
	val areQueryResultsUpToDate = new ConditionLock(false)

	/** Element -> Pairs of SourceFilterElements (optional) -> Decorators */
	var Map<EObject, Collection<Pair<Collection<EObject>, Collection<ADecorator>>>> decorators = null
	var Map<EObject, Collection<Pair<Collection<EObject>, Collection<ADecorator>>>> previousDecorators = null
	var Set<EObject> toNotifyAbout = newHashSet()
	val Map<Selector, Collection<EObject>> queryResults = newHashMap()

	static synchronized def getInstance(ResourceSet resSet) {
		if (!instances.containsKey(resSet)) {
			EditorHelper.getPage()?.addPartListener(new PartClosedListener());
			instances.put(resSet, new RepresentationHelper(resSet))
		}
		return instances.get(resSet)
	}

	def dispose() {
		queryJob.interrupt()
		instances.remove(resSet)
	}

	static def dispose(ResourceSet resSet) {
		instances.get(resSet)?.dispose()
	}

	private new(ResourceSet resSet) {
		this.resSet = resSet
		queryInterpreter = QueryInterpreterFactory.createQueryInterpreter(resSet)
		resSet.eAdapters.add(this)
		queryJob = new Thread [
			while (true) {
				try {
					queue.take()
					decorators = newHashMap()
					queryResults.clear
					doQueryDecorators()
					areQueryResultsUpToDate.condition = true
					toNotifyAbout.forEach[notifyObservers]
					previousDecorators = decorators?.deepEcoreCopy
				} catch (InterruptedException e) {
					return
				} catch (Exception e) {
					// ignore
				}
			}
		]
		queryJob.start
		queryDecorators()
	}

	def Map<EObject, Collection<Pair<Collection<EObject>, Collection<ADecorator>>>> deepEcoreCopy(
		Map<EObject, Collection<Pair<Collection<EObject>, Collection<ADecorator>>>> map) {
		val newMap = newLinkedHashMap()
		map.forEach [ k, v |
			newMap.put(k, v.map[key -> value.copyAll].toSet)
		]
		return newMap
	}

	def addObserver(Consumer<EObject> observer) {
		observers.add(observer)
	}

	def private void notifyObservers(EObject element) {
		observers.immutableCopy.forEach[accept(element)]
	}

	override notifyChanged(Notification notification) {
		super.notifyChanged(notification)
		queryDecorators()
	}

	def private void queryDecorators() {
		areQueryResultsUpToDate.condition = false
		queue.offer(new Object())
	}

	def private void doQueryDecorators() {
		val selectors = resSet.resources.flatMap[contents].filter(Representation).flatMap[selectors].toList.
			unmodifiableView

		selectors.forEach [ selector |
			if(selector.query.nullOrEmpty) return
			val results = try {
					#[selector as EObject].query(selector.query)
				} catch (ParserException e) {
					println(e.cause.message)
					#[]
				}
			results.forEach [ element |
				var Collection<EObject> sourceSelector
				if (!selector.sourceSelector.nullOrEmpty && element instanceof IEdge<?, ?>) {
					sourceSelector = try {
						#[element].query(selector.sourceSelector)
					} catch (ParserException e) {
						println(e)
						null
					}
				}
				decorators.assoc(element, sourceSelector -> selector.decorators)
			]
			toNotifyAbout = (if (previousDecorators != null) {
				decorators.filter [ element, decorators |
					!(previousDecorators.containsKey(element) &&
						decoratorsEqual(previousDecorators.get(element), decorators))
				].keySet
			}) ?: newHashSet()
			queryResults.put(selector, results)
		]
	}

	def private void assoc(Map<EObject, Collection<Pair<Collection<EObject>, Collection<ADecorator>>>> map, EObject key,
		Pair<Collection<EObject>, Collection<ADecorator>> pair) {
		if (!map.containsKey(key)) {
			map.put(key, newArrayList(pair))
		} else {
			map.get(key).add(pair)
		}
	}

	def private boolean decoratorsEqual(Collection<Pair<Collection<EObject>, Collection<ADecorator>>> a,
		Collection<Pair<Collection<EObject>, Collection<ADecorator>>> b) {
		if (a.map[key].toList != b.map[key].toList) {
			return false
		}
		return new EcoreUtil.EqualityHelper().equals(a.flatMap[value].filter(EObject).toList,
			b.flatMap[value].filter(EObject).toList)
	}

	def Color getColor(EObject obj) {
		getDecorator(obj, Color)
	}

	def Color getColor(EObject obj, EObject source) {
		getDecorator(obj, source, Color)
	}

	def NodeShape getShape(EObject obj) {
		getDecorator(obj, NodeShape)
	}

	def Coordinate getCoordinate(EObject obj) {
		getDecorator(obj, Coordinate)
	}

	def LineStyle getStyle(EObject obj) {
		getDecorator(obj, LineStyle)
	}

	def LineStyle getStyle(EObject obj, EObject source) {
		getDecorator(obj, source, LineStyle)
	}

	def LabelStyle getLabelStyle(EObject obj) {
		getDecorator(obj, LabelStyle)
	}

	def LabelStyle getLabelStyle(EObject obj, EObject source) {
		getDecorator(obj, source, LabelStyle)
	}

	def <T extends ADecorator> T getDecorator(EObject obj, Class<T> type) {
		areQueryResultsUpToDate.await()
		decorators.get(obj)?.filter[key === null]?.map[value.filter(type)]?.flatten?.last
	}

	def <T extends ADecorator> T getDecorator(EObject obj, EObject source, Class<T> type) {
		areQueryResultsUpToDate.await()
		decorators.get(obj)?.filter[key === null || key.contains(source)]?.map[value.filter(type)]?.flatten?.last
	}

	def Collection<EObject> getQueryResults(Selector selector) {
		areQueryResultsUpToDate.await()
		queryResults.getOrDefault(selector, #[])
	}

	private static class PartClosedListener implements IPartListener2 {

		override partClosed(IWorkbenchPartReference partRef) {
			val part = partRef.getPart(false)
			if (part instanceof IEditorPart) {
				val resSet = EditorHelper.getResourceSet(part)
				dispose(resSet)
			}
		}

		override partActivated(IWorkbenchPartReference partRef) {}

		override partBroughtToTop(IWorkbenchPartReference partRef) {}

		override partDeactivated(IWorkbenchPartReference partRef) {}

		override partHidden(IWorkbenchPartReference partRef) {}

		override partInputChanged(IWorkbenchPartReference partRef) {}

		override partOpened(IWorkbenchPartReference partRef) {}

		override partVisible(IWorkbenchPartReference partRef) {}

	}
}
