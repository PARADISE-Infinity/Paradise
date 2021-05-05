/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.xtext;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.ui.shared.SharedStateModule;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import de.dlr.premise.xtext.ui.PremiseUiModule;


public class Activator extends AbstractUIPlugin {
    
    // The plug-in ID
    public static final String PLUGIN_ID = "de.dlr.premise.xtext"; //$NON-NLS-1$
 
    // The shared instance
    private static Activator plugin;
 
    private Injector injector;
 
    /**
     * The constructor
     */
    public Activator() {
    }
 
    public Injector getInjector() {
        return injector;
    }
 
    private void initializePremiseInjector() {
        injector = Guice.createInjector(
                Modules.override(Modules.override(new PremiseRuntimeModule())
                .with(new PremiseUiModule(plugin)))
                .with(new SharedStateModule()));
    }
 
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        try {
            initializePremiseInjector();
        } catch(Exception e) {
            throw e;
        }
    }
 
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        injector = null;
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
 
}
