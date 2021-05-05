/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.base.xtend.ui;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class EcoreUtilMy extends EcoreUtil {

	public static <T extends EObject> T copy(T eObject) {
		Copier copier = new Copier();
		EObject result = copier.copy(eObject);
		copier.copyReferences();

		@SuppressWarnings("unchecked")
		T t = (T) result;
		return t;
	}

	public static <T> Collection<T> copyAll(Collection<? extends T> eObjects) {
		Copier copier = new Copier();
		Collection<T> result = copier.copyAll(eObjects);
		copier.copyReferences();
		return result;
	}

	public static class Copier extends EcoreUtil.Copier {

		private static final long serialVersionUID = 1L;

		@Override
		protected EObject createCopy(EObject eObject) {
			EObject copy = create(getTarget(eObject.eClass()));
			copy.eSetDeliver(false);
			return copy;
		}
	}
}
