/**
* Copyright (C) 2011-2016 systemsdesign.de, Germany
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Holger Schumann
*
*/

package de.dlr.premise.util.logging;

import java.io.IOException;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Implements a console log appender for the eclipse console.
 */
public class ConsoleLogAppender extends AppenderSkeleton {

    /** Color max value. */
    private static final int MAX = 255;

    /** console instance. */
    private IConsoleManager conMan;

    /** log level.*/
    private String _logLevel = "ERROR";

    /**
     * Default constructor.
     * @param layout layout pattern
     */
    public ConsoleLogAppender(final PatternLayout layout) {
        setLayout(layout);
        setLogLevel("DEBUG");
    }

    /**
     * Append a new event.
     * @param event new logging event.
     */
    protected final void append(final LoggingEvent event) {
        if (this.conMan == null) {
            this.conMan = ConsolePlugin.getDefault().getConsoleManager();
        }

        if (this.layout == null) {
            final int value = 5;
            this.errorHandler.error("Missing layout for appender " + this.name,
                    null, value);
            return;
        }

        StringBuffer text = new StringBuffer();
        text.append(this.layout.format(event));

        Level level = event.getLevel();
        if (level.toInt() >= Level.toLevel(_logLevel).toInt()) {
            IConsole[] existing = this.conMan.getConsoles();
            for (int i = 0; i < existing.length; i++) {
                MessageConsoleStream stream = ((MessageConsole) existing[i])
                        .newMessageStream();

                if (event.getLevel().equals(Level.ERROR)) {
                    stream.setColor(new Color(Display.getCurrent(), MAX, 0, 0));
                }

                if (event.getLevel().equals(Level.WARN)) {
                    stream.setColor(new Color(Display.getCurrent(), 0, 0, MAX));
                }

                try {
                    stream.write(text.toString().getBytes());
                    stream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public final void close() {
        this.conMan = null;
    }

    @Override
    public final boolean requiresLayout() {
        return true;
    }

    /**
     * @return
     *  Returns the current console log level.
     */
    public final String getLogLevel() {
        return _logLevel;
    }

    /**
     * @param logLevel log level string
     * Sets the log level.
     */
    public final void setLogLevel(final String logLevel) {
        _logLevel = logLevel;
    }
}