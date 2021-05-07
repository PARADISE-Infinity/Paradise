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

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.functionpool.provider.my.LabelHelper;
import de.dlr.premise.registry.MetaTypeDef;
import de.dlr.premise.registry.Registry;
import de.dlr.premise.registry.provider.MetaTypeDefItemProvider;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

public class MetaTypeDefItemProviderMy extends MetaTypeDefItemProvider {

    private static String typeName;
    
    public MetaTypeDefItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
        
        // initialize data type name only once
        typeName = LabelHelper.cleanSpaces(getString("_UI_MetaTypeDef_type"));
    }

    @Override
    public String getText(Object object) {
        
        // check input
        if (object == null) {
            return "";
        }

        // check if data types shall be shown
        boolean dataTypeEnabled = checkDataTypeEnabled(object);
        dataTypeEnabled = isRootRegistry(object) == true ? false : dataTypeEnabled;

        // set name to label if available
        String label = "";
        if (object != null) {
              MetaTypeDef typeDef = (MetaTypeDef) object;
              if (typeDef.getName().length() > 0) {
                  label = typeDef.getName();
              } else {
                  dataTypeEnabled = true;
              }
          }

        // return name
        return dataTypeEnabled == true ? typeName + " " + label : label;
    }

    private boolean checkDataTypeEnabled(Object object) {
        EObject root = PremiseHelper.getRoot((EObject) object);
        return PremiseHelper.isSet(root, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES);            
    }    
    
    private boolean isRootRegistry(Object object) {
        return PremiseHelper.getRoot((EObject) object) instanceof Registry;    
    }
    
    @Override
    public Object getImage(Object object) {

        // get alternative icon
        if (object instanceof MetaTypeDef != true) {
            return null;
        }

        // get project resource path
        MetaTypeDef stereotype = (MetaTypeDef) object;
        Object image = imageHelper.getImage(stereotype);

        if (image == null) {
            image = getResourceLocator().getImage("full/obj16/MetaTypeDef");
        }

        // return icon
        return overlayImage(object, image);
    }    
}
