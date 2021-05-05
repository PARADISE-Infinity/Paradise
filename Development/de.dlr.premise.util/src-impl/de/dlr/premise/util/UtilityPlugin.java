/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.util;

import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.dlr.premise.util.logging.MultiOutputStream;

/**
 * The activator class controls the plug-in life cycle.
 */
public class UtilityPlugin extends AbstractUIPlugin {

    /** color max value. */
    private static final int MAX = 255;

    /** The plug-in ID. */
    public static final String PLUGIN_ID = "de.dlr.premise.util";

    /** The shared instance. */
    private static UtilityPlugin plugin = null;

    /** Console for the login message. */
    private static MessageConsole systemConsole = null;

    @Override
    public final void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        initConsole();
    }

    @Override
    public final void stop(final BundleContext context) throws Exception {
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     *
     * @return the shared instance
     */
    public static UtilityPlugin getDefault() {
        return plugin;
    }

    /**
     * @return Initialize a message console.
     */
    public static final MessageConsole initConsole() {
        ConsolePlugin conPlugin = ConsolePlugin.getDefault();
        if (conPlugin == null || !PlatformUI.isWorkbenchRunning()) {
            return null;
        }

        IConsoleManager conMan = conPlugin.getConsoleManager();
        String systemConsoleName = "System.out";
        IConsole[] existing = conMan.getConsoles();
        for (IConsole element : existing) {
            if (systemConsoleName.equals(element.getName())) {
                return (MessageConsole) element;
            }
        }

        systemConsole = new MessageConsole(systemConsoleName, null);
        conMan.addConsoles(new IConsole[] { systemConsole });

        MessageConsoleStream defaultMessageOutputStream = systemConsole.newMessageStream();

        MessageConsoleStream errorMessageOutputStream = systemConsole.newMessageStream();
        errorMessageOutputStream.setColor(new Color(Display.getCurrent(), MAX, 0, 0));

        OutputStream stdoutAndDefaultMessageOutputStream = new MultiOutputStream(System.out, defaultMessageOutputStream);
        OutputStream stderrAndErrorMessageOutputStream = new MultiOutputStream(System.err, errorMessageOutputStream);

        System.setOut(new PrintStream(stdoutAndDefaultMessageOutputStream));
        System.setErr(new PrintStream(stderrAndErrorMessageOutputStream));

        return systemConsole;
    }
}
