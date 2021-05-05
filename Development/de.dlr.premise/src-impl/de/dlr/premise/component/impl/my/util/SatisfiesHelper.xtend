/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.component.impl.my.util

import com.google.common.collect.Iterables
import de.dlr.premise.component.ISatisfieable
import de.dlr.premise.component.ISatisfyingRoot
import de.dlr.premise.component.Satisfies
import de.dlr.premise.graph.APointer
import de.dlr.premise.graph.GraphFactory
import de.dlr.premise.graph.INode
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.xbase.lib.Functions.Function1

import static extension de.dlr.premise.util.PremiseHelper.closure
import static extension de.dlr.premise.util.PremiseHelper.flatMap

class SatisfiesHelper {
	def static <S extends INode> APointer<S> getSourcePointer(Satisfies<S, ?> satisfies) {
		GraphFactory.eINSTANCE.createDirectPointer => [
			target = satisfies.eContainer as S
		]
	}
	
	def static queryIsValidTargetPointerValue(Satisfies<?, ?> it, ISatisfieable target) {
		if (eContainer instanceof ISatisfyingRoot<?, ?>) {
			return true
		}
		
		eContainer
			.closure[#[eContainer]]
			.filter(ISatisfyingRoot)
			.flatMap[ISatisfyingRoot<INode, ISatisfieable> it | satisfiedSatisfieables]
			.filter(EObject)
			.flatMapIncludingSelf[eAllContents.toIterable]
			.exists[it == target]
	}
	
	private static def <T> Iterable<T> flatMapIncludingSelf(Iterable<? extends T> it, Function1<? super T, Iterable<? extends T>> transformation) {
		flatMap[Iterables.concat(transformation.apply(it), #[it])]
	}
}