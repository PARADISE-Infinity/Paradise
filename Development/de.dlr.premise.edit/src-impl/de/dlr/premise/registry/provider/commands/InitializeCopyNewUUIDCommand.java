/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.registry.provider.commands;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.CopyCommand.Helper;
import org.eclipse.emf.edit.command.InitializeCopyCommand;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.dlr.premise.registry.ADataItem;
import de.dlr.premise.util.PremiseHelper;

public class InitializeCopyNewUUIDCommand extends InitializeCopyCommand {

    public InitializeCopyNewUUIDCommand(EditingDomain domain, EObject owner,
			Helper copyHelper) {
		super(domain, owner, copyHelper);
	}
    
	@Override
	public void doExecute() {
		super.doExecute();
		if (copy instanceof ADataItem) {
				((ADataItem) copy).setId(PremiseHelper.createId());
		}  	
	}
   
}
