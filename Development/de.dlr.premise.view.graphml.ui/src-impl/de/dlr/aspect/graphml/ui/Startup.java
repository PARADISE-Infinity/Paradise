/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.aspect.graphml.ui;

import org.eclipse.ui.IStartup;

/**
 * Do nothing on startup.
 * 
 * This does nothing, but is still needed, because it forces the plugin to be loaded. Only if the plugin is loaded, the property tester can
 * function
 * 
 * @see http://www.robertwloch.net/2011/01/eclipse-tips-tricks-property-testers-with-command-core-expressions/
 */
public class Startup implements IStartup {

    @Override
    public void earlyStartup() {
        // Yes, I'm not doing a thing. But I'm still necessary, so please don't delete me.
    }

}
