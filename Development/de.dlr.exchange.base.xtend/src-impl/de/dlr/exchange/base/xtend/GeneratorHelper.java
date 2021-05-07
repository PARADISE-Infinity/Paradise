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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import de.dlr.premise.registry.ADataItem;
import de.dlr.premise.registry.MetaData;
import de.dlr.premise.element.AElement;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.system.ABalancing;
import de.dlr.premise.system.IComponent;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.util.PremiseHelper;

public class GeneratorHelper {

    static Integer counter = 1;
    static Integer edgeID = -1;
    static HashMap<String, Integer> map = new HashMap<String, Integer>();

    /**
     * Creates a reference number (Integer starting from 1) used by 'SimMechanics Joints' using java's HashMap with id/refNumber assigned to
     * key/value pair
     * 
     * @param Any PREMISE object holding an id (ADataItem)
     * @return Id as String
     */
    public static String getRef(final Object item) {
        String id = "";
        if (!(item instanceof ABalancing<?>)) {
            if (((ADataItem) item).getId() != null) {
                id = ((ADataItem) item).getId().toString();
            }
        } else {
            id = ((ABalancing<?>) item).toString();
        }

        if (!map.containsKey(id)) {
            map.put(id, counter);
            counter++;
        }
        return map.get(id).toString();
    }

    public static String getEdgeID() {
        edgeID++;
        return edgeID.toString();
    }

    public static Boolean isRef(final Object item) {
        Boolean result = false;
        if (item != null) {
            if (!(item instanceof ABalancing<?>)) {
                result = map.containsKey(((ADataItem) item).getId());
            } else {
                result = map.containsKey(((ABalancing<?>) item).toString());
            }
        }
        return result;
    }

    public static void clearRefs() {
        counter = 1;
        edgeID = -1;
        map.clear();
    }

    // Delegation
    public static String getMetaDataValue(final EObject obj, final String metaDataName) {
        String result = "";

        MetaData meta = PremiseHelper.getMetaData(obj, metaDataName);
        if (meta != null) {
            result = meta.getValue();
        }
        return result;
    }

    public static String encodeFileName(String name) {
        String result = "";
        if (name != null) {
            result = name.replace("\\n", "").replace('/', '_').replace('\\', '_').replace(':', '_').replace(" ", "_").replace("\"", "");
        }
        return result;
    }

    public static String encodeXML(String text) {
        String result = "";
        if (text != null) {
            result = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
            // line break (code line before must not have indentation)
            result = result.replace("\\n", "\r\n");
        }
        return result;
    }

    public static boolean isInSelectedTrees(AElement element, List<? extends AElement> subTrees, boolean includeComponentReferences) {
        for (AElement subTree : subTrees) {
            if (element.equals(subTree)) {
                return true;
            } else {
                List<AElement> children = new ArrayList<>();
                if (subTree instanceof SystemComponent) {
                    EList<? extends IComponent> scChildren;
                    if (includeComponentReferences) {
                        scChildren = ((SystemComponent) subTree).getReferencedChildren();
                    } else {
                        scChildren = ((SystemComponent) subTree).getChildren();
                    }
                    for (IComponent child : scChildren) {
                        if (child instanceof SystemComponent) {
                            children.add((SystemComponent) child);
                        }
                    }
                } else {
                    children.addAll(((UseCase) subTree).getChildren());
                }

                for (AElement child : children) {
                    // put child into a list to satisfy method interface
                    List<AElement> subTreeAsList = new ArrayList<AElement>();
                    subTreeAsList.add(child);

                    boolean result = isInSelectedTrees(element, subTreeAsList, includeComponentReferences);
                    if (result) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
