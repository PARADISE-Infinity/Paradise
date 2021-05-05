/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.parameterviewer;

import static de.dlr.premise.util.PremiseHelper.closure;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.ChangeCommand;

import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.registry.AValueDef;
import de.dlr.premise.registry.Value;
import de.dlr.premise.system.ABalancing;
import de.dlr.premise.view.parameterviewer.data.ColumnValue;

class ChangeValueCommand extends ChangeCommand {

    /**
     * 
     */
    private final ParameterViewerPage parameterViewerPage;
    private Object element;
    private Object stringValue;
    private ColumnValue col;

    public ChangeValueCommand(ParameterViewerPage parameterViewerPage, Object element, ColumnValue col, Object stringValue) {
        super(((EObject) element).eResource().getResourceSet());
        this.parameterViewerPage = parameterViewerPage;
        this.element = element;
        this.col = col;
        this.stringValue = stringValue;
    }

    @Override
    public boolean canExecute() {
        return true;
    };

    private Map<AValueDef, String> computeTransitivelyChangedValues(AValueDef sourceVal) {
        return asMap(closure(sourceVal, this::targetValuesForSourceValue));
    }

    private Collection<Value> targetValuesForSourceValue(AValueDef v) {
        return this.parameterViewerPage.getAllBalancings().stream().filter(bal -> bal.getSources().stream().anyMatch(par -> v.equals(par.getValue())))
                                 .map(ABalancing::getTarget).filter(t -> t != null).map(AParameterDef::getValue)
                                 .collect(Collectors.toSet());
    }

    private Map<AValueDef, String> asMap(Collection<AValueDef> values) {
        return values.stream().collect(Collectors.toMap(v -> v, AValueDef::getValue));
    }

    @Override
    protected void doExecute() {
        final AValueDef val = ((AValueDef) element);
        if (col.id == ColumnValue.ID.VALUE) {
            this.parameterViewerPage.getChangedValues().clear();
            this.parameterViewerPage.getChangedValues().putAll(computeTransitivelyChangedValues(val));
            // copy changedValues into oldValues
            this.parameterViewerPage.getOldValues().putAll(this.parameterViewerPage.getChangedValues());
            val.setValue(String.valueOf(stringValue));
        } else if (col.id == ColumnValue.ID.UNCERTAINTY) {
            val.setUncertainty(String.valueOf(stringValue));
        }
    }
}