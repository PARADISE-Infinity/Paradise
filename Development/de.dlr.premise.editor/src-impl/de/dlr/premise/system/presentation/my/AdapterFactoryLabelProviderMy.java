/**
 * 
 */
package de.dlr.premise.system.presentation.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import de.dlr.premise.registry.ADataItem;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;

/**
 * @author hschum
 * 
 */
public class AdapterFactoryLabelProviderMy extends AdapterFactoryLabelProvider.StyledLabelProvider {


    public AdapterFactoryLabelProviderMy(AdapterFactory adapterFactory, Font defaultFont, Color defaultForeground, Color defaultBackground) {
        super(adapterFactory, defaultFont, defaultForeground, defaultBackground);
    }

    public AdapterFactoryLabelProviderMy(AdapterFactory adapterFactory, Viewer viewer) {
        super(adapterFactory, viewer);
    }

    @Override
    public String getText(Object object) {
        String result = super.getText(object);
        if (object instanceof ADataItem) {
            // simplify name for tree view (not property view)
            int index = result.indexOf(SystemItemProviderAdapterFactoryMy.QNAME_PRE);
            if (index >= 0) {
                result = result.substring(0, index);
            }
        }
        return result;
    }
}
