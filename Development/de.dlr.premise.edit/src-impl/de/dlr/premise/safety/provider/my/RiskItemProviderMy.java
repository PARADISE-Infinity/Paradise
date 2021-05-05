/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.safety.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.safety.Assessment;
import de.dlr.premise.safety.Risk;
import de.dlr.premise.safety.provider.RiskItemProvider;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;


public class RiskItemProviderMy extends RiskItemProvider {

    public RiskItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }
    
    @Override
    public String getText(Object object) {

        // check input
        if (object == null) {
            return null;
        }

        Risk risk = (Risk) object;        
        String type = getAssociatedRisk(risk);
        
        // get type name
        String typeName = "";
        if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            typeName = getString("_UI_Risk_type");
            typeName = typeName.replace(" ", "") + " ";
        }

        // return name
        return type + " " + typeName + " " + risk.getObjective().toString() ;
    }

    @Override
    public Object getImage(Object object) {
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/Risk.png"));
    }

    /**
     * Return the associated risk
     * @param risk
     * @return
     */
    private String getAssociatedRisk(Risk risk) {
        
        Assessment assessment = (Assessment) risk.eContainer();
        
        if (assessment.getBaseRisk() == risk) {
            return "Base";
        }
        
        if (assessment.getMitigatedRisk() == risk) {
            return "Mitigated";
        }
        
        return null;
    }
}
