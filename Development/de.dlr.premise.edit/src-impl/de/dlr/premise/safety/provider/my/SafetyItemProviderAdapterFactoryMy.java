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

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.edit.provider.IUpdateableItemText;

import de.dlr.premise.safety.provider.SafetyItemProviderAdapterFactory;

/**
 * @author berr_ae
 *
 */
public class SafetyItemProviderAdapterFactoryMy extends SafetyItemProviderAdapterFactory {

    public SafetyItemProviderAdapterFactoryMy() {
        super();

        // enable support for inline editing of attributes in tree editor
        supportedTypes.add(IUpdateableItemText.class);
    }

    
    @Override
    public Adapter createRiskAdapter() {
        
        if (riskItemProvider == null) {
            riskItemProvider = new RiskItemProviderMy(this);
        }        
        return riskItemProvider;
    }
}
