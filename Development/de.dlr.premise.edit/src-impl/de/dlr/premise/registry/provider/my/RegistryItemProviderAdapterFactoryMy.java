/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.registry.provider.my;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.edit.provider.IUpdateableItemText;

import de.dlr.premise.registry.provider.RegistryItemProviderAdapterFactory;


/**
 * @author hschum, berr_ae
 *
 */
public class RegistryItemProviderAdapterFactoryMy extends
		RegistryItemProviderAdapterFactory {

    public RegistryItemProviderAdapterFactoryMy() {
        super();

        // enable support for inline editing of attributes in tree editor
        supportedTypes.add(IUpdateableItemText.class);
    }

	@Override
	public Adapter createUnitAdapter() {
		if (unitItemProvider == null) {
			unitItemProvider = new UnitItemProviderMy(this);
		}
		return unitItemProvider;
	}
	
	@Override
	public Adapter createMetaDataAdapter() {
		if (metaDataItemProvider == null) {
			metaDataItemProvider = new MetaDataItemProviderMy(this);
		}

		return metaDataItemProvider;
	}
	
	@Override
	public Adapter createRegistryAdapter(){
		if (registryItemProvider == null) {
			registryItemProvider = new RegistryItemProviderMy(this);
		}
		return registryItemProvider;
	}
	
	@Override
	public Adapter createConstantAdapter(){
		if (constantItemProvider == null) {
			constantItemProvider = new ConstantItemProviderMy(this);
		}
		return constantItemProvider;
	}
	
	@Override
	public Adapter createMetaTypeDefAdapter() {
		if (metaTypeDefItemProvider == null) {
			metaTypeDefItemProvider =  new MetaTypeDefItemProviderMy(this);
		}
		
		return metaTypeDefItemProvider;
	}	

    @Override
    public Adapter createValueAdapter() {
        if (valueItemProvider == null) {
            valueItemProvider = new ValueItemProviderMy(this);
        }
        return valueItemProvider;
    }
}
