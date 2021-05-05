/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.migration.strategies;

import static de.dlr.premise.migration.util.MigrationHelper.listFromNodeList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.dlr.premise.migration.IPremiseMigration;
import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.ModelVersion;
import de.dlr.premise.util.PremiseHelper;

public class MigrateV114ToV115 implements IPremiseMigration {

    @Override
    public String getTargetVersion() {
        return ModelVersion.V115.toString();
    }

    public void migrate(final MigrationModel model) {
        Map<String, Document> refFiles = new HashMap<>(model.getReferencedFiles());
        refFiles.put(model.getPath(), model.getModelDocument());

        Map<String, String> elemURIToIdMap = new HashMap<>();

        // in the first run, we assign the ids and store the mapping
        for (Entry<String, Document> fileEntry : refFiles.entrySet()) {
            String pathString = fileEntry.getKey();
            Document document = fileEntry.getValue();

            List<Node> uniqueNameItems = new ArrayList<>();
            uniqueNameItems.addAll(listFromNodeList(document.getElementsByTagName("rep:Representation")));
            uniqueNameItems.addAll(listFromNodeList(document.getElementsByTagName("calcEngines")));
            for (Node node : uniqueNameItems) {
                Element elem = (Element) node;

                String uuid = elem.getAttribute("id");
                if ("".equals(uuid)) {
                    // assign uuid
                    uuid = PremiseHelper.createId();
                    elem.setAttribute("id", uuid);
                    model.setChange();
                }

                // add into map
                String absoluteElementURI = pathString + "#" + elem.getAttribute("name");
                elemURIToIdMap.put(absoluteElementURI, uuid);
            }
        }
        
        // in the secound run, we redo the references
        for (Entry<String, Document> fileEntry : refFiles.entrySet()) {
            String pathString = fileEntry.getKey();
            Document document = fileEntry.getValue();

            List<Node> referencingElements = new ArrayList<>();
            referencingElements.addAll(listFromNodeList(document.getElementsByTagName("extensions")));
            referencingElements.addAll(listFromNodeList(document.getElementsByTagName("calcEngine")));
            for (Node node : referencingElements) {
                Element elem = (Element) node;

                String referenceURI = elem.getAttribute("href");
                String[] splitReferenceURI = referenceURI.split("#");
                String referencePath = splitReferenceURI[0];
                String referenceFragment = splitReferenceURI[1];

                String matchingUUID = getUUIDForNameBasedRef(pathString, referencePath, referenceFragment, elemURIToIdMap);
                if (matchingUUID != null) {
                    // create the new reference uri which is relative again (just replace name fragment with id fragment)
                    String newReferenceURI = referencePath + "#" + matchingUUID;
                    elem.setAttribute("href", newReferenceURI);
                    model.setChange();
                }
            }

            List<Node> functions = listFromNodeList(document.getElementsByTagName("functions"));
            for (Node node : functions) {
                Element elem = (Element) node;

                String referenceValue = elem.getAttribute("calcEngine");

                // it's a simple reference by name (not cross file)
                String referencePath = new File(pathString).getName();
                String referenceFragment = referenceValue;

                String matchingUUID = getUUIDForNameBasedRef(pathString, referencePath, referenceFragment, elemURIToIdMap);

                if (matchingUUID != null) {
                    elem.setAttribute("calcEngine", matchingUUID);
                    model.setChange();
                }
            }
        }
    }

    protected String getUUIDForNameBasedRef(String baseFile, String referencePath, String referenceFragment,
            Map<String, String> elemURIToIdMap) {
        String absoluteReferencePath;
        try {
            // resolve the relative path against the main file path to get an absolute path
            File f = new File(new File(baseFile).getCanonicalFile().getParent() + File.separator + referencePath);
            absoluteReferencePath = f.getCanonicalPath();
        } catch (IOException e) {
            // The file is apperantly unusable, so ignore it
            return null;
        }

        // string together with the fragment to create an absolute uri
        String absoluteReferenceURI = absoluteReferencePath + "#" + referenceFragment;

        // find the UUID assigned earlier
        String matchingUUID = elemURIToIdMap.get(absoluteReferenceURI);
        return matchingUUID;
    }
}
