/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.util.urihandlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.emf.ecore.resource.impl.URIHandlerImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLHandler;

/**
 * Handles all namespace URIs starting with http://www.dlr.de by throwing an error to prevent excessive network requests.
 * 
 * When parsing a XML file, EMF needs to resolve any namespace URIs present in the document and get the associated metamodel (see
 * {@link XMLHandler#getPackageForURI}). This uses the EMF {@link EPackage.Registry} at first and then tries several fallback approaches,
 * read the code of the linked method for details. In the end it will perform a HTTP request to fetch the namespace URI, under the
 * assumption that the schema for the namespace is located there.
 * 
 * Usually, our own namespaces will be registered so the fallback doesn't bother us. But in case a namespace URI was used previously and is
 * now no longer used, it is not registered anymore. Therefore when old files are opened, the fallback will come into play, which will
 * result in excessive and completely useless network I/O (that, to add insult to injury, runs on the main thread and therefore freezes the
 * application).
 * 
 * To prevent this problem, this URI handler can be registered, which will fail in a controlled manner, and therefore prevent the network
 * request being issued.
 */
public class LegacyPremiseURIHandler extends URIHandlerImpl {
    
    /**
     * Registers an instance of the LegacyPremiseURIHandler into the handlers of a {@link URIConverter} at the right position.
     * 
     * We only add, if no instance is already present. In this case we want to be registered before the {@link URIHandlerImpl}, which is the
     * one to cause the network request.
     * 
     * @param converter
     */
    public static void registerInto(URIConverter converter) {
        List<URIHandler> handlers = converter.getURIHandlers();

        // by default, add at the end
        int index = handlers.size();

        // check all handlers for a) LegacyPremiseURIHandler and b) URIHandlerImpl
        for (int i = 0; i < handlers.size(); i++) {
            URIHandler handler = handlers.get(i);

            // a) LegacyPremiseURIHandler: If we are already registered, there is nothing to do!
            if (handler instanceof LegacyPremiseURIHandler) {
                return;
            }

            // b) URIHandlerImpl: We must add before it, so use its positon
            if (handler instanceof URIHandlerImpl) {
                index = i;
                break;
            }
        }

        handlers.add(index, new LegacyPremiseURIHandler());
    }

    @Override
    public boolean canHandle(URI uri) {
        return uri.toString().startsWith("http://www.dlr.de") || uri.toString().startsWith("https://www.dlr.de");
    }

    @Override
    public InputStream createInputStream(URI uri, Map<?, ?> options) throws IOException {
        throw new IOException();
    }

    @Override
    public OutputStream createOutputStream(URI uri, Map<?, ?> options) throws IOException {
        throw new IOException();
    }
}
