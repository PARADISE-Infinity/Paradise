/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.matlab.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.dlr.calc.engine.matlab.Activator;
import de.dlr.calc.engine.matlab.MatlabConfig;
import de.dlr.calc.engine.matlab.MatlabEngineJMBridge;

public class MatlabPreferencePage 	extends FieldEditorPreferencePage 
									implements IWorkbenchPreferencePage{

	BooleanFieldEditor multiCalc;
	StringFieldEditor matlabPath;
	StringFieldEditor jmBridge;
	IntegerFieldEditor port;
	IntegerFieldEditor timeOut;
	
	public MatlabPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {	
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription("Set the preferences for using a matlab calculation engine");
	}

	@Override
	protected void createFieldEditors() {
		
		Composite parent = getFieldEditorParent();

		multiCalc = new BooleanFieldEditor(MatlabPreferenceConfig.CALCULATION, "Calculation at startup", BooleanFieldEditor.DEFAULT, parent);		
		addField(multiCalc);
		
		matlabPath = new StringFieldEditor(MatlabPreferenceConfig.MATLABPATH, "Matlab Path", -1, StringFieldEditor.VALIDATE_ON_KEY_STROKE, parent);		
		addField(matlabPath);

		jmBridge = new StringFieldEditor(MatlabPreferenceConfig.JMBRIDGEPATH, "JMBridge Path", -1, StringFieldEditor.VALIDATE_ON_KEY_STROKE, parent);
		addField(jmBridge);
		
		port = new IntegerFieldEditor(MatlabPreferenceConfig.JMBRIDGEPORT, "JMBridge Port", getFieldEditorParent());
		port.setValidRange(1, 26000);
		addField(port);
		
		timeOut = new IntegerFieldEditor(MatlabPreferenceConfig.JMBRIDGETIMEOUT, "JMBridge Timeout", getFieldEditorParent());
		timeOut.setValidRange(0, 120000);
		addField(timeOut);		
	}

	@Override
	public void performApply() {
		savePreferences();
		performGetEngine();	
	}
	
	@Override
	public boolean performOk() {
		savePreferences();
		performGetEngine();
		return super.performOk();
	}

	private void performGetEngine() {
		if (multiCalc.getBooleanValue() == true) {
		    MatlabEngineJMBridge matlab = Activator.getCalculationScriptEngine();
			MatlabConfig config = matlab.getConfig();
			config.setMultiCalc("on");
		} else {
			Activator.closeCalculationScriptEngine();
		}		
	}
	
	private void savePreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
	    store.setValue(MatlabPreferenceConfig.CALCULATION, multiCalc.getBooleanValue());
	    store.setValue(MatlabPreferenceConfig.MATLABPATH, matlabPath.getStringValue());
	    store.setValue(MatlabPreferenceConfig.JMBRIDGEPATH, jmBridge.getStringValue());
	    store.setValue(MatlabPreferenceConfig.JMBRIDGEPORT, port.getIntValue());
	    store.setValue(MatlabPreferenceConfig.JMBRIDGETIMEOUT, timeOut.getIntValue());
	}	
}
