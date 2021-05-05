/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.representation.provider.my;

import org.eclipse.emf.edit.provider.IUpdateableItemText;

import de.dlr.premise.representation.provider.RepresentationItemProviderAdapterFactory;


public class RepresentationItemProviderAdapterFactoryMy extends RepresentationItemProviderAdapterFactory {

    public RepresentationItemProviderAdapterFactoryMy() {
        super();

        // enable support for inline editing of attributes in tree editor
        supportedTypes.add(IUpdateableItemText.class);
    }

}
