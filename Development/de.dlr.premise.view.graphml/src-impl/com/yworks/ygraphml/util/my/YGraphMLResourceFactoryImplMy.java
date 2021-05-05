/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package com.yworks.ygraphml.util.my;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;

import com.yworks.ygraphml.util.YGraphMLResourceFactoryImpl;

public class YGraphMLResourceFactoryImplMy extends YGraphMLResourceFactoryImpl {

    @Override
    public Resource createResource(URI uri) {
        // Changed to custom implementation
        XMLResource result = new YGraphMLResourceImplMy(uri);

        // read in unknown xml (so we don't choke if some unknown data is written by external tool)
        result.getDefaultLoadOptions().put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);
        result.getDefaultSaveOptions().put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);

        // copied from superclass implementation from here on
        result.getDefaultSaveOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
        result.getDefaultLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);

        result.getDefaultSaveOptions().put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);

        result.getDefaultLoadOptions().put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
        result.getDefaultSaveOptions().put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);

        result.getDefaultLoadOptions().put(XMLResource.OPTION_USE_LEXICAL_HANDLER, Boolean.TRUE);

        return result;
    }
}
