/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.matlab;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.dlr.calc.engine.matlab.ui.MatlabPreferenceConfig;
import de.dlr.premise.util.logging.LoggerFacade;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	
	private static MatlabEngineJMBridge matlab;

	// The plug-in ID
	public static final String PLUGIN_ID = "de.dlr.calc.engine.matlab"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		initialize();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {

		closeCalculationScriptEngine();
		
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static MatlabEngineJMBridge getCalculationScriptEngine() {

		// if the engine is available return
		if (matlab != null && matlab.isAvailable()) {
			return matlab;
		}

		// create and return engine
		MatlabConfig config = getMatlabConfiguration();
		matlab = new MatlabEngineJMBridge(config);
		
		return matlab;
	}
	
	public static void closeCalculationScriptEngine() {
		
		
		// in case of multi-calculation mode its necessary to turn the mode off
		// otherwise MATLAB will be still open
		if (matlab == null) return;
		MatlabConfig config = matlab.getConfig();
		config.setMultiCalc("off");

		// close MATLAB engine
		if (matlab != null) {
			matlab.close();
		}
		
		matlab = null;
		
	}

	private void initialize () {		
		LoggerFacade.getLogger(PLUGIN_ID);
		
		MatlabConfig config = getMatlabConfiguration();		
		if (config.getMultiCalc() == true) {
			matlab = getCalculationScriptEngine();
		}
	}
	
	private static MatlabConfig getMatlabConfiguration() {

		IPreferenceStore store = getDefault().getPreferenceStore();
		MatlabConfig config = new MatlabConfig();
		
		// 
		if (store.getBoolean(MatlabPreferenceConfig.CALCULATION)) {
			config.setMultiCalc("on");
		} else {
			config.setMultiCalc("off");
		}
		
		config.setMatlabPath(store.getString(MatlabPreferenceConfig.MATLABPATH));
		config.setJMBridgePath(store.getString(MatlabPreferenceConfig.JMBRIDGEPATH));
		config.setJMBridgePort(store.getInt(MatlabPreferenceConfig.JMBRIDGEPORT));
		config.setJMBridgeTimeOut(store.getInt(MatlabPreferenceConfig.JMBRIDGETIMEOUT));

		return config;
	}
}
