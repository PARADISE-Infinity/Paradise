/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.functions.presentation.my;

import java.util.ArrayList;
import java.util.Collection;

import de.dlr.premise.functions.presentation.UseCaseModelWizard;


public class UseCaseModelWizardMy extends UseCaseModelWizard {
    @Override
    protected Collection<String> getInitialObjectNames() {
        if (initialObjectNames == null) {
            initialObjectNames = new ArrayList<String>();
            initialObjectNames.add(useCasePackage.getUseCaseRepository().getName());
        }
        return initialObjectNames;
    }
}
