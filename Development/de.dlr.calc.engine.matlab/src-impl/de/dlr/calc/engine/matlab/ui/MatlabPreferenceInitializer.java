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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.dlr.calc.engine.matlab.Activator;
import de.dlr.calc.engine.matlab.MatlabConfig;

public class MatlabPreferenceInitializer extends AbstractPreferenceInitializer {  
	  
	public MatlabPreferenceInitializer() {  
	}
  
	@Override  
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();		
		store.setDefault(MatlabPreferenceConfig.CALCULATION, MatlabConfig.CALCULATION);
		store.setDefault(MatlabPreferenceConfig.MATLABPATH, MatlabConfig.MATLAB_EXE);
		store.setDefault(MatlabPreferenceConfig.JMBRIDGEPATH, MatlabConfig.JMBRIDGE_PATH);
		store.setDefault(MatlabPreferenceConfig.JMBRIDGEPORT, MatlabConfig.JMBRIDGE_PORT);
		store.setDefault(MatlabPreferenceConfig.JMBRIDGETIMEOUT, MatlabConfig.JMBRIDGE_TIMEOUT);
	}  
} 