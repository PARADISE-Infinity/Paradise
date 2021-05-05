/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.calculation.legacy;

import static org.junit.Assert.assertEquals;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import system.util.my.ValueChangedContentAdapter;

/**
 * @author hschum
 *
 */
public class ValueChangedContentAdapterTestHelper extends ValueChangedContentAdapter {

	private static EList<String> notificationList = new BasicEList<String>();
	private static boolean verbose = true;

	public static EList<String> getNotifications() {
		return notificationList;
	}

	public static void setVerbose(boolean value) {
		verbose = value;
		if (verbose) {
	        // set max. verbose level of super class
		    verboseLevel = 3;
		} else {
		    verboseLevel = 0;
		}
	}

	@Override
	protected void log(String msg) {
	    String trimmmedMsg = msg.replace("  ", "");
        notificationList.add(trimmmedMsg);
        if (verbose) {
            System.out.println("Notification: " + trimmmedMsg);
            // increase notifications counter
            notifications++;
        }
	}

	@Override
	protected void log2(String msg) {
	}

	@Override
    protected void log3(String msg) {
        String trimmmedMsg = msg.replace("  ", "");
        notificationList.add(trimmmedMsg);
		if (verbose) {
			System.out.println("Notification: " + trimmmedMsg);
		}
	}

    public static void assertLog(int msgIdx, Class<?> notifierClass, String eventType, String featureName, String action) {
        String[] logMsgParts = notificationList.get(msgIdx).split(" ");
        assertEquals(notifierClass.getSimpleName(), logMsgParts[1]);
        assertEquals(eventType, logMsgParts[3]);
        if (featureName != null) {
            assertEquals(featureName, logMsgParts[4]);
        }
        if (action != null) {
            String actualAction = null;
            if (logMsgParts.length >= 6) {
                actualAction = logMsgParts[5];
            }
            assertEquals(action, actualAction);
        } else if (featureName != null) {
            if (logMsgParts.length >= 6) {
                assertEquals(null, logMsgParts[5]);
            }
        }
    }
}
