/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package com.yworks.ygraphml.util.my;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.yworks.ygraphml.util.YGraphMLResourceImpl;

public class YGraphMLResourceImplMy extends YGraphMLResourceImpl {

    public YGraphMLResourceImplMy(URI uri) {
        super(uri);
    }

    @Override
    protected XMLHelper createXMLHelper() {
        return new XMLHelperImpl(this) {

            @Override
            public String getHREF(EObject obj) {
                String href = null;

                // short-circut the simple case of referencing by id (to prevent # being prepended to it)
                if (!obj.eIsProxy() && obj.eResource() == resource) {
                    href = EcoreUtil.getID(obj);
                }

                if (href == null) {
                    href = super.getHREF(obj);
                }

                return href;
            }
        };
    }
    
    protected XMLLoad createXMLLoad()
    {
        return new XMLLoadImplMy(createXMLHelper());
    }


    private class XMLLoadImplMy extends XMLLoadImpl {

        public XMLLoadImplMy(XMLHelper helper) {
            super(helper);
        }

        protected DefaultHandler makeDefaultHandler() {
            // Use customized XMLHandler
            return new SAXXMLHandlerMy(resource, helper, options);
        }

    }

}
