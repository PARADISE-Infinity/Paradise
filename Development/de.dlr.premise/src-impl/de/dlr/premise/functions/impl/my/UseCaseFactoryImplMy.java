/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.functions.impl.my;

import de.dlr.premise.functions.RequiredParameter;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.functions.UseCaseRepository;
import de.dlr.premise.functions.impl.UseCaseFactoryImpl;
import de.dlr.premise.util.PremiseHelper;


/**
 *
 */
public class UseCaseFactoryImplMy extends UseCaseFactoryImpl {

    @Override
    public UseCaseRepository createUseCaseRepository() {
        UseCaseRepository repository = super.createUseCaseRepository();
        // EMF does not serialize/save default values of attributes, so, get and set it explicitly to ensure serialization:
        repository.setMetaModel(repository.getMetaModel());
        return repository;
    }

    @Override
    public UseCase createUseCase() {
        UseCase dataItem = super.createUseCase();
        dataItem.setId(PremiseHelper.createId());
        return dataItem;
    }


    @Override
    public RequiredParameter createRequiredParameter() {
        RequiredParameter dataItem = super.createRequiredParameter();
        dataItem.setId(PremiseHelper.createId());
        return dataItem;
    }
}
