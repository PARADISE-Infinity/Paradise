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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.registry.Value;

import com.google.common.base.Strings;

public class AParameterDefItemProviderHelper {

    public static String getUpdateableText(Object object) {
        if (!(object instanceof AParameterDef)) {
            return "";
        }
        AParameterDef paramDef = (AParameterDef) object;

        String nameStr = Strings.nullToEmpty(paramDef.getName()).trim();

        String valueStr = "";
        if (paramDef.getValue() != null) {
            valueStr = Strings.nullToEmpty(paramDef.getValue().getValue()).trim();
        }

        // don't show equal sign when name and value are empty, that is for new parameters
        if (nameStr.isEmpty() && valueStr.isEmpty()) {
            return "";
        } else {
            return nameStr + " = " + valueStr;
        }
    }

    public static void setText(Object object, String string) {
        if (!(object instanceof AParameterDef)) {
            return;
        }
        if (string == null) {
            return;
        }

        final AParameterDef paramDef = (AParameterDef) object;

        Map<String, String> extracted = extractFromNameValueString(string);

        final String nameStr = extracted.get("name");
        final String valueStr = extracted.get("value");

        ChangeCommand command = new ChangeCommand(paramDef) {

            @Override
            protected void doExecute() {
                if (nameStr != null) {
                    paramDef.setName(nameStr);
                }

                if (valueStr != null) {
                    Value value = paramDef.getValue();
                    if (value == null) {
                        value = RegistryFactory.eINSTANCE.createValue();
                        paramDef.setValue(value);
                    }
                    value.setValue(valueStr);
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

    public static Map<String, String> extractFromNameValueString(String string) {
        Map<String, String> returnMap = new HashMap<>(2);
        // split at last occurence of equals sign
        int lastIndex = string.lastIndexOf('=');
        if (lastIndex == -1) {
            returnMap.put("name", string.trim());
        } else {
            returnMap.put("name", string.substring(0, lastIndex).trim());
            returnMap.put("value", string.substring(lastIndex + 1).trim());
        }
        return returnMap;
    }
}
