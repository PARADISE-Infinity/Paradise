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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.util.RuntimeIOException;


public class CharsetProvidingEclipseResourceFileSystemAccess2 extends EclipseResourceFileSystemAccess2 implements ICharsetProvidingFileSystemAccess {
    public String getFileCharset(String fileName, String outputName) {
        OutputConfiguration outputConfig = getOutputConfig(outputName);

        if (!ensureOutputConfigurationDirectoryExists(outputConfig))
            return null;

        IFile file = getFile(fileName, outputName);
        if (file == null)
            return "";
        
        try {
            return getEncoding(file);
        } catch (CoreException e) {
            throw new RuntimeIOException(e);
        }
    }
    
    public String getFileCharset(String fileName) {
        return getFileCharset(fileName, DEFAULT_OUTPUT);
    }
}
