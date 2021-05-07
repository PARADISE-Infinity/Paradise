/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.element.impl.my;

import de.dlr.premise.element.Mode;
import de.dlr.premise.element.StateMachine;
import de.dlr.premise.element.Transition;
import de.dlr.premise.element.impl.ElementFactoryImpl;
import de.dlr.premise.util.PremiseHelper;

public class ElementFactoryImplMy extends ElementFactoryImpl {

    @Override
    public Mode createMode() {
        Mode dataItem = super.createMode();
        dataItem.setId(PremiseHelper.createId());
        return dataItem;
    }
    
    @Override
    public Transition createTransition() {
        Transition dataItem = super.createTransition();
        dataItem.setId(PremiseHelper.createId());
        return dataItem;
    }
    
    @Override
    public StateMachine createStateMachine() {
        StateMachine dataItem = super.createStateMachine();
        dataItem.setId(PremiseHelper.createId());
        return dataItem;
    }
}
