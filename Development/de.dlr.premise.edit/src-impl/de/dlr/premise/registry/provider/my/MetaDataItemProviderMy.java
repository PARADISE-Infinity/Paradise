/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.registry.provider.my;

import static de.dlr.premise.registry.provider.my.AParameterDefItemProviderHelper.extractFromNameValueString;

import java.util.Map;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Strings;

import de.dlr.premise.registry.MetaData;
import de.dlr.premise.registry.provider.MetaDataItemProvider;
import de.dlr.premise.system.provider.my.SystemItemProviderAdapterFactoryMy;
import de.dlr.premise.util.PremiseHelper;

/**
 * @author hschum
 * 
 */
public class MetaDataItemProviderMy extends MetaDataItemProvider {

    public MetaDataItemProviderMy(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    @Override
    public String getText(Object object) {
        // get type name
        String typeName = "";
        if (PremiseHelper.isSet((EObject) object, SystemItemProviderAdapterFactoryMy.OPT_DATA_TYPE_NAMES)) {
            typeName = getString("_UI_MetaData_type");
            typeName = typeName.replace(" ", "") + " ";
        }

        // create label
        String label = ((MetaData) object).getName();
        if (label == null) {
            label = "";
        }
        String labelValue = ((MetaData) object).getValue();
        if (labelValue != null && !labelValue.isEmpty()) {
            label += " = " + labelValue;
        }

        // return name
        return typeName + label;
    }

    @Override
    public String getUpdateableText(Object object) {
        MetaData metaData = (MetaData) object;

        String nameStr = Strings.nullToEmpty(metaData.getName());
        String valueStr = Strings.nullToEmpty(metaData.getValue());

        String equalsSign = nameStr.isEmpty() && valueStr.isEmpty() ? "" : " = ";

        return nameStr + equalsSign + valueStr;
    }

    @Override
    public void setText(Object object, String string) {
        if (!(object instanceof MetaData) || string == null) {
            return;
        }

        final MetaData metaData = (MetaData) object;

        Map<String, String> extracted = extractFromNameValueString(string);

        final String nameStr = Strings.emptyToNull(extracted.get("name"));
        final String valueStr = extracted.get("value");

        if (nameStr == null && valueStr == null)
            return;

        ChangeCommand command = new ChangeCommand(metaData) {

            @Override
            protected void doExecute() {
                if (nameStr != null) {
                    metaData.setName(nameStr);
                }

                if (valueStr != null) {
                    metaData.setValue(valueStr);
                }
            }
        };

        IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (activeEditor instanceof IEditingDomainProvider) {
            EditingDomain editingDomain = ((IEditingDomainProvider) activeEditor).getEditingDomain();
            editingDomain.getCommandStack().execute(command);
        } else {
            command.execute();
        }
    }
}
