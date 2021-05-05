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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.dlr.premise.migration.MigrationModel;
import de.dlr.premise.migration.ModelVersion;
import de.dlr.premise.migration.util.MigrationHelper;


public class MigrateV112ToV113 extends AMigration {

    @Override
    public String getTargetVersion() {
        return ModelVersion.V113.toString();
    }

    @Override
    protected void migrateRoot(MigrationModel model, Element modelRoot) {
        // copy, as node list is live in regard to query (and will therefore change if we rename elements)
        List<Node> constraints = new ArrayList<Node>(MigrationHelper.listFromNodeList(modelRoot.getElementsByTagName("constraints")));
        for (Node constraintNode : constraints) {
            Element constraint = (Element) constraintNode;

            // add id
            if ("".equals(constraint.getAttribute("id"))) {
                constraint.setAttribute("id", UUID.randomUUID().toString());
            }

            // Rename of the reference to RequiredParameters (formerly ParameterConstraints) in UseCase
            modelRoot.getOwnerDocument().renameNode(constraint, constraint.getNamespaceURI(), "requiredParameters");
            model.setChange();
        }

        /**
         * Parameters have a reference called satisfies to RequiredParameters, since required parameters were no ADataItems until now (and
         * thus had no id) references were made by a uri fragment (see {@link ResourceImpl#getURIFragment(EObject)}). This contains the name
         * of the containment reference, and therefore has to change as well.
         */
        List<Node> satisfiesReferences = MigrationHelper.listFromNodeList(modelRoot.getElementsByTagName("satisfies"));
        for (Node satisfiesNode : satisfiesReferences) {
            Element satisfies = (Element) satisfiesNode;
            String href = satisfies.getAttribute("href").replaceAll("@constraints\\.", "@requiredParameters.");
            satisfies.setAttribute("href", href);
            model.setChange();
        }
    }

}
