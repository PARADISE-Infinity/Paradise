/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.base.xtend;

import org.eclipse.xtext.generator.IFileSystemAccess2;

/**
 * For XML files, the charset is written to the XML declaration. Therefore clients need to be able to get it before they call {@link IFileSystemAccess2#generateFile(String, CharSequence)}
 */
public interface ICharsetProvidingFileSystemAccess extends IFileSystemAccess2 {
    public String getFileCharset(String fileName, String outputName);
    
    public String getFileCharset(String fileName);
}