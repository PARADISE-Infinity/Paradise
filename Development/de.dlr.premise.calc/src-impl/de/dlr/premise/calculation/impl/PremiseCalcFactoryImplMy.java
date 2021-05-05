/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.calculation.impl;

import de.dlr.premise.system.TransitionParameter;
import de.dlr.premise.system.impl.my.SystemFactoryImplMy;
import de.dlr.premise.util.PremiseHelper;

public class PremiseCalcFactoryImplMy extends SystemFactoryImplMy {
    @Override
    public TransitionParameter createTransitionParameter() {
        TransitionParameter parameter = new TransitionParameterImplMy(); // use specific implementation
        parameter.setId(PremiseHelper.createId());
        return parameter;
    }
}
