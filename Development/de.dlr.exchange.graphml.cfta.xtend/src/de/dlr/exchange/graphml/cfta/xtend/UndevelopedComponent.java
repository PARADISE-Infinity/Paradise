/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.graphml.cfta.xtend;

import java.util.List;

import de.dlr.premise.element.AGuardCondition;
import de.dlr.premise.element.Mode;
import de.dlr.premise.registry.AParameterDef;

public class UndevelopedComponent {
    
    public Mode triggerMode;
    public Mode parentNode;
    public List<AParameterDef> params;
    public AGuardCondition rootGuardCon;
    
    
    public UndevelopedComponent(Mode triggerMode, List<AParameterDef> params, Mode parentNode, AGuardCondition rootGuardCon){
        this.triggerMode = triggerMode;
        this.params = params;
        this.parentNode = parentNode;
        this.rootGuardCon = rootGuardCon;
    }
    
    public boolean verfiy(){
        if (triggerMode != null && parentNode != null && rootGuardCon != null)
            return true;
        else
            return false;
    }
}
