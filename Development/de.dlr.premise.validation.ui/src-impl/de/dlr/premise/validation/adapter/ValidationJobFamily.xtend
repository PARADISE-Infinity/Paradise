/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.validation.adapter

import org.eclipse.emf.common.util.URI
import org.eclipse.xtend.lib.annotations.Data

/**
 * Describes all validation jobs that operate on a given resource. Used to prevent multiple validation jobs from running at the same time
 */
@Data
class ValidationJobFamily {
	val URI resourceURI
}