/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.element.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;

import de.dlr.premise.registry.IMetaTypable;
import de.dlr.premise.element.AElement;
import de.dlr.premise.element.Connection;
import de.dlr.premise.element.provider.ConnectionItemProvider;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author hschum
 *
 */
public class ConnectionItemProviderMy extends ConnectionItemProvider {

    public ConnectionItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    @Override
    public String getText(Object object) {

        // check input
        if ((object instanceof Connection) == false) {
            return null;
        }

        Connection<?> conn = (Connection<?>) object;
        boolean withType = PremiseHelper.isSet(conn, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES);

        // type name
        String typeName = "";
        if (withType) {
            typeName = getString("_UI_Connection_type");
            typeName = typeName.replace(" ", "") + " ";
        }

        String outName = null;
        if (conn.getSource() != null) {
            outName = getPort(conn.getSource(), conn.getSourcePortName());
            if (withType) {
                outName = "Output " + outName;
            }
        }

        String inName = null;
        if (conn.getTarget() != null) {
            inName = getPort(conn.getTarget(), conn.getTargetPortName());
            if (withType) {
                inName = "Input " + inName;
            }
        }

        String label = "";
        if (outName != null && inName != null) {
            label = outName + " > " + inName;
        }

        // return name
        return typeName + label;
    }

    // use SystemComponentItemProvider to create name
    private String getPort(final AElement comp, final String portName) {

        String label = null;

        // SystemComponentItemProviderMy adapter;
        // adapter = (SystemComponentItemProviderMy) adapterFactory.adapt(comp, SystemComponentItemProviderMy.class);
        // String parent = adapter.getText(comp);

        String parent = getPortComponentName(comp);

        if (parent != null) {
            label = parent;
            if (portName != null) {
                label = portName + " " + label;
            }
        }

        return label;
    }

    private String getPortComponentName(final AElement comp) {

        final String pre = SystemItemProviderAdapterFactoryMy.QNAME_PRE;
        final String post = SystemItemProviderAdapterFactoryMy.QNAME_POST;
        final String line = SystemItemProviderAdapterFactoryMy.NEWLINE;

        // label
        String label = comp.getName();
        if (label != null) {
            label = label.replace(SystemItemProviderAdapterFactoryMy.NEWLINE, "");
        } else {
            label = "";
        }

        // with qualified name
        String postfix = PremiseHelper.getQualifyingNamePrefix(comp);
        if (!postfix.isEmpty()) {
            label = pre + postfix + label + post;
            label = label.replace(line, "");
        } else {
            label = pre + label + post;
        }

        return label.trim();
    }

    @Override
    public Object getImage(Object object) {
        return imageHelper.getImage((IMetaTypable) object, getResourceLocator().getImage("full/obj16/Connection"));
    }
}
