/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.calc.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.dlr.premise.calc.ui.Activator;
import de.dlr.premise.calc.ui.Recalculator;
import de.dlr.premise.calc.ui.RegisterCalculationAdapterPartListener;

public class CalculationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    
    

    BooleanFieldEditor multiCalc;

    public CalculationPreferencePage() {
        super(GRID);
    }

    @Override
    public void init(IWorkbench workbench) {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        setPreferenceStore(store);
        setDescription("Set the preferences for using calculation during modeling");
    }

    @Override
    protected void createFieldEditors() {

        Composite parent = getFieldEditorParent();

        multiCalc =
                new BooleanFieldEditor(Activator.PREFERENCE_CALCULATION, "online calculation", BooleanFieldEditor.DEFAULT, parent);
        addField(multiCalc);
    }

    @Override
    public void performApply() {
        savePreferences();
    }

    @Override
    public boolean performOk() {
        savePreferences();
        return super.performOk();
    }

    private void savePreferences() {        
        boolean oldCalculation = Activator.getDefault().getCalculationPreference();
        boolean newCalculation = multiCalc.getBooleanValue();
        
        Activator.getDefault().setCalculationPreference(newCalculation);
        
        if (newCalculation && !oldCalculation) {
            // calculation switched on
            RegisterCalculationAdapterPartListener.addCalculationAdapters();
            Recalculator.recalculateAll(getShell());
        } else if(!newCalculation && oldCalculation) {
            // calculation switched off
            RegisterCalculationAdapterPartListener.removeCalculationAdapters();
            
        }
    }
}
