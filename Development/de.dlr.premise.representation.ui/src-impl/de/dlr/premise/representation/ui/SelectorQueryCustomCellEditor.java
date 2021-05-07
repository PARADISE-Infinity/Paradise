/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.representation.ui;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClassifier;

import de.dlr.premise.representation.RepresentationPackage;
import de.dlr.premise.representation.Selector;
import de.dlr.premise.representation.ui.ASelectorCustomCellEditor;

public class SelectorQueryCustomCellEditor extends ASelectorCustomCellEditor {

    @Override
    protected EAttribute getSupportedFeature() {
        return RepresentationPackage.Literals.SELECTOR__QUERY;
    }

    @Override
    protected EClassifier getContext(Selector selector) {
        return RepresentationPackage.Literals.SELECTOR;
    }
}
