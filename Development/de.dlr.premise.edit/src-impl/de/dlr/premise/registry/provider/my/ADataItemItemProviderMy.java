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

import java.util.Collection;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.dlr.premise.registry.ADataItem;
import de.dlr.premise.registry.provider.ADataItemItemProvider;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author hschum
 *
 */
public class ADataItemItemProviderMy extends ADataItemItemProvider {

	public ADataItemItemProviderMy(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	@Override
	protected Command createAddCommand(EditingDomain domain, EObject owner,
			EStructuralFeature feature, Collection<?> collection, int index) {
		Command addCmd = super.createAddCommand(domain, owner, feature, collection, index);
		for (Object object : addCmd.getResult()) {
			if (object instanceof ADataItem) {
				((ADataItem)object).setId(PremiseHelper.createId());
			}
		}
		return addCmd;
	}
}
