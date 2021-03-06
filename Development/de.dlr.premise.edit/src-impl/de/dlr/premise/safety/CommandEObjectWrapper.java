/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.safety;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;

/** This is a class allowing to return a command and an eobject*/
public class CommandEObjectWrapper {
    
    public Command command;
    public EObject object;
    
    public CommandEObjectWrapper(Command command, EObject object) {
        this.command = command;
        this.object = object;
    }
    
}
