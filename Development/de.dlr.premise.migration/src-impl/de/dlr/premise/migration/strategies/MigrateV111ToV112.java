/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.migration.strategies;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

import de.dlr.premise.migration.IPremiseMigration;
import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.ModelVersion;
import de.dlr.premise.migration.util.MigrationHelper;

public class MigrateV111ToV112 extends AMigration implements IPremiseMigration {

    private Set<String> ucTransitionIds;
    private Set<String> premTransitionIds;

    @Override
    public void migrate(final MigrationModel model) {
        ucTransitionIds = new HashSet<String>();
        premTransitionIds = new HashSet<String>();

        // scan files
        scanRoot(model.getModelRoot());
        for (Document document : model.getReferencedFiles().values()) {
            Element root = document.getDocumentElement();
            root.normalize();
            scanRoot(root);
        }

        super.migrate(model);
    }

    @Override
    public String getTargetVersion() {
        return ModelVersion.V112.toString();
    }

    protected void scanRoot(Element root) {
        // get all ids of transitions inside uc/prem files
        NodeList transitions = root.getElementsByTagName("transitions");

        for (int i = 0, l = transitions.getLength(); i < l; i++) {
            Element transition = (Element) transitions.item(i);
            String id = transition.getAttribute("id");

            if (id != null) {
                if (root.getNodeName().startsWith("prem:")) {
                    premTransitionIds.add(id);
                } else if (root.getNodeName().startsWith("uc:")) {
                    ucTransitionIds.add(id);
                }
            }
        }
    }

    @Override
    protected void migrateRoot(final MigrationModel model, final Element root) {
        boolean elemNamespaceNeeded = false;
        boolean premNamespaceNeeded = false;
        boolean ucNamespaceNeeded = false;

        System.out.println();

        // change mode xsi:type (only if it was there before)
        NodeList modes = root.getElementsByTagName("modes");

        for (int i = 0, l = modes.getLength(); i < l; i++) {
            Element mode = (Element) modes.item(i);

            if (mode.hasAttribute("xsi:type")) {
                mode.setAttribute("xsi:type", "elem:Mode");
                elemNamespaceNeeded = true;
            }
        }

        // set transition xsi:type
        NodeList transitions = root.getElementsByTagName("transitions");

        for (int i = 0, l = transitions.getLength(); i < l; i++) {
            Element transition = (Element) transitions.item(i);

            if (root.getNodeName().startsWith("prem:")) {
                transition.setAttribute("xsi:type", "prem:Transition");
                premNamespaceNeeded = true;
            } else if (root.getNodeName().startsWith("uc:")) {
                transition.setAttribute("xsi:type", "uc:Transition");
                ucNamespaceNeeded = true;

                // delete transition parameters and balancings
                NodeList balancings = transition.getElementsByTagName("balancings");
                for (int j = 0, balancingLength = balancings.getLength(); j < balancingLength; j++) {
                    Element balancing = (Element) balancings.item(j);
                    balancing.getParentNode().removeChild(balancing);
                }

                NodeList parameters = transition.getElementsByTagName("parameters");
                for (int j = 0, parameterLength = parameters.getLength(); j < parameterLength; j++) {
                    Element parameter = (Element) parameters.item(j);
                    parameter.getParentNode().removeChild(parameter);
                }
            }
        }

        // change xsi types of trigger cross reference when necessary
        NodeList triggers = root.getElementsByTagName("trigger");

        for (int i = 0, l = triggers.getLength(); i < l; i++) {
            Element trigger = (Element) triggers.item(i);

            String refId = trigger.getAttribute("href").split("#")[1];

            if (premTransitionIds.contains(refId)) {
                trigger.setAttribute("xsi:type", "prem:Transition");
                premNamespaceNeeded = true;
            } else if (ucTransitionIds.contains(refId)) {
                trigger.setAttribute("xsi:type", "uc:Transition");
                ucNamespaceNeeded = true;
            }
        }

        // change xsi:type of transition constraints
        // all element names that might be constraints
        List<Node> constraintsCandidates = Lists.newArrayList();
        constraintsCandidates.addAll(MigrationHelper.listFromNodeList(root.getElementsByTagName("constraint")));
        constraintsCandidates.addAll(MigrationHelper.listFromNodeList(root.getElementsByTagName("children")));
        
        for (Node constraintCandidateNode : constraintsCandidates) {
            Element constraintCandidate = (Element)  constraintCandidateNode;
            
            if (constraintCandidate.hasAttribute("xsi:type")) {
                if (constraintCandidate.getAttribute("xsi:type").equals("prem:NestedTransitionConstraint")) {
                    constraintCandidate.setAttribute("xsi:type", "elem:NestedTransitionConstraint");
                    elemNamespaceNeeded = true;
                } else if (constraintCandidate.getAttribute("xsi:type").equals("prem:TransitionConstraint")) {
                    constraintCandidate.setAttribute("xsi:type", "elem:TransitionConstraint");
                    elemNamespaceNeeded = true;
                }
            }
        }

           
        // set namespaces
        if (elemNamespaceNeeded || premNamespaceNeeded) {
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

            if (premNamespaceNeeded) {
                root.setAttribute("xmlns:prem", "http://www.dlr.de/ft/premise/2010/");
            }

            if (elemNamespaceNeeded) {
                root.setAttribute("xmlns:elem", "http://www.dlr.de/premise/element/2014/");
            }

            if (ucNamespaceNeeded) {
                root.setAttribute("xmlns:uc", "http://www.dlr.de/premise/usecase/2014/");
            }

            model.setChange();
        }
    }
}