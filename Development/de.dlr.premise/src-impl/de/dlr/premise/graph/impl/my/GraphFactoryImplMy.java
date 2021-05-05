/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.graph.impl.my;

import de.dlr.premise.graph.DirectPointer;
import de.dlr.premise.graph.impl.DirectPointerImpl;
import de.dlr.premise.graph.impl.GraphFactoryImpl;


public class GraphFactoryImplMy extends GraphFactoryImpl {


    @Override
    public <T> DirectPointer<T> createDirectPointer() {
        DirectPointerImpl<T> directPointer = new DirectPointerImplMy<T>();
        return directPointer;
    }
}
