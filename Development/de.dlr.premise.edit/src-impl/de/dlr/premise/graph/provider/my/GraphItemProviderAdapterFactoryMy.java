/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.graph.provider.my;

import org.eclipse.emf.common.notify.Adapter;

import de.dlr.premise.graph.provider.GraphItemProviderAdapterFactory;


public class GraphItemProviderAdapterFactoryMy extends GraphItemProviderAdapterFactory {

    @Override
    public Adapter createDirectPointerAdapter() {
        if (directPointerItemProvider == null) {
            directPointerItemProvider = new DirectPointerItemProviderMy(this);
        }

        return directPointerItemProvider;
    }
}
