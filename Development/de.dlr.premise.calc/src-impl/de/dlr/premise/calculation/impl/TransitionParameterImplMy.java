/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.calculation.impl;

import de.dlr.premise.system.impl.TransitionParameterImpl;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author hschum
 *
 */
public class TransitionParameterImplMy extends TransitionParameterImpl {

	@Override
	public boolean equals(Object obj) {	    
	    return PremiseHelper.sameID(this, obj);
	}
}
