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

import org.apache.log4j.Level;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

import de.dlr.premise.util.UtilityPlugin;

/**
 * Hide the concrete logger implementation.
 */
public class LoggerFacade {

    /** */
    private static String pattern = "%d{ISO8601} %-5p [%t] %c: %m %n";

    private static Logger logger = null;
    
    private static Level level = Level.INFO;
    
    /**
     * @param name logger name
     * @return
     *  Returns a new logger with the given logger name.
     */
    public static final Logger getLogger(final String name) {

    	if (logger != null) {
    		return logger;
    	}
    	
        logger = Logger.getLogger(name);
        logger.setLevel(level);
        
        PatternLayout layout = new PatternLayout(pattern);

        MessageConsole con = UtilityPlugin.initConsole();

        if (con != null) {
            ConsoleLogAppender consoleAppender = new ConsoleLogAppender(layout);
            logger.addAppender(consoleAppender);
        } else {
            ConsoleAppender conAppender = new ConsoleAppender(layout);
            logger.addAppender(conAppender);
        }

        return logger;        
    }
    
    /**
     * Set the log level.
     * @param level
     */
    public void setLevel(final Level level) {
    	logger.setLevel(level);
    }
    
    /**
     * @param logger logger
     * @return
     *     Returns a new console.
     */
    protected final MessageConsole initConsole(final Logger logger) {

        if (logger.isInfoEnabled()) {
            logger.info("Ausführen des Commandos " + this);
        }
        ConsolePlugin plugin = ConsolePlugin.getDefault();

        IConsoleManager conMan = plugin.getConsoleManager();
        String systemConsoleName = "System.out";
        IConsole[] existing = conMan.getConsoles();
        for (int i = 0; i < existing.length; i++) {
            if (systemConsoleName.equals(existing[i].getName())) {
                ((MessageConsole) existing[i]).activate();
                return (MessageConsole) existing[i];
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Suche vorhandene Console oder erstelle eine Neue");
        }

        MessageConsole systemConsole =
                new MessageConsole(systemConsoleName, null);
        conMan.addConsoles(new IConsole[] {systemConsole});
        return systemConsole;
    }
}
