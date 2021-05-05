/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.calc.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.dlr.premise.util.logging.LoggerFacade;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
    
	// The plug-in ID
	public static final String PLUGIN_ID = "de.dlr.premise.calculation"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	// 
    public static final String PREFERENCE_CALCULATION = "calculation";

	
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
		plugin = null;
		super.stop(context);
	}

	public boolean getCalculationPreference() {
	    return getPreferenceStore().getBoolean(Activator.PREFERENCE_CALCULATION);
	}
	
   public void setCalculationPreference(boolean newCalculation) {
        getPreferenceStore().setValue(Activator.PREFERENCE_CALCULATION, newCalculation);
    }
	
	/**
	 * Returns the shared instance
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	private void initialize () {
		 LoggerFacade.getLogger(PLUGIN_ID);
	}
}
