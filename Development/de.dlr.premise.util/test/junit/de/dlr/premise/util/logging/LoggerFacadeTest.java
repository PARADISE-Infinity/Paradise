/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.util.logging;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.Test;

import de.dlr.premise.util.logging.LoggerFacade;

/**
 * Unit test for logger facade class
 */
public class LoggerFacadeTest {

	private static final String TEST = "SystemsDesign";

    @Test
    public void testStdOut() {
        try {
            System.out.println("Test string for printing on standard out (console).");
        } catch (Exception e) {
            fail("Can not print on standard out! System.out.print() will throw an Exception during tests!");
        }
    }

	@Test
	public void testLoggerFacade() {
		assertNotNull("Logger facade creation failed", new LoggerFacade());
	}
	
	
	@Test
	public void testGetLogger() {
		final Logger logger = LoggerFacade.getLogger(TEST);
		assertNotNull("Logger creation failed", logger);
	}

	@Test
	public void testSetFileName() {		
		final Logger logger = LoggerFacade.getLogger(TEST);
		assertNotNull("Logger creation failed", logger);
	}
}
