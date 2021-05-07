/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.application.rcp;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.dlr.application.rcp.p2.CloudPolicy;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("deprecation")
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.dlr.application.rcp"; //$NON-NLS-1$

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

        // register the p2 UI policy
        CloudPolicy policy = new CloudPolicy();
        context.registerService(CloudPolicy.class.getName(), policy, null);
        
        // force auto refresh workspace to be always on
        setAutoRefereshWorkspace();

	}

	private void setAutoRefereshWorkspace() {
	    // see org.eclipse.ui.internal.ide.dialogs.IDEWorkspacePreferencePage.performOk()
        Preferences preferences = ResourcesPlugin.getPlugin()
                .getPluginPreferences();
        preferences.setValue(ResourcesPlugin.PREF_AUTO_REFRESH, true);
        preferences.setValue(ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
