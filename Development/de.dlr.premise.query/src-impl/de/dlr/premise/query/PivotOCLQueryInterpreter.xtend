/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.query

import de.dlr.premise.query.exceptions.ParserException
import de.dlr.premise.ocl.hacks.ResourceSetAwareDelegatingGlobalEnvironmentFactory
import de.dlr.premise.system.SystemPackage
import java.util.Collection
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.ocl.pivot.internal.utilities.GlobalEnvironmentFactory
import org.eclipse.ocl.pivot.utilities.OCL
import org.eclipse.ocl.pivot.utilities.OCLHelper
import org.eclipse.ocl.pivot.values.InvalidValueException

class PivotOCLQueryInterpreter implements QueryInterpreter {

	var OCL ocl
	var OCLHelper helper

	package new(ResourceSet resourceSet) {
		ocl = new ResourceSetAwareDelegatingGlobalEnvironmentFactory(GlobalEnvironmentFactory.getInstance()).createOCL()
		helper = ocl.createOCLHelper(SystemPackage.Literals.SYSTEM_COMPONENT)
	}

	override synchronized Collection<EObject> query(Collection<EObject> roots, String expression) throws ParserException {
		helper = ocl.createOCLHelper(roots.head?.eClass)
		try {
			val query = helper.createQuery(expression)
			val result = roots.map[ocl.createQuery(query).evaluateEcore(it)].toSet as Collection<?>
			if (!result.nullOrEmpty && result.get(0) instanceof Collection<?>) {
				return (result as Collection<Collection<EObject>>).flatten.filterNull.toSet
			} else {
				return result.filterNull.toSet as Collection<EObject>
			}
		} catch (org.eclipse.ocl.pivot.utilities.ParserException e) {
			throw new ParserException(e)
		} catch (InvalidValueException e) {
			println('''Could not parse expression: «e.message»''');
			throw new ParserException(e)
		}
	}
}
