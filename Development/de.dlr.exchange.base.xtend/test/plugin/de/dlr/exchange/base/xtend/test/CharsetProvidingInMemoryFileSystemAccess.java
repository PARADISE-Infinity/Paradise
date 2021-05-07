/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.base.xtend.test;

import org.eclipse.xtext.generator.InMemoryFileSystemAccess;

import de.dlr.exchange.base.xtend.ICharsetProvidingFileSystemAccess;


public class CharsetProvidingInMemoryFileSystemAccess extends InMemoryFileSystemAccess implements ICharsetProvidingFileSystemAccess {

    @Override
    public String getFileCharset(String fileName, String outputName) {
        return getTextFileEncoding();
    }

    @Override
    public String getFileCharset(String fileName) {
        return getTextFileEncoding();
    }

}
