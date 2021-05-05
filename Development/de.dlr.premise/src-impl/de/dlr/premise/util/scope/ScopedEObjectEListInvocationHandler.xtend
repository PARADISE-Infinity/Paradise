/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.util.scope

import com.google.common.collect.Streams
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.Proxy
import java.util.Collection
import java.util.List
import java.util.function.Function
import java.util.stream.Stream
import org.eclipse.emf.common.util.EList
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static extension de.dlr.premise.util.PremiseHelper.*

class ScopedEObjectEListInvocationHandler<E, O> implements InvocationHandler {
	
	def static <E, O> EList<E> create(EList<O> delegate, Function<O, E> transform, Function<E, O> inverseTransform) {
		val allInterfaces = (delegate.class as Class<?>).closure[#[superclass]].flatMap[interfaces.toList].toList
		val handler = new ScopedEObjectEListInvocationHandler(delegate, transform, inverseTransform)		
		return Proxy.newProxyInstance(delegate.class.classLoader, allInterfaces, handler) as EList<E>
	} 
	

	val EList<O> delegate
	val Function<O, E> transform
	val Function<E, O> inverseTransform
	
	@FinalFieldsConstructor package new() {
		
	}
	
	override invoke(Object proxy, Method method, Object[] originalArgs) throws Throwable {
		val needsInverseTransform = method.name == "add" || method.name == "addAll"
		
		if (needsInverseTransform && inverseTransform == null) {
			throw new UnsupportedOperationException('''Method «method» can't be called without inverse transform!''')
		}
		
		// If the arguments don't need to be added to the delegate, simply transform the delegate list and perform the method on it
		if (!needsInverseTransform && method.declaringClass.isAssignableFrom(List)) {
			return method.invoke(delegate.map[transform.apply(it)], originalArgs)
		}
		
		
		// We need to transform the parameters back to the types of the delegate
		val args = Streams
			.zip(method.parameters.stream, originalArgs?.stream ?: Stream.empty, [a, b | a->b])
			.map[doInverseTransform]
			.toArray
		
		val result = method.invoke(delegate, args)
		
		switch (result) {
			Collection<O>: result.map[transform.apply(it)].toList
			case method.returnType == Object: transform.apply(result as O)
			default: result
		}
	}
	
	def private doInverseTransform(Pair<Parameter, Object> methodParameterToValue) {
		// Object stands in for the erased type E of the collection
		if (methodParameterToValue.key.type == Object)  {
			return inverseTransform.apply(methodParameterToValue.value as E)
		}
		if (methodParameterToValue.value instanceof Collection<?>) {
			return (methodParameterToValue.value as Collection<E>).map[inverseTransform.apply(it)].toList
		}
		return methodParameterToValue.value
	}
	
}
