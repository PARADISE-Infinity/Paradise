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

import java.io.File;

public class MatlabConfig {

	// default values
	public final static String JMBRIDGE_PATH = "C:/Program Files/MATLAB/Tools/JMBridge";
    public final static int JMBRIDGE_PORT = 5564;
    public final static int JMBRIDGE_TIMEOUT = 60000;
    public final static String MATLAB_EXE = "C:/Program Files/MATLAB/R2007b/bin/MATLAB.exe";
    public final static boolean CALCULATION = false;
	
	// local values
    private String _path = JMBRIDGE_PATH;
    private int _port = JMBRIDGE_PORT;
    private int _timeOut = JMBRIDGE_TIMEOUT;
    private String _matlab = MATLAB_EXE;

    private boolean multicalc = false;

    public String getJMBridgePath () {
        return _path;
    }
    
    /**
     * Sets the path to the JMBridge.
     * 
     * @param path
     */
    public void setJMBridgePath(final String path) {
    
    	// check input
        if (path == null || path.length() == 0) {
            System.err.println("JMBridge path invalid");
            return;
        }
            
        // set new JMBridge path
        _path = path;
    }
    
    public String getJMPRidgePath() {
    	return _path;
    }

    public int getJMBridgePort() {
        return _port;
    }

    public void setJMBridgePort(final int port) {        
        if (port < 1024 || port > 32768) {
            System.err.println("JMBridge port " + port + " invalid.");
            return;            
        }
        
        _port = port;
    }
    
    public int getJMBridgeTimeout() {
        return _timeOut;
    }

    public void setJMBridgeTimeOut(final int timeout) {
        if (timeout < 1000 || timeout > 600000) {
            System.err.println("JMBridge timeout " + timeout + " invalid.");
            return;                        
        }
        
        _timeOut = timeout;
     }
    
    public String getMatlabPath() {
        return _matlab;
    }

    /**
     * Sets the path to the MATLAB executables.
     * 
     * @param path
     */
    public void setMatlabPath(final String path) {
    	
        if (path == null || path.length() == 0) {
            System.err.println("Matlab path is invalid");
            return;
        }

        _matlab = path;
    }

    /**
     * Checks if the matlab path exists or not
     * @return
     */
    public boolean MatlabExists() {
    	// check if path exists
        File file = new File(_matlab);
        if (!file.exists()) {
            System.err.println("MATLAB path " + _matlab + " doesn't exists.");
            return false;
        }        
    	return true;
    }

    /**
     * Checks if the jmbridge path exists or not
     * @return
     */
    public boolean JMBridgeExists() {
    	// check if path exists
        File file = new File(_path);
        if (!file.exists()) {
            System.err.println("JMBridge path " + _path + " doesn't exists.");
            return false;
        }        
    	return true;
    }

    /**
     * Sets the multi-calculation property
     * @param value
     */
	public void setMultiCalc(final String value) {
		if (value.equals("on")) {
			multicalc = true;
		}
		if (value.equals("off")) {
			multicalc = false;
		}		
	}

	/**
	 * returns the status of the multi-calculation property
	 * @return
	 */
	public boolean getMultiCalc() {
		return multicalc;
	}
}
