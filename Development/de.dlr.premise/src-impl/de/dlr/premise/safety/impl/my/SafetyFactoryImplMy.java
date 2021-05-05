/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.safety.impl.my;

import de.dlr.premise.registry.ADataItem;
import de.dlr.premise.safety.Analysis;
import de.dlr.premise.safety.Mitigation;
import de.dlr.premise.safety.SafetyAnalyses;
import de.dlr.premise.safety.impl.SafetyFactoryImpl;
import de.dlr.premise.util.PremiseHelper;

public class SafetyFactoryImplMy extends SafetyFactoryImpl {

    @Override
    public SafetyAnalyses createSafetyAnalyses() {
        // EMF does not serialize/save default values of attributes, so, get and set it explicitly to ensure serialization:        
        SafetyAnalyses analyses = super.createSafetyAnalyses();
        analyses.setMetaModel(analyses.getMetaModel());
        return analyses;   
    }
    
    @Override
    public Mitigation createMitigation() {
        ADataItem dataItem = super.createMitigation();
        dataItem.setId(PremiseHelper.createId());
        return (Mitigation) dataItem;
    }

    @Override
    public Analysis createAnalysis(){
        ADataItem dataItem = super.createAnalysis();
        dataItem.setId(PremiseHelper.createId());
        return (Analysis) dataItem;        
    }    
}
