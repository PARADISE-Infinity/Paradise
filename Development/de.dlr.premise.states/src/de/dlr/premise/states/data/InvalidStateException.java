/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.states.data;

public class InvalidStateException extends Exception {

    private static final long serialVersionUID = -193073613899790674L;

    public InvalidStateException(String message) {
        super(message);
    }
    
    public InvalidStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
