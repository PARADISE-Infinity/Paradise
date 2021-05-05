/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.util.scope

import java.lang.reflect.Proxy
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil

class ScopedEObjectHelper {
	def static isScopedEObject(EObject obj) {
		return obj.invocationHandler != null
	}
	
	def static getScope(EObject obj) {
		return obj.invocationHandler?.scope
	}
	
	def static <T extends EObject> T getWrappedElement(T obj) {
		return obj.invocationHandler?.delegate as T
	}
	
	def static getId(EObject obj) {
		return obj.invocationHandler?.idWithScope ?: obj.EObjectIdenfification
	}
	
	def static <T extends EObject> ScopedEObjectInvocationHandler<T> getInvocationHandler(T obj) {
		if (obj == null || !Proxy.isProxyClass(obj.class)) {
			return null
		}
		val handler = Proxy.getInvocationHandler(obj)
		if (handler instanceof ScopedEObjectInvocationHandler<?>) {
			return handler as ScopedEObjectInvocationHandler<T>
		}
		return null
	}
	
		
	/**
	 * Gets an identification of an EObject, either ID or if there is none the URI
	 */
	def static String getEObjectIdenfification(EObject it) {
		val id = EcoreUtil.getID(it)
		if (id != null) {
			return id
		}
		val uri = EcoreUtil.getURI(it)
		val relativeURI = uri.deresolve(uri.trimSegments(1).appendSegment(""))
		return relativeURI.toString
	}
}