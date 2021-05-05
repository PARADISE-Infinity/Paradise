/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.base.xtend;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Lists;

import de.dlr.premise.element.ARepository;
import de.dlr.premise.registry.MetaData;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.util.PremiseHelper;

public abstract class StructuredOptions implements IOptions {

    /**
     * @return A Map describing the options and default values.<br>
     *         Keys are always option-names, whereas values are either Strings for default values or Maps for a group.
     */
    public abstract Map<String, ?> getDefaultOptions();

    public static final String EXPORT = PremiseHelper.NAME_META_SECTION_EXPORT;
    public static final String VIEW = PremiseHelper.NAME_META_SECTION_VIEW;

    @Override
    public void setOptions(ARepository aRep) {
        if (aRep != null) {
            setOptions(aRep, getDefaultOptions());
        }
    }

    @SuppressWarnings("unchecked")
    private void setOptions(EObject root, Map<String, ?> options) {
        options.forEach((name, value) -> {
            if (value instanceof Map) { // this is another group
                MetaData metaGroup = PremiseHelper.getMetaData(root, name);
                if (metaGroup == null) {
                    metaGroup = RegistryFactory.eINSTANCE.createMetaData();
                    getMetaDataChildren(root).add(metaGroup);
                    metaGroup.setName(name);
                }
                setOptions(metaGroup, (Map<String, ?>) value);
            } else { // it is a key, value pair
                MetaData md = PremiseHelper.getMetaData(root, name);
                if (md == null) {
                    md = RegistryFactory.eINSTANCE.createMetaData();
                    getMetaDataChildren(root).add(md);
                    md.setName(name);
                    md.setValue((String) value);
                }
            }
        });
    }

    private List<MetaData> getMetaDataChildren(EObject root) {
        if (root instanceof ARepository) {
            return ((ARepository) root).getMetaData();
        } else if (root instanceof MetaData) {
            return ((MetaData) root).getMetaData();
        }
        return Lists.newArrayList();
    }
}
